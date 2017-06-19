import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpServer;

/**
 * Main class for the Trixie server
 * 
 * @author Andrew Wilder
 */
public class TrixieServer {

	/** Command-line data */
	private static String RootDir = ".";
	private static int BindPort = 80;
	
	/**
	 * Validate args and start the server
	 * @param args Arguments
	 */
	public static void main(String[] args) {
		
		// Validate arguments
		validateArgs(args);

		// Create HTTP server
		System.out.println("Root dir: " + RootDir);
		System.out.println("Bind: port " + BindPort);
		HttpServer hs = null;
		try {
			hs = HttpServer.create(new InetSocketAddress(BindPort), 0);
		} catch (IOException e) {
			error(e.getMessage());
		}
		hs.createContext("/", new FrontpageHandler());
		hs.createContext("/content/", new ContentHandler(RootDir));
		hs.createContext("/music/", new MusicHandler(RootDir));
		hs.createContext("/resources/", new ResourcesHandler(RootDir));
		hs.setExecutor(null);
		hs.start();
	}
	
	/**
	 * Validate the command-line arguments
	 * @param args The command-line arguments
	 */
	private static void validateArgs(String[] args) {
		for(int i = 0; i < args.length; ++i) {
			switch(args[i]) {
			case "-p":
				if(++i == args.length) {
					error("No port provided for " + args[i - 1]);
				} else if(!Pattern.matches("\\d+", args[i])) {
					error("Invalid port format for " + args[i - 1] + ": " + args[i]);
				}
				BindPort = Integer.parseInt(args[i]);
				if(BindPort > 0xFFFF) {
					error("Port out of range for option " + args[i - 1] + ": " + BindPort);
				}
				break;
			case "-d":
				if(++i == args.length) {
					error("No directory provided for " + args[i - 1]);
				}
				RootDir = args[i];
				File dirF = new File(RootDir);
				if(!dirF.exists()) {
					error("Directory for option " + args[i - 1] + " doesn't exist: " + RootDir);
				} else if(!dirF.isDirectory()) {
					error("Parameter for option " + args[i - 1] + " is not a directory: " + RootDir);
				}
				break;
			case "-h":
				usage();
				System.exit(0);
			default:
				error("Unrecognized option: " + args[i]);
			}
		}
	}

	/**
	 * Display the program usage
	 */
	private static void usage() {
		System.out.println("Program usage: java -jar TrixieServer.jar [options]");
		System.out.println("\t-p <port>: Bind to port p");
		System.out.println("\t-d <rootdir>: Use d as the root directory for the server");
		System.out.println("\t-h: Display this help information");
	}
	
	/**
	 * Fail, displaying an error message and program usage
	 * @param message The cause of the failure
	 */
	private static void error(String message) {
		System.out.println("Error: " + message);
		System.out.println("Use option -h for usage options");
		System.exit(1);
	}
}

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.util.Scanner;

import com.sun.net.httpserver.HttpExchange;

/**
 * Utility function class for common HttpExchange uses
 * 
 * @author Andrew Wilder
 */
public class TrixieServerUtils {
	
	/**
	 * Get a template file as a String from within the JAR
	 * @param path Path to the template from the JAR's root
	 * @return The template as a String
	 */
	public static String GetTemplate(String path) {
		InputStream is = TrixieServerUtils.class.getResourceAsStream(path);
		Scanner sc = new Scanner(is, "UTF-8");
		sc.useDelimiter("\\A");
		String ret = sc.next();
		sc.close();
		return ret;
	}
	
	/**
	 * Convert an HTTP-escaped String to the original String
	 * @param str The String to de-escape
	 * @return The de-escaped String
	 */
	public static String ConvertHTTPEscapes(String str) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < str.length(); ++i) {
			if(str.charAt(i) != '%') {
				sb.append(str.charAt(i));
			} else {
				int c = Integer.decode("0x" + str.substring(i + 1, i + 3));
				sb.append((char) c);
				i += 2;
			}
		}
		return sb.toString();
	}
	
	/**
	 * Send a file to the user
	 * @param he The HTTP exchange object
	 * @param resFile The file to send
	 * @throws IOException Passed to the caller method
	 */
	public static void SendFile(HttpExchange he, File resFile) throws IOException {
		if(!resFile.exists()) {
			SendString(he, HttpURLConnection.HTTP_NOT_FOUND,
					"Resource " + resFile + " does not exist");
			return;
		}
		if(!resFile.isFile()) {
			SendString(he, HttpURLConnection.HTTP_BAD_REQUEST,
					"Resource " + resFile + " is a directory, not a file");
			return;
		}
		try {
			he.sendResponseHeaders(HttpURLConnection.HTTP_OK, resFile.length());
			OutputStream os = he.getResponseBody();
			Files.copy(resFile.toPath(), os);
			os.close();
			he.close();
		} catch(IOException e) {
			switch(e.getMessage()) {
			case "Connection reset by peer":
			case "Broken pipe":
				break;
			default:
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Send a String message to the user
	 * @param he The HTTP exchange object
	 * @param code The HTTP response code to send
	 * @param str The String to send to the user
	 * @throws IOException Passed to the caller method
	 */
	public static void SendString(HttpExchange he, int code, String str) throws IOException {
		String response = str + "\n";
		he.getResponseHeaders().set("Content-Type", "text/html");
		he.sendResponseHeaders(code, response.length());
		OutputStream os = he.getResponseBody();
		os.write(response.getBytes());
		os.flush();
		os.close();
		he.close();
	}
}

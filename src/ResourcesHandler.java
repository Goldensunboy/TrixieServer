import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handling sending resource files
 * 
 * @author Andrew Wilder
 */
public class ResourcesHandler implements HttpHandler {
	
	/** Static data */
	private static final String RESOURCE_DIR = "resources";
	
	/** Root content directory */
	private Path RootDir;
	
	/**
	 * Handle request
	 * @param he The HTTP exchange object
	 */
	@Override
	public void handle(HttpExchange he) throws IOException {
		try {
			if(!he.getRequestMethod().equalsIgnoreCase("GET")) {
				TrixieServerUtils.SendString(he, HttpURLConnection.HTTP_BAD_METHOD,
						"Unsupported request type: " + he.getRequestMethod());
				return;
			}
			String resURI = he.getRequestURI().toString().substring(RESOURCE_DIR.length() + 2);
			File resFile = Paths.get(RootDir.toString(), resURI).toFile();
			resFile = new File(TrixieServerUtils.ConvertHTTPEscapes(resFile.toString()));
			TrixieServerUtils.SendFile(he, resFile);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Construct a new handler object
	 * @param RootDir The root of the content directory
	 */
	public ResourcesHandler(String RootDir) {
		this.RootDir = Paths.get(RootDir, RESOURCE_DIR);
	}
}

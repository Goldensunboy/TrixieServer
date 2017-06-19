import java.io.IOException;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handling displaying the front page
 * 
 * @author Andrew Wilder
 */
public class FrontpageHandler implements HttpHandler {
	
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
			String page = TrixieServerUtils.GetTemplate("home.html");
			TrixieServerUtils.SendString(he, HttpURLConnection.HTTP_OK, page);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}

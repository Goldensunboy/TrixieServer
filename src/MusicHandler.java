import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handling the music pages
 * 
 * @author Andrew Wilder
 */
public class MusicHandler implements HttpHandler {
	
	/** Static data */
	public static final String CONTENT_DIR = "content/music";
	private static final int TABLE_WIDTH = 3;
	
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
			if(he.getRequestURI().toString().equals("/music/")) { // All music page
				// Get albums
				File musicDir = RootDir.toFile();
				List<File> albums = new ArrayList<File>();
				for(File f : musicDir.listFiles()) {
					if(f.isDirectory()) {
						albums.add(f);
					}
				}
				Collections.sort(albums);
				// Create table
				String tableStr = "";
				for(int i = 0; i < albums.size(); i += TABLE_WIDTH) {
					tableStr += "<tr height=\"300\">";
					for(int j = 0; j < TABLE_WIDTH; ++j) {
						tableStr += "<td width=\"300\">";
						if(i + j < albums.size()) {
							String albumName = albums.get(i + j).getName();
							tableStr += "<a href=\"" + albumName + "\">";
							tableStr += "<IMG src=\"/" + CONTENT_DIR + "/" + albumName +
									"/cover_small.jpg\" width=\"300\" height=\"300\"></a>";
						}
						tableStr += "</td>";
					}
					tableStr += "</tr><tr>";
					for(int j = 0; j < TABLE_WIDTH; ++j) {
						tableStr += "<td width=\"300\">";
						if(i + j < albums.size()) {
							tableStr += albums.get(i + j).getName();
						}
						tableStr += "</td>";
					}
					tableStr += "</tr>";
				}
				// Add table to template
				String page = TrixieServerUtils.GetTemplate("music.html");
				page = page.replaceAll("\\$TABLE", tableStr);
				TrixieServerUtils.SendString(he, HttpURLConnection.HTTP_OK, page);
			} else { // Album page
				// Get songs
				String albumTitle = he.getRequestURI().toString().substring(7);
				albumTitle = TrixieServerUtils.ConvertHTTPEscapes(albumTitle);
				String albumURL = "/" + CONTENT_DIR + "/" + albumTitle + "/";
				File albumDir = RootDir.resolve(albumTitle).toFile();
				List<File> songs = new ArrayList<File>();
				for(File f : albumDir.listFiles()) {
					if(Pattern.matches(".*(mp3|ogg|wav|flac)", f.getName().toLowerCase())) {
						songs.add(f);
					}
				}
				Collections.sort(songs);
				// Create table and applet
				String tableStr = "";
				for(int i = 0; i < songs.size(); ++i) {
					String songTitle = songs.get(i).getName();
					tableStr += "<tr><td><a href=\"" + albumURL + songTitle + "\">" +
							songTitle + "</a></tr></td>";
				}
				String appletStr = TrixieCacheUtils.getAppletData(albumDir);
				// Add table and applet to template
				String page = TrixieServerUtils.GetTemplate("album.html");
				page = page.replaceAll("\\$TITLE", albumTitle);
				page = page.replaceAll("\\$TABLE", tableStr);
				page = page.replaceAll("\\$APPLET", appletStr);
				TrixieServerUtils.SendString(he, HttpURLConnection.HTTP_OK, page);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Construct a new handler object
	 * @param RootDir The root of the content directory
	 */
	public MusicHandler(String RootDir) {
		this.RootDir = Paths.get(RootDir, CONTENT_DIR);
	}
}

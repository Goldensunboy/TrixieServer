import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

/**
 * Functions for caching static data
 * 
 * @author Andrew Wilder
 */
public class TrixieCacheUtils {

	/** Static data*/
	private static Map<File, String> cachedAppletData = new HashMap<File, String>();
	
	/**
	 * Get the applet data String for an album
	 * @param albumDir The album directory
	 * @return The applet data for the HTML5 player
	 * @throws Exception Reading audio file exception passed to caller
	 */
	public static String getAppletData(File albumDir) throws Exception {
		if(cachedAppletData.containsKey(albumDir)) {
			return cachedAppletData.get(albumDir);
		} else {
			String albumURL = "/" + MusicHandler.CONTENT_DIR + "/" + albumDir.getName() + "/";
			List<File> songs = new ArrayList<File>();
			for(File f : albumDir.listFiles()) {
				if(Pattern.matches(".*(mp3|ogg|wav|flac)", f.getName().toLowerCase())) {
					songs.add(f);
				}
			}
			Collections.sort(songs);
			String appletStr = "";
			for(File f : songs) {
				AudioFile af = AudioFileIO.read(f);
				Tag t = af.getTag();
				appletStr += "{mp3:\"" + albumURL + f.getName() + "\",";
				appletStr += "title:\"" + t.getFirst(FieldKey.TITLE) + "\",";
				appletStr += "artist:\"" + t.getFirst(FieldKey.ARTIST) + "\",";
				appletStr += "cover:\"" + albumURL + "cover.jpg\"},";
			}
			cachedAppletData.put(albumDir, appletStr);
			return appletStr;
		}
	}
	
	/**
	 * Cache the album directories with a worker thread
	 * @param musicDir The directory for the music albums
	 */
	public static void preCacheAppletData(File musicDir) {
		new PreCacheThread(musicDir).start();
	}
	
	/**
	 * Worker thread for pre-cacheing album data
	 * 
	 * @author Andrew Wilder
	 */
	private static class PreCacheThread extends Thread {
		
		/** Instance data */
		private File musicDir;
		
		/**
		 * Create a new pre-cache worker thread
		 * @param musicDir
		 */
		public PreCacheThread(File musicDir) {
			super("Pre-cache");
			this.musicDir = musicDir;
		}
		
		/**
		 * Pre-cache the album data
		 */
		@Override
		public void run() {
			File[] dirs = musicDir.listFiles();
			int count = 0;
			for(File f : dirs) {
				if(f.isDirectory()) {
					++count;
				}
			}
			int i = 0;
			for(File f : dirs) {
				if(f.isDirectory()) {
					System.out.println("Cacheing \"" + f.getName() +
							"\" (" + ++i + "/" + count + ")...");
					try {
						getAppletData(f);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}

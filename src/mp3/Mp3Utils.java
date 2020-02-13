package mp3;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mp3.domain.Artist;

/**
 * Utility functions used by other classes in the package.
 *
 * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class Mp3Utils {
	private static final Logger log = LogManager.getLogger(Mp3Utils.class);

	public static final NumberFormat trackNumberFormat = NumberFormat.getIntegerInstance();
	static {
		trackNumberFormat.setMinimumIntegerDigits(2);
	}

	public static final FileFilter MP3_FILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.getName().toLowerCase().endsWith(".mp3");
		}
	};

	public static SortedSet<Artist> readArtists(File root) {
		SortedSet<Artist> artists = new TreeSet<Artist>();

		// Read the folder structure and build a list of artists to check.
		log.info("Reading artists from folder: " + root.getAbsolutePath());
		for (File artistDir : root.listFiles()) {
			if (artistDir.isDirectory()) {
				Artist artist = new Artist(artistDir);
				artists.add(artist);
			}
		}

		return artists;
	}

	public static String filterChars(String name) {
		name = name.replace('?', '_');
		name = name.replace('*', '_');
		name = name.replace(":", " -");
		name = name.replace("\"", "''");
		name = name.replace('/', '-');
		name = name.replace('>', '-');
		name = name.replace('<', '-');
		return name;
	}

	// Copied from http://www.java2s.com/Code/Java/File-Input-Output/CopyfilesusingJavaIOAPI.htm
	public static void fileCopy(File input, File output) throws IOException {
		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(input);
			to = new FileOutputStream(output);
			byte[] buffer = new byte[4096];
			int bytesRead;

			while ((bytesRead = from.read(buffer)) != -1)
				to.write(buffer, 0, bytesRead);
		}
		finally {
			if (from != null) from.close();
			if (to != null) to.close();
		}
	}
}

package mp3.domain;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * Domain class used by Mp3CheckTagsAndFolders.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class Artist implements Comparable<Artist> {
	private static final Logger log = Logger.getLogger(Artist.class);

	private String name;

	private File folder;

	private SortedSet<Album> albums;

	private Map<String, Object> extras = new HashMap<String, Object>();

	public Artist(File folder) {
		this.folder = folder;

		log.debug("Reading artist from folder: " + folder.getAbsolutePath());
		extractInfo();
	}

	protected void extractInfo() {
		// TODO: check string pattern, throw malformed name exception and treat it elsewhere.

		// Gets the name of the artist from the name of the folder.
		this.name = folder.getName();
		log.debug("Extracted artist name: " + name);

		// Checks for albums in the subdirectories.
		albums = new TreeSet<Album>();
		for (File albumDir : folder.listFiles()) {
			if (albumDir.isDirectory()) albums.add(new Album(this, albumDir));
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getFolder() {
		return folder;
	}

	public void setFolder(File folder) {
		this.folder = folder;
	}

	public SortedSet<Album> getAlbums() {
		return albums;
	}

	public void setAlbums(SortedSet<Album> albums) {
		this.albums = albums;
	}

	public Map<String, Object> getExtras() {
		return extras;
	}

	public void setExtras(Map<String, Object> extras) {
		this.extras = extras;
	}

	@Override
	public int compareTo(Artist o) {
		return name.compareTo(o.name);
	}

	@Override
	public String toString() {
		return name;
	}
}

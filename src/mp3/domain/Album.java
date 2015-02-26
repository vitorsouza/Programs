package mp3.domain;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import mp3.Mp3Utils;

import org.apache.log4j.Logger;

/**
 * Domain class used by Mp3CheckTagsAndFolders.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class Album implements Comparable<Album> {
	private static final Logger log = Logger.getLogger(Album.class);

	private Artist artist;

	private String name;

	private Integer year;

	private File folder;

	private SortedSet<Track> tracks;

	private Map<String, Object> extras = new HashMap<String, Object>();

	public Album(Artist artist, File folder) {
		this.artist = artist;
		this.folder = folder;

		log.debug("Reading album from folder: " + folder.getAbsolutePath());
		extractInfo();
	}

	protected void extractInfo() {
		// TODO: check string pattern, throw malformed name exception and treat it elsewhere.

		// Gets the year of the album from the name of the folder.
		String name = folder.getName();
		int idxA = name.indexOf('(');
		int idxB = name.indexOf(')');
		year = Integer.parseInt(name.substring(idxA + 1, idxB));

		// Gets the name of the album from the name of the folder.
		this.name = name.substring(idxB + 2);
		log.debug("Extracted album year and name: " + year + " / " + this.name);

		// Checks for tracks in the files.
		tracks = new TreeSet<Track>();
		for (File trackFile : folder.listFiles()) {
			if (trackFile.isFile() && Mp3Utils.MP3_FILTER.accept(trackFile)) tracks.add(new Track(this, trackFile));
		}
	}

	public Artist getArtist() {
		return artist;
	}

	public void setArtist(Artist artist) {
		this.artist = artist;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public File getFolder() {
		return folder;
	}

	public void setFolder(File folder) {
		this.folder = folder;
	}

	public SortedSet<Track> getTracks() {
		return tracks;
	}

	public void setTracks(SortedSet<Track> tracks) {
		this.tracks = tracks;
	}

	public Map<String, Object> getExtras() {
		return extras;
	}

	public void setExtras(Map<String, Object> extras) {
		this.extras = extras;
	}

	@Override
	public int compareTo(Album o) {
		int cmp = year.compareTo(o.year);
		return (cmp == 0) ? name.compareTo(o.name) : cmp;
	}

	@Override
	public String toString() {
		return "(" + year + ") " + name;
	}
}

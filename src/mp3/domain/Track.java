package mp3.domain;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mp3.Mp3Utils;

/**
 * Domain class used by Mp3CheckTagsAndFolders.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class Track implements Comparable<Track> {
	private static final Logger log = LogManager.getLogger(Track.class);

	private Album album;

	private Integer number;

	private String artistName;

	private String name;

	private File file;

	private Map<String, Object> extras = new HashMap<String, Object>();

	public Track(Album album, File file) {
		this.album = album;
		this.file = file;

		log.debug("Reading track from file: " + file.getAbsolutePath());
		extractInfo();
	}

	private void extractInfo() {
		// TODO: check string pattern, throw malformed name exception and treat it elsewhere.

		// Gets the artist of the track from the name of the file (sometimes it's different than the artist of the album).
		String name = file.getName();
		int idxA = name.indexOf('[');
		int idxB = name.indexOf(']');
		artistName = name.substring(idxA + 1, idxB);

		// Gets the number of the track from the name of the file.
		name = name.substring(idxB + 2);
		idxB = name.indexOf('.');
		number = Integer.parseInt(name.substring(0, idxB));

		// Gets the name of the track from the name of the file.
		this.name = name.substring(idxB + 2, name.length() - 4);
		log.debug("Extracted track artist, number and name: " + artistName + " / " + number + " / " + this.name);
	}

	public Album getAlbum() {
		return album;
	}

	public void setAlbum(Album album) {
		this.album = album;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public Map<String, Object> getExtras() {
		return extras;
	}

	public void setExtras(Map<String, Object> extras) {
		this.extras = extras;
	}

	@Override
	public int compareTo(Track o) {
		int cmp = number.compareTo(o.number);
		return (cmp == 0) ? name.compareTo(o.name) : cmp;
	}

	@Override
	public String toString() {
		return "[" + artistName + "] " + Mp3Utils.trackNumberFormat.format(number) + ". " + name;
	}
}

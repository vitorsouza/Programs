package mp3.tasks;

import java.io.File;

import org.apache.log4j.Logger;

import mp3.domain.Artist;

public class RenameArtistFolderTask extends RenameTask {
	private static final Logger log = Logger.getLogger(RenameArtistFolderTask.class);

	private Artist artist;

	public RenameArtistFolderTask(String newName, Artist artist) {
		super(newName);
		this.artist = artist;
	}

	@Override
	protected int weight() {
		// Artists should be renamed third.
		return 3;
	}

	@Override
	public int compareTo(Task o) {
		// If not the same kind of rename task, use the superclass' implementation.
		if (! (o instanceof RenameArtistFolderTask)) return super.compareTo(o);
		
		// If it's the same, compare by artist. There shouldn't be two tasks for the same artist.
		else return artist.compareTo(((RenameArtistFolderTask)o).artist);
	}

	@Override
	public String toString() {
		return "Artist rename: \"" + artist.getName() + "\" -> \"" + newName + "\"";
	}

	@Override
	public void execute() {
		artist.setName(newName);
		File folder = artist.getFolder();
		File newFolder = new File(folder.getParent(), "" + artist);
		log.info("Renaming: [" + folder.getName() + "] -> [" + newFolder.getName() + "]");
		folder.renameTo(newFolder);
	}
}

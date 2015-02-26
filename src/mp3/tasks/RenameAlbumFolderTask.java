package mp3.tasks;

import java.io.File;

import mp3.domain.Album;

import org.apache.log4j.Logger;

/**
 * One of the tasks used by Mp3CheckTagsAndFolders, which builds a task list, confirms all actions and only then
 * performs the tasks.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class RenameAlbumFolderTask extends RenameTask {
	private static final Logger log = Logger.getLogger(RenameAlbumFolderTask.class);

	private Album album;

	public RenameAlbumFolderTask(String newName, Album album) {
		super(newName);
		this.album = album;
	}

	@Override
	protected int weight() {
		// Albums should be renamed second.
		return 2;
	}

	@Override
	public int compareTo(Task o) {
		// If not the same kind of rename task, use the superclass' implementation.
		if (!(o instanceof RenameAlbumFolderTask)) return super.compareTo(o);

		// If it's the same, compare by album. There shouldn't be two tasks for the same album.
		else return album.compareTo(((RenameAlbumFolderTask) o).album);
	}

	@Override
	public String toString() {
		return "Album rename: \"" + album.getName() + "\" -> \"" + newName + "\"";
	}

	@Override
	public void execute() {
		album.setName(newName);
		File folder = album.getFolder();
		File newFolder = new File(folder.getParent(), "" + album);
		log.info("Renaming: [" + folder.getName() + "] -> [" + newFolder.getName() + "]");
		folder.renameTo(newFolder);
	}
}

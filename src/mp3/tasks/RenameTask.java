package mp3.tasks;

/**
 * One of the tasks used by Mp3CheckTagsAndFolders, which builds a task list, confirms all actions and only then
 * performs the tasks.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public abstract class RenameTask implements Task {
	protected String newName;

	public RenameTask(String newName) {
		this.newName = newName;
	}

	protected abstract int weight();

	@Override
	public int compareTo(Task o) {
		// Renaming tasks should be done last.
		if (!(o instanceof RenameTask)) return 1;
		RenameTask t = (RenameTask) o;

		// Compare weight (first rename tracks, then albums, then artists).
		return weight() - t.weight();
	}
}

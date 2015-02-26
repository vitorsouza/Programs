package mp3.tasks;

/**
 * Interface for tasks used by Mp3CheckTagsAndFolders, which builds a task list, confirms all actions and only then
 * performs the tasks.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public interface Task extends Comparable<Task> {
	void execute();
}

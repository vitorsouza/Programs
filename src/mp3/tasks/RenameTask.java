package mp3.tasks;

public abstract class RenameTask implements Task {
	protected String newName;
	
	public RenameTask(String newName) {
		this.newName = newName;
	}
	
	protected abstract int weight();

	@Override
	public int compareTo(Task o) {
		// Renaming tasks should be done last.
		if (! (o instanceof RenameTask)) return 1;
		RenameTask t = (RenameTask)o;
		
		// Compare weight (first rename tracks, then albums, then artists).
		return weight() - t.weight();
	}
}

package text;

/**
 * Helper class used by FileMatcher.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class FileInfo {
	String name;
	String date;
	String time;
	String size;
	String folder;

	FileInfo(String name, String date, String time, String size, String folder) {
		this.name = name;
		this.date = date;
		this.time = time;
		this.size = size;
		this.folder = folder;
	}
}

package bibtex.domain;

import java.util.Scanner;

/**
 * TODO: document this type.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class BibtexEntry implements Comparable<BibtexEntry> {
	/** TODO: document this field. */
	private String title;

	/** TODO: document this field. */
	private int year;

	/** TODO: document this field. */
	private String entry;

	/** Constructor. */
	public BibtexEntry(String entry) {
		this.entry = entry;

		// Looks for the title and the year.
		Scanner scanner = new Scanner(entry);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();
			if (line.startsWith("title")) {
				int from = line.indexOf("{{");
				int to = line.lastIndexOf("}}");
				title = line.substring(from + 2, to);
			}
			if (line.startsWith("year")) {
				int from = line.indexOf("{");
				int to = line.lastIndexOf("}");
				year = Integer.parseInt(line.substring(from + 1, to));
			}
		}
	}

	/** @see java.lang.Comparable#compareTo(java.lang.Object) */
	@Override
	public int compareTo(BibtexEntry o) {
		// Compare first by year, descending.
		int cmp = o.year - year;
		if (cmp != 0) return cmp;

		// If same year, compare by title, ascending.
		return title.compareTo(o.title);
	}

	/** @see java.lang.Object#toString() */
	@Override
	public String toString() {
		return entry;
	}
}

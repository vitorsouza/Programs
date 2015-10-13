package bibtex.domain;

import java.util.ArrayList;
import java.util.List;

public abstract class Publication implements Comparable<Publication> {
	/** TODO: document this field. */
	protected String bibtexKey;
	
	/** TODO: document this field. */
	protected String title;
	
	/** TODO: document this field. */
	protected List<String> authors = new ArrayList<>();
	
	/** TODO: document this field. */
	protected int year;
	
	/** Constructor. */
	protected Publication(String bibtexKey, String title, int year, String ... authors) {
		this.bibtexKey = bibtexKey;
		this.title = title;
		this.year = year;
		for (String author : authors) this.authors.add(author);
	}

	/** Getter for title. */
	public String getTitle() {
		return title;
	}

	/** Getter for year. */
	public int getYear() {
		return year;
	}
	
	/**
	 * TODO: document this method.
	 * @return
	 */
	public abstract String toBibtex();
	
	/** @see java.lang.Comparable#compareTo(java.lang.Object) */
	@Override
	public int compareTo(Publication o) {
		// First, compare by year.
		int cmp = o.year - year;
		if (cmp != 0) return cmp;
		
		// If the year is the same, compare by title.
		return title.compareTo(o.title);
	}
	
	/** @see java.lang.Object#equals(java.lang.Object) */
	@Override
	public boolean equals(Object o) {
		if (! (o instanceof Publication)) return false;
		Publication p = (Publication)o;
		return (year == p.year) && bibtexKey.equals(p.bibtexKey) && title.equals(p.title);
	}
}

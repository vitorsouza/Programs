package bibtex.domain;

public abstract class GreyLiterature extends Publication {
	/** TODO: document this field. */
	protected String institution;
	
	/** Constructor. */
	protected GreyLiterature(String bibtexKey, String title, int year, String institution, String ... authors) {
		super(bibtexKey, title, year, authors);
		this.institution = institution;
	}
	
	/** @see java.lang.Object#toString() */
	@Override
	public String toString() {
		return authors.get(0) + "; " + title + ". " + institution + ", " + year;
	}
	
	/**
	 * TODO: document this method.
	 * @return
	 */
	protected abstract String getBibtexEntryType();
	
	/** @see bibtex.domain.Publication#toBibtex() */
	@Override
	public String toBibtex() {
		StringBuilder builder = new StringBuilder();
		
		// Produces the BibTeX entry for this publication, using template methods for the differences b/w undergrad, masters and PhD.
		builder.append(getBibtexEntryType()).append("{").append(bibtexKey).append(",\n");			// @techreport{publicationKey,
		builder.append(" title = {{").append(title).append("}},\n");													//  title = {{Publication's Title}},
		builder.append(" author = {").append(authors.get(0)).append("},\n");									//  author = {Surname, First Name},
		builder.append(" institution = {").append(institution).append("},\n");								//  institution = {University of BibTeX},
		builder.append(" year = {").append(year).append("}\n");																//  year = {2015}
		builder.append("}\n");																																// }
		
		return builder.toString();
	}
}

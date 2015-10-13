package bibtex.domain;

public class PhdThesis extends GreyLiterature {
	/** Constructor. */
	public PhdThesis(String bibtexKey, String title, int year, String institution, String ... authors) {
		super(bibtexKey, title, year, institution, authors);
	}

	/** @see bibtex.domain.GreyLiterature#getBibtexEntryType() */
	@Override
	protected String getBibtexEntryType() {
		return "@phdthesis";
	}
}

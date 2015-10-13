package bibtex.domain;

public class UndergradMonograph extends GreyLiterature {
	/** Constructor. */
	public UndergradMonograph(String bibtexKey, String title, int year, String institution, String ... authors) {
		super(bibtexKey, title, year, institution, authors);
	}

	/** @see bibtex.domain.GreyLiterature#getBibtexEntryType() */
	@Override
	protected String getBibtexEntryType() {
		return "@techreport";
	}
}

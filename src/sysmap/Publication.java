package sysmap;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * Domain class used by ProcessDuplicates.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class Publication {
	/** Name of the properties file with the search URL for different sources. */
	private static final String SOURCE_SEARCH_PROPERTIES_FILE = "sysmap-sourcesearch.properties";
	
	/** Database of search URLs to be able to produce HTML versions of a publication's sources. */
	private static final Properties sourceSearchDB = new Properties();
	static {
		try (FileReader reader = new FileReader(new File(SOURCE_SEARCH_PROPERTIES_FILE))) {
			sourceSearchDB.load(reader);
		}
		catch (IOException e) {
			System.out.printf("Could not load source search properties file: %s. Sources as HTML will not contain links!");
		}
	}
	
	/** Publication title. */
	private String title;
	
	/** Publication year. */
	private int year;
	
	/** Publication's keywords. */
	private String keywords;
	
	/** Publication's abstract. */
	private String abztract;
	
	/** Sources that have returned this publication as result of the search. */
	private Set<String> sources = new TreeSet<>();

	/** Constructor. */
	public Publication(String title, int year, String source) {
		this.title = title;
		this.year = year;
		sources.add(source);
	}
	
	/** Constructor. */
	public Publication(String title, int year, String keywords, String abztract, String source) {
		this(title, year, source);
		this.keywords = keywords;
		this.abztract = abztract;
	}

	/** Getter for title. */
	public String getTitle() {
		return title;
	}

	/** Setter for title. */
	public void setTitle(String title) {
		this.title = title;
	}

	/** Getter for year. */
	public int getYear() {
		return year;
	}

	/** Setter for year. */
	public void setYear(int year) {
		this.year = year;
	}

	/** Getter for sources. */
	public Set<String> getSources() {
		return sources;
	}

	/** Setter for sources. */
	public void setSources(Set<String> sources) {
		this.sources = sources;
	}
	
	/** Getter for keywords. */
	public String getKeywords() {
		return keywords;
	}

	/** Setter for keywords. */
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	/** Getter for abztract. */
	public String getAbztract() {
		return abztract;
	}

	/** Setter for abztract. */
	public void setAbztract(String abztract) {
		this.abztract = abztract;
	}

	/** Returns the only publication source for publications that have only one. */
	public String getOnlySource() {
		if (sources.size() == 1) return sources.iterator().next();
		else throw new IllegalStateException("Publication \"" + title + "\" has " + sources.size() + " sources!");
	}
	
	/** Merges one publication with another. */
	public void mergeWith(Publication pub) {
		// Uses the longest title.
		if (pub.title.length() > title.length()) title = pub.title;
		
		// Adds the other publications sources.
		for (String source : pub.sources) sources.add(source);
	}
	
	/** Produces a string with the sources names in alphabetical order. */
	public String getSourcesString() {
		StringBuilder builder = new StringBuilder();
		for (String source : sources) builder.append(source).append(", ");
		if (builder.length() > 1) builder.delete(builder.length() - 2, builder.length());
		return builder.toString();
	}
	
	/** Produces a string with the sources names in alphabetical order and HTML links to searching the paper in the source. */
	public String getSourcesHtml() {
		StringBuilder builder = new StringBuilder();
		for (String source : sources) {
			// If this source has a search URL registered in the properties file, add an HTML link around its name.
			if (sourceSearchDB.containsKey(source)) {
				String searchUrl = ("" + sourceSearchDB.get(source)).replace("{0}", title);
				source = "<a href=\"" + searchUrl + "\" target=\"_blank\">" + source + "</a>";
			}
			
			// Adds the source to the list.
			builder.append(source).append(", ");
		}
		if (builder.length() > 1) builder.delete(builder.length() - 2, builder.length());
		return builder.toString();
	}
}

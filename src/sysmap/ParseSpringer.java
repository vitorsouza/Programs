package sysmap;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * TODO: document this type.
 *
 * @author Pedro Negri
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class ParseSpringer {
	private static final String SOURCE_NAME = "Springer";
	
	private static final String SPRINGER_BASE = "http://link.springer.com";
	
	private static final String START_URL = SPRINGER_BASE + "/search?query=%22requirements+at+runtime%22";
	
	private static final String OUTPUT_FILE = "sysmap-raw-springer.csv";
	
	public static void main(String[] args) throws Exception {
		// Retrieves all publications returned from this search.
		List<Publication> publications = retrieveAllPublications();
		
		File outputFile = new File(OUTPUT_FILE);
		try (PrintWriter out = new PrintWriter(outputFile)) {
			out.println("Source;Year;Title;Keywords;Abstract");
			
			for (Publication publication : publications) {
				out.printf("%s;%d;%s;%s;%s%n", SOURCE_NAME, publication.getYear(), publication.getTitle().replace(";", ",,"), publication.getKeywords().replace(";", ",,"), publication.getAbztract().replace(";", ",,"));
			}			
		}
		
		System.out.println("Done! Output file: " + outputFile.getName());
	}
	
	private static List<Publication> retrieveAllPublications() throws Exception {
		// Extracts the base address of the URL.
		String baseUrl = START_URL.substring(0, START_URL.indexOf('/', 7));

		// Stores the relevant publications in a list.
		List<Publication> publications = new ArrayList<>();
		int count = 0, pageCount = 0;
		
		// Processes all pages, following "next" links.
		String url = START_URL;
		while (url != null) {
			// Opens the page and extracts the HTML DOM structure into Jsoup. Assumes this is the last page.
			System.out.printf("Fetching page %02d (%s)%n", ++pageCount, url);
			Document doc = Jsoup.connect(url).timeout(10000*10000).get();
			url = null;

			// Finds the elements that represent the results and iterate through them.
			Elements results = doc.select("#results-list").first().select("li");
			for (Element result : results) {
				// Extracts publication name and URL.
				Element link = result.select("h2").first().select("a").first();
				String pubName = link.text();
				String pubUrl = SPRINGER_BASE + link.attr("href");
				
				// Opens the publication's page and extracts the HTML DOM structure into Jsoup.
				System.out.printf("\tFetching paper %02d: %s (%s)%n", ++count, pubName, pubUrl);
				Document pubDoc = Jsoup.connect(pubUrl).timeout(10000*10000).get();
				
				// Extracts year.
				int year = 0;
				Elements yearElems = pubDoc.select("#abstract-about-book-chapter-copyright-year");
				if (yearElems.isEmpty()) yearElems = pubDoc.select("span.ArticleCitation_Year > time");
				if (! yearElems.isEmpty()) year = parseYear(yearElems.first().text(), count);
				else System.out.printf("%s: line %d has no year! Using 0 as year.%n", SOURCE_NAME, count);
				
				// Extracts abstract and keywords
				String abztract = pubDoc.select("section.Abstract > p.Para").first().text();
				Elements keyElems = pubDoc.select("ul.abstract-keywords > li");
				StringBuilder keywords = new StringBuilder();
				for (Element keyElem : keyElems) keywords.append(keyElem.text()).append(", ");
				
				// Adds the publication.
				publications.add(new Publication(pubName, year, keywords.toString(), abztract, SOURCE_NAME));
			}
			
			// Checks if there's another page and sets its URL to open it in the next iteration.
			Elements nextLinks = doc.select("a.next");
			if (! nextLinks.isEmpty()) url = nextLinks.first().attr("href");
			if (url != null && url.startsWith("/")) url = baseUrl + url;
			else url = null;
			System.out.println();
		}
		
		return publications;
	}

	private static int parseYear(String yearData, long recordNumber) {
		if (yearData.matches("\\d{4}")) return Integer.parseInt(yearData);
				
		String[] data = yearData.split(" ");
		for (int i = 0; i < data.length; i++) if (data[i].matches("\\d{4}")) return Integer.parseInt(data[i]);
		
		System.out.printf("%s: line %d has publication with unrecognizable year: %s! Using 0 as year.%n", SOURCE_NAME, recordNumber, yearData);
		return 0;
	}
}

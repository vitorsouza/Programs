package text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public class ExtractProductionFromLattesXml {
	/** Path to the XML file that contains the Lattes CV to be parsed. */
	private static final String LATTES_XML_FOLDER_PATH = "lattes-ppgi";

	/** Path to the configuration file with all the settings for this script. */
	private static final String CONFIG_FILE_PATH = "production-from-lattes.properties";

	/** Properties object that holds all the configuration. */
	private static final Properties CONFIG = new Properties();

	/** Set of Lattes production entries. */
	private static final SortedSet<LattesProduction> entries = new TreeSet<>();

	/** Main method. */
	public static void main(String[] args) throws Exception {
		// Loads the properties file.
		CONFIG.load(new FileInputStream(new File(CONFIG_FILE_PATH)));

		// References the folder where all Lattes XML files should be located.
		File lattesXmlFolder = new File(LATTES_XML_FOLDER_PATH);

		// Creates file descriptors for the output files.
		File outputFile = new File(CONFIG.getProperty("outputFile"));

		// Processes each file in the Lattes XML folder.
		for (File lattesXmlFile : lattesXmlFolder.listFiles()) {
			System.out.printf("***** PROCESSING: %s *****%n%n", lattesXmlFile.getName());

			// Reads the entire contents of the XML file.
			StringBuilder xmlContents = new StringBuilder();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(lattesXmlFile), CONFIG.getProperty("encoding")))) {
				String line = reader.readLine();
				while (line != null) {
					xmlContents.append(line).append('\n');
					line = reader.readLine();
				}
			}

			// Parses the XML file with the Lattes CV using Jsoup.
			Document doc = Jsoup.parse(xmlContents.toString(), "", Parser.xmlParser());

			// Extracts the entries from the CV, placing them on the global collections.
			extractEntries(doc);
			System.out.println("\n\n");
		}

		// Writes the CSV file with the result.
		writeOutput(outputFile, entries);

		System.out.println("\nDone!");
	}

	/**
	 * TODO: document this method.
	 * 
	 * @param outputFile
	 * @param entries
	 * @throws FileNotFoundException
	 */
	private static void writeOutput(File outputFile, Set<LattesProduction> entries) throws FileNotFoundException {
		try (PrintWriter out = new PrintWriter(outputFile)) {
			out.println(CONFIG.getProperty("outputHeader"));
			for (LattesProduction entry : entries) {
				out.println(entry.toCSV());
			}
		}
	}

	/**
	 * TODO: document this method.
	 * 
	 * @param doc
	 */
	private static void extractEntries(Document doc) {
		// Extracts the name of the researcher first.
		Element generalData = doc.select(CONFIG.getProperty("jsoupSelectorGeneralData")).first();
		String researcher = generalData.attr(CONFIG.getProperty("generalDataName"));
		System.out.printf("Parsing Lattes CV of: %s%n", researcher);
		
		// Navigates to the nodes that contains all bibliographic production (supports multiple, althoug we expect a single one).
		Elements bibliographyLists = doc.select(CONFIG.getProperty("jsoupSelectorBibliographic"));

		// Goes through all the bibliography.
		for (Element elem : bibliographyLists) {
			// Extracts conference, journal, books & chapters, magazine and others.
			extractEntries(researcher, elem, "Events");
			extractEntries(researcher, elem, "Journals");
			extractEntries(researcher, elem, "Books");
			extractEntries(researcher, elem, "Chapters");
			extractEntries(researcher, elem, "Magazines");
			extractEntries(researcher, elem, "Others");
		}
	}

	/**
	 * TODO: document this method.
	 * @param researcher
	 * @param element
	 * @param name
	 */
	private static void extractEntries(String researcher, Element element, String name) {
		// Gets the structural information from the configuration.
		int startYear = Integer.parseInt(CONFIG.getProperty("startYear"));
		int endYear = Integer.parseInt(CONFIG.getProperty("endYear"));
		String selector = CONFIG.getProperty("jsoupSelectorBibliographic" + name);
		String selectorGeneral = CONFIG.getProperty("jsoupSelectorBibliographic" + name + "General");
		String selectorDetails = CONFIG.getProperty("jsoupSelectorBibliographic" + name + "Details");
		String selectorAuthors = CONFIG.getProperty("jsoupSelectorBibliographic" + name + "Authors");
		String baseType = CONFIG.getProperty("bibliographic" + name + "BaseType");
		String attrType = CONFIG.getProperty("bibliographic" + name + "Type");
		String attrYear = CONFIG.getProperty("bibliographic" + name + "Year");
		String attrTitle = CONFIG.getProperty("bibliographic" + name + "Title");
		String[] attrVenue = CONFIG.getProperty("bibliographic" + name + "Venue").split("\\s*\\|\\s*");
		String attrAuthors = CONFIG.getProperty("bibliographic" + name + "Authors");
		
		// Goes through all entries.
		Elements elems = element.select(selector);
		for (Element elem : elems) {
			// Checks if the year is within the desired range. 
			Element general = elem.select(selectorGeneral).first();
			int year = parseYear(general.attr(attrYear));
			if ((startYear == 0 || startYear <= year) && (endYear == 0 || endYear >= year)) {
				// Extracts the information needed.
				String type = baseType +  " / " + general.attr(attrType);
				String title = general.attr(attrTitle);
				
				// Venue can be split into more than one attribute.
				StringBuilder venues = new StringBuilder();
				for (String attr : attrVenue) venues.append(elem.select(selectorDetails).first().attr(attr)).append(" / ");
				venues.deleteCharAt(venues.length() - 1);
				venues.deleteCharAt(venues.length() - 1);
				venues.deleteCharAt(venues.length() - 1);				
				
				// Authors can be multiple.
				StringBuilder authors = new StringBuilder();
				for (Element author : elem.select(selectorAuthors)) authors.append(author.attr(attrAuthors)).append(", ");
				authors.deleteCharAt(authors.length() - 1);
				authors.deleteCharAt(authors.length() - 1);
				
				// Creates a production entry and adds to the set.
				entries.add(new LattesProduction(type, researcher, year, title, venues.toString(), authors.toString()));
			}
		}
	}
	
	private static int parseYear(String yearData) {
		if (yearData.matches("\\d{4}")) return Integer.parseInt(yearData);
		String[] data = yearData.split(" ");
		for (int i = 0; i < data.length; i++) if (data[i].matches("\\d{4}")) return Integer.parseInt(data[i]);
		return 0;
	}
}

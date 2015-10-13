package bibtex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.Normalizer;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import bibtex.domain.GreyLiterature;
import bibtex.domain.MastersDissertation;
import bibtex.domain.PhdThesis;
import bibtex.domain.UndergradMonograph;

public class ExtractBibtexFromLattesXml {
	/** Path to the XML file that contains the Lattes CV to be parsed. */
	private static final String LATTES_XML_FOLDER_PATH = "lattes-nemo";

	/** Path to the configuration file with all the settings for this script. */
	private static final String CONFIG_FILE_PATH = "bibtex-from-lattes.properties";

	/** Properties object that holds all the configuration. */
	private static final Properties CONFIG = new Properties();

	/** Map of undergrad monograph entries. */
	private static final Set<UndergradMonograph> undergradEntries = new TreeSet<>();

	/** Map of master dissertation entries. */
	private static final Set<MastersDissertation> mastersEntries = new TreeSet<>();

	/** Map of PhD theses entries. */
	private static final Set<PhdThesis> phdEntries = new TreeSet<>();

	/** Main method. */
	public static void main(String[] args) throws Exception {
		// Loads the properties file.
		CONFIG.load(new FileInputStream(new File(CONFIG_FILE_PATH)));

		// References the folder where all Lattes XML files should be located.
		File lattesXmlFolder = new File(LATTES_XML_FOLDER_PATH);

		// Creates file descriptors for the output files.
		File outputUndergradFile = new File(CONFIG.getProperty("bibtexOutputFileUndergrad"));
		File outputMastersFile = new File(CONFIG.getProperty("bibtexOutputFileMasters"));
		File outputPhdFile = new File(CONFIG.getProperty("bibtexOutputFilePhd"));

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

		// Writes the BibTeX files for each type of entry.
		writeBibtexOutput(outputUndergradFile, undergradEntries);
		writeBibtexOutput(outputMastersFile, mastersEntries);
		writeBibtexOutput(outputPhdFile, phdEntries);

		System.out.println("\nDone!");
	}

	/**
	 * TODO: document this method.
	 * 
	 * @param outputFile
	 * @param entries
	 * @throws FileNotFoundException
	 */
	private static void writeBibtexOutput(File outputFile, Set<? extends GreyLiterature> entries) throws FileNotFoundException {
		try (PrintWriter out = new PrintWriter(outputFile)) {
			for (GreyLiterature entry : entries) {
				out.println(entry.toBibtex());
			}
		}
	}

	/**
	 * TODO: document this method.
	 * 
	 * @param doc
	 */
	private static void extractEntries(Document doc) {
		// Navigates to the nodes that contains all supervisions (supports multiple, althoug we expect a single one).
		Elements supervisionLists = doc.select(CONFIG.getProperty("jsoupSelectorSupervisions"));
		System.out.printf("Discovered %d supervision list(s) in the XML file...%n", supervisionLists.size());

		// Goes through all the supervision lists.
		for (Element supervisionList : supervisionLists) {
			// Navigates to the elements representing undergrad monographs and extracts the entries, placing them on the map.
			// Uses a generic extraction method, thus provides a builder instance that is able to build entries of this kind.
			// (Mongraphs, dissertations and theses all have the same BibTeX attributes, basically).
			Elements elems = supervisionList.select(CONFIG.getProperty("jsoupSelectorUndergradEntry"));
			System.out.printf("%nDiscovered %d supervision entries of type: undergrad monograph. Filtering...%n", elems.size());
			extractEntries(elems, undergradEntries, new GreylitBuilder<UndergradMonograph>() {
				@Override
				public UndergradMonograph build(String bibtexKey, String title, int year, String institution, String author) {
					return new UndergradMonograph(bibtexKey, title, year, institution, author);
				}

				@Override
				public String getEntryTypeKey() {
					return "entryTypeForUndergrad";
				}

				@Override
				public String getBasicDataSelectorKey() {
					return "jsoupSelectorUndergradBasic";
				}

				@Override
				public String getDetailedDataSelectorKey() {
					return "jsoupSelectorUndergradDetails";
				}
			});

			// Same as before, for masters dissertations.
			elems = supervisionList.select(CONFIG.getProperty("jsoupSelectorMastersEntry"));
			System.out.printf("%nDiscovered %d supervision entries of type: masters dissertation. Filtering...%n", elems.size());
			extractEntries(elems, mastersEntries, new GreylitBuilder<MastersDissertation>() {
				@Override
				public MastersDissertation build(String bibtexKey, String title, int year, String institution, String author) {
					return new MastersDissertation(bibtexKey, title, year, institution, author);
				}

				@Override
				public String getEntryTypeKey() {
					return "entryTypeForMasters";
				}

				@Override
				public String getBasicDataSelectorKey() {
					return "jsoupSelectorMastersBasic";
				}

				@Override
				public String getDetailedDataSelectorKey() {
					return "jsoupSelectorMastersDetails";
				}
			});

			// Same as before, for PhD theses.
			elems = supervisionList.select(CONFIG.getProperty("jsoupSelectorPhdEntry"));
			System.out.printf("%nDiscovered %d supervision entries of type: PhD thesis. Filtering...%n", elems.size());
			extractEntries(elems, phdEntries, new GreylitBuilder<PhdThesis>() {
				@Override
				public PhdThesis build(String bibtexKey, String title, int year, String institution, String author) {
					return new PhdThesis(bibtexKey, title, year, institution, author);
				}

				@Override
				public String getEntryTypeKey() {
					return "entryTypeForPhd";
				}

				@Override
				public String getBasicDataSelectorKey() {
					return "jsoupSelectorPhdBasic";
				}

				@Override
				public String getDetailedDataSelectorKey() {
					return "jsoupSelectorPhdDetails";
				}
			});
		}
	}

	/**
	 * TODO: document this method.
	 * 
	 * @param elements
	 * @param entries
	 * @param builder
	 */
	private static <T extends GreyLiterature> void extractEntries(Elements elements, Set<T> entries, GreylitBuilder<T> builder) {
		int count = 0;

		// Get the properties from the configuration before iterating.
		int startYear = Integer.parseInt(CONFIG.getProperty("startYear"));
		int endYear = Integer.parseInt(CONFIG.getProperty("endYear"));
		String jsoupSelectorBasic = CONFIG.getProperty(builder.getBasicDataSelectorKey());
		String jsoupSelectorDetailed = CONFIG.getProperty(builder.getDetailedDataSelectorKey());
		String entryId = CONFIG.getProperty("entryId");
		String entryAttributeYear = CONFIG.getProperty("entryAttributeYear");
		String entryAttributeNature = CONFIG.getProperty("entryAttributeNature");
		String entryAttributeTitle = CONFIG.getProperty("entryAttributeTitle");
		String entryAttributeInstitution = CONFIG.getProperty("entryAttributeInstitution");
		String entryAttributeSupervised = CONFIG.getProperty("entryAttributeSupervised");
		String entryAttributeSupervisorType = CONFIG.getProperty("entryAttributeSupervisorType");
		String correctEntryType = CONFIG.getProperty(builder.getEntryTypeKey());
		String mainSupervisorType = CONFIG.getProperty("mainSupervisorType");

		// Goes through every element that has been parsed.
		for (Element elem : elements) {
			// Obtains the basic and detailed data from the element.
			Element basic = elem.select(jsoupSelectorBasic).first();
			Element detail = elem.select(jsoupSelectorDetailed).first();
			String id = elem.attr(entryId);

			// Checks if the nature matches the entry type and that the supervisor is the main one.
			String nature = basic.attr(entryAttributeNature);
			String supervisorType = detail.attr(entryAttributeSupervisorType);
			if (correctEntryType.equals(nature) && ((! detail.hasAttr(entryAttributeSupervisorType)) || mainSupervisorType.equals(supervisorType))) {
				// Checks that the year is in the period of interest.
				int year = Integer.parseInt(basic.attr(entryAttributeYear));
				if ((startYear == 0 || startYear <= year) && (endYear == 0 || endYear >= year)) {
					// Gets the other relevant attributes for the publication.
					String title = basic.attr(entryAttributeTitle);
					String institution = detail.attr(entryAttributeInstitution);
					String supervised = bibtexAuthorFormat(detail.attr(entryAttributeSupervised));

					// Creates the publication, generates a key for it and places it in the map.
					String bibtexKey = generateEntryKey(title, year);
					T entry = builder.build(bibtexKey, title, year, institution, supervised);
					System.out.printf("\t- %s: %s%n", id, entry);
					entries.add(entry);
					count++;
				}
			}
		}

		// Prints the count information.
		System.out.printf("\t** %d entries matched the configured filters%n", count);
	}

	/**
	 * TODO: document this method.
	 * 
	 * @param attr
	 * @return
	 */
	private static String bibtexAuthorFormat(String author) {
		// Separate the last name.
		int idx = author.lastIndexOf(' ');
		if (idx != -1) {
			String lastname = author.substring(idx + 1);

			// Place it in front of the first name, separated by a comma.
			author = lastname + ", " + author.substring(0, idx);
		}
		return author;
	}

	/**
	 * TODO: document this method.
	 * 
	 * @param entry
	 * @return
	 */
	private static String generateEntryKey(String title, int year) {
		// Start the key with the title in lower case.
		String key = title;
		key = key.toLowerCase();

		// Remove all accents and diacritical marks.
		key = Normalizer.normalize(key, Normalizer.Form.NFD).replaceAll("\\p{M}", "");

		// Replace non-alphanumeric characters with underscores.
		key = key.replaceAll("[^a-z0-9]", "_");

		// Append the year and return.
		key = key + "_" + year;
		return key;
	}
}

/**
 * TODO: document this type.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 * @param <T>
 */
interface GreylitBuilder<T extends GreyLiterature> {
	/**
	 * TODO: document this method.
	 * 
	 * @param bibtexKey
	 * @param title
	 * @param year
	 * @param institution
	 * @param author
	 * @return
	 */
	T build(String bibtexKey, String title, int year, String institution, String author);

	/**
	 * TODO: document this method.
	 * 
	 * @return
	 */
	String getEntryTypeKey();

	/**
	 * TODO: document this method.
	 * 
	 * @return
	 */
	String getBasicDataSelectorKey();

	/**
	 * TODO: document this method.
	 * 
	 * @return
	 */
	String getDetailedDataSelectorKey();
}

package text;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * TODO: document this type.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class ParseUfesStudentsTable {
	private static final String DATA_FILE_PATH = "parse-ufes-students.csv";
	
	private static final String OUTPUT_FILE_PATH = "parse-ufes-students.html";
	
	private static final String START_URL = "http://www.informatica.ufes.br/pos-graduacao/PPGI/lista-de-discentes-de-mestrado";

	private static final String SELECTOR_TABLE = "table";

	private static final String SELECTOR_ROW = "tr";

	private static final String SELECTOR_COLUMN = "td";

	private static final String SELECTOR_NEXT_LINK = "li.pager-next > a";

	private static final String SELECTOR_DETAIL_LINK = "a";

	private static final String ATTRIBUTE_LINK = "href";

	private static final int[] COLUMNS_TO_READ = new int[] { 0, 1, 3 };

	private static final int COLUMN_STUDENT = 0;
	
	private static final int COLUMN_SUPERVISOR = 3;

	private static final int COLUMN_DETAIL_LINK = 0;

	private static final String CSV_SEPARATOR = ";";
	
	private static final String MEMBERS_TABLE_LINE_TEMPLATE = "<tr%s>%n\t<td><a href=\"%s\">%s</a></td>%n\t<td><a href=\"%s\">%s</a></td>%n</tr>%n";

	private static final Map<String, String> homepageMap = new HashMap<>();

	private static final Set<String> supervisorFilter = new HashSet<>();

	private static final SortedMap<String, String> studentMap = new TreeMap<>();

	/** Main method. */
	public static void main(String[] args) throws Exception {
		// Loads the information on the data file. It maps names to homepage URLs and indicates supervisors to filter.
		try (Scanner scanner = new Scanner(new File(DATA_FILE_PATH))) {
			while (scanner.hasNextLine()) {
				String[] line = scanner.nextLine().split(";");
				if (Boolean.parseBoolean(line[1])) supervisorFilter.add(line[0]);
				homepageMap.put(line[0], line[2]);
			}
		}

		// Performs the parsing.
		parse();
		
		// Writes the HTML output for the website.
		writeHtmlOutput();
		
		System.out.println("\nDone!");
	}

	private static void writeHtmlOutput() throws Exception {
		boolean odd = false;
		
		// Opens the output file for writing.
		try (PrintWriter out = new PrintWriter(new File(OUTPUT_FILE_PATH))) {
			for (Map.Entry<String, String> entry : studentMap.entrySet()) {
				// Gets the student and supervisor information.
				String student = entry.getKey();
				String supervisor = entry.getValue();
				String studentHomepage = homepageMap.get(student);
				String supervisorHomepage = homepageMap.get(supervisor);
				
				// Writes a line to the output file.
				out.printf(MEMBERS_TABLE_LINE_TEMPLATE, (odd ? " class=\"odd\"" : ""), studentHomepage, student, supervisorHomepage, supervisor);
				odd = ! odd;
			}
		}
	}

	/**
	 * TODO: document this method.
	 * 
	 * @throws Exception
	 */
	private static void parse() throws Exception {
		// Extracts the base address of the URL.
		String baseUrl = START_URL.substring(0, START_URL.indexOf('/', 7));

		// Processes all pages, following "next" links.
		String url = START_URL;
		while (url != null) {
			// Opens the page and extracts the HTML DOM structure into Jsoup.
			Document doc = Jsoup.connect(url).get();
			url = null;

			// Looks for the first table in the document, where the names of the students are supposed to be.
			Element table = doc.select(SELECTOR_TABLE).first();

			// Extracts the rows from the table. Goes through all of them.
			Elements rows = table.select(SELECTOR_ROW);
			for (Element row : rows) {
				StringBuilder builder = new StringBuilder();

				// Extracts the columns from the row.
				Elements columns = row.select(SELECTOR_COLUMN);

				// Only reads the columns that have useful information for us.
				if (!columns.isEmpty()) {
					for (int idx : COLUMNS_TO_READ) builder.append(columns.get(idx).text()).append(CSV_SEPARATOR);

					// Also extracts the link to the detail page of the alumni.
					Element cell = columns.get(COLUMN_DETAIL_LINK);
					String link = cell.select(SELECTOR_DETAIL_LINK).attr(ATTRIBUTE_LINK);
					builder.append(link).append(CSV_SEPARATOR);

					// Checks if we should filter by supervisor.
					String supervisor = columns.get(COLUMN_SUPERVISOR).text();
					if (supervisorFilter.isEmpty() || supervisorFilter.contains(supervisor)) {
						// Outputs the information in CSV.
						System.out.println(builder);
						
						// Saves the information in the student map.
						String student = columns.get(COLUMN_STUDENT).text();
						studentMap.put(student, supervisor);
						
						// If the student doesn't have a homepage address yet, add her to the homepageMap.
						if (! homepageMap.containsKey(student)) homepageMap.put(student, baseUrl + link);
					}
				}
			}

			// Checks if there's a next page.
			Elements nextLinks = doc.select(SELECTOR_NEXT_LINK);
			if (!nextLinks.isEmpty()) url = nextLinks.first().attr(ATTRIBUTE_LINK);
			if (url != null && url.startsWith("/")) url = baseUrl + url;
		}

	}
}

package text;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Performs HTML scrapping at the website of UFES' Post-graduate Program in Computer Science looking for past students
 * (alumni) from a certain group of professors, generating an HTML output with the information from the students to be
 * published at a website (e.g., a research group website like nemo.inf.ufes.br).
 * 
 * This script depends on an input file called parse-ufes-alumni.csv in CSV format which should contain the following
 * information (one line per person):
 * 
 * [name];[is a supervisor? (true/false)];[website URL]
 * 
 * The script will find students from everyone that is listed as a supervisor (true in the second column). The format of
 * the file is the same as the input file of ParseUfesStudentsTable, however this script does not use the website
 * information for non-supervisors, so there is not point in also listing students/alumni in this file.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class ParseUfesAlumniTable {
	private static final String DATA_FILE_PATH = "parse-ufes-alumni.csv";

	private static final String OUTPUT_FILE_PATH = "parse-ufes-alumni.html";

	private static final int TIMEOUT_TIME = 20000;

	private static final long SLEEP_TIME = 2000;

	private static final String START_URL = "http://www.informatica.ufes.br/pos-graduacao/PPGI/lista-de-discentes-egressos";

	private static final String SELECTOR_TABLE = "table";

	private static final String SELECTOR_ROW = "tr";

	private static final String SELECTOR_COLUMN = "td";

	private static final String SELECTOR_HEADER = "tr > th";

	private static final String SELECTOR_NEXT_LINK = "li.pager-next > a";

	private static final String SELECTOR_DETAIL_LINK = "a";

	private static final String ATTRIBUTE_LINK = "href";

	private static final int COLUMN_NAME = 0;

	private static final int COLUMN_DEFENSE_DATE = 1;

	private static final int COLUMN_LEVEL = 2;

	private static final int HEADER_DETAIL_DEFENSEDATE = 2;

	private static final String HEADER_DETAIL_DEFENSEDATE_TEXT = "Data de defesa";

	private static final int COLUMN_DETAIL_TITLE = 0;

	private static final int COLUMN_DETAIL_TYPE = 1;

	private static final String COLUMN_DETAIL_TYPE_MASTERS = "Dissertação de mestrado acadêmico";

	private static final int COLUMN_WORK_DETAIL_SUPERVISOR_NAME = 0;

	private static final int COLUMN_WORK_DETAIL_SUPERVISOR_ROLE = 1;

	private static final String COLUMN_WORK_DETAIL_SUPERVISOR_ROLE_MAIN = "Orientador";

	private static String baseUrl;

	private static final Map<String, String> homepageMap = new HashMap<>();

	private static final Set<String> supervisorFilter = new HashSet<>();

	private static final SortedSet<Alumnus> alumni = new TreeSet<>();

	private static final SortedSet<Alumnus> filteredAlumni = new TreeSet<>();

	/** Main method. */
	public static void main(String[] args) throws Exception {
		// Extracts the base address of the URL.
		baseUrl = START_URL.substring(0, START_URL.indexOf('/', 7));

		// Loads the information on the data file. It maps names to homepage URLs and indicates supervisors to filter.
		try (Scanner scanner = new Scanner(new File(DATA_FILE_PATH))) {
			while (scanner.hasNextLine()) {
				String[] line = scanner.nextLine().split(";");
				if (Boolean.parseBoolean(line[1])) supervisorFilter.add(line[0]);
				homepageMap.put(line[0], line[2]);
			}
		}

		// Parses all alumni.
		int count = parseAlumni();
		System.out.printf("%nParsed %d alumni from the main table. Now checking details and filtering by supervisor...%n%n", count);

		// Parses details from the alumni, also filtering by supervisor.
		parseDetails();

		// Writes the HTML output for the website.
		writeHtmlOutput();

		System.out.println("\n\nDone!");
	}

	private static void writeHtmlOutput() throws Exception {
		boolean odd = false;

		// Opens the output file for writing.
		try (PrintWriter out = new PrintWriter(new File(OUTPUT_FILE_PATH))) {
			for (Alumnus alumnus : filteredAlumni) {
				// Writes a line to the output file.
				alumnus.printTableLine(out, odd, homepageMap);
				odd = !odd;
			}
		}
	}

	private static void parseDetails() throws Exception {
		// Processes all alumni.
		for (Alumnus alumnus : alumni) {
			// Opens the detail page of the alumnus. Sleeps for a while first to avoid spamming the server.
			Thread.sleep(SLEEP_TIME);
			System.out.printf("Checking details for %s... ", alumnus);
			Document doc = Jsoup.connect(alumnus.getUrl()).timeout(TIMEOUT_TIME).get();

			// Obtains the tables in the document and looks for the products table.
			Elements tables = doc.select(SELECTOR_TABLE);
			Element table = null;
			for (Element aTable : tables) {
				Elements headers = aTable.select(SELECTOR_HEADER);
				if (HEADER_DETAIL_DEFENSEDATE_TEXT.equals(headers.get(HEADER_DETAIL_DEFENSEDATE).text())) {
					table = aTable;
					break;
				}
			}

			// Checks if the table was found.
			if (table == null) {
				System.out.println("product table not found!");
			}
			else {
				// Extracts the rows from the table. Goes through all of them looking for the alumnus' defended work.
				String workUri = null;
				Elements rows = table.select(SELECTOR_ROW);
				for (Element row : rows) {
					// Extracts the columns from the row.
					Elements columns = row.select(SELECTOR_COLUMN);

					// Read the columns with useful information.
					if (!columns.isEmpty()) {
						Element titleCell = columns.get(COLUMN_DETAIL_TITLE);
						String type = columns.get(COLUMN_DETAIL_TYPE).text();

						if (COLUMN_DETAIL_TYPE_MASTERS.equals(type)) {
							workUri = baseUrl + titleCell.select(SELECTOR_DETAIL_LINK).attr(ATTRIBUTE_LINK);
							alumnus.setWorkTitle(titleCell.text());
						}
					}
				}

				// Checks if the alumnus' defended work was found.
				if (workUri == null) {
					System.out.println("defended work not found!");
				}
				else {
					// Opens the detail page of the alumnus' defended work.
					doc = Jsoup.connect(workUri).timeout(TIMEOUT_TIME).get();

					// Looks for the first table in the document, where the names of the supervisors are.
					table = doc.select(SELECTOR_TABLE).first();

					// Extracts the rows from the table. Goes through all of them looking for the main supervisor.
					String supervisor = null;
					if (table != null) {
						rows = table.select(SELECTOR_ROW);
						for (Element aRow : rows) {
							// Extracts the columns from the row.
							Elements columns = aRow.select(SELECTOR_COLUMN);

							// Checks if it's the main supervisor.
							if (!columns.isEmpty()) {
								if (COLUMN_WORK_DETAIL_SUPERVISOR_ROLE_MAIN.equals(columns.get(COLUMN_WORK_DETAIL_SUPERVISOR_ROLE).text())) {
									supervisor = columns.get(COLUMN_WORK_DETAIL_SUPERVISOR_NAME).text();
								}
							}
						}
					}

					// Checks if the supervisor was found.
					if (supervisor == null) {
						System.out.println("supervisor name not found!");
					}

					// Checks if the alumnus passes the supervisor filter.
					else if (supervisorFilter.contains(supervisor)) {
						alumnus.setSupervisor(supervisor);
						filteredAlumni.add(alumnus);
						System.out.printf("OK! (%s)%n", supervisor);
					}

					// Otherwise...
					else {
						System.out.println("filtered out by supervisor.");
					}
				}
			}
		}
	}

	private static int parseAlumni() throws Exception {
		int count = 0;

		// Processes all pages, following "next" links.
		String url = START_URL;
		while (url != null) {
			// Opens the page and extracts the HTML DOM structure into Jsoup.
			System.out.printf("Parsing %s...%n", url);
			Document doc = Jsoup.connect(url).timeout(TIMEOUT_TIME).get();
			url = null;

			// Looks for the first table in the document, where the names of the students are supposed to be.
			Element table = doc.select(SELECTOR_TABLE).first();

			// Extracts the rows from the table. Goes through all of them.
			Elements rows = table.select(SELECTOR_ROW);
			for (Element row : rows) {
				// Extracts the columns from the row.
				Elements columns = row.select(SELECTOR_COLUMN);

				// Read the columns with useful information.
				if (!columns.isEmpty()) {
					count++;
					Element nameCell = columns.get(COLUMN_NAME);
					Element defenseDateCell = columns.get(COLUMN_DEFENSE_DATE);
					Element levelCell = columns.get(COLUMN_LEVEL);

					// Also extracts the link to the detail page of the alumni.
					String link = baseUrl + nameCell.select(SELECTOR_DETAIL_LINK).attr(ATTRIBUTE_LINK);

					// Creates and stores the alumni in the set.
					alumni.add(new Alumnus(nameCell.text(), defenseDateCell.text(), levelCell.text(), link));
				}
			}

			// Checks if there's a next page.
			Elements nextLinks = doc.select(SELECTOR_NEXT_LINK);
			if (!nextLinks.isEmpty()) url = nextLinks.first().attr(ATTRIBUTE_LINK);
			if (url != null && url.startsWith("/")) url = baseUrl + url;
		}

		return count;
	}
}

class Alumnus implements Comparable<Alumnus> {
	private static final String ALUMNI_TABLE_LINE_TEMPLATE = "<tr%s>%n" + "\t<td><a href=\"%s\">%s</a></td>%n" + "\t<td>%s</td>%n" + "\t<td><a href=\"%s\">%s</a></td>%n" + "\t<td>%s</td>%n" + "\t<td>%s</td>%n" + "</tr>%n";

	private String name;
	private String defenseDate;
	private String level;
	private String url;
	private String supervisor;
	private String workTitle;

	Alumnus(String name, String defenseDate, String level, String url) {
		this.name = name;
		this.defenseDate = defenseDate;
		this.level = level;
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setSupervisor(String supervisor) {
		this.supervisor = supervisor;
	}

	public void setWorkTitle(String workTitle) {
		this.workTitle = workTitle;
	}

	public void printTableLine(PrintWriter out, boolean odd, Map<String, String> homepageMap) {
		String supervisorHomepage = homepageMap.get(supervisor);
		out.printf(ALUMNI_TABLE_LINE_TEMPLATE, (odd ? " class=\"odd\"" : ""), url, name, level, supervisorHomepage, supervisor, defenseDate, workTitle);
	}

	@Override
	public int compareTo(Alumnus o) {
		return name.compareTo(o.name);
	}

	@Override
	public String toString() {
		return name + " (" + level + ")";
	}
}

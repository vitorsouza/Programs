package text;

import java.util.SortedSet;
import java.util.TreeSet;

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
public class ParseUfesAlumniTableForStatistics {
//	private static final String OUTPUT_FILE_PATH = "parse-ufes-alumni-statistics.csv";
	
	private static final String START_URL = "http://www.informatica.ufes.br/pos-graduacao/PPGI/lista-de-discentes-egressos";

	private static final String SELECTOR_TABLE = "table";
	
	private static final String SELECTOR_ROW = "tr";
	
	private static final String SELECTOR_COLUMN = "td";
	
	private static final String SELECTOR_HEADER = "tr > th";
	
	private static final String SELECTOR_NEXT_LINK = "li.pager-next > a";
	
	private static final String SELECTOR_DETAIL_LINK = "a";
	
	private static final String SELECTOR_ALUMNI_INFO = "div.field-item > p";
	
	private static final String SELECTOR_ALUMNI_INFO_KEY = "span.token-ufes";
	
	private static final String ATTRIBUTE_LINK = "href";
	
	private static final int COLUMN_NAME = 0;
	
	private static final int COLUMN_DEFENSE_DATE = 1;
	
	private static final int COLUMN_LEVEL = 2;
	
	private static final int HEADER_DETAIL_DEFENSEDATE = 2;
	
	private static final String HEADER_DETAIL_DEFENSEDATE_TEXT = "Data de defesa";
	
	private static final String SPAN_COURSE_START_TEXT = "Início do curso:";
	
	private static final int COLUMN_DETAIL_TITLE = 0;
	
	private static final int COLUMN_DETAIL_TYPE = 1;
	
	private static final String COLUMN_DETAIL_TYPE_MASTERS = "Dissertação de mestrado acadêmico";
	
	private static final int COLUMN_WORK_DETAIL_SUPERVISOR_NAME = 0;
	
	private static final int COLUMN_WORK_DETAIL_SUPERVISOR_ROLE = 1;
	
	private static final String COLUMN_WORK_DETAIL_SUPERVISOR_ROLE_MAIN = "Orientador";

	private static String baseUrl;
	
	private static final SortedSet<Alumnus> alumni = new TreeSet<>();
	
	/** Main method. */
	public static void main(String[] args) throws Exception {
		// Extracts the base address of the URL.
		baseUrl = START_URL.substring(0, START_URL.indexOf('/', 7));

		// Parses all alumni.
		int count = parseAlumni();
		System.out.printf("%nParsed %d alumni from the main table. Now checking details...%n%n", count);
		
		// Parses details from the alumni.
		parseDetails();
		
		// Writes the output.
		writeOutput();
		
		System.out.println("\n\nDone!");
	}
	
	private static void writeOutput() throws Exception {
//		boolean odd = false;
//		
//		// Opens the output file for writing.
//		try (PrintWriter out = new PrintWriter(new File(OUTPUT_FILE_PATH))) {
//			for (Alumnus alumnus : alumni) {
//				// Writes a line to the output file.
//				// FIXME: implement CSV.
//			}
//		}
	}

	private static void parseDetails() throws Exception {
		// Processes all alumni.
		for (Alumnus alumnus : alumni) {
			// Opens the detail page of the alumnus.
			System.out.printf("Checking details for %s... ", alumnus);
			Document doc = Jsoup.connect(alumnus.getUrl()).timeout(30*1000).get();
			
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
					if (! columns.isEmpty()) {
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
					doc = Jsoup.connect(workUri).timeout(30*1000).get();
					
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
							if (! columns.isEmpty()) {
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
					else {
						alumnus.setSupervisor(supervisor);
						System.out.printf("OK! (%s)%n", supervisor);
					}
				}
			}
			
			// Looks for alumni info to search for the beginning of the course.
			Elements alumniInfo = doc.select(SELECTOR_ALUMNI_INFO);
			for (Element elem : alumniInfo) {
				Elements keys = elem.select(SELECTOR_ALUMNI_INFO_KEY);
				for (Element keyElem : keys) {
					if (SPAN_COURSE_START_TEXT.equals(keyElem.text()))
						System.out.println(elem.text());
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
			Document doc = Jsoup.connect(url).timeout(30*1000).get();
			url = null;
			
			// Looks for the first table in the document, where the names of the students are supposed to be.
			Element table = doc.select(SELECTOR_TABLE).first();
			
			// Extracts the rows from the table. Goes through all of them.
			Elements rows = table.select(SELECTOR_ROW);
			for (Element row : rows) {
				// Extracts the columns from the row.
				Elements columns = row.select(SELECTOR_COLUMN);
				
				// Read the columns with useful information.
				if (! columns.isEmpty()) {
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
			if (! nextLinks.isEmpty()) url = nextLinks.first().attr(ATTRIBUTE_LINK);
			if (url != null && url.startsWith("/")) url = baseUrl + url;
			
			// FIXME: remove.
			url = null;
		}
		
		return count;
	}
}


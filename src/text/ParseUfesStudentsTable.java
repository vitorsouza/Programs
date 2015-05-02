package text;

import java.util.HashSet;
import java.util.Set;

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
	private static final String START_URL = "http://www.informatica.ufes.br/pos-graduacao/ppgi/lista-de-discentes-de-doutorado";

	private static final Set<String> supervisorFilter = new HashSet<>();
	static {
		// Filters students by supervisor. Only includes students from these professors.
		/*supervisorFilter.add("Giancarlo Guizzardi");
		supervisorFilter.add("João Paulo Andrade Almeida");
		supervisorFilter.add("Monalessa Perini Barcellos");
		supervisorFilter.add("Renata Silva Souza Guizzardi");
		supervisorFilter.add("Ricardo de Almeida Falbo");
		supervisorFilter.add("Vítor Estêvão Silva Souza");*/
	}
	
	private static final String SELECTOR_TABLE = "table";
	
	private static final String SELECTOR_ROW = "tr";
	
	private static final String SELECTOR_COLUMN = "td";
	
	private static final String SELECTOR_NEXT_LINK = "li.pager-next > a";
	
	private static final String SELECTOR_DETAIL_LINK = "a";
	
	private static final String ATTRIBUTE_LINK = "href";
	
	private static final int[] COLUMNS_TO_READ = new int[] {0, 1, 3};
	
	private static final int COLUMN_SUPERVISOR = 3;
	
	private static final int COLUMN_DETAIL_LINK = 0;

	private static final String CSV_SEPARATOR = ";";
	
	/** Main method. */
	public static void main(String[] args) throws Exception {
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
				if (! columns.isEmpty()) {
					for (int idx : COLUMNS_TO_READ) builder.append(columns.get(idx).text()).append(CSV_SEPARATOR);
					
					// Also extracts the link to the detail page of the alumni.
					Element cell = columns.get(COLUMN_DETAIL_LINK);
					String link = cell.select(SELECTOR_DETAIL_LINK).attr(ATTRIBUTE_LINK);
					builder.append(link).append(CSV_SEPARATOR);
				
					// Checks if we should filter by supervisor.
					String supervisor = columns.get(COLUMN_SUPERVISOR).text();
					if (supervisorFilter.isEmpty() || supervisorFilter.contains(supervisor))
						// Outputs the information in CSV.
						System.out.println(builder);
				}
			}
			
			// Checks if there's a next page.
			Elements nextLinks = doc.select(SELECTOR_NEXT_LINK);
			if (! nextLinks.isEmpty()) url = nextLinks.first().attr(ATTRIBUTE_LINK);
			if (url != null && url.startsWith("/")) url = baseUrl + url;
		}
		
	}
}

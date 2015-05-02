package sysmap;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Reads a CSV file with the raw result of a systematic mapping search, detects duplicate entries (from multiple
 * sources) and merges them, producing as output a new CSV file without duplicates plus an HTML file which displays the
 * results in a table.
 * 
 * To use this script, you should provide a file called sysmap-raw.csv with the raw reults of your search in different
 * sources of publications. The script expects this file to have a title row as first column and to have the following
 * columns, in this order: source; year; title.
 * 
 * Moreover, if you want the HTML results to provide links to direct searches of the publication's title, you should
 * have the file sysmap-sourcesearch.properties filled in with the URL of the searches, using {0} as placeholder for the
 * publication title. The file provided in this repository already has the search strings for sources commonly used in
 * Computer Science. In order to use these you just have to use the exact name that appears in the properties file (you
 * can replace \u0020 with a blank space, e.g.: for 'Web\u0020of\u0020Science' use 'Web of Science').
 * 
 * I created this script to automate some steps of a systematic literature mapping / systematic literature review
 * processes. See http://en.wikipedia.org/wiki/Systematic_review.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class ProcessDuplicates {
	/** Source file with the raw result of the search. */
	private static final String RAW_FILENAME = "sysmap-raw.csv";

	/** Resulting file with duplicates grouped by title. */
	private static final String CSV_RESULT_FILENAME = "sysmap-noduplicates.csv";

	/** Resulting file in HTML to make it easier to check for the paper data. */
	private static final String HTML_RESULT_FILENAME = "sysmap-noduplicates.html";

	/** Map that indexes publications by title. */
	private static final Map<String, Publication> pubMap = new TreeMap<>();

	/** Keys to be removed from the publications map after the merging process. */
	private static final Set<String> mergedKeys = new TreeSet<>();

	/** The program. */
	public static void main(String[] args) throws Exception {
		int count = 0;

		// Reads the source (raw) file. Drops the title row.
		try (Scanner scanner = new Scanner(new File(RAW_FILENAME))) {
			scanner.nextLine();
			while (scanner.hasNextLine()) {
				count++;

				// Reads each line and breaks the information in CSV format.
				String line = scanner.nextLine().trim();
				String[] data = line.split(";");
				
				// Checks if the column separator has been used inside a cell.
				if (data.length != 3) {
					System.out.println("\nFATAL: separator used inside a cell. Even if quoted, this script doesn't support this. Please \"fix\" the source file:");
					System.out.printf("%d: %s%n%n", count, line);
					System.exit(1);
				}
				
				// Extracts the information from the columns.
				String source = data[0].trim();
				int year = Integer.parseInt(data[1].trim());
				String title = data[2].trim().replace("\"", "");

				// Uses the lowercase title as key.
				String key = title.toLowerCase();

				// If the publication is already indexed, add a source to it.
				if (pubMap.containsKey(key)) pubMap.get(key).getSources().add(source);

				// Otherwise, add the publication to the map.
				else pubMap.put(key, new Publication(title, year, source));
			}
		}

		// Reports statistics.
		System.out.printf("Read %d lines in file %s, resulting in %d indexed publications.%n%n", count, RAW_FILENAME, pubMap.size());

		// Merges similar results (publications whose titles are included in another).
		count = 0;
		String previous = "\u0000";
		for (String key : pubMap.keySet()) {
			if (previous.startsWith(key) || key.startsWith(previous)) {
				Publication p1 = pubMap.get(previous);
				Publication p2 = pubMap.get(key);
				if (p1.getYear() == p2.getYear()) {
					count++;
					System.out.printf("Merging similar results:%n\t%d (%s): %s%n\t%d (%s): %s%n", p1.getYear(), p1.getSourcesString(), p1.getTitle(), p2.getYear(), p2.getSourcesString(), p2.getTitle());
					p1.mergeWith(p2);
					mergedKeys.add(key);
				}
			}
			previous = key;
		}

		// Removes merged results from the publications map.
		for (String key : mergedKeys)
			pubMap.remove(key);

		// Reports statistics.
		System.out.printf("%nMerged %d publication pairs, resulting in %d indexed publications.%n%n", count, pubMap.size());

		// Produces the result in CSV and HTML formats to be used in the next phase of the systematic mapping.
		count = 0;
		try (PrintWriter csvOut = new PrintWriter(new File(CSV_RESULT_FILENAME)); PrintWriter htmlOut = new PrintWriter(new File(HTML_RESULT_FILENAME))) {
			// Prints the beginning of the HTML file.
			htmlOut.println("<html><body>\n<table border='1' cellpadding='3' cellspacing='0'>\n<tr><th>ID</th><th>Year</th><th>Sources</th><th>Title</th></tr>");

			// Process all entries in order.
			for (Map.Entry<String, Publication> entry : pubMap.entrySet()) {
				Publication pub = entry.getValue();

				// Outputs the line in CSV and HTML.
				csvOut.printf("%d;%d;\"%s\";\"%s\"%n", ++count, pub.getYear(), pub.getSourcesString(), pub.getTitle());
				htmlOut.printf("<tr><td>%d</td><td>%d</td><td>%s</td><td>%s</td></tr>", count, pub.getYear(), pub.getSourcesHtml(), pub.getTitle());
			}

			// Finishes the HTML file.
			htmlOut.println("</table>\n</body></html>");
		}

		// Reports statistics.
		System.out.printf("Wrote %d lines to output files %s and %s. Done!%n%n", count, CSV_RESULT_FILENAME, HTML_RESULT_FILENAME);
	}
}

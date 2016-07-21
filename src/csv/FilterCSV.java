package csv;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * TODO: document this type.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class FilterCSV {
	private static final String[] URLS = { 
			"https://api.taiga.io/api/v1/userstories/csv?uuid=3ec0788a8bce451fb028e32bd0e752cd", // RPD-BCS
			"https://api.taiga.io/api/v1/userstories/csv?uuid=e6c4662fb1414ad98808109f264a9cd4" // EDP-Sigal
			};
	
	private static final String[] OUTPUTS = { "/Users/vitor/Desktop/userstories-rpdbcs.csv", "/Users/vitor/Desktop/userstories-sigal.csv" };
	
	private static final String CSV_SEPARATOR = ";";
	
	private static final int[] indexes = {0, 1, 3, 9, 10, 13, 25, 26, 29};
	
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < URLS.length; i++) {
			System.out.printf("Downloading %s...%n", URLS[i]);
			URL url = new URL(URLS[i]);
			
			try (Reader reader = new BufferedReader(new InputStreamReader(url.openStream())); PrintWriter out = new PrintWriter(OUTPUTS[i])) {
				CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT);
				
				for (CSVRecord record : parser) {
					for (int idx : indexes) out.printf("%s%s", record.get(idx), CSV_SEPARATOR);
					out.println();
				}
				System.out.printf("Generated %s.%n", OUTPUTS[i]);
			}
		}
		
		System.out.println("Done!");
	}
}

package sysmap;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.csv.CSVFormat;

/**
 * TODO: document this type.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class ParseExportedData {
	
	private static final String DATA_FOLDER = "/Users/paulossjunior/Google Drive/UFES/Doutorado/Disciplinas/MetodologiaPesquisa/";
	
	private static final String OUTPUT_FILE = DATA_FOLDER+"sysmap-raw.csv";
	
	private static final Map<String, ExportedDataParser> parsers = new TreeMap<>();
	static {
		parsers.put("ACM.csv", new CSVExportedDataParser("ACM", 18, 6, 10, -1));
		parsers.put("Engineering Village.csv", new CSVExportedDataParser("Engineering Village", 14, 0, -1, 32));
		parsers.put("IEEE.csv", new CSVExportedDataParser("IEEE", 5, 0, 16, 10));
		parsers.put("Scopus.csv", new CSVExportedDataParser("SCOPUS", 2, 1, 16, 15));
		parsers.put("Web of Science.txt", new CSVExportedDataParser("Web of Science", 32, 9, -1, 33, CSVFormat.TDF));
		parsers.put("ScienceDirect.bib", new BibTeXExportedDataParser("ScienceDirect"));
	}
	
	public static void main(String[] args) throws Exception {
		File outputFile = new File(OUTPUT_FILE);
		try (PrintWriter out = new PrintWriter(outputFile)) {
			out.println("Source;Year;Title;Keywords;Abstract");
			
			File folder = new File(DATA_FOLDER);

			for (Map.Entry<String, ExportedDataParser> entry : parsers.entrySet()) {
				String fileName = entry.getKey();
				ExportedDataParser parser = entry.getValue();
				
				File file = new File(folder, fileName);
				List<Publication> publications = parser.parseExportedData(file);
				
				for (Publication publication : publications) {
					out.printf("%s;%d;%s;%s;%s%n", parser.getSource(), publication.getYear(), publication.getTitle().replace(";", ",,"), publication.getKeywords().replace(";", ",,"), publication.getAbztract().replace(";", ",,"));
				}
			}
		}
		
		System.out.println("Done! Output file: " + outputFile.getName());
	}
}

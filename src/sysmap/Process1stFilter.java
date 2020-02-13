package sysmap;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Reads a CSV file with the result of the 1st filter of a systematic mapping search, selecting those who have been
 * marked as OK and writing them into a new CSV file for the next step of the process.
 * 
 * To use this script, you should provide a file called sysmap-1stfilter.csv with the results of the 1st filter of the
 * mapping. The script expects this file to have a title row as first column and to have the following columns, in this
 * order: ID; year; source; title; analysis. Moreover, the analysis column should start with OK for publications that
 * will pass the filter and anything else for publications that will be cut out.
 * 
 * I created this script to automate some steps of a systematic literature mapping / systematic literature review
 * processes. See http://en.wikipedia.org/wiki/Systematic_review.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class Process1stFilter {
	
	private static final String DATA_FOLDER = "/Users/paulossjunior/Google Drive/UFES/Doutorado/Disciplinas/MetodologiaPesquisa/";
	
	/** Source file with the raw result of the search. */
	private static final String CSV_SOURCE_FILENAME = DATA_FOLDER+"sysmap-1stfilter.csv";

	/** Resulting file with duplicates grouped by title. */
	private static final String CSV_RESULT_FILENAME = DATA_FOLDER+"sysmap-1stfilter-result.csv";

	/** The program. */
	public static void main(String[] args) throws Exception {
		int countAll = 0, countFiltered = 0;

		// Reads the source (1st filter) file. Drops the title row.
		try (Scanner scanner = new Scanner(new File(CSV_SOURCE_FILENAME)); PrintWriter csvOut = new PrintWriter(new File(CSV_RESULT_FILENAME))) {
			String line = scanner.nextLine();
			csvOut.println(line);
			while (scanner.hasNextLine()) {
				countAll++;

				// Reads each line and breaks the information in CSV format.
				line = scanner.nextLine().trim();
				String[] data = line.split(";");
				
				// Checks if the column separator has been used inside a cell.
				if (data.length < 5) {
					System.out.println("\nFATAL: separator used inside a cell. Even if quoted, this script doesn't support this. Please \"fix\" the source file:");
					System.out.printf("%d: %s%n%n", countAll, line);
					System.exit(1);
				}
				
				// Extracts the information from the analysis column.
				String analysis = data[4].trim();

				// Outputs the line if it passed the filter.
				if (analysis.toUpperCase().startsWith("OK")) {
					countFiltered++;
					csvOut.println(line);
				}
			}
		}

		// Reports statistics.
		System.out.printf("Read %d lines in file %s, resulting in %d filtered publications in file %s.%n%n", countAll, CSV_SOURCE_FILENAME, countFiltered, CSV_RESULT_FILENAME);
	}
}

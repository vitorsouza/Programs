package text;

import java.io.File;
import java.util.Scanner;

/**
 * Extract student names from CSV files produced by UFES' academic information systems regarding students that are
 * enrolled in specific classes.
 * 
 * I wrote this program in order to extract the names of students enrolled in classes I'm teaching in order to produce
 * attendance lists and other useful material for class organization.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class UfesExtractStudentNamesFromCSV {
	private static final String FILE_PATH = "/Users/vitor/Downloads/2015.1.INF09307-02-lp2.csv";

	public static void main(String[] args) throws Exception {
		File file = new File(FILE_PATH);
		try (Scanner scanner = new Scanner(file)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] data = line.split(";");
				String name = data[3];
				StringBuilder builder = new StringBuilder(name.replace("\"", ""));
				for (int i = 1; i < builder.length(); i++) {
					char c = builder.charAt(i);
					if (builder.charAt(i - 1) != ' ') builder.setCharAt(i, Character.toLowerCase(c));
				}
				System.out.println(builder);
			}
		}
	}
}

package text;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Reads a chunk of HTML from a file (constant inName) relative to the elements present in an HTML listbox (<option /> tags, one
 * per line) and extracts the contents of each option (the text between quotes, e.g., <option
 * value="vitorsouza@gmail.com">vitorsouza@gmail.com</option>. Writes the values in a new file (constant outName), also one per
 * line.
 * 
 * I created this program to extract the e-mail addresses of the members of the Espírito Santo JUG (esjug.dev.java.net) mailing
 * list and also quickly count how many participants we had registered. It worked in the older version of java.net, haven't tried
 * with the new one.
 * 
 * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class EsjugListParticipantsCleanup {
	private static final String inName = "in.txt";
	private static final String outName = "out.txt";

	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner(new File(inName));
		PrintWriter out = new PrintWriter(new File(outName));

		System.out.println("Reading from: " + inName);
		System.out.println("Writing to: " + outName);
		System.out.println();

		int count = 0;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			count++;

			int idx = line.indexOf('"');
			if (idx != -1) {
				line = line.substring(idx + 1);

				idx = line.indexOf('"');
				if (idx != -1) {
					line = line.substring(0, idx);
					System.out.println(line);
				}
			}

			out.println(line);
		}

		scanner.close();
		out.close();

		System.out.println();
		System.out.println("Read " + count + " line(s) of text.");
	}
}

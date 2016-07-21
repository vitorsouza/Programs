package text;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * TODO: document this type.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class StripLineBreak {
	private static final String INPUT_FILE = "texto-input.txt";
	
	private static final String OUTPUT_FILE = "texto-output.txt";
	
	public static void main(String[] args) throws Exception {
		try (Scanner in = new Scanner(new File(INPUT_FILE)); PrintWriter out = new PrintWriter(new FileWriter(OUTPUT_FILE))) {
			while (in.hasNextLine()) {
				String line = in.nextLine();
				if (line.isEmpty()) out.println("\n");
				else out.print(line + " ");
			}
		}
		
		System.out.println("Done!");
	}
}

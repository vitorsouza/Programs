package text;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Reads the CSV file exported from Google Contacts (should be in Outlook format) and produces another CSV file with just some
 * fields of interest.
 * 
 * I wrote this program to have easier access to the backup information I have on my contacts.
 * 
 * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class GoogleContactsCSVCleaner {
	private static final String FILE_PATH = "/home/vitor/Downloads/contacts.csv";
	private static final String MIDDLE_FILE_PATH = "/home/vitor/Downloads/contacts-nobreaks.csv";
	private static final String OUT_FILE_PATH = "/home/vitor/Downloads/contacts-clean.csv";

	private static final int FIELD_FIRST_NAME = 0;
	private static final int FIELD_MIDDLE_NAME = 1;
	private static final int FIELD_LAST_NAME = 2;
	private static final int FIELD_BIRTHDAY = 8;
	private static final int FIELD_EMAIL = 14;
	private static final int FIELD_EMAIL2 = 15;
	private static final int FIELD_EMAIL3 = 16;
	private static final int FIELD_PHONE_PRIMARY = 17;
	private static final int FIELD_PHONE_HOME = 18;
	private static final int FIELD_PHONE_CELL = 20;

	private static final int[] INCLUDED_FIELDS = { 
		FIELD_FIRST_NAME, 
		FIELD_MIDDLE_NAME, 
		FIELD_LAST_NAME, 
		FIELD_BIRTHDAY, 
		FIELD_EMAIL, 
		FIELD_EMAIL2, 
		FIELD_EMAIL3, 
		FIELD_PHONE_PRIMARY, 
		FIELD_PHONE_HOME, 
		FIELD_PHONE_CELL 
	};

	public static void main(String[] args) throws Exception {
		// Opens the contacts file.
		File contactsFile = new File(FILE_PATH);
		Scanner scanner = new Scanner(contactsFile);
		
		// Creates an intermediate file to fix line breaks in the middle of the CSV. Deletes the file if it already exists.
		File middleFile = new File(MIDDLE_FILE_PATH);
		if (middleFile.exists()) middleFile.delete();
		PrintWriter out = new PrintWriter(middleFile);
		
		// Processes each line in the file.
		int count = 0, quoteCount = 0;
		while (scanner.hasNextLine()) {
			// Writes the line in the intermediate file.
			String line = scanner.nextLine();
			out.print(line);
			
			// Counts the number of quotes in the line.
			int idx = line.indexOf('"');
			while (idx != -1) {
				quoteCount++;
				line = line.substring(idx + 1);
				idx = line.indexOf('"');
			}
			
			// If the number of quotes is even, all quotes were properly closed, so print the line break. Else, print a separator.
			if ((quoteCount % 2) == 0) out.println();
			else out.print(" - ");
			count++;
		}
		
		// Logs the operation and closes resources.
		System.out.println("Read and wrote " + count + " lines in the intermediate output file");
		scanner.close();
		out.close();
		
		// Opens the intermediate file for reading.
		scanner = new Scanner(middleFile);
		
		// Creates a new file to output the result. Deletes the file if it already exists.
		File outFile = new File(OUT_FILE_PATH);
		if (outFile.exists()) outFile.delete();
		out = new PrintWriter(outFile);

		// Processes each line of the file.
		count = 0;
		while (scanner.hasNextLine()) {
			// Splits the information in the line in an array considering commas as delimiters.
			String line = scanner.nextLine();
			String[] fields = line.split(",");

			// Builds a new line with just the relevant fields (as listed in the constant).
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < INCLUDED_FIELDS.length; i++)
				if (fields.length > INCLUDED_FIELDS[i])
					builder.append(fields[INCLUDED_FIELDS[i]]).append(',');
			builder.deleteCharAt(builder.length() - 1);

			// Prints the new line in the output file. 
			out.println(builder.toString());
			count++;
		}
		
		// Closes the resources and concludes the program.
		System.out.println("Read and wrote " + count + " lines in the final output file");
		scanner.close();
		out.close();
	}
}

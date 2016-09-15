package bibtex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;

import bibtex.domain.BibtexEntry;

public class SortBibtex {
	private static final String INPUT_FILE = "input.bib";
	
	private static final String OUTPUT_FILE = "output.bib";
	
	public static void main(String[] args) throws FileNotFoundException {
		// Creates a sorted set of entries.
		SortedSet<BibtexEntry> entries = new TreeSet<>();
		
		// Uses a buffer for entries.
		StringBuilder builder = new StringBuilder();
		
		// Reads the input file line by line.
		try (Scanner in = new Scanner(new File(INPUT_FILE))) {
			while (in.hasNextLine()) {
				String line = in.nextLine();
				String trimmed = line.trim();
				
				// If it's the beginning of an entry, clears the buffer and starts a new entry.
				if (trimmed.startsWith("@")) {
					builder = new StringBuilder();
					builder.append(line);
				}
				
				// If it's the end of an entry, finish the buffer and add it to the set.
				else if (trimmed.startsWith("}")) {
					builder.append(line);
					BibtexEntry entry = new BibtexEntry(builder.toString());
					//if (entries.contains(entry)) System.out.println(entry);		// Uncomment if you want to spot duplicates.
					entries.add(entry);
				}
				
				// Otherwise, only append the line to the buffer.
				else builder.append(line);
				builder.append('\n');
			}
		}
		
		// Writes the entries, now sorted in the set, to the output.
		try (PrintWriter out = new PrintWriter(new File(OUTPUT_FILE))) {
			for (BibtexEntry entry : entries) {
				out.println(entry);
				out.println();
			}
		}
		
		System.out.println("Done! Sorted " + entries.size() + " BibTeX entries from " + INPUT_FILE);
	}
}

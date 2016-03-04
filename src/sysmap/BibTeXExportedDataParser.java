package sysmap;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXParser;
import org.jbibtex.Key;

/**
 * TODO: document this type.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class BibTeXExportedDataParser implements ExportedDataParser {
	private String source;

	/** Constructor. */
	public BibTeXExportedDataParser(String source) {
		this.source = source;
	}

	private String makePlain(String latexString) throws Exception {
		return latexString.replace("\\{", "").replace("\\}", "").replace(";", ",,");
	}

	/** @see sysmap.ExportedDataParser#parseExportedData(java.io.File) */
	@Override
	public List<Publication> parseExportedData(File file) throws Exception {
		List<Publication> publications = new ArrayList<>();

		StringBuilder builder = new StringBuilder();
		try (Scanner scanner = new Scanner(file)) {
			StringBuilder keywordsBuilder = null;

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();

				if (line != null && line.length() > 0) {
					if (line.startsWith("keywords")) {
						if (keywordsBuilder == null) keywordsBuilder = new StringBuilder("keywords = \"");
						line = line.substring(line.indexOf('=') + 1).replace('"', ' ').trim();
						keywordsBuilder.append(line).append(' ');
						continue;
					}

					else if (keywordsBuilder != null) {
						keywordsBuilder.append("\",");
						builder.append(keywordsBuilder.toString().replace(" ,", ",")).append('\n');
						keywordsBuilder = null;
					}

					builder.append(line).append('\n');
				}
			}
		}

		try (Reader reader = new StringReader(builder.toString())) {
			BibTeXParser parser = new BibTeXParser();
			BibTeXDatabase database = parser.parse(reader);

			for (Map.Entry<Key, BibTeXEntry> mapEntry : database.getEntries().entrySet()) {
				BibTeXEntry entry = mapEntry.getValue();

				String title = "";
				if (entry.getField(BibTeXEntry.KEY_TITLE) != null) title = makePlain(entry.getField(BibTeXEntry.KEY_TITLE).toUserString());

				int year = 0;
				if (entry.getField(BibTeXEntry.KEY_YEAR) == null) System.out.printf("%s: entry \"%s\" (%s) has publication without year! Using 0 as year.%n", source, mapEntry.getKey(), title);
				else year = Integer.parseInt(entry.getField(BibTeXEntry.KEY_YEAR).toUserString());

				String keywords = "";
				if (entry.getField(new Key("keywords")) != null) keywords = makePlain(entry.getField(new Key("keywords")).toUserString());

				String abztract = "";
				if (entry.getField(new Key("abstract")) != null) abztract = makePlain(entry.getField(new Key("abstract")).toUserString());

				Publication publication = new Publication(title, year, keywords, abztract, source);
				publications.add(publication);
			}
		}

		return publications;
	}

	/** @see sysmap.ExportedDataParser#getSource() */
	@Override
	public String getSource() {
		return source;
	}

}

package sysmap;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * TODO: document this type.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class CSVExportedDataParser implements ExportedDataParser {
	private CSVFormat format;
	
	private String source;
	
	private int yearIdx;
	
	private int titleIdx;
	
	private int keywordsIdx;
	
	private int abstractIdx;
	
	/** Constructor. */
	public CSVExportedDataParser(String source, int yearIdx, int titleIdx, int keywordsIdx, int abstractIdx) {
		this(source, yearIdx, titleIdx, keywordsIdx, abstractIdx, CSVFormat.DEFAULT);
	}
	
	public CSVExportedDataParser(String source, int yearIdx, int titleIdx, int keywordsIdx, int abstractIdx, CSVFormat format) {
		this.source = source;
		this.yearIdx = yearIdx;
		this.titleIdx = titleIdx;
		this.keywordsIdx = keywordsIdx;
		this.abstractIdx = abstractIdx;
		this.format = format;
	}
	
	private int parseYear(String yearData, long recordNumber) {
		if (yearData.matches("\\d{4}")) return Integer.parseInt(yearData);
				
		String[] data = yearData.split(" ");
		for (int i = 0; i < data.length; i++) if (data[i].matches("\\d{4}")) return Integer.parseInt(data[i]);
		
		System.out.printf("%s: line %d has publication with unrecognizable year: %s! Using 0 as year.%n", source, recordNumber, yearData);
		return 0;
	}
	
	@Override
	public List<Publication> parseExportedData(File file) throws Exception {
		List<Publication> publications = new ArrayList<>();
		
		try (Reader reader = new FileReader(file)) {
			CSVParser parser = new CSVParser(reader, format);
			
			for (CSVRecord record : parser) {
				if (record.getRecordNumber() > 1) {
					int size = record.size();
					if ((size <= titleIdx) || (record.get(titleIdx) == null) || (record.get(titleIdx).trim().length() == 0)) System.out.printf("%s: line %d has publication without title!%n", source, record.getRecordNumber());
					else {
						int year = 0;
						if ((size <= yearIdx) || (record.get(yearIdx) == null) || (record.get(yearIdx).trim().length() == 0)) System.out.printf("%s: line %d (%s) has publication without year! Using 0 as year.%n", source, record.getRecordNumber(), record.get(titleIdx));
						else year = parseYear(record.get(yearIdx), record.getRecordNumber());
						
						String keywords = (keywordsIdx > -1 && size > keywordsIdx) ? record.get(keywordsIdx) : "";
						String abztract = (abstractIdx > -1 && size > abstractIdx) ? record.get(abstractIdx) : "";
						Publication publication = new Publication(record.get(titleIdx), year, keywords, abztract, source);
						publications.add(publication);
					}
				}				
			}
		}
		
		return publications;
	}
	
	@Override
	public String getSource() {
		return source;
	}
}

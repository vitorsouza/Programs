package sysmap;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * TODO: document this type.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public interface ExportedDataParser {
	List<Publication> parseExportedData(File file) throws Exception;
	
	String getSource();
}

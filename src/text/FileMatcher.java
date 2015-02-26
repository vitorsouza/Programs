package text;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Reads two files containing the results of the "dir /a /s" Windows command and tries to identify files that are
 * probably the same. Compares files first by name, then by size, then by timestamp.
 * 
 * I created this program because I had downloaded pictures from the digital camera into my computer and my
 * sister-in-law downloaded photos also from the same digital camera. Since this happened during a trip that lasted many
 * days we didn't know if we had the same set of photos or if one had photos the other didn't. Since the names depend on
 * the software used to download them, I used this program to try and identify matches.
 * 
 * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class FileMatcher {
	private static final String FILE_MATCH = "\\d\\d/\\d\\d/\\d\\d\\d\\d\\s*\\d\\d:\\d\\d\\s*\\d*\\.\\d*\\s*.*";
	private static final String SRC_FILE = "myphotos.txt";
	private static final String DST_FILE = "theirphotos.txt";

	// private static final String BATCH_FILE = "copia.bat";
	// private static final String DST_DIR = "D:\\Selecao";

	public static void main(String[] args) throws Exception {
		File srcFile = new File(SRC_FILE);
		Map<String, FileInfo> srcMap = buildFileInfoMap(srcFile);
		System.out.println("Read " + srcMap.size() + " file names from " + srcFile.getName());

		File dstFile = new File(DST_FILE);
		Map<String, FileInfo> dstMap = buildFileInfoMap(dstFile);
		System.out.println("Read " + dstMap.size() + " file names from " + dstFile.getName());

		System.out.println();
		System.out.println("Looking for the files from the 1st set in the 2nd set...");
		System.out.println();

		int found = 0, notfound = 0;
		List<FileInfo> notFounds = new ArrayList<FileInfo>();
		for (Map.Entry<String, FileInfo> entry : srcMap.entrySet()) {
			String key = entry.getKey();
			FileInfo srcFileInfo = entry.getValue();
			FileInfo dstFileInfo = dstMap.get(key);

			if (dstFileInfo == null) {
				System.out.println(srcFileInfo.name + ": match not found!");
				notfound++;
				notFounds.add(srcFileInfo);
			}
			else {
				System.out.println(srcFileInfo.name + " = " + dstFileInfo.name);
				found++;
			}
		}

		System.out.println();
		System.out.println("1st scan done! " + found + " matches found and " + notfound + " not found.");
		System.out.println("Now scanning the files with no matches looking only for files with the same size...");
		System.out.println();

		Map<String, FileInfo> sizeOnlyFileInfoMap = buildSizeOnlyFileInfoMap(dstMap);
		found = notfound = 0;
		List<FileInfo> stillNotFounds = new ArrayList<FileInfo>();
		for (FileInfo srcFileInfo : notFounds) {
			FileInfo dstFileInfo = sizeOnlyFileInfoMap.get(srcFileInfo.size);

			if (dstFileInfo == null) {
				System.out.println(srcFileInfo.name + ": match not found!");
				notfound++;
				stillNotFounds.add(srcFileInfo);
			}
			else {
				System.out.println(srcFileInfo.name + " = " + dstFileInfo.name);
				found++;
			}
		}

		System.out.println();
		System.out.println("2nd scan done! " + found + " matches found and " + notfound + " not found.");
		System.out.println("Now scanning the files with no matches looking only for files with the same date/time...");
		System.out.println();

		Map<String, FileInfo> dateTimeOnlyFileInfoMap = buildDateTimeOnlyFileInfoMap(dstMap);
		found = notfound = 0;
		for (FileInfo srcFileInfo : stillNotFounds) {
			FileInfo dstFileInfo = dateTimeOnlyFileInfoMap.get(srcFileInfo.date + " " + srcFileInfo.time);

			if (dstFileInfo == null) {
				System.out.println(srcFileInfo.name + ": match not found!");
				notfound++;
			}
			else {
				System.out.println(srcFileInfo.name + " = " + dstFileInfo.name);
				found++;
			}
		}

		System.out.println();
		System.out.println("3rd scan done! " + found + " matches found and " + notfound + " not found.");
	}

	private static Map<String, FileInfo> buildFileInfoMap(File file) throws Exception {
		Scanner scanner = new Scanner(file);
		Map<String, FileInfo> map = new HashMap<String, FileInfo>();

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();

			if (line.matches(FILE_MATCH)) {
				String date = line;
				String time = line.substring(line.indexOf(' ')).trim();
				String size = time.substring(time.indexOf(' ')).trim();
				String name = size.substring(size.indexOf(' ')).trim();

				date = date.substring(0, date.indexOf(' ')).trim();
				time = time.substring(0, time.indexOf(' ')).trim();
				size = size.substring(0, size.indexOf(' ')).trim();

				String key = "[" + date + "|" + time + "|" + size + "]";
				map.put(key, new FileInfo(name, date, time, size, null));
			}
		}

		return map;
	}

	private static Map<String, FileInfo> buildSizeOnlyFileInfoMap(Map<String, FileInfo> fileInfoMap) {
		Map<String, FileInfo> sizeOnlyFileInfoMap = new HashMap<String, FileInfo>();
		for (FileInfo fileInfo : fileInfoMap.values()) {
			String key = fileInfo.size;
			if (sizeOnlyFileInfoMap.containsKey(key)) System.out.println("Ooops! Two files with exact same size (" + key + "): " + fileInfo.name + " and " + sizeOnlyFileInfoMap.get(key).name);
			else sizeOnlyFileInfoMap.put(key, fileInfo);
		}

		return sizeOnlyFileInfoMap;
	}

	private static Map<String, FileInfo> buildDateTimeOnlyFileInfoMap(Map<String, FileInfo> fileInfoMap) {
		Map<String, FileInfo> dateTimeOnlyFileInfoMap = new HashMap<String, FileInfo>();
		for (FileInfo fileInfo : fileInfoMap.values()) {
			String key = fileInfo.date + " " + fileInfo.time;
			if (dateTimeOnlyFileInfoMap.containsKey(key)) System.out.println("Ooops! Two files with exact same date/time (" + key + "): " + fileInfo.name + " and " + dateTimeOnlyFileInfoMap.get(key).name);
			else dateTimeOnlyFileInfoMap.put(key, fileInfo);
		}

		return dateTimeOnlyFileInfoMap;
	}
}

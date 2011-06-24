package text;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class NewFileMatcher {
	private static final String FILE_MATCH = "\\d\\d/\\d\\d/\\d\\d\\d\\d\\s*\\d\\d:\\d\\d\\s*\\d*\\.\\d*\\s*.*";

	private static final String SELECTION_FILE = "fotos_vanete.txt";
	private static final String ALLPHOTOS_FILE = "fotos_renata.txt";
	
	private static final String BATCH_FILE = "copia.sh";
	private static final String DST_DIR = "/media/Iomega/FTSelecao/";

	
	public static void main(String[] args) throws Exception {
		File selectionFile = new File(SELECTION_FILE);
		Map<String, FileInfo> selectionMap = buildFileInfoMap(selectionFile);
		System.out.println("Read " + selectionMap.size() + " file names from " + selectionFile.getName());
		
		File allPhotosFile = new File(ALLPHOTOS_FILE);
		Map<String, FileInfo> allPhotosMap = buildFileInfoMap(allPhotosFile);
		System.out.println("Read " + allPhotosMap.size() + " file names from " + allPhotosFile.getName());
		
		List<FileInfo> matches01 = new ArrayList<FileInfo>();
		List<FileInfo> matches02 = new ArrayList<FileInfo>();
		List<FileInfo> matches03 = new ArrayList<FileInfo>();
		List<FileInfo> matches04 = new ArrayList<FileInfo>();
		List<FileInfo> removes = new ArrayList<FileInfo>();

		System.out.println();
		System.out.println("Finding matches using the names of the files");
		
		for (FileInfo selectedFile : selectionMap.values()) {
			FileInfo matchFile = allPhotosMap.get(selectedFile.name);
			if (matchFile != null) {
				matches01.add(matchFile);
				removes.add(selectedFile);
			}
		}
		System.out.println(matches01.size() + " files matched by name");
		removeMatches(selectionMap, removes);

		
		Map<String, FileInfo>[] alternativeMaps = buildAlternativeMaps(allPhotosMap);
		
		
		System.out.println();
		System.out.println("Finding matches using date, time and size");
		
		for (FileInfo selectedFile : selectionMap.values()) {
			String key = selectedFile.date + "|" + selectedFile.time + "|" + selectedFile.size;
			FileInfo matchFile = alternativeMaps[0].get(key);
			if (matchFile != null) {
				matches02.add(matchFile);
				removes.add(selectedFile);
			}
		}
		System.out.println(matches02.size() + " files matched by date, time and size");
		removeMatches(selectionMap, removes);
		
		
		
		System.out.println();
		System.out.println("Finding matches using date and time");
		
		for (FileInfo selectedFile : selectionMap.values()) {
			String key = selectedFile.date + "|" + selectedFile.time;
			FileInfo matchFile = alternativeMaps[1].get(key);
			if (matchFile != null) {
				matches03.add(matchFile);
				removes.add(selectedFile);
			}
		}
		System.out.println(matches03.size() + " files matched by date and time");
		removeMatches(selectionMap, removes);

		
		
		System.out.println();
		System.out.println("Finding matches using size");

		for (FileInfo selectedFile : selectionMap.values()) {
			String key = selectedFile.size;
			FileInfo matchFile = alternativeMaps[2].get(key);
			if (matchFile != null) {
				matches04.add(matchFile);
				removes.add(selectedFile);
			}
		}
		System.out.println(matches02.size() + " files matched by size");
		removeMatches(selectionMap, removes);

		System.out.println();
		System.out.println("Arquivos não encontrados:\n");
		for (FileInfo fileInfo : selectionMap.values()) {
			System.out.println(fileInfo.folder + "\\" + fileInfo.name + " (" + fileInfo.date + " " +  fileInfo.time + " / " + fileInfo.size + ")");
		}
		System.exit(0);
		
		PrintWriter out = new PrintWriter(new File(BATCH_FILE));
		createFolders(out, matches01, "01");
		for (FileInfo fileInfo : matches01) out.println("cp \"" + linux(fileInfo.folder + "\\" + fileInfo.name) + "\" \"" + DST_DIR + "01/" + linux(fileInfo.folder.substring(33)) + "/\"");
		createFolders(out, matches02, "02");
		for (FileInfo fileInfo : matches02) out.println("cp \"" + linux(fileInfo.folder + "\\" + fileInfo.name) + "\" \"" + DST_DIR + "02/" + linux(fileInfo.folder.substring(33)) + "/\"");
		createFolders(out, matches03, "03");
		for (FileInfo fileInfo : matches03) out.println("cp \"" + linux(fileInfo.folder + "\\" + fileInfo.name) + "\" \"" + DST_DIR + "03/" + linux(fileInfo.folder.substring(33)) + "/\"");
		createFolders(out, matches04, "04");
		for (FileInfo fileInfo : matches04) out.println("cp \"" + linux(fileInfo.folder + "\\" + fileInfo.name) + "\" \"" + DST_DIR + "04/" + linux(fileInfo.folder.substring(33)) + "/\"");
		out.close();
	}
	
	private static void createFolders(PrintWriter out, List<FileInfo> matches, String number) throws Exception {
		Set<String> folders = new HashSet<String>();
		for (FileInfo fileInfo : matches) folders.add(fileInfo.folder.substring(33));
		for (String folder : folders) out.println("mkdir -p \"" + DST_DIR + number + "/" + linux(folder) + "\"");
	}
	
	private static String linux(String path) {
		path = path.replace("D:\\Rê\\Pictures\\Fotos\\", "/media/Iomega/");
		path = path.replace('\\', '/');
		return path;
	}
	
	private static void removeMatches(Map<String, FileInfo> fileMap, List<FileInfo> removes) throws Exception {
		System.out.print("Removing " + removes.size() + " new matches... ");
		for (FileInfo fileInfo : removes) {
			fileMap.remove(fileInfo.name);
		}
		System.out.println(fileMap.size() + " left to match.");
		removes.clear();
	}

	private static Map<String, FileInfo> buildFileInfoMap(File file) throws Exception {
		Scanner scanner = new Scanner(file);
		Map<String, FileInfo> map = new HashMap<String, FileInfo>();
		String folder = "";
		
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
				
				map.put(name, new FileInfo(name, date, time, size, folder));
			}
			
			else if (line.startsWith(" Directory of ")) {
				folder = line.substring(14).trim();
			}
		}
		
		return map;
	}
	
	private static Map<String, FileInfo>[] buildAlternativeMaps(Map<String, FileInfo> fileInfoMap) throws Exception {
		// Maps to build - indexed by:
		// 1 - date|time|size
		// 2 - date|time
		// 3 - size
		@SuppressWarnings("unchecked")
		Map<String, FileInfo>[] maps = new Map[] {new HashMap<String, FileInfo>(), new HashMap<String, FileInfo>(), new HashMap<String, FileInfo>()};
		
		for (FileInfo fileInfo : fileInfoMap.values()) {
			String[] keys = new String[] {
					fileInfo.date + "|" + fileInfo.time + "|" + fileInfo.size,
					fileInfo.date + "|" + fileInfo.time,
					fileInfo.size
			};
			for (int i = 0; i < keys.length; i++) maps[i].put(keys[i], fileInfo);
		}
		
		return maps;
	}
}


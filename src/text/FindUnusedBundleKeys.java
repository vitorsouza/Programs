package text;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 * Analyzes .properties from a Java Web application containing externalized strings and tries to find at least one web
 * page in which each of the keys are used. The goal is to identify keys that are not being used and can be discarded.
 * 
 * I wrote this script to use in the development of http://www.github.com/feees/Sigme because changes in some of its
 * features were making some externalized strings obsolete and I was loosing track of which ones.
 *
 * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class FindUnusedBundleKeys {
	private static final String BUNDLE_PATH = "/home/vitor/Workspaces/eclipse-indigo/Sigme-JavaEE6/SigmeWeb/src/br/com/engenhodesoftware/sigme/core/view/messages_pt_BR.properties";

	private static final String[] SOURCE_FOLDERS = new String[] { "/home/vitor/Workspaces/eclipse-indigo/Sigme-JavaEE6/SigmeWeb/WebContent/core/installSystem", "/home/vitor/Workspaces/eclipse-indigo/Sigme-JavaEE6/SigmeWeb/WebContent/core/manageInstitutions", "/home/vitor/Workspaces/eclipse-indigo/Sigme-JavaEE6/SigmeWeb/WebContent/core/manageSpiritists", "/home/vitor/Workspaces/eclipse-indigo/Sigme-JavaEE6/SigmeWeb/src/br/com/engenhodesoftware/sigme/core/controller" };

	private static Map<String, List<String>> sourceMap;

	public static void main(String[] args) throws Exception {
		Properties bundle = new Properties();
		bundle.load(new FileReader(new File(BUNDLE_PATH)));

		initSourceMap();

		Set<String> unusedKeys = new TreeSet<String>();
		for (Object obj : bundle.keySet()) {
			String key = "" + obj;
			System.out.println("Looking for key: " + key);

			boolean found = findKeyInSources(key);
			if (!found) unusedKeys.add(key);
		}

		System.out.println("\n\nThe following keys were not found in any source file:");
		for (String key : unusedKeys)
			System.out.println("\t- " + key);
		System.out.println("Unused keys found: " + unusedKeys.size());
	}

	private static void initSourceMap() throws Exception {
		sourceMap = new HashMap<String, List<String>>();

		for (String path : SOURCE_FOLDERS) {
			File folder = new File(path);
			File[] sources = folder.listFiles();

			for (File src : sources)
				if (src.isFile()) {
					List<String> lines = new ArrayList<String>();
					Scanner scanner = new Scanner(src);
					while (scanner.hasNextLine())
						lines.add(scanner.nextLine());

					sourceMap.put(src.getName(), lines);
				}
		}
	}

	private static boolean findKeyInSources(String key) throws Exception {
		for (Map.Entry<String, List<String>> entry : sourceMap.entrySet()) {
			String srcName = entry.getKey();
			List<String> lines = entry.getValue();
			System.out.println("\t- Searching: " + srcName);

			for (String line : lines) {
				if (line.contains(key)) {
					System.out.println("\t- Found " + key + " in " + srcName);
					return true;
				}
			}
		}

		return false;
	}
}

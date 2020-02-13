package lattes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import resource.ResourceUtil;

/**
 * TODO: document this type.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class CheckNumeroSequenciaLattes {
	private static final String CONFIG_FILE_NAME = "lattes-parser.properties";

	private static final String CV_OLD = "curriculo-velho.xml";
	
	private static final String CV_NEW = "curriculo-novo.xml";
	
	private static Properties CONFIG;
	
	public static void main(String[] args) throws Exception {
		// Loads the configurations.
		CONFIG = new Properties();
		String parserConfigFileName = CheckNumeroSequenciaLattes.class.getPackage().getName().replaceAll("\\.", "/") + "/" + CONFIG_FILE_NAME;
		File parserConfigFile = ResourceUtil.getResourceAsFile(parserConfigFileName);
		CONFIG.load(new FileInputStream(parserConfigFile));
		
		// Reads the old CV.
		StringBuilder xmlContentsOld = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(CV_OLD), CONFIG.getProperty("encoding")))) {
			String line = reader.readLine();
			while (line != null) {
				xmlContentsOld.append(line).append('\n');
				line = reader.readLine();
			}
		}

		// Reads the new CV.
		StringBuilder xmlContentsNew = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(CV_NEW), CONFIG.getProperty("encoding")))) {
			String line = reader.readLine();
			while (line != null) {
				xmlContentsNew.append(line).append('\n');
				line = reader.readLine();
			}
		}
		
		// Builds Jsoup documents for both CVs.
		Document docOld = Jsoup.parse(xmlContentsOld.toString(), "", Parser.xmlParser());
		Document docNew = Jsoup.parse(xmlContentsNew.toString(), "", Parser.xmlParser());

		// Compares the different kinds of entries.
		compareEntries(docOld, docNew, "Journals");
		compareEntries(docOld, docNew, "Books");
		compareEntries(docOld, docNew, "Chapters");
		compareEntries(docOld, docNew, "Events");
	}

	private static void compareEntries(Document docOld, Document docNew, String type) throws Exception {
		System.out.println("\n\nComparing " + type + ":");
		
		Map<Integer, String> mapOld = buildMap(docOld, type);
		Map<Integer, String> mapNew = buildMap(docNew, type);
		
		for (Map.Entry<Integer, String> entry : mapOld.entrySet()) {
			Integer numOld = entry.getKey();
			String titleOld = entry.getValue();
			System.out.println("\t- " + numOld + ": " + titleOld);
			String titleNew = "NOT FOUND!";
			if (mapNew.containsKey(numOld)) {
				titleNew = mapNew.get(numOld);
				if (titleOld.equals(titleNew)) titleNew = "SAME!";
				mapNew.remove(numOld);
			}
			System.out.println("\t\t>> " + titleNew);
		}
		
		for (Map.Entry<Integer, String> entry : mapNew.entrySet()) {
			System.out.println("\t- NEW ENTRY - " + entry.getKey() + ": " + entry.getValue());
		}
	}
	
	private static Map<Integer, String> buildMap(Document doc, String type) throws Exception {
		Map<Integer, String> map = new HashMap<>();
		
		// Navigates to the nodes that contains the production (supports multiple, although we expect a single one).
		Elements bibliographyLists = doc.select(CONFIG.getProperty("jsoupSelectorBibliographic"));

		// Gets the structural information from the configuration.
		String selector = CONFIG.getProperty("jsoupSelectorBibliographic" + type);
		String selectorGeneral = CONFIG.getProperty("jsoupSelectorBibliographic" + type + "General");
		String attrSeqNum = "SEQUENCIA-PRODUCAO";
		String attrTitle = CONFIG.getProperty("bibliographic" + type + "Title");
		
		// Goes through all the bibliography.
		for (Element bibList : bibliographyLists) {
			Elements elems = bibList.select(selector);
			for (Element elem : elems) {
				Element general = elem.select(selectorGeneral).first();
				
				// Extracts sequence number and title. Puts in the map.
				int number = Integer.parseInt(elem.attr(attrSeqNum));
				String title = general.attr(attrTitle);
				map.put(number, title);
			}
		}		
		return map;
	}
}

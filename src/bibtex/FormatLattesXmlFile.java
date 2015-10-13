package bibtex;

import java.io.File;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class FormatLattesXmlFile {
	/** Path to the XML file that contains the Lattes CV to be parsed. */
	private static final String LATTES_XML_FILE_PATH = "lattes-nemo/curriculo-vitor.xml";

	/** Path to the output file to be generated. */
	private static final String OUTPUT_FILE_PATH = "curriculo-formatado.xml";

	/** Main method. */
	public static void main(String[] args) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new File(LATTES_XML_FILE_PATH));

		// From: http://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java
		// How to pretty print an XML file from a DOM tree.
		final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
		final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
		final LSSerializer writer = impl.createLSSerializer();
		writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
		try (PrintWriter out = new PrintWriter(new File(OUTPUT_FILE_PATH))) {
			out.println(writer.writeToString(doc));
		}
		
		System.out.println("Done!");
	}

}

package xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * TODO: document this type.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class XmlDomUtils {
	/**
	 * TODO: document this method.
	 * @param parent
	 * @param name
	 * @return
	 */
	public static Node getFirstChildByName(Node parent, String name) {
		// Goes through all the child nodes.
		NodeList children = parent.getChildNodes();
		for (int idx = 0; idx < children.getLength(); idx++) {
			Node child = children.item(idx);
			
			// If a node with the name we're looking for is found, returns it.
			if (child.getNodeName().equalsIgnoreCase(name)) return child;
		}
		
		// If no node with the name we're looking for was found, returns null.
		return null;
	}
}

package imc.disxmldb.dom.formatter;

import java.io.DataOutput;
import java.io.IOException;

import imc.disxmldb.dom.XMLNode;

/**
 * IXMLNodeFormatter defines the interface needs to format the string
 * representation of an XML node tree.
 * 
 * @author xiafan
 * 
 */
public interface IXMLNodeFormatter {
	/**
	 * output the node in some format to the outputstream
	 * 
	 * @param node
	 * @param out
	 */
	public void format(XMLNode node, DataOutput out) throws IOException;

	public void format(XMLNode node) throws IOException;
}

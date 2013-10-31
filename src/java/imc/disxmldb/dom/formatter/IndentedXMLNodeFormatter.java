package imc.disxmldb.dom.formatter;

import imc.disxmldb.dom.ElementNode;
import imc.disxmldb.dom.XMLNode;

import java.io.DataOutput;
import java.io.IOException;

/**
 * This class formats the XML node tree in a pretty style. Each start element
 * tag is indented and placed on a single line. The end element tag is indented the same as
 * the start element tag.
 * 
 * @author xiafan
 * 
 */
public class IndentedXMLNodeFormatter implements IXMLNodeFormatter {
	private DataOutput output = null;

	public IndentedXMLNodeFormatter(DataOutput output) {
		this.output = output;
	}

	@Override
	public void format(XMLNode node, DataOutput out) throws IOException {
		formatIntern(node, out, 0);
	}

	@Override
	public void format(XMLNode node) throws IOException {
		if (output == null)
			throw new IOException("no ouputstream is provided");
		format(node, output);
	}

	public void formatIntern(XMLNode node, DataOutput out, int indent)
			throws IOException {
		if (node.getNodeType() == XMLNode.ELEMENTNODE) {
			ElementNode temp = (ElementNode) node;
			out.writeUTF(composeIndent(indent));
			out.writeUTF("<" + node.getTagName());
			for (XMLNode attr : temp.getAttributeNodes()) {
				// avoid the case that the attribute node is still not inserted
				// while the parent node does
				if (attr.getNodeType() != XMLNode.PROXYNODE) {
					out.writeUTF(" " + attr.getTagName() + "=\""
							+ attr.asXMLValue() + "\"");
				}
			}
			out.writeUTF(">\n");

			for (XMLNode eleNode : temp.getChilds())
				formatIntern(eleNode, out, indent + 1);

			String textStr = node.asXMLValue();
			if (textStr.trim().length() > 0) {
				out.writeUTF(composeIndent(indent + 1));
				out.writeUTF(node.asXMLValue());
				out.writeUTF("\n");
			}

			if (indent != 0) {
				out.writeUTF(composeIndent(indent));
				out.writeUTF("</" + node.getTagName() + ">\n");
			} else {
				out.writeUTF("</" + node.getTagName() + ">");
			}
		} else if (node.getNodeType() == XMLNode.ATTRIBUTENODE) {
			out.writeUTF(node.asXMLValue());
		}
	}

	public static String composeIndent(int indent) {
		StringBuffer ret = new StringBuffer();
		for (int i = 0; i < indent; i++) {
			ret.append("  ");
		}
		return ret.toString();
	}

}

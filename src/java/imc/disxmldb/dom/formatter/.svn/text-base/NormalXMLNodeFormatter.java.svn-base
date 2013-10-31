package imc.disxmldb.dom.formatter;

import imc.disxmldb.dom.ElementNode;
import imc.disxmldb.dom.XMLNode;

import java.io.DataOutput;
import java.io.IOException;

/**
 * NormalXMLNodeFormatter formats the XML node tree in a single line.
 * @author xiafan
 *
 */
public class NormalXMLNodeFormatter implements IXMLNodeFormatter {
	private DataOutput output = null;

	public NormalXMLNodeFormatter(DataOutput output) {
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
			out.writeUTF("<" + node.getTagName() + " ");
			for (XMLNode attr : temp.getAttributeNodes()) {
				//avoid the case that the attribute node is still not inserted while the parent node does
				if (attr.getNodeType() != XMLNode.PROXYNODE) {
					out.writeUTF(attr.getTagName() + "=\"" + attr.asXMLValue()
							+ "\" ");
				}
			}
			out.writeUTF(">");
			for (XMLNode eleNode : temp.getChilds())
				formatIntern(eleNode, out, indent + 1);
			String textStr = node.asXMLValue();
			if (textStr.length() > 0) {
				out.writeUTF(node.asXMLValue());
			}
			if (indent != 0) {
				out.writeUTF("</" + node.getTagName() + ">");
			} else {
				out.writeUTF("</" + node.getTagName() + ">");
			}
		} else if (node.getNodeType() == XMLNode.ATTRIBUTENODE) {
			out.writeUTF(node.asXMLValue());
		}
	}
}

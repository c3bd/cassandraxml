package imc.disxmldb.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of element node
 *
 */
public class ElementNodeImpl extends XMLNodeImpl implements ElementNode{
	private List<XMLNode> attrs = new LinkedList<XMLNode>();
	private List<XMLNode> childs = new ArrayList<XMLNode>();

	public ElementNodeImpl(String tagNameIn, int idIn, int depthIn, XMLNode parentIn) {
		super(tagNameIn, idIn, parentIn, depthIn, XMLNode.ELEMENTNODE);
	}

	public int getAttrNum() {
		return attrs.size();
	}

	public List<XMLNode> getAttributeNodes() {
		return attrs;
	}

	public void addAttribute(XMLNode attr) {
		attrs.add(attr);
	}

	public void deleteAttribute(XMLNode attr) {
		for (Iterator<XMLNode> iter = attrs.iterator(); iter.hasNext();) {
			XMLNode node = iter.next();
			if (compByID.compare(node, attr) == 0) {
				iter.remove();
			}
		}
	}

	public int getChildNum() {
		return childs.size();
	}

	public List<XMLNode> getChilds() {
		return childs;
	}

	public void addChild(XMLNode child) {
		childs.add(child);
	}

	public void deleteChild(XMLNode child) {
		int idx = Collections.binarySearch(childs, child, compByID);
		if (idx >= 0) {
			childs.remove(idx);
		}
	}
}

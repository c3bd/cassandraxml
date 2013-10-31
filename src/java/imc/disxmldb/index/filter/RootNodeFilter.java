package imc.disxmldb.index.filter;

import imc.disxmldb.index.Node;
import imc.disxmldb.index.NodeUnit;

public class RootNodeFilter implements IXMLFilter {
	IXMLFilter other = null;;
	public RootNodeFilter(IXMLFilter other) {
		this.other = other;
	}
	
	@Override
	public boolean filter(int xmlDocID, NodeUnit nodeUnit) {
		if (other != null && other.filter(xmlDocID, nodeUnit))
			return true;
		if (nodeUnit != null && nodeUnit.getLevel() != 1)
			return true;
		return false;
	}

	@Override
	public boolean filter(Node node) {
		if (other != null && other.filter(node))
			return true;
		if (node.getLevel() != 1)
			return true;
		return false;
	}
}

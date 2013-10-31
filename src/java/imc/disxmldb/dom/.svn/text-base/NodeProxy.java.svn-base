package imc.disxmldb.dom;

import imc.disxmldb.dom.typesystem.ValueType;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.cassandra.cache.ICache;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.utils.Pair;

/**
 * This is an proxy class that implements both the interface of
 * element node and attribute node. It can behave as an attribute node or
 * element node in according to the node it proxies for.
 *
 */
public class NodeProxy implements XMLNode, ElementNode, AttributeNode {
	public enum NodeProxyType {
		remote, local
	};

	private NodeProxyType proxyType = null;
	private ThreadLocal<XMLNode> node = new ThreadLocal<XMLNode>();

	private int XMLID = -1;
	private int nodeID = -1;

	public NodeProxy(int XMLID, int nodeID) {
		this.XMLID = XMLID;
		this.nodeID = nodeID;
	}

	public NodeProxy(int XMLID, int nodeID, NodeProxyType proxyType) {
		this.XMLID = XMLID;
		this.nodeID = nodeID;
		this.proxyType = proxyType;
	}

	public void fillNode(XMLNode node) {
		this.node.set(node);
	}

	public boolean isLoaded() {
		return node.get() != null;
	}

	public void unloadNodeSelf() {
		XMLNode sNode = this.node.get();
		if (sNode == null)
			return;
		this.node.set(null);
	}
	
	
	public void unloadNode() {
		XMLNode sNode = this.node.get();
		if (sNode == null)
			return;

		if (sNode.getNodeType() == XMLNodeImpl.ELEMENTNODE) {
			for (XMLNode node : ((ElementNode) sNode).getAttributeNodes()) {
				((NodeProxy) node).unloadNode();
			}

			for (XMLNode node : ((ElementNode) sNode).getChilds()) {
				((NodeProxy) node).unloadNode();
			}
		}

		/*
		 * TODO maybe we should update the cache to update the reference ?
		 */
		this.node.set(null);
	}

	@Override
	public void finalize() {
		if (isLoaded())
			unloadNode();
	}

	public int getXMLID() {
		return XMLID;
	}

	public int getNodeID() {
		return nodeID;
	}

	@Override
	public int getAttrNum() {
		return ((ElementNode) node.get()).getAttrNum();
	}

	@Override
	public List<XMLNode> getAttributeNodes() {
		return ((ElementNode) node.get()).getAttributeNodes();
	}

	@Override
	public void addAttribute(XMLNode attr) {
		((ElementNode) node.get()).addAttribute(attr);
	}

	@Override
	public void deleteAttribute(XMLNode attr) {
		((ElementNode) node.get()).deleteAttribute(attr);
	}

	@Override
	public int getChildNum() {
		return ((ElementNode) node.get()).getChildNum();
	}

	@Override
	public List<XMLNode> getChilds() {
		return ((ElementNode) node.get()).getChilds();
	}

	@Override
	public void addChild(XMLNode child) {
		((ElementNode) node.get()).addChild(child);
	}

	@Override
	public void deleteChild(XMLNode child) {
		((ElementNode) node.get()).deleteChild(child);
	}

	@Override
	public String getTagName() {
		return node.get().getTagName();
	}

	@Override
	public void setTagName(String tagName) {
		node.get().setTagName(tagName);
	}

	@Override
	public XMLNode getParent() {
		return node.get().getParent();
	}

	@Override
	public void setParent(XMLNode parent) {
		node.get().setParent(parent);
	}

	@Override
	public boolean isRoot() {
		return node.get().isRoot();
	}

	@Override
	public int getId() {
		return nodeID;
	}

	@Override
	public void setId(int id) {
		nodeID = id;
		node.get().setId(id);
	}

	@Override
	public double[] getRange() {
		return node.get().getRange();
	}

	@Override
	public void setRange(double[] range) {
		node.get().setRange(range);

	}

	@Override
	public void setRangeLower(double lower) {
		node.get().setRangeLower(lower);

	}

	@Override
	public void setRangeUpper(double upper) {
		node.get().setRangeUpper(upper);
	}

	@Override
	public ValueType getValueType() {
		return node.get().getValueType();
	}

	@Override
	public void setValueType(ValueType valueType) {
		node.get().setValueType(valueType);
	}

	@Override
	public AbstractType getValidator() {
		return node.get().getValidator();
	}

	@Override
	public String getValueStr() {
		return node.get().getValueStr();
	}

	@Override
	public String asXMLValue() {
		return node.get().asXMLValue();
	}

	@Override
	public ByteBuffer getValue() {
		return node.get().getValue();
	}

	@Override
	public ByteBuffer getValue(String strVal) {
		return node.get().getValue(strVal);
	}

	@Override
	public void setValue(String value) {
		node.get().setValue(value);
	}

	@Override
	public void setValue(ByteBuffer value) {
		node.get().setValue(value);
	}

	@Override
	public byte getNodeType() {
		if (node.get() == null)
			return XMLNode.PROXYNODE;
		return node.get().getNodeType();
	}

	@Override
	public int getLevel() {
		return node.get().getLevel();
	}

	@Override
	public void setLevel(int level) {
		node.get().setLevel(level);
	}

	@Override
	public XMLNodeForMutation sketch() {
		return node.get().sketch();
	}

}

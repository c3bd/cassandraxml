package imc.disxmldb.dom;

/**
 * the implementation of attribute node
 */
public class AttributeNodeImpl extends XMLNodeImpl implements AttributeNode{
	
	public AttributeNodeImpl(String tagNameIn, int idIn, int depthIn, XMLNode parentIn) {
		super(tagNameIn, idIn, parentIn, depthIn, XMLNode.ATTRIBUTENODE);
		// TODO Auto-generated constructor stub
	}
}

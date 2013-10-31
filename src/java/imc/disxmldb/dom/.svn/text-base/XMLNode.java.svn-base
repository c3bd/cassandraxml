package imc.disxmldb.dom;

import imc.disxmldb.dom.typesystem.ValueType;

import java.nio.ByteBuffer;
import java.util.Comparator;

import org.apache.cassandra.db.marshal.AbstractType;


/**
 * Define the common interface needed by an XML node
 *
 */
public interface XMLNode {
	public static final byte ELEMENTNODE = 1;
	public static final byte ATTRIBUTENODE = 2;
	public static final byte PROXYNODE = 3;
	public static final Comparator<XMLNode> compByID = new XMLNodeComparatorByID();

	public static class XMLNodeComparatorByID implements
			Comparator<XMLNode> {

		@Override
		public int compare(XMLNode o1, XMLNode o2) {
			return o1.getId() - o2.getId();
		}

	};

	public String getTagName();

	public void setTagName(String tagName);

	public XMLNode getParent();

	public void setParent(XMLNode parent);

	/**
	 * judge whether this node is a root node
	 * 
	 * @return
	 */
	public boolean isRoot();

	public int getId();

	public void setId(int id);

	public double[] getRange();

	public void setRange(double[] range);

	public void setRangeLower(double lower);

	public void setRangeUpper(double upper);

	public ValueType getValueType();

	public void setValueType(ValueType valueType);

	public AbstractType getValidator();

	/**
	 * the transformed representation of the value, which maybe used to compare
	 * 
	 * @return
	 */
	public String getValueStr();

	/**
	 * the origin XML string representation of the value
	 * 
	 * @return
	 */
	public String asXMLValue();

	public ByteBuffer getValue();

	public ByteBuffer getValue(String strVal);

	/**
	 * setValueType must be invoked before this function
	 * 
	 * @param value
	 */
	public void setValue(String value);

	public void setValue(ByteBuffer value);

	public byte getNodeType();

	public int getLevel();

	public void setLevel(int level);

	public XMLNodeForMutation sketch();
}

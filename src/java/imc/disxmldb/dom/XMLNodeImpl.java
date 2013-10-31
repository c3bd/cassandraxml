/**
 *@author:xiafan68
 *@version: 2011-9-27 0.1
 */
package imc.disxmldb.dom;

import imc.disxmldb.dom.typesystem.ValueType;

import java.nio.ByteBuffer;
import java.util.Comparator;

import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.DateType;
import org.apache.cassandra.db.marshal.DoubleType;
import org.apache.cassandra.db.marshal.FileSizeType;
import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.db.marshal.LongType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.utils.ByteBufferUtil;

/**
 * This class implements the common interface of XML node. There are
 * two kinds of XMl nodes: element node and attribute node.
 * 
 */
public abstract class XMLNodeImpl implements XMLNode {
	private static XMLNodeSerializer serializer = new XMLNodeSerializer();

	public static XMLNodeSerializer serializer() {
		return serializer;
	}

	String tagName = null;

	public int id;
	public double[] range = new double[2];
	public XMLNode parent = null;
	public ValueType valueType = ValueType.UNKNOW;
	public ByteBuffer value = ByteBufferUtil.EMPTY_BYTE_BUFFER;
	public int level = 0;
	protected final byte nodeType;

	public XMLNodeImpl(String tagNameIn, int idIn, XMLNode parentIn,
			int levelIn, byte nodeTypeIn) {
		this.tagName = tagNameIn;
		this.id = idIn;
		this.parent = parentIn;
		this.nodeType = nodeTypeIn;
		this.level = levelIn;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public XMLNode getParent() {
		return parent;
	}

	public void setParent(XMLNode parent) {
		this.parent = parent;
	}

	public boolean isRoot() {
		return parent == null;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double[] getRange() {
		return range;
	}

	public void setRange(double[] range) {
		this.range[0] = range[0];
		this.range[1] = range[1];
	}

	public void setRangeLower(double lower) {
		this.range[0] = lower;
	}

	public void setRangeUpper(double upper) {
		this.range[1] = upper;
	}

	public ValueType getValueType() {
		return valueType;
	}

	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}

	public AbstractType getValidator() {
		return valueType.getValidator(valueType);
	}

	public String getValueStr() {
		if (value == null)
			return "";
		return valueType.getValidator(valueType).compose(value).toString();
	}

	public String asXMLValue() {
		return (String) valueType.getValidator(valueType).getString(value);
	}

	public ByteBuffer getValue() {
		if (value == null)
			return ByteBufferUtil.EMPTY_BYTE_BUFFER;
		return value;
	}

	public ByteBuffer getValue(String strVal) {
		return valueType.getValidator(valueType).fromString(strVal);
	}

	/**
	 * setValueType must be invoked before this function
	 * 
	 * @param value
	 */
	public void setValue(String value) {
		this.value = valueType.getValidator(valueType).fromString(value);
	}

	public void setValue(ByteBuffer value) {
		this.value = value;
	}

	public byte getNodeType() {
		return nodeType;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	public XMLNodeForMutation sketch() {
		XMLNodeForMutation ret = new XMLNodeForMutation(nodeType);
		ret.tagName = tagName;
		ret.id = id;
		ret.level = level;
		ret.range = new double[]{range[0], range[1]};
		ret.value = value.duplicate();
		ret.valueType = valueType;	
		return ret;		
	}
}

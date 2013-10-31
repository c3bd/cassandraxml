package imc.disxmldb.dom.typesystem;

import java.util.Comparator;

import imc.disxmldb.dom.schema.SchemaType;

import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.DateType;
import org.apache.cassandra.db.marshal.DoubleType;
import org.apache.cassandra.db.marshal.FileSizeType;
import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.db.marshal.LongType;
import org.apache.cassandra.db.marshal.UTF8Type;

public enum ValueType {
	INT, LONG, DOUBLE, DATE, STRING, UNKNOW, FILESIZE, KILOSIZE, MEGASIZE, GIGASIZE, NODETAG, ATTRTAG,ELEMENTS, BOOLEAN;

	public static final ValueType[] VALUETYPES = ValueType.values();

	public static final AbstractType[] validators = new AbstractType[] {
			Int32Type.instance, LongType.instance, DoubleType.instance,
			DateType.instance, UTF8Type.instance, UTF8Type.instance,
			DoubleType.instance, FileSizeType.getInstance(ValueType.KILOSIZE),
			FileSizeType.getInstance(ValueType.MEGASIZE),
			FileSizeType.getInstance(ValueType.GIGASIZE), UTF8Type.instance,
			UTF8Type.instance};

	public static final Comparator<byte[]>[] comparators = new Comparator[] {
			new IntegerComparator(), new LongComparator(),
			new DoubleComparator(), new UTF8Comparator(), new UTF8Comparator(),
			new UTF8Comparator(), new DoubleComparator(),
			new DoubleComparator(), new DoubleComparator(),
			new DoubleComparator(),new UTF8Comparator(),new UTF8Comparator() };

	public static boolean isFileSizeType(ValueType valueType) {
		if (valueType == FILESIZE || valueType == KILOSIZE
				|| valueType == MEGASIZE || valueType == GIGASIZE) {
			return true;
		}
		return false;
	}

	/**
	 * used by xml schema
	 * 
	 * @param type
	 * @return
	 */
	public static ValueType getValueType(String type) {
		if (type.equals(SchemaType.BASIC_STRING))
			return ValueType.STRING;
		else if (type.equals(SchemaType.BASIC_FLOAT)) {
			return ValueType.DOUBLE;
		} else if (type.equals(SchemaType.BASIC_DOUBLE)) {
			return ValueType.DOUBLE;
		} else if (type.equals(SchemaType.BASIC_BOOLEAN)) {
			return ValueType.BOOLEAN;
		} else if (type.equals(SchemaType.BASIC_DATE)) {
			return ValueType.DATE;
		} else {
			return ValueType.UNKNOW;
		}
	}

	public static AbstractType getValidator(ValueType valueType_) {
		return validators[valueType_.ordinal()];
	}

	public static AbstractType getValidator(int ord_) {
		assert ord_ >= 0 && ord_ < validators.length;
		return validators[ord_];
	}

	public static ValueType getValueType(int ord) {
		return VALUETYPES[ord];
	}

	/**
	 * used by btree
	 * 
	 * @param valueType_
	 * @return
	 */
	public static Comparator<byte[]> getComparator(ValueType valueType_) {
		return comparators[valueType_.ordinal()];
	}
};

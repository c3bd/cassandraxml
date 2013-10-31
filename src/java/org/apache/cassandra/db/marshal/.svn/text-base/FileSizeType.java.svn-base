package org.apache.cassandra.db.marshal;


import imc.disxmldb.dom.typesystem.ValueType;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.cassandra.utils.ByteBufferUtil;

public class FileSizeType extends AbstractType<String> {
	public static int KILO_BYTES = 1024;
	public static int MEGA_BYTES = KILO_BYTES * 1024;
	public static int GIGA_BYTES = MEGA_BYTES * 1024;
	
	private static Map<ValueType, FileSizeType> instances = new HashMap<ValueType, FileSizeType>();
	
	private int scale = KILO_BYTES;
	private String suffix = "k";
	
	private FileSizeType(ValueType valueType) {
		if (valueType == ValueType.KILOSIZE || valueType == ValueType.FILESIZE) {
			//nothing todo
		} else if (valueType == ValueType.MEGASIZE) {
			scale = MEGA_BYTES;
			suffix = "MB";
		} else if (valueType == ValueType.GIGASIZE) {
			scale = GIGA_BYTES;
			suffix = "G";
		} 
	}
	
	public static FileSizeType getInstance(ValueType valueType) {
		if (instances.get(valueType) == null) {
			instances.put(valueType, new FileSizeType(valueType));
		}
		return instances.get(valueType);
	}
	@Override
	public int compare(ByteBuffer o1, ByteBuffer o2) {
		return DoubleType.instance.compare(o1, o2);
	}

	@Override
	public String compose(ByteBuffer bytes) {
		Double size = ByteBufferUtil.toDouble(bytes);
		return size.toString();
	}

	@Override
	public ByteBuffer decompose(String value) {
		value = value.trim();
		if (value.length() == 0)
			return ByteBufferUtil.EMPTY_BYTE_BUFFER;
		else {
			value = value.substring(0, value.length() - 1);
			double size = Double.parseDouble(value);
			size = size * scale;
			return ByteBufferUtil.bytes(size);
		}
	}

	@Override
	public String getString(ByteBuffer bytes) {
		double size = ByteBufferUtil.toDouble(bytes);
		size = size / scale;
		return (size + suffix);
	}

	@Override
	public ByteBuffer fromString(String text) {
		int i = text.length() - 1;
		
		for (; i >= 0; i--) {
			if (Character.isDigit(text.charAt(i)))
				break;
		}
		
		if (i >= 0) {
			text = text.substring(0, i + 1);
			double size = Double.parseDouble(text);
			size *= scale;
			return DoubleType.instance.decompose(size);
		} else {
			return ByteBufferUtil.EMPTY_BYTE_BUFFER;
		}
	}
	@Override
	public void validate(ByteBuffer bytes) throws MarshalException {
		//bytes only needs to be double
		DoubleType.instance.validate(bytes);
	}

}

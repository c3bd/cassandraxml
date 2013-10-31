package imc.disxmldb.dom.schema;

import imc.disxmldb.dom.typesystem.ValueType;

import java.util.regex.Pattern;


public class SchemaSimpleType extends SchemaType {
	public static SchemaSimpleType SCHEMA_INTEGER = new SchemaSimpleType(
			SchemaType.BASIC_INTEGER, SchemaType.BASIC_INTEGER, null);
	public static SchemaSimpleType SCHEMA_DOUBLE = new SchemaSimpleType(
			SchemaType.BASIC_DOUBLE, SchemaType.BASIC_DOUBLE, null);
	public static SchemaSimpleType SCHEMA_FLOAT = new SchemaSimpleType(
			SchemaType.BASIC_FLOAT, SchemaType.BASIC_FLOAT, null);
	public static SchemaSimpleType SCHEMA_BOOLEAN = new SchemaSimpleType(
			SchemaType.BASIC_BOOLEAN, SchemaType.BASIC_BOOLEAN, null);
	public static SchemaSimpleType SCHEMA_STRING = new SchemaSimpleType(
			SchemaType.BASIC_STRING, SchemaType.BASIC_STRING, null);

	private ValueType basicType = null;
	private Pattern pattern = null;

	public SchemaSimpleType(String name, String baseType, String pattern) {
		super(name);
		this.basicType = ValueType.getValueType(baseType);
		if (pattern != null)
			this.pattern = Pattern.compile(pattern);
	}
}

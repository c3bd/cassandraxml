package imc.disxmldb.dom.schema;

import imc.disxmldb.config.SysConfig;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class XMLSchema {
	public static String NAME = "name";
	public static String TYPE = "type";
	public static String ELEMENT = "element";
	public static String COMPLEXTYPE = "complexType";
	public static String SIMPLETYPE = "simpleType";
	public static String ATTRIBUTE = "attribute";
	public static String ALL = "all";
	public static String SEQUENCE = "sequence";
	
	private Integer anonyID = new Integer(0);
	private SchemaElement rootElem = null;
	private Map<String, SchemaType> typeMap = new HashMap<String, SchemaType>();

	public XMLSchema() {

	}

	public boolean parseFromString(String schema) {
		InputStream input = new ByteArrayInputStream(schema.getBytes());
		SAXReader reader = new SAXReader();
		reader.setEncoding(SysConfig.ENCODING);
		Document doc;
		try {
			doc = reader.read(input);
			Element schemaRoot = doc.getRootElement();

			for (Iterator<Element> iter = schemaRoot.elementIterator(); iter
					.hasNext();) {
				Element elem = iter.next();
				if (elem.getName().equals(ELEMENT)) {
					rootElem = parseElement(elem);
				} else {
					parseType(elem);
				}
			}

		} catch (DocumentException e) {
			return false;
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private SchemaElement parseElement(Element element) {
		String tagName = element.attributeValue(NAME);
		String typeName = element.attributeValue(TYPE);
		if (typeName == null) {
			// anonymous complex type
			typeName = parseComplexType((Element) element.elements().get(0));
		}
		return new SchemaElement(tagName, tagName, false);
	}

	private String parseType(Element type) throws Exception {
		if (type.getName().equals(COMPLEXTYPE)) {
			return parseComplexType(type);
		} else if (type.getName().equals(SIMPLETYPE)) {
			return parseSimpleType(type);
		} else {
			throw new Exception("unexpected elements");
		}
	}
	
	private String parseComplexType(Element type) {
		String typeName = type.attributeValue(NAME);
		if (typeName != null) {
			typeName = "anony" + (anonyID++);
		}
		ComplexType complexType = new ComplexType(typeName);
		
		for(Iterator<Element> iter = type.elementIterator(); iter.hasNext(); ) {
			Element child = iter.next();
			
			if (child.getName().equals(ATTRIBUTE)) {
				complexType.addAttribute(parseElement(child));
			} else {
				parseComplexElementSet(complexType, child);
			}
		}
		
		typeMap.put(complexType.getName(), complexType);
		
		return typeName;
	}

	private void parseComplexElementSet(ComplexType type, Element elem) {
		for (Iterator<Element> iter = elem.elementIterator(); iter.hasNext(); ) {
			type.addElement(parseElement(iter.next()));
		}
	}
	
	private String parseSimpleType(Element type) {
		String typeName = type.attributeValue(NAME);
		Element restrictElem = type.element("restriction");
		
		String baseType = restrictElem.attributeValue("base");
		String[] baseTypes = baseType.split(":");
		baseType = baseTypes[baseTypes.length - 1];
		
		Element patternElem = restrictElem.element("pattern");
		String pattern = null;
		if (patternElem != null)
			pattern = patternElem.attributeValue("value");
		
		SchemaSimpleType simpleType = new SchemaSimpleType(typeName, baseType, pattern);
		typeMap.put(simpleType.getName(), simpleType);
		return typeName;
	}
	
	public SchemaType getSchemaType(String typeName) {
		return typeMap.get(typeName);
	}
}

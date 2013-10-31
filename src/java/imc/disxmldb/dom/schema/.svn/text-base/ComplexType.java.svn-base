package imc.disxmldb.dom.schema;

import java.util.LinkedList;
import java.util.List;

public class ComplexType extends SchemaType {
	private List<SchemaElement> elements = null;
	private List<SchemaElement> attributes = null;

	public ComplexType(String name) {
		super(name);
		elements = new LinkedList<SchemaElement>();
		attributes = new LinkedList<SchemaElement>();
	}

	public ComplexType(String name, List<SchemaElement> elements, List<SchemaElement> attrs) {
		super(name);
		this.elements = elements;
		this.attributes = attrs;
	}

	public void addElement(SchemaElement element) {
		elements.add(element);
	}

	public List<SchemaElement> getElements() {
		return elements;
	}
	
	public void setElements(List<SchemaElement> elements) {
		this.elements = elements;
	}

	public void addAttribute(SchemaElement attr) {
		attributes.add(attr);
	}

	public List<SchemaElement> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<SchemaElement> attributes) {
		this.attributes = attributes;
	}
}

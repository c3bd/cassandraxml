/**
 * author: zhangtl: OwenZhang1990@gmail.com
 * 
 * @version: 2011-9-26 0.1 modify by: xiafan {xiafan68@gmail.com}
 * @version: 2011-12-10 0.2 modify by: xiafan {xiafan68@gmail.com} it assumes
 *           that users always store an empty document that contains the common
 *           parts of the xml doc
 */
package imc.disxmldb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Iterator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.cassandra.db.RowMutation;
import org.apache.cassandra.service.StorageProxy;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.cassandra.utils.LatencyTracker;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.XMLDBException;

import imc.disxmldb.config.SysConfig;
import imc.disxmldb.config.XMLDBCatalogManager;
import imc.disxmldb.config.XMLMetaData;
import imc.disxmldb.dom.AttributeNode;
import imc.disxmldb.dom.AttributeNodeImpl;
import imc.disxmldb.dom.ElementNode;
import imc.disxmldb.dom.ElementNodeImpl;
import imc.disxmldb.dom.XMLNode;
import imc.disxmldb.dom.XMLNodeImpl;
import imc.disxmldb.dom.numbering.INumberingSchema;
import imc.disxmldb.dom.numbering.NumberingSchema;
import imc.disxmldb.dom.typesystem.TypeResolver;
import imc.disxmldb.dom.typesystem.ValueType;


/**
 * this class uses the SAX methods to parse the XML document. It will partition the 
 * XML document and encoding the XML node. Then it will store these nodes to the underlying
 * distribute store.
 *
 */
public class XMLStore implements XMLStoreMBean {
	private static LatencyTracker totalLatency = new LatencyTracker();
	private static LatencyTracker mutationLatency = new LatencyTracker();
	private static LatencyTracker startElementLatency = new LatencyTracker();
	private static LatencyTracker endElementLatency = new LatencyTracker();
	private static LatencyTracker characterLatency = new LatencyTracker();
	private static LatencyTracker storeLatency = new LatencyTracker();

	static {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			mbs.registerMBean(new XMLStore(null, null), new ObjectName(
					"imc.disxmldb:type=XMLStore"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	private CollectionStore colStore = null;
	private XMLMetaData xmlMetaData = null;
	private String xmlSchema;
	private double range_size = SysConfig.DEFAULT_RANGE_SIZE; // range size
																// for leaf node

	public XMLStore(XMLMetaData metaData, CollectionStore colStore) {
		this.colStore = colStore;
		this.xmlMetaData = metaData;
	}

	private void setSchema() {
		if (XMLDBCatalogManager.instance().getSchema(
				xmlMetaData.getSchemaName()) != null)
			xmlSchema = XMLDBCatalogManager.instance()
					.getSchema(xmlMetaData.getSchemaName()).getSchema();
	}

	private String getSchema() {
		return xmlSchema;
	}

	/**
	 * @param xml
	 * @throws XMLDBException
	 */
	public void storeXML(String xml) throws XMLDBException {
		try {
			// setSchema();

			// parse and store xml
			XMLHandler xmlHandler = new XMLHandler();
			XMLReader xmlReader = getXMLReader(xmlHandler, xmlHandler, false);
			// InputSource xmlSource = new InputSource(new StringReader(xml));

			ByteArrayInputStream bis = new ByteArrayInputStream(
					xml.getBytes(SysConfig.ENCODING));
			InputSource xmlSource = new InputSource(bis);
			long startTime = System.currentTimeMillis();
			xmlReader.parse(xmlSource);
			totalLatency.addMicro(System.currentTimeMillis() - startTime);
		} catch (IOException e) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR, e.getClass()
					+ ":" + e.getMessage());
		} catch (SAXException e) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR, e.getClass()
					+ ":" + e.getMessage());
		} catch (ParserConfigurationException e) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR, e.getClass()
					+ ":" + e.getMessage());
		} catch (Exception ex) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ex.getClass()
					+ ":" + ex.getMessage());
		}

	}

	private XMLReader getXMLReader(ContentHandler contentHandler,
			ErrorHandler errorHandler, boolean validating)
			throws ParserConfigurationException, SAXException {
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();

		if (validating) {
			// xml schema validation
			saxFactory.setValidating(false);
			SchemaFactory schemaFactory = SchemaFactory
					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			StreamSource streamSource = new StreamSource(new StringReader(
					getSchema()));
			Schema schema = schemaFactory.newSchema(streamSource);
			saxFactory.setSchema(schema);
			saxFactory.setNamespaceAware(true);
		}

		// create xml reader
		SAXParser saxParser;
		saxParser = saxFactory.newSAXParser();
		XMLReader xmlReader = saxParser.getXMLReader();

		if (validating) {
			// setup xml reader for validation
			xmlReader.setFeature(SysConfig.SAX_NAMESPACES, true);
			xmlReader.setFeature(SysConfig.SAX_NAMESPACES_PREFIX, true);
			xmlReader.setFeature(SysConfig.SAX_VALIDATION, true);
			xmlReader.setFeature(SysConfig.APACHE_VALIDATION, true);
			xmlReader.setFeature(SysConfig.APACHE_VALIDATION_DYNAMIC, false);
		}

		xmlReader.setContentHandler(contentHandler);
		xmlReader.setErrorHandler(errorHandler);

		return xmlReader;
	}

	private final class XMLHandler extends DefaultHandler {
		private ElementNode currentElement = null;
		private String partitionPath = "";
		int count = 0;
		int depth = 0;
		// store uri to prefix mappings
		private Map<String, String> namespaceMappings = new HashMap<String, String>();

		List<RowMutation> mutations = new LinkedList<RowMutation>();

		//private double[] range = { 0, 0 };
		private INumberingSchema numbering;
		private XMLMetaData metaData = null;

		public XMLHandler() {
			numbering = new NumberingSchema(new double[]{0.0, Double.MAX_VALUE}, SysConfig.DEFAULT_RANGE_SIZE);
			if (xmlMetaData.getSplitted() == 1) {
				metaData = XMLDBCatalogManager.instance().newXMLPart(
						xmlMetaData);
			} else {
				range_size = SysConfig.DEFAULT_RANGE_SIZE * 10;
				metaData = xmlMetaData;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			long startTime = System.currentTimeMillis();
			String value = new String(ch, start, length);
			ValueType type = TypeResolver.resolve(value);
			((XMLNode) currentElement).setValueType(type);
			((XMLNode) currentElement).setValue(ValueType.getValidator(type).fromString(value));
			characterLatency.addMicro(System.currentTimeMillis() - startTime);
		}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length)
				throws SAXException {

		}

		@Override
		public void endDocument() throws SAXException {
			if (xmlMetaData.getSplitted() == 1 && partitionPath.length() > 0)
				colStore.getMetaData().addXMLPartitionPath(partitionPath,
						xmlMetaData.getXMLID());
			/*
			 * metaData.setDirty(true, true); xmlMetaData.setDirty(true, true);
			 */
			try {
				if (mutations.size() > 0) {
					long startTime = System.currentTimeMillis();
					StorageProxy.mutate(mutations,
							SysConfig.XMLSTORE_CONSISTENCY_LEVEL);
					mutationLatency.addMicro(System.currentTimeMillis()
							- startTime);
				}
				metaData.syncMaxNodeID();
			} catch (Exception e) {
				/*
				 * TODO in this case we should delete the whole xml doc
				 */
				e.printStackTrace();
			}
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			long startTime = System.currentTimeMillis();
			try {
				//range[1] += range_size; // in the case that endElement called
				// continuously
				if (depth > 1) {
					//((XMLNode) currentElement).setRangeUpper(range[1]);
					((XMLNode) currentElement).setRangeUpper(numbering.endElement());
				} else
					((XMLNode)currentElement).setRangeUpper(Double.MAX_VALUE);
				mutations.add(colStore.ElementNodeMutation(metaData,
						currentElement, null));
				/*
				 * long mutateTime = System.currentTimeMillis();
				 * colStore.storeElementNode(metaData, currentElement);
				 * mutationLatency.addMicro(System.currentTimeMillis() -
				 * mutateTime);
				 */

				if (xmlMetaData.getSplitted() == 1 && depth == 2
						&& xmlMetaData.shouldSplit(count)) {
					metaData.syncMaxNodeID();
					metaData.increXMLNodeNum(count);
					count = 0;
					ElementNode parent = (ElementNode) ((XMLNode) currentElement)
							.getParent();
					((XMLNode)parent).setRangeUpper(Double.MAX_VALUE);
					/*
					 * mutateTime = System.currentTimeMillis();
					 * colStore.storeElementNode(metaData, parent);
					 * mutationLatency.addMicro(System.currentTimeMillis() -
					 * mutateTime);
					 */
					mutations.add(colStore.ElementNodeMutation(metaData,
							parent, null));

					long mutateTime = System.currentTimeMillis();
					StorageProxy.mutate(mutations,
							SysConfig.XMLSTORE_CONSISTENCY_LEVEL);
					mutationLatency.addMicro(System.currentTimeMillis()
							- mutateTime);

					mutations.clear();
					// do some cleaning
					/*range[0] = ((XMLNode)parent).getRange()[0];
					range[1] = range[0] + range_size;*/
					numbering = new NumberingSchema(new double[]{0.0, Double.MAX_VALUE}, SysConfig.DEFAULT_RANGE_SIZE);
					parent.getChilds().clear(); 
					metaData = XMLDBCatalogManager.instance().newXMLPart(
							xmlMetaData);
					metaData.setMaxNodeID(((XMLNode)currentElement).getParent().getId());

				}

				depth--;
			} catch (UnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// walk up the tree
			currentElement = (ElementNode) ((XMLNode)currentElement).getParent();
			endElementLatency.addMicro(System.currentTimeMillis() - startTime);
		}

		public void endPrefixMapping(String prefix) throws SAXException {
			for (Iterator<String> i = namespaceMappings.keySet().iterator(); i
					.hasNext();) {
				String uri = (String) i.next();
				String thisPrefix = (String) namespaceMappings.get(uri);
				if (prefix.equals(thisPrefix)) {
					namespaceMappings.remove(uri);
					break;
				}
			}
		}

		public void startDocument() throws SAXException {
		}

		public void startElement(String namespaceURI, String localName,
				String qName, Attributes attributes) throws SAXException {
			long startTime = System.currentTimeMillis();
			depth++;
			if (depth == 2)
				count++;
			else if (depth < 2 && xmlMetaData.getSplitted() == 1) {
				if (namespaceURI.length() > 0)
					partitionPath += "/" + localName;
				else
					partitionPath += "/" + qName;
			}

			ElementNode parent;
			if (currentElement == null) // root element: possibly
										// <semanticfeature>
				parent = null;
			else
				parent = currentElement; // walk down the tree
			if (namespaceURI.length() > 0)
				currentElement = new ElementNodeImpl(localName,
						metaData.increAndGetMaxNodeID(), depth, (XMLNode)parent);
			else
				currentElement = new ElementNodeImpl(qName,
						metaData.increAndGetMaxNodeID(), depth, (XMLNode)parent);

			if (parent != null) {
				parent.addChild((XMLNode)currentElement);
				// calculate the range (range[0], range[1]) for the current
				// element
				// but only leaf node's range[1] is set now
				//range[0] = range[1] + range_size;
				//((XMLNode)currentElement).setRangeLower(range[0]); // set the lower limit
				((XMLNode)currentElement).setRangeLower(numbering.startElement());
			}
			//range[1] = range[0]; // in the case endElement is called next

			// store prefix and uri
			if (namespaceURI.length() > 0) {
				String prefix = (String) namespaceMappings.get(namespaceURI);
				metaData.addNamespaceMappings(prefix, namespaceURI);
			}

			// work on currentElement's attributes
			int attsLength = attributes.getLength();

			for (int i = 0; i < attsLength; i++) {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append(attributes.getQName(i));
				String attURI = attributes.getURI(i);
				if (attURI.length() > 0) {
					String attPrefix = (String) namespaceMappings.get(attURI);
					metaData.addNamespaceMappings(attPrefix, namespaceURI);
				}
				AttributeNode attributeNode = new AttributeNodeImpl(
						stringBuilder.toString(),
						metaData.increAndGetMaxNodeID(), depth + 1,
						(XMLNode)currentElement);
				ValueType type = TypeResolver.resolve(attributes.getValue(i).trim());
				((XMLNode) attributeNode).setValueType(type);
				((XMLNode) attributeNode).setValue(ValueType.getValidator(type).fromString(attributes.getValue(i).trim()));
				/*range[0] = range[1] + range_size;
				range[1] = range[0] + range_size;
				((XMLNode)attributeNode).setRangeLower(range[0]);
				((XMLNode)attributeNode).setRangeUpper(range[1]);*/
				((XMLNode)attributeNode).setRangeLower(numbering.startAttribute());
				((XMLNode)attributeNode).setRangeUpper(numbering.endAttribute());
				// attributeNode.setRange(range);

				mutations.add(colStore.AttribNodeMutation(metaData,
						(XMLNode)attributeNode, null));
				currentElement.addAttribute((XMLNode)attributeNode);

			}
			startElementLatency
					.addMicro(System.currentTimeMillis() - startTime);
		}

		public void startPrefixMapping(String prefix, String uri)
				throws SAXException {
			namespaceMappings.put(uri, prefix);
		}

		public void warning(SAXParseException exception) throws SAXException {
			String msg = "warning at (" + exception.getLineNumber() + ","
					+ exception.getColumnNumber() + ") : "
					+ exception.getMessage();
			throw new SAXException(msg, exception);
		}

		public void error(SAXParseException exception) throws SAXException {
			String msg = "error at (" + exception.getLineNumber() + ","
					+ exception.getColumnNumber() + ") : "
					+ exception.getMessage();
			throw new SAXException(msg, exception);
		}

		public void fatalError(SAXParseException exception) throws SAXException {
			String msg = "fatal error at (" + exception.getLineNumber() + ","
					+ exception.getColumnNumber() + ") : "
					+ exception.getMessage();
			throw new SAXException(msg, exception);
		}
	}

	@Override
	public double getAvgTotalLatencyMs() {
		if (totalLatency.getOpCount() == 0)
			return 0;
		return (double) totalLatency.getTotalLatencyMicros()
				/ totalLatency.getOpCount();
	}

	@Override
	public double getAvgMutationLatencyMs() {
		if (mutationLatency.getOpCount() == 0)
			return 0;
		return (double) mutationLatency.getTotalLatencyMicros()
				/ mutationLatency.getOpCount();
	}

	@Override
	public long getMutationLatency() {
		return mutationLatency.getTotalLatencyMicros();
	}

	@Override
	public long getStartElementLatency() {
		return startElementLatency.getTotalLatencyMicros();
	}

	@Override
	public long getEndElementLatency() {
		return endElementLatency.getTotalLatencyMicros();
	}

	@Override
	public long getCharacterLatency() {
		return characterLatency.getTotalLatencyMicros();
	}

}

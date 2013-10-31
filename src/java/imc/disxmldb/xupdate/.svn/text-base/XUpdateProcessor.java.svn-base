/**
 *@author:xiafan xiafan68@gmail.com
 *@version: 2011-10-25 0.1
 */
package imc.disxmldb.xupdate;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.cassandra.db.RowMutation.XUpdateCommand;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.XMLDBStore;
import imc.disxmldb.cassandra.verbhandler.XPathQueryCommand;
import imc.disxmldb.cassandra.verbhandler.XPathResult;
import imc.disxmldb.cassandra.verbhandler.result.XMLDocNodeIDs;
import imc.disxmldb.cassandra.verbhandler.result.XMLNodeForMutations;
import imc.disxmldb.config.CollectionMetaData;
import imc.disxmldb.config.SysConfig;
import imc.disxmldb.config.XMLDBCatalogManager;
import imc.disxmldb.config.XMLMetaData;
import imc.disxmldb.dom.ElementNode;
import imc.disxmldb.dom.ElementNodeImpl;
import imc.disxmldb.dom.XMLNode;
import imc.disxmldb.dom.XMLNodeForMutation;
import imc.disxmldb.util.XMLUtilities;

/**
 * This class implements the parser of XUpdate query. The delete and update
 * command is also implemented here. Currently An XUpdate command will lock the
 * whole XML document which it updates;
 * 
 * TODO transactions need to be supported
 */
public class XUpdateProcessor {

	private CollectionStore colStore = null;

	public XUpdateProcessor(CollectionStore collectionStore) {
		this.colStore = collectionStore;
	}

	/**
	 * the xupdate query will be parsed into an AppendContext object, then it is
	 * used to update the xml store.
	 * 
	 * @param command
	 * @return
	 * @throws Exception
	 */
	public int xupdate(byte[] command, boolean recursive) throws Exception {
		// parse the xupdate command
		ByteArrayInputStream bis = new ByteArrayInputStream(command);
		InputSource xmlSource = new InputSource(bis);
		XMLHandler xmlHandler = new XMLHandler();
		XMLReader xmlReader;
		xmlReader = getXMLReader(xmlHandler, xmlHandler, false);
		xmlReader.parse(xmlSource);
		if (xmlHandler.level != 0) {
			throw new Exception("the end tags doesn't match the start tags");
		}

		/*
		 * execute the xupdate command TODO lock strategies are needed
		 */
		int ret = 0;
		if (xmlHandler.appendContext != null) {
			ret += append(xmlHandler.appendContext);
			if (recursive) {
				// recursively invoke the xupdate on the child collection
				for (String colName : colStore.getMetaData().getChildColNames()) {
					Integer colID = colStore.getMetaData().getCollectionID(
							colName);
					if (colID != null) {
						CollectionStore childStore = XMLDBStore.instance()
								.getCollection(colID);
						XUpdateProcessor processor = new XUpdateProcessor(
								childStore);
						ret += processor.xupdate(command, recursive);
					}
				}
			}
		} else if (xmlHandler.deleteContext != null) {
			ret = delete(xmlHandler.deleteContext, recursive);
		} else if (xmlHandler.updateContext != null) {
			ret = update(xmlHandler.updateContext, recursive);
		}

		return ret;
	}

	/**
	 * the parameter for an update operation has been parsed into updateContext
	 * this function will select out these nodes and update their value
	 * 
	 * @param updateContext
	 * @param recursive
	 * @return
	 * @throws Exception
	 */
	private int update(UpdateContext updateContext, boolean recursive)
			throws Exception {
		int updateCount = 0;
		for (int i = 0; i < updateContext.xpath.size(); i++) {
			String xpath = updateContext.xpath.get(i);
			String value = updateContext.value.get(i);
			byte flag = 0;
			if (recursive) {
				flag = XPathQueryCommand.installFlag(flag,
						XPathQueryCommand.XPATH_RECURSIVE);
			}
			flag = XPathQueryCommand.installFlag(flag,
					XPathQueryCommand.XPATH_NODEONLY);

			XPathResult result = colStore.XPath(
					xpath.getBytes(SysConfig.ENCODING), flag);

			if (result == null || result.size() == 0) {
				continue;
			}

			XMLNodeForMutations nodeIDs = (XMLNodeForMutations) result
					.getResult();
			for (Entry<Integer, Map<Integer, List<XMLNodeForMutation>>> entry : nodeIDs.nodes
					.entrySet()) {
				CollectionStore tempStore = XMLDBStore.instance()
						.getCollection(entry.getKey());
				if (tempStore != null)
					tempStore.updateNodeText(entry.getValue(), value);
			}

			for (Entry<Integer, Map<Integer, List<XMLNodeForMutation>>> colEntry : nodeIDs.nodes
					.entrySet()) {
				Map<Integer, List<XMLNodeForMutation>> xml2NodeIDs = colEntry
						.getValue();
				for (Entry<Integer, List<XMLNodeForMutation>> entry : xml2NodeIDs
						.entrySet()) {
					updateCount += entry.getValue().size();
				}
			}

		}

		return updateCount;
	}

	private XMLReader getXMLReader(ContentHandler contentHandler,
			ErrorHandler errorHandler, boolean validating)
			throws ParserConfigurationException, SAXException {
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		// create xml reader
		SAXParser saxParser = saxFactory.newSAXParser();
		XMLReader xmlReader = saxParser.getXMLReader();

		xmlReader.setFeature(SysConfig.SAX_NAMESPACES, true);
		xmlReader.setFeature(SysConfig.SAX_NAMESPACES_PREFIX, true);
		xmlReader.setFeature(SysConfig.SAX_VALIDATION, false);
		xmlReader.setFeature(SysConfig.APACHE_VALIDATION, false);
		xmlReader.setFeature(SysConfig.APACHE_VALIDATION_DYNAMIC, false);

		xmlReader.setContentHandler(contentHandler);
		xmlReader.setErrorHandler(errorHandler);
		return xmlReader;
	}

	public static final class DeleteContext {
		public CollectionStore colStore = null;
		public List<String> xpath = new ArrayList<String>();

		// public List<RowMutation> rms = new LinkedList<RowMutation>();

		public DeleteContext(CollectionStore colStore) {
			this.colStore = colStore;
		}
	}

	public static final class AppendContext {
		public CollectionStore colStore = null;
		/*
		 * public List<XMLMetaData> xmlMetaDataList = new
		 * LinkedList<XMLMetaData>(); public List<ElementNode> parentNode = new
		 * LinkedList<ElementNode>(); public List<Pair<Double, Double>>
		 * rangeList = new LinkedList<Pair<Double, Double>>();
		 */

		public List<String> xpath = new ArrayList<String>();
		public List<String> xmlParts = new ArrayList<String>();
		public List<Integer> nodeCount = new ArrayList<Integer>();
		public List<Integer> nodeUnitCount = new ArrayList<Integer>();

		public AppendContext(CollectionStore colStore) {
			this.colStore = colStore;
		}
	}

	public static final class UpdateContext {
		public CollectionStore colStore = null;
		public List<String> value = new ArrayList<String>();
		public List<String> xpath = new ArrayList<String>();

		public UpdateContext(CollectionStore colStore) {
			this.colStore = colStore;
		}
	}

	private final class XMLHandler extends DefaultHandler {
		private static final String SELECT_ATTR = "select";

		private XUpdateCommand cmd = null;
		private XPathResult result = null;
		int level = 0;
		// return the number of nodes that is modified
		public int modifyCount = 0;
		public boolean success = true;
		int nodeCount = 0;
		int nodeUnitCount = 0;
		public AppendContext appendContext = null;
		public DeleteContext deleteContext = null;
		public UpdateContext updateContext = null;

		public StringBuffer xmlBuffer = new StringBuffer();
		private int levelThreshHold = Integer.MAX_VALUE;

		public XMLHandler() {

		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (level == levelThreshHold + 2 && cmd != XUpdateCommand.update) {
				throw new SAXException(
						"only update can be used to update the text value of a node");
			}
			String text = new String(ch, start, length);
			xmlBuffer.append(XMLUtilities.encodeString(text));
		}

		@Override
		public void endDocument() throws SAXException {
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			level--;
			if (level == levelThreshHold + 1) {
				// It means this xml node is an command node
				if (cmd == XUpdateCommand.append) {
					appendContext.xmlParts.add(AppendHandler.ROOTNDOE_START_TAG
							+ xmlBuffer.toString()
							+ AppendHandler.ROOTNODE_END_TAG);
					appendContext.nodeCount.add(nodeCount);
					appendContext.nodeUnitCount.add(nodeUnitCount);
				} else if (cmd == XUpdateCommand.update) {
					updateContext.value.add(xmlBuffer.toString());
				}

				xmlBuffer = new StringBuffer();
			} else if (level > levelThreshHold + 1) {
				if (level == levelThreshHold + 2
						&& cmd == XUpdateCommand.append)
					nodeUnitCount++;
				// the xml nodes under the command node
				if (uri.length() > 0) {
					xmlBuffer.append("</" + localName + ">");
				} else {
					xmlBuffer.append("</" + qName + ">");
				}
				// nodeCount++;
			}
		}

		@Override
		public void endPrefixMapping(String prefix) throws SAXException {

		}

		@Override
		public void startDocument() throws SAXException {
		}

		@Override
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes attributes) throws SAXException {
			String tagName = null;
			if (namespaceURI.length() > 0)
				tagName = localName;
			else
				tagName = qName;
			if (tagName.equals("modifications"))
				levelThreshHold = level;

			if (level == levelThreshHold + 1) {
				setCommand(tagName);

				// fetch the xmlnodes of xpath
				String xpath = attributes.getValue(SELECT_ATTR);
				if (cmd == XUpdateCommand.append) {
					if (appendContext == null) {
						appendContext = new AppendContext(colStore);
					}
					appendContext.xpath.add(xpath);
				} else if (cmd == XUpdateCommand.delete) {
					if (deleteContext == null) {
						deleteContext = new DeleteContext(colStore);
					}
					deleteContext.xpath.add(xpath);
				} else if (cmd == XUpdateCommand.update) {
					if (updateContext == null) {
						updateContext = new UpdateContext(colStore);
					}
					updateContext.xpath.add(xpath);
				}

			} else if (level > levelThreshHold) {
				// the case for append
				if (level == levelThreshHold + 1) {
					nodeUnitCount++;
				}
				xmlBuffer.append(compositeElement(tagName, attributes));
			}
			level++;
		}

		@Override
		public void startPrefixMapping(String prefix, String uri)
				throws SAXException {
		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			String msg = "warning at (" + exception.getLineNumber() + ","
					+ exception.getColumnNumber() + ") : "
					+ exception.getMessage();
			throw new SAXException(msg, exception);
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			String msg = "error at (" + exception.getLineNumber() + ","
					+ exception.getColumnNumber() + ") : "
					+ exception.getMessage();
			throw new SAXException(msg, exception);
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			String msg = "fatal error at (" + exception.getLineNumber() + ","
					+ exception.getColumnNumber() + ") : "
					+ exception.getMessage();
			throw new SAXException(msg, exception);
		}

		public void setCommand(String cmdStr) {
			if (cmdStr.equals("update"))
				cmd = XUpdateCommand.update;
			else if (cmdStr.equals("remove")) {
				cmd = XUpdateCommand.delete;
			} else if (cmdStr.equals("append")) {
				cmd = XUpdateCommand.append;
			} else {
				throw new AssertionError("unknow xupdate command: " + cmdStr);
			}
		}

		private String compositeElement(String tag, Attributes attrs) {
			StringBuffer ret = new StringBuffer();
			ret.append("<" + tag);
			for (int i = 0; i < attrs.getLength(); i++) {
				ret.append(" " + attrs.getQName(i) + "=\"" + attrs.getValue(i)
						+ "\"");
				nodeCount++;
			}
			ret.append(">");
			nodeCount++;
			return ret.toString();
		}
	}

	/**
	 * delete the selected nodes specified by the xpath in the xupdate query
	 * 
	 * @param context
	 * @param recursive
	 * @return
	 * @throws Exception
	 */
	private int delete(DeleteContext context, boolean recursive)
			throws Exception {
		// exeucte in seperate servers or in a centralized manner?
		int delCount = 0;
		byte flag = 0;
		if (recursive) {
			flag = XPathQueryCommand.installFlag(flag,
					XPathQueryCommand.XPATH_RECURSIVE);
		}
		flag = XPathQueryCommand.installFlag(flag,
				XPathQueryCommand.XPATH_NODEONLY);

		for (String xpath : context.xpath) {
			// recursive determines whether or not the delete all nodes in the
			// child collections
			XPathResult queryRet = colStore.XPath(
					xpath.getBytes(SysConfig.ENCODING), flag);
			if (queryRet == null || queryRet.size() == 0)
				continue;

			XMLNodeForMutations nodes = (XMLNodeForMutations) queryRet
					.getResult();
			for (Entry<Integer, Map<Integer, List<XMLNodeForMutation>>> entry : nodes.nodes
					.entrySet()) {
				CollectionStore tempStore = XMLDBStore.instance()
						.getCollection(entry.getKey());
				if (tempStore != null)
					delCount += tempStore.deleteNodeRemote(entry.getValue());
			}
		}
		return delCount;
	}

	/**
	 * update the text value of the selected nodes
	 * 
	 * @param context
	 * @param recursive
	 * @return
	 * @throws Exception
	 */
	private int xupdate(UpdateContext context, boolean recursive)
			throws Exception {
		assert context.xpath.size() == context.value.size();
		int updateCount = 0;

		Iterator<String> xpathIter = null, valIter = null;
		while (xpathIter.hasNext()) {
			String xpath = xpathIter.next();
			String value = valIter.next();
			byte flag = 0;
			if (recursive) {
				flag = XPathQueryCommand.installFlag(flag,
						XPathQueryCommand.XPATH_RECURSIVE);
			}
			flag = XPathQueryCommand.installFlag(flag,
					XPathQueryCommand.XPATH_NODEONLY);
			// recursive is important to determine whether or not to query to
			// sub collection
			XPathResult queryRet = colStore.XPath(
					xpath.getBytes(SysConfig.ENCODING), flag);
			if (queryRet == null || queryRet.size() == 0)
				continue;

			XMLNodeForMutations nodeIDs = (XMLNodeForMutations) queryRet
					.getResult();
			for (Entry<Integer, Map<Integer, List<XMLNodeForMutation>>> entry : nodeIDs.nodes
					.entrySet()) {
				CollectionStore tempStore = XMLDBStore.instance()
						.getCollection(entry.getKey());
				if (tempStore != null)
					tempStore.updateNodeText(entry.getValue(), value);
			}

			for (Entry<Integer, Map<Integer, List<XMLNodeForMutation>>> colEntry : nodeIDs.nodes
					.entrySet()) {
				for (Entry<Integer, List<XMLNodeForMutation>> entry : colEntry
						.getValue().entrySet()) {
					updateCount += entry.getValue().size();
				}
			}

		}

		return updateCount;
	}

	/**
	 * this function iterately invoke appendNode function to execute each single
	 * append command in this xupdate command
	 * 
	 * @param context
	 * @return
	 * @throws Exception
	 */
	private int append(AppendContext context) throws Exception {
		int appendCount = 0;

		for (int i = 0; i < context.xmlParts.size(); i++) {
			appendCount += appendNode(context.xpath.get(i),
					context.xmlParts.get(i), context.nodeCount.get(i))
					* context.nodeUnitCount.get(i);
		}

		return appendCount;
	}

	/**
	 * lock is implemented in the xml document level
	 * 
	 * @param xpath
	 * @param xmlParts
	 * @param nodeCount
	 * @return
	 * @throws Exception
	 */
	private int appendNode(String xpath, String xmlParts, int nodeCount)
			throws Exception {
		// TODO
		int appendCount = 0;
		CollectionMetaData colMeta = colStore.getMetaData();
		if (xpath.endsWith("/"))
			xpath = xpath.substring(0, xpath.length() - 1);
		List<Integer> docs = colMeta.getXMLDocByPartitionPath(xpath);
		for (Integer doc : docs) {
			XMLMetaData effectMeta = null;
			try {
				/*
				 * TODO fetch the last xml partial doc of docs, judge if the
				 * size is ok.
				 */
				XMLMetaData origin = XMLDBCatalogManager.instance()
						.getXMLMetaDataByID(colMeta.getColID(), doc);
				effectMeta = chooseXMLDoc(origin);

				/*
				 * TODO lock the xml document
				 */
				int lockTime = 0;
				do {
					lockTime = new Random().nextInt();
					lockTime = Math.abs(lockTime) % SysConfig.XML_TRYLOCK_TIME;
				} while (!effectMeta.lock.tryLock(lockTime,
						TimeUnit.MILLISECONDS));

				AppendHandler handler = null;
				ElementNode parent = null;
				double[] range = new double[2];
				XMLNode child = null;

				if (effectMeta.getMaxNodeID() == -1) {
					// the case for a new created xml doc
					parent = composeXMLNode(effectMeta, xpath);
					// calculate the range that can be consumed by
					// subtree
					range[0] = ((XMLNode) parent).getRange()[0];
					range[1] = ((XMLNode) parent).getRange()[1];
					handler = new AppendHandler(colStore, effectMeta, range,
							nodeCount, parent);
				} else {
					// the case for an already existed doc
					handler = addAppendNode(effectMeta.getXMLID(), 0, nodeCount);
				}

				executeAppend(xmlParts, handler);
				appendCount++;
			} finally {
				if (effectMeta != null)
					effectMeta.lock.unlock();
			}
		}// end of docs.size > 0

		// it means the xpath is not a partition path, so we should select out
		// all
		// the nodes that conforms to this xpath
		if (docs.size() == 0) {
			// recursively append is not implemented by recursively querying all
			// the sub collection the xpath
			byte flag = XPathQueryCommand.installFlag((byte) 0,
					XPathQueryCommand.XPATH_NODEID);
			XPathResult result = colStore.XPath(
					xpath.getBytes(SysConfig.ENCODING), flag);

			if (result == null || result.size() == 0) {
				return 0;
			}

			XMLDocNodeIDs nodeIDs = (XMLDocNodeIDs) result.getResult();
			for (Entry<Integer, Map<Integer, List<Integer>>> colEntry : nodeIDs.nodeIDs
					.entrySet()) {
				for (Entry<Integer, List<Integer>> entry : colEntry.getValue()
						.entrySet()) {
					XMLMetaData meta = XMLDBCatalogManager.instance()
							.getXMLMetaDataByID(
									colStore.getMetaData().getColID(),
									entry.getKey());
					for (Integer rootNodeID : entry.getValue()) {
						AppendHandler handler = addAppendNode(entry.getKey(),
								rootNodeID, nodeCount);
						executeAppend(xmlParts, handler);
						appendCount++;
					}
				}
			}
		}

		return appendCount;
	}

	private void executeAppend(String xmlParts, AppendHandler handler)
			throws Exception {
		XMLReader xmlReader;
		ByteArrayInputStream bis = null;
		try {
			xmlReader = getXMLReader(handler, handler, false);
			bis = new ByteArrayInputStream(
					xmlParts.getBytes(SysConfig.ENCODING));
		} catch (Exception e) {
			throw e;
		}
		InputSource xmlSource = new InputSource(bis);
		try {
			xmlReader.parse(xmlSource);
		} catch (Exception e) {
			handler.rollBack();
			throw e;
		}
	}

	/**
	 * 
	 * @param rootNodeID
	 *            the node which is the parent node of the appending nodes
	 * @throws SAXException
	 */
	private AppendHandler addAppendNode(int XMLID, int rootNodeID, int nodeCount)
			throws Exception {
		ElementNode parent = null;
		double[] range = new double[2];
		XMLMetaData meta = null;

		HashMap<Integer, List<Integer>> xmls = new HashMap<Integer, List<Integer>>();
		List<Integer> nodes = new LinkedList<Integer>();
		nodes.add(rootNodeID);
		xmls.put(XMLID, nodes);
		HashMap<Integer, List<XMLNode>> nodeRet;
		XMLNode child = null;

		nodeRet = colStore.retrieveXMLNode(xmls);

		if (nodeRet == null || nodeRet.get(XMLID) == null
				|| nodeRet.get(XMLID).size() == 0) {
			throw new SAXException();
		} else {
			if (nodeRet.get(XMLID).size() == 1) {
				meta = XMLDBCatalogManager.instance().getXMLMetaDataByID(
						colStore.getMetaData().getColID(), XMLID);
				parent = (ElementNode) nodeRet.get(XMLID).get(0);
				xmls.clear();
				nodes = new LinkedList<Integer>();
				// List<XMLNode> childs = parent.getChilds();
				if (parent.getAttrNum() == 0 && parent.getChildNum() == 0) {
					// the node to append is the first child
					range[0] = ((XMLNode) parent).getRange()[0];
					range[1] = ((XMLNode) parent).getRange()[1];
				} else {
					if (parent.getAttrNum() != 0) {
						nodes.add(parent.getAttributeNodes()
								.get(parent.getAttrNum() - 1).getId());
					}

					if (parent.getChildNum() != 0) {
						nodes.clear();
						nodes.add(parent.getChilds()
								.get(parent.getChildNum() - 1).getId());
					}
					xmls.put(XMLID, nodes);
					nodeRet = colStore.retrieveXMLNode(xmls);

					if (nodeRet.get(XMLID).size() == 1) {
						child = nodeRet.get(XMLID).get(0);
						range[0] = child.getRange()[1];
						range[1] = (((XMLNode) parent).getRange()[1] - child
								.getRange()[1]) / 2 + child.getRange()[1];
					} else {
						throw new Exception("child node is missed");
					}
				}
			}
		}

		return new AppendHandler(colStore, meta, range, nodeCount, parent);
	}

	private XMLMetaData chooseXMLDoc(XMLMetaData origin) {
		assert origin.getSplitted() == 1;
		XMLMetaData ret = null;
		Integer id = origin.getLastEffectPartID();
		ret = XMLDBCatalogManager.instance().getXMLMetaDataByID(
				origin.getColID(), id);
		return ret;
	}

	/**
	 * construct the common nodes for the new created partial xml
	 * 
	 * @param xpath
	 * @return
	 */
	private ElementNode composeXMLNode(XMLMetaData XMLMeta, String xpath) {
		XMLNode cur = null, pre = null;
		if (xpath.startsWith("/"))
			xpath = xpath.substring(1, xpath.length());
		if (xpath.endsWith("/"))
			xpath = xpath.substring(0, xpath.length() - 1);

		String[] nodeStrs = xpath.split("/");

		double startRange = 0.0;
		double endRange = Double.MAX_VALUE;

		int level = 1;
		for (String nodeStr : nodeStrs) {
			cur = new ElementNodeImpl(nodeStr, XMLMeta.increAndGetMaxNodeID(),
					level++, pre);
			cur.setRangeLower(startRange);
			cur.setRangeUpper(endRange);
			double range = (endRange - startRange) / 3;
			startRange += range;
			endRange -= range;
			pre = cur;
		}
		return (ElementNode) cur;
	}
}

/**
 * @author xiafan
 */
package imc.disxmldb.xupdate;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.config.SysConfig;
import imc.disxmldb.config.XMLMetaData;
import imc.disxmldb.dom.AttributeNode;
import imc.disxmldb.dom.AttributeNodeImpl;
import imc.disxmldb.dom.ElementNode;
import imc.disxmldb.dom.ElementNodeImpl;
import imc.disxmldb.dom.XMLNode;
import imc.disxmldb.dom.typesystem.ValueType;
import imc.disxmldb.dom.numbering.INumberingSchema;
import imc.disxmldb.dom.numbering.NumberingSchema;
import imc.disxmldb.dom.typesystem.TypeResolver;
import imc.disxmldb.xupdate.XUpdateProcessor.AppendContext;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.cassandra.db.RowMutation;
import org.apache.cassandra.service.StorageProxy;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * this class implements the append command in the XUpdate query. It use SAX to
 * parse the XML segment and encode them using the numbering schema. Those
 * encoded XML nodes will them be stored distributed.
 */
public class AppendHandler extends DefaultHandler implements AppendHandlerMBean {
	public static final String ROOTNODE = "root";
	public static final String ROOTNDOE_START_TAG = "<" + ROOTNODE + ">";
	public static final String ROOTNODE_END_TAG = "</" + ROOTNODE + ">";

	/*
	 * double rangeSize = SysConfig.DEFAULT_RANGE_SIZE; double[] range = new
	 * double[2]; double rangeBound = 0.0;
	 */
	INumberingSchema numbering;
	int nodeCount = 0;

	CollectionStore colStore = null;
	XMLMetaData metaData = null;
	ElementNode rootNode = null;
	XMLNode lastNode = null;
	XMLNode curNode = null;
	List<RowMutation> mutations = new LinkedList<RowMutation>();
	RowMutation mutation = null;
	StringBuilder value = new StringBuilder();
	List<XMLNode> rootAppendedNodes = new LinkedList<XMLNode>();
	Exception ex = null;
	int level = -1;

	/*
	 * static { MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	 * try { mbs.registerMBean(new AppendHandler(), new ObjectName(
	 * "imc.disxmldb.xupdate:type=AppendHandler")); } catch (Exception e) {
	 * throw new RuntimeException(e); } } private AppendHandler() {
	 * 
	 * }
	 */

	public AppendHandler(CollectionStore colStore, XMLMetaData metaData,
			double[] range, int nodeCount, ElementNode parent) {
		this.nodeCount = nodeCount;
		this.numbering = new NumberingSchema(range,
				SysConfig.DEFAULT_RANGE_SIZE, nodeCount);

		/*
		 * this.range[0] = range[0]; this.range[1] = range[1];
		 * 
		 * if (range[1] == Double.MAX_VALUE) this.range[1] = range[0];
		 * 
		 * rangeBound = range[1];
		 */

		this.colStore = colStore;
		this.metaData = metaData;

		rootNode = parent;
		curNode = (XMLNode) parent;

		/*
		 * if (nodeCount * SysConfig.DEFAULT_RANGE_SIZE * 4 < (range[1] -
		 * range[0])) { rangeSize = SysConfig.DEFAULT_RANGE_SIZE; } else {
		 * rangeSize = (range[1] - range[0]) / (nodeCount * 3.2); }
		 * this.range[1] = this.range[0];
		 */
	}

	@Override
	public void ignorableWhitespace(char ch[], int start, int length)
			throws SAXException {
		value.append(ch, start, length);
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		value.append(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		/*
		 * if (lastNode.getRange()[1] > rangeBound) { throw new SAXException(
		 * "append node fails, which is caused by the lack of coding range for the xml nodes"
		 * ); }
		 */
		if (numbering.isOverflow()) {
			throw new SAXException(
					"append node fails, which is caused by the lack of coding range for the xml nodes");
		}
		try {
			/*
			 * if (mutations.size() > 0) {
			 * mutations.add(colStore.ElementNodeMutation(metaData, rootNode));
			 * StorageProxy.mutate(mutations,
			 * SysConstant.XMLSTORE_CONSISTENCY_LEVEL); }
			 */
			if (mutation != null) {
				mutation = colStore.ElementNodeMutation(metaData, rootNode,
						mutation);
				StorageProxy.mutate(Arrays.asList(mutation),
						SysConfig.XMLSTORE_CONSISTENCY_LEVEL);
			}
			metaData.syncMaxNodeID();
			metaData.increXMLNodeNum(nodeCount);
		} catch (Exception e) {
			/*
			 * TODO in this case, we should rollback the mutations that have
			 * been applied.
			 */
			ex = e;
			throw new SAXException(e);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (level > 0) {
			if (value.length() != 0) {
				String tmp = value.toString().trim();
				ValueType type = TypeResolver.resolve(tmp);
				curNode.setValueType(type);
				curNode.setValue(ValueType.getValidator(type)
						.fromString(tmp));
				value = new StringBuilder();
			}

			/*
			 * range[1] += rangeSize; curNode.setRangeUpper(range[1]);
			 */
			curNode.setRangeUpper(numbering.endElement());
			/*
			 * mutations.add(colStore.ElementNodeMutation(metaData,
			 * (ElementNode) curNode));
			 */
			mutation = colStore.ElementNodeMutation(metaData,
					(ElementNode) curNode, mutation);
			XMLNode pre = curNode;
			curNode = curNode.getParent();

			if (curNode == rootNode) {
				lastNode = pre;
			}
		}
		level--;
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
		level++;
		if (level > 0) {
			String tagName = null;

			if (namespaceURI.length() > 0) {
				tagName = localName;
			} else {
				tagName = qName;
			}

			XMLNode parent = curNode;
			curNode = new ElementNodeImpl(tagName,
					metaData.increAndGetMaxNodeID(), parent.getLevel() + 1,
					parent);
			((ElementNode) parent).addChild(curNode);
			if (parent == rootNode)
				rootAppendedNodes.add(curNode);
			curNode.setRangeLower(numbering.startElement());
			/*
			 * range[0] = range[1] + rangeSize; curNode.setRangeLower(range[0]);
			 * range[1] = range[0];
			 */

			for (int i = 0; i < attributes.getLength(); i++) {
				/*
				 * range[0] = range[1] + rangeSize; range[1] = range[0] +
				 * rangeSize;
				 */

				XMLNode attr = new AttributeNodeImpl(attributes.getQName(i),
						metaData.increAndGetMaxNodeID(),
						curNode.getLevel() + 1, curNode);
				((ElementNode) curNode).addAttribute(attr);
				ValueType type = TypeResolver.resolve(attributes.getValue(i));
				attr.setValueType(type);
				attr.setValue(attributes.getValue(i));
				/*
				 * attr.setRangeLower(range[0]); attr.setRangeUpper(range[1]);
				 */
				attr.setRangeLower(numbering.startAttribute());
				attr.setRangeUpper(numbering.endAttribute());
				// mutations.add(colStore.AttribNodeMutation(metaData, attr));
				mutation = colStore
						.AttribNodeMutation(metaData, attr, mutation);
			}
		}
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
	}

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		String msg = "warning at (" + exception.getLineNumber() + ","
				+ exception.getColumnNumber() + ") : " + exception.getMessage();
		throw new SAXException(msg, exception);
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		String msg = "error at (" + exception.getLineNumber() + ","
				+ exception.getColumnNumber() + ") : " + exception.getMessage();
		throw new SAXException(msg, exception);
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		String msg = "fatal error at (" + exception.getLineNumber() + ","
				+ exception.getColumnNumber() + ") : " + exception.getMessage();
		throw new SAXException(msg, exception);
	}

	public void rollBack() {
		// if the
		if (ex != null && ex instanceof TimeoutException) {
			// it means we should wait for sometime, some node is busy now
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
		}
		AppendRollBack rollBack = new AppendRollBack(colStore, metaData,
				rootNode, rootAppendedNodes);
		rollBack.rollback();
	}
}

/**
 *@author:xiafan xiafan68@gmail.com
 *@version: 2011-9-27 0.1
 */
package imc.disxmldb;

import imc.disxmldb.cassandra.verbhandler.XMLNodeDigestResolver;
import imc.disxmldb.cassandra.verbhandler.XMLReadNodeCallback;
import imc.disxmldb.cassandra.verbhandler.XMLReadNodeCommand;
import imc.disxmldb.cassandra.verbhandler.XPathQueryCallback;
import imc.disxmldb.cassandra.verbhandler.XPathQueryCommand;
import imc.disxmldb.cassandra.verbhandler.XPathResult;
import imc.disxmldb.cassandra.verbhandler.XPathResult.XPathResultType;
import imc.disxmldb.cassandra.verbhandler.result.*;
import imc.disxmldb.config.CollectionMetaData;
import imc.disxmldb.config.SysConfig;
import imc.disxmldb.config.XMLDBCatalogManager;
import imc.disxmldb.config.XMLMetaData;
import imc.disxmldb.dom.AttributeNode;
import imc.disxmldb.dom.ElementNode;
import imc.disxmldb.dom.LocalXMLReadRunnable;
import imc.disxmldb.dom.NodeProxy;
import imc.disxmldb.dom.XMLNode;
import imc.disxmldb.dom.XMLNodeForMutation;
import imc.disxmldb.dom.XMLNodeImpl;
import imc.disxmldb.dom.NodeProxy.NodeProxyType;
import imc.disxmldb.dom.cache.CacheManager;
import imc.disxmldb.dom.cache.DomNodeCache;
import imc.disxmldb.dom.cache.CacheManager.CacheType;
import imc.disxmldb.dom.cache.SubSequenceCache;
import imc.disxmldb.dom.formatter.IndentedXMLNodeFormatter;
import imc.disxmldb.dom.formatter.NormalXMLNodeFormatter;
import imc.disxmldb.dom.formatter.IXMLNodeFormatter;
import imc.disxmldb.dom.typesystem.TypeResolver;
import imc.disxmldb.dom.typesystem.ValueType;
import imc.disxmldb.index.XMLIndexManager;
import imc.disxmldb.index.btree.BtreeDecorator;
import imc.disxmldb.index.btree.IBtree;
import imc.disxmldb.index.btree.IndexQueryResult;
import imc.disxmldb.index.invertindex.IInvertIndex;
import imc.disxmldb.index.invertindex.InvertIndexDecorator;
import imc.disxmldb.util.ColumnFamilyStoreProxy;
import imc.disxmldb.util.LocalCFStoreProxy;
import imc.disxmldb.util.StringOutputStream;
import imc.disxmldb.xpath.DistributeXPathProcessor;
import imc.disxmldb.xpath.xpathcompile.XPathCompiler;
import imc.disxmldb.xpath.xpathparser.IMCXPathException;
import imc.disxmldb.xpath.xpathparser.ParseException;
import imc.disxmldb.xpath.xpathparser.XPathParser;
import imc.disxmldb.xupdate.XUpdateProcessor;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.cassandra.cache.ICache;
import org.apache.cassandra.concurrent.Stage;
import org.apache.cassandra.concurrent.StageManager;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.ColumnFamilyType;
import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.ReadCommand;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.RowMutation;
import org.apache.cassandra.db.SliceByNamesReadCommand;
import org.apache.cassandra.db.SuperColumn;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.db.RowMutation.XUpdateCommand;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.dht.AbstractBounds;
import org.apache.cassandra.dht.LocalPartitioner;
import org.apache.cassandra.net.CachingMessageProducer;
import org.apache.cassandra.net.MessageProducer;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.service.DatacenterReadCallback;
import org.apache.cassandra.service.DigestMismatchException;
import org.apache.cassandra.service.IReadCommand;
import org.apache.cassandra.service.IResponseResolver;
import org.apache.cassandra.service.ReadCallback;
import org.apache.cassandra.service.RepairCallback;
import org.apache.cassandra.service.StorageProxy;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.LatencyTracker;
import org.apache.cassandra.utils.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.XMLDBException;

/**
 * CollectionStore 1. manage the metadata about the collections under this
 * collection 2. manage the native store , inverted index and btree index of xml
 * docs stored in this collection
 * 
 * TODO: what information should be stored in the catalog
 */
public class CollectionStore implements CollectionStoreMBean {
	public static final ByteBuffer TAGNAME = Int32Type.instance.fromString("1");
	public static final ByteBuffer NODETYPE = Int32Type.instance
			.fromString("2");
	public static final ByteBuffer CHILDNUM = Int32Type.instance
			.fromString("3");
	public static final ByteBuffer CHILDLIST = Int32Type.instance
			.fromString("4");
	// for element node, it is the text under it, for attribute node, it is
	// attribute value;
	public static final ByteBuffer VALUE = Int32Type.instance.fromString("5");
	public static final ByteBuffer VALUETYPE = Int32Type.instance
			.fromString("6");
	public static final ByteBuffer UPPERRANGE = Int32Type.instance
			.fromString("7");
	public static final ByteBuffer LOWERRANGE = Int32Type.instance
			.fromString("8");
	public static final ByteBuffer DEPTH = Int32Type.instance.fromString("9");
	public static final ByteBuffer PARENTID = Int32Type.instance
			.fromString("10");
	public static final ByteBuffer ATTRNUM = Int32Type.instance
			.fromString("11");
	public static final ByteBuffer ATTRLIST = Int32Type.instance
			.fromString("12");

	public static final AbstractType SPNAMETYPE = Int32Type.instance;
	public static final AbstractType COLKEYVALIDATOR = Int32Type.instance;

	public static final int ROOT_XML_NODEID = 0;
	// constants related by invert index
	public static final String NODETAGINDEX_NAME_SUFFIX = "nodetag_idx";
	public static final String BTREE_VALUE_SUFFIX = "btreeidx";
	public static final AbstractType INVINDEX_KEY_VALIDATOR = UTF8Type.instance;
	public final ColumnFamilyStoreProxy dataStore;
	private List<XPathParser> parserPool = new LinkedList<XPathParser>();
	// private List<XPathCompiler> compilerPool = new
	// LinkedList<XPathCompiler>();

	/**
	 * the following fields is used by jmx to monitor the performance of an
	 * instance of this class.
	 */
	private LatencyTracker retrieveLocalLatencyTracker = new LatencyTracker();
	private LatencyTracker assembleXMLLatencyTracker = new LatencyTracker();
	private LatencyTracker rtLocalCfGetSpLatencyTracker = new LatencyTracker();
	private LatencyTracker retrieveRemoteLatencyTracker = new LatencyTracker();
	private LatencyTracker xupdateLatencyTracker = new LatencyTracker();

	public static final Logger logger = LoggerFactory
			.getLogger(CollectionStore.class);

	// private CollectionMetaData metaData = null;
	private int colID = -1;
	private LocalCFStoreProxy cfStore = null;

	/*
	 * all text value in xml document will be indexed by btreeIndex. It can be
	 * used toevaluate equal, great than, small than,etc queries.
	 */

	/*
	 * all text value in xml document will be indexed by the invert index. It
	 * can only be used to evaluate contain function
	 */
	private XMLIndexManager xmlIndexManager = null;
	private XUpdateProcessor xupdateProcessor = null;

	private CacheManager cacheManager = new CacheManager();

	//private IXMLNodeFormatter defaultFormatter = new NormalXMLNodeFormatter(
	//		null);
	private IXMLNodeFormatter defaultFormatter = new IndentedXMLNodeFormatter(
			null);
	private DistributeXPathProcessor disXPathProc = null;

	// private IXMLNodeFormatter defaultFormatter = new
	// IndentedXMLNodeFormatter(null);
	public CollectionStore(CollectionMetaData metaData) {
		// this.metaData = metaData;
		this.colID = metaData.getColID();
		this.cfStore = new LocalCFStoreProxy(SysConfig.DEFAULT_KEYSPACE,
				metaData.getCFName());
		dataStore = new ColumnFamilyStoreProxy(SysConfig.DEFAULT_KEYSPACE,
				metaData.getCFName());
		this.xmlIndexManager = cfStore.getCfStore().getXMLIndexManager();
		xupdateProcessor = new XUpdateProcessor(this);
		// init the cache
		cacheManager.register(CacheType.LOCAL, new DomNodeCache(
				DatabaseDescriptor.getDomNodeCacheSize(),
				(int) (DatabaseDescriptor.getDomNodeCacheSize() * 1.5)));
		cacheManager.register(CacheType.SUBSEQ, new SubSequenceCache());
		init();

		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			mbs.registerMBean(this, new ObjectName(
					"imc.disxmldb:type=CollectionStore," + "colName="
							+ metaData.getName() + ",id=" + metaData.getColID()
							+ ",addr=" + FBUtilities.getBroadcastAddress()));
		} catch (Exception e) {
			// multiple creating of a collectionstore is permitted because of
			// multiple threads race to create
			// the same collection
			// throw new RuntimeException(e);
		}
		disXPathProc = new DistributeXPathProcessor(this);
	}

	private void init() {
	}

	public void close() {
		/*
		 * btreeIndex.close(); nodeTagIndex.close(); invertIndex.close();
		 */
		unRegister();
	}

	public void unRegister() {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			mbs.unregisterMBean(new ObjectName(
					"imc.disxmldb:type=CollectionStore," + "colName="
							+ getMetaData().getName() + ",id="
							+ getMetaData().getColID() + ",addr="
							+ FBUtilities.getBroadcastAddress()));
		} catch (Exception e) {
			// throw new RuntimeException(e);
		}
	}

	/*
	 * private ColumnFamilyStore createInvIndexCFStore(String indexName) {
	 * return ColumnFamilyStore.createColumnFamilyStore(
	 * Table.open(SysConfig.DEFAULT_KEYSPACE), indexName, new
	 * LocalPartitioner(INVINDEX_KEY_VALIDATOR),
	 * newInvIndexCFMetaData(SysConfig.DEFAULT_KEYSPACE, indexName)); }
	 */

	/*
	 * private CFMetaData newInvIndexCFMetaData(String keyspace, String
	 * indexName) { return new CFMetaData(keyspace, indexName,
	 * ColumnFamilyType.Super, Int32Type.instance, getInvIndexSubComp())
	 * .keyCacheSize(SysConfig.INVINDEXCF_KEYCACHE)
	 * .readRepairChance(SysConfig.INVINDEXCF_READ_REPAIR_CHANCE)
	 * .gcGraceSeconds(SysConfig.INVINDEX_GCGRACESECONDS)
	 * .minCompactionThreshold( SysConfig.INVINDEX_MINCOMPACTIONTHRESHOLD)
	 * .maxCompactionThreshold( SysConfig.INVINDEX_MAXCOMPACTIONTHRESHOLD); }
	 */
	public CompositeType getInvIndexSubComp() {
		List<AbstractType> types = new LinkedList<AbstractType>();
		types.add(Int32Type.instance);
		types.add(Int32Type.instance);
		CompositeType ret = null;
		try {
			ret = CompositeType.getInstance(types);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	public LocalCFStoreProxy getCfStore() {
		return cfStore;
	}

	public void setCfStore(LocalCFStoreProxy cfStore) {
		this.cfStore = cfStore;
	}

	public void registerBtreeIndex(ValueType type, IBtree btree) {

	}

	/**
	 * the xml must specify schema, or the system should throw exception
	 * 
	 * @param XMLMetaData
	 *            metadata of the xml doc to be stored
	 * @param xmlContent
	 *            the content of the xml
	 * @param overwrite
	 * @param created
	 * @param modified
	 * @throws XMLDBException
	 */
	public void storeXML(XMLMetaData metaData, String xmlContent)
			throws XMLDBException {
		XMLStore xmlStore = new XMLStore(metaData, this);
		xmlStore.storeXML(xmlContent);
	}

	/**
	 * split the xml and disseminate the parts
	 * 
	 * @param docName
	 * @param xml
	 * @param overwrite
	 * @param created
	 * @param modified
	 */
	public void SplitAndStoreXML(String docName, String xml, String schema,
			boolean overwrite, Date created, Date modified) {

	}

	/**
	 * remove the data of xml related to docName, including the data of xml
	 * parts the remove the metadata is handled by XMLDBCatalogManager
	 * 
	 * @param docName
	 * @throws XMLDBException
	 */
	public void removeXMLResource(String docName) throws XMLDBException {
		XMLMetaData xmlMeta = XMLDBCatalogManager.instance()
				.getXMLMetaDataByID(getMetaData().getColID(),
						getMetaData().getXMLID(docName));

		// we assume this case means the document has been deleted
		if (xmlMeta == null)
			return;

		List<RowMutation> rms = new LinkedList<RowMutation>();
		RowMutation mutation = new RowMutation(SysConfig.DEFAULT_KEYSPACE,
				Int32Type.instance.fromString(xmlMeta.getXMLID() + ""));
		mutation.delete(new QueryPath(getMetaData().getCFName(), null, null),
				System.currentTimeMillis());
		rms.add(mutation);
		if (xmlMeta.getSplitted() == 1) {
			for (Integer xmlDocID : xmlMeta.getXMLPartIDs()) {
				mutation = new RowMutation(SysConfig.DEFAULT_KEYSPACE,
						Int32Type.instance.fromString(xmlDocID.toString()));
				mutation.delete(new QueryPath(getMetaData().getCFName(), null,
						null), System.currentTimeMillis());
				rms.add(mutation);
			}
		}
		try {
			StorageProxy.mutate(rms, SysConfig.XMLSTORE_CONSISTENCY_LEVEL);
		} catch (UnavailableException e) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR, e.getMessage());
		} catch (TimeoutException e) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR, e.getMessage());
		}
	}

	public int XUpdate(byte[] command, boolean recursive) throws Exception {
		long start = System.currentTimeMillis();
		int ret = xupdateProcessor.xupdate(command, recursive);
		xupdateLatencyTracker.addMicro(System.currentTimeMillis() - start);
		return ret;
	}

	public XPathResult XPath(byte[] cmd_, byte flag_)
			throws UnavailableException, TimeoutException, IOException,
			InvalidRequestException,
			imc.disxmldb.xpath.xpathcompile.ParseException {
		return disXPathProc.XPathInternal(cmd_, flag_);
	}

	public XPathQueryCallback ExecXPathOnNode(XPathQueryCommand cmd_,
			AbstractBounds range_) throws UnavailableException {
		return disXPathProc.ExecXPathOnNode(cmd_, range_);
	}

	/**
	 * 
	 * @param command
	 * @return a list of (xmlDocID, nodeID) that satisfy the command
	 * @throws UnsupportedEncodingException
	 */
	public XPathResult XPathLocal(XPathQueryCommand command)
			throws UnsupportedEncodingException {
		// XPathProcessor proc = new XPathProcessor(command, this);
		Object xpathRet = null;
		XPathParser xpathParser = null;
		boolean shouldReUse = true;
		try {
			// xpathRet = proc.excuteXPath();
			// xpathRet = XPathProcessorV2.execXPath(command, this);
			while (true) {
				xpathParser = getXPathParserTool();
				if (xpathParser == null) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
					continue;
				}
				try {
					xpathRet = xpathParser.execXPath(command, this);
					break;
				} catch (IOException ex) {
				}
			}
			// 不是这里抛出的所有异常都以XPathException的方式返回给协作节点
		} catch (IMCXPathException e) {
			logger.error(e.getMessage());
			shouldReUse = xpathParser.reConnect();
			return new XPathResult(e.toString());
		} catch (ParseException e) {
			logger.error(e.getMessage());
			shouldReUse = xpathParser.reConnect();
			return new XPathResult(e.toString());
		} catch (Exception ex) {
			logger.error(ex.toString());
			shouldReUse = xpathParser.reConnect();
			return new XPathResult(XPathResultType.Null);
		} finally {
			if (shouldReUse)
				putXPathParser(xpathParser);
		}

		if (xpathRet == null)
			return new XPathResult(XPathResultType.Null);

		IndexQueryResult qRet = null;
		if (xpathRet instanceof IndexQueryResult)
			qRet = (IndexQueryResult) xpathRet;
		else {
			return (XPathResult) xpathRet;
		}
		XPathResult ret = null;
		// 在这里增加nodeonly的逻辑
		if (command.isReadFull()) {
			ret = new XPathResult(XPathResultType.xmlParts);
			((XMLDocXMLParts) ret.getResult()).addXMLParts(qRet);
		} else if (command.isReadNodeOnly()) {
			// 增加只读节点内容的接口
			ret = new XPathResult(XPathResultType.NodeSketch);
			((XMLNodeForMutations) ret.getResult()).addNodeSketch(qRet);
		} else {
			ret = new XPathResult(XPathResultType.NodeIDs);
			((XMLDocNodeIDs) ret.getResult()).addNodeID(qRet);
		}
		return ret;
	}

	public RowMutation modifyUpdateMutation(RowMutation rm) {
		// TODO 在这里将缓存失效？
		return null;
	}

	/**
	 * 
	 * @param xmlDocID
	 * @param nodeID
	 * @param text
	 * @return the number of nodes modified
	 * @throws TimeoutException
	 * @throws UnavailableException
	 */
	public int updateNodeText(Map<Integer, List<XMLNodeForMutation>> nodes,
			String text) throws UnavailableException, TimeoutException {
		List<RowMutation> mutations = new LinkedList<RowMutation>();
		ValueType newType = TypeResolver.resolve(text);
		ByteBuffer newValue = ValueType.getValidator(newType).fromString(text);
		for (Entry<Integer, List<XMLNodeForMutation>> entry : nodes.entrySet()) {
			int xmlDocID = entry.getKey();

			ColumnFamily cf = ColumnFamily.create(SysConfig.DEFAULT_KEYSPACE,
					getMetaData().getCFName());
			for (XMLNodeForMutation node : entry.getValue()) {
				node.valueType = newType;
				node.value = newValue.duplicate();
				node.udpate(cf);
			}
			String key = compositeColKey(getMetaData().getColID(), xmlDocID);
			RowMutation mutation = new RowMutation(SysConfig.DEFAULT_KEYSPACE,
					COLKEYVALIDATOR.fromString(key));
			mutation.setCommand(XUpdateCommand.update);
			mutation.setColID(getMetaData().getColID());
			mutation.add(cf);
			mutations.add(mutation);
		}
		List<Integer> counts = new LinkedList<Integer>();
		StorageProxy.mutate(mutations, SysConfig.XMLSTORE_CONSISTENCY_LEVEL,
				counts);
		int sum = 0;
		for (Integer count : counts)
			sum += count;
		return sum;
	}

	/**
	 * retrieve text only from local store
	 * 
	 * @param xmlDocID
	 * @param nodeID
	 * @return
	 */
	public String textLocal(int xmlDocID, int nodeID) {
		XMLNode node = readCache(CacheType.LOCAL, xmlDocID, nodeID);
		if (node != null)
			return node.asXMLValue();
		node = fetchNodeCacheLocal(xmlDocID, nodeID);
		if (node == null)
			return null;

		return node.asXMLValue();
	}

	/**
	 * recursively construct the xml tree of node represented by the proxy node
	 * proxy may not be filled if it doesn't exist in the disk, which may be
	 * caused by the latency between the insert of parent and child nodes
	 * 
	 * @param xmlDocID
	 * @param nodeID
	 * @param proxy
	 * @return
	 */
	private void fullFillNodeProxy(NodeProxy proxy) {
		XMLNode fillNode = fetchNodeCacheLocal(proxy.getXMLID(),
				proxy.getNodeID());
		if (fillNode != null) {
			proxy.fillNode(fillNode);

			if (fillNode instanceof ElementNode) {
				for (XMLNode node : ((ElementNode) fillNode)
						.getAttributeNodes()) {
					fullFillNodeProxy((NodeProxy) node);
				}

				for (XMLNode node : ((ElementNode) fillNode).getChilds()) {
					fullFillNodeProxy((NodeProxy) node);
				}
			}
		} else {
			// TODO maybe we should delete this node
		}
	}

	/**
	 * only retrieve local columnfamily, returns the xml representation of the
	 * nodeID, including the children
	 * 
	 * @param xmlDocID
	 * @param nodeID
	 * @return
	 */
	public String retrieveLocal(int xmlDocID, int nodeID) {
		NodeProxy proxy = new NodeProxy(xmlDocID, nodeID, NodeProxyType.local);
		fullFillNodeProxy(proxy);
		if (proxy.isLoaded() == false)
			return "";
		StringOutputStream sos = new StringOutputStream();
		try {
			defaultFormatter.format(proxy, sos);
		} catch (IOException e) {
			logger.error(e.toString());
		}
		proxy.unloadNode();
		return sos.toString();
	}

	/**
	 * only retrieve the node
	 * 
	 * @param xmlID
	 * @param nodeID
	 * @return
	 */
	public NodeProxy retrieveXMLNodeLocal(int xmlID, int nodeID) {
		NodeProxy ret = new NodeProxy(xmlID, nodeID, NodeProxyType.local);

		XMLNode fillNode = fetchNodeCacheLocal(xmlID, nodeID);
		if (fillNode != null) {
			ret.fillNode(fillNode);
			return ret;
		} else {
			CFMetaData cfMeta = cfStore.getCfStore().metadata;
			Row row = cfStore.get(
					cfMeta.getKeyValidator()
							.fromString(Integer.toString(xmlID)),
					cfMeta.getComparatorFor(null).fromString(
							Integer.toString(nodeID)), null);
			if (row.cf == null)
				return ret;
			fillNode = fetchNodeCacheLocal(xmlID, nodeID, row);
			ret.fillNode(fillNode);
			return ret;
		}
	}

	/**
	 * retrieve the text value of the node provided by the map
	 * 
	 * @param xmlNodes
	 * @return
	 * @throws IOException
	 * @throws UnavailableException
	 * @throws TimeoutException
	 * @throws InvalidRequestException
	 */
	public Map<Integer, List<String>> textRemote(
			Map<Integer, List<Integer>> xmlNodes) throws IOException,
			UnavailableException, TimeoutException, InvalidRequestException {
		List<ReadCommand> commands = new LinkedList<ReadCommand>();
		CFMetaData cfMeta = cfStore.getCfStore().metadata;
		List<ByteBuffer> columns = new LinkedList<ByteBuffer>();
		columns.add(CollectionStore.VALUE);
		columns.add(CollectionStore.VALUETYPE);

		for (Entry<Integer, List<Integer>> entry : xmlNodes.entrySet()) {
			for (Integer nodeID : entry.getValue()) {
				ByteBuffer spName = cfMeta.getComparatorFor(null).fromString(
						nodeID.toString());
				SliceByNamesReadCommand command = new SliceByNamesReadCommand(
						SysConfig.DEFAULT_KEYSPACE, cfMeta.getKeyValidator()
								.fromString(entry.getKey().toString()),
						new QueryPath(cfMeta.cfName, spName, null), columns);
				commands.add(command);
			}
		}
		List<Row> rows = StorageProxy.read(commands,
				SysConfig.XMLREAD_CONSISTENCY_LEVEL);

		Map<Integer, List<String>> ret = new TreeMap<Integer, List<String>>();
		IColumn subCol = null;
		for (Row row : rows) {
			if (row.cf == null)
				continue;
			List<String> texts = null;
			Integer xmlID = Integer.parseInt(cfMeta.getKeyValidator()
					.getString(row.key.key));
			texts = ret.get(xmlID);
			if (texts == null) {
				texts = new LinkedList<String>();
				ret.put(xmlID, texts);
			}

			for (IColumn spCol : row.cf.getSortedColumns()) {
				int valueTypeIndex = 0;
				if ((subCol = spCol.getSubColumn(CollectionStore.VALUETYPE)) != null) {
					valueTypeIndex = ByteBufferUtil.toInt(subCol.value()
							.duplicate());
				} else {
					continue;
				}

				if ((subCol = spCol.getSubColumn(CollectionStore.VALUE)) != null
						&& !subCol.value().equals(
								ByteBufferUtil.EMPTY_BYTE_BUFFER)) {
					ByteBuffer byteBuffer = subCol.value().duplicate();
					texts.add(ValueType.getValidator(valueTypeIndex).getString(
							byteBuffer));
				}
			}
		}
		return ret;
	}

	/**
	 * when providing the xmlDoc and nodeIDs, retrieve the contents of these
	 * from all the replicas
	 * 
	 * @param xmlNodes
	 * @return
	 * @throws IOException
	 * @throws UnavailableException
	 * @throws TimeoutException
	 * @throws InvalidRequestException
	 */
	public HashMap<Integer, List<XMLNode>> retrieveXMLNode(
			HashMap<Integer, List<Integer>> xmlNodes) throws IOException,
			UnavailableException, TimeoutException, InvalidRequestException {
		List<ReadCommand> commands = new LinkedList<ReadCommand>();
		CFMetaData cfMeta = cfStore.getCfStore().metadata;
		for (Entry<Integer, List<Integer>> entry : xmlNodes.entrySet()) {
			List<ByteBuffer> spNames = new LinkedList<ByteBuffer>();
			for (Integer nodeID : entry.getValue()) {
				spNames.add(cfMeta.getComparatorFor(null).fromString(
						nodeID.toString()));
			}
			SliceByNamesReadCommand command = new SliceByNamesReadCommand(
					SysConfig.DEFAULT_KEYSPACE, cfMeta.getKeyValidator()
							.fromString(entry.getKey().toString()),
					new QueryPath(cfMeta.cfName, null, null), spNames);
			commands.add(command);
		}
		List<Row> rows = StorageProxy.read(commands,
				SysConfig.XMLREAD_CONSISTENCY_LEVEL);

		HashMap<Integer, List<XMLNode>> ret = new HashMap<Integer, List<XMLNode>>();

		for (Row row : rows) {
			if (row.cf == null)
				continue;
			List<XMLNode> nodes = new LinkedList<XMLNode>();
			Integer xmlID = Integer.parseInt(cfMeta.getKeyValidator()
					.getString(row.key.key));
			ret.put(xmlID, nodes);

			for (IColumn spCol : row.cf.getSortedColumns()) {
				nodes.add(XMLNodeImpl.serializer().deserialize(xmlID,
						(SuperColumn) spCol));
			}
		}

		return ret;
	}

	/**
	 * 从本地读取XML节点，返回的节点的部分sketch
	 * 
	 * @param nodes
	 * @return
	 */
	public Map<Integer, List<XMLNodeForMutation>> retrieveNodeSketch(
			Map<Integer, List<Integer>> nodes) {
		Map<Integer, List<XMLNodeForMutation>> ret = new HashMap<Integer, List<XMLNodeForMutation>>();
		for (Entry<Integer, List<Integer>> entry : nodes.entrySet()) {
			int XMLID = entry.getKey();
			List<XMLNodeForMutation> list = new LinkedList<XMLNodeForMutation>();
			for (Integer nodeID : entry.getValue()) {
				XMLNode node = fetchNodeCacheLocal(XMLID, nodeID);
				if (node != null)
					list.add(node.sketch());
			}
		}
		return ret;
	}

	public XMLNodeForMutation retrieveNodeSketch(int XMLID, int nodeID) {
		XMLNode node = fetchNodeCacheLocal(XMLID, nodeID);
		if (node != null)
			return node.sketch();
		else
			return null;
	}

	/**
	 * retrieve all the replicas in according to the consistency_level
	 * 
	 * @param nodes
	 *            a list of <xmlID, nodeID>
	 * @return
	 * @throws UnavailableException
	 * @throws TimeoutException
	 */
	public List<String> retrieve(List<Pair<Integer, Integer>> nodes)
			throws UnavailableException, TimeoutException {
		List<XMLReadNodeCommand> commands = new LinkedList<XMLReadNodeCommand>();
		for (Pair<Integer, Integer> node : nodes) {
			XMLReadNodeCommand command = new XMLReadNodeCommand(getMetaData()
					.getColID(), node.left, node.right, (byte) 0);
			commands.add(command);
		}
		long start = System.currentTimeMillis();
		List<String> ret = readXMLNodeFull(commands,
				SysConfig.XMLREAD_CONSISTENCY_LEVEL);
		retrieveRemoteLatencyTracker.addMicro(System.currentTimeMillis()
				- start);
		return ret;
	}

	/**
	 * this function should take the replication strategy and read repair
	 * strategy into acount
	 * 
	 * @param commands
	 * @param consistency_level
	 * @return
	 * @throws UnavailableException
	 * @throws TimeoutException
	 */
	public static List<String> readXMLNodeFull(
			List<XMLReadNodeCommand> initialCommands,
			ConsistencyLevel consistency_level) throws UnavailableException,
			TimeoutException {
		List<String> ret = new LinkedList<String>();
		List<XMLReadNodeCommand> retryCommands = new LinkedList<XMLReadNodeCommand>();
		do {
			List<XMLReadNodeCommand> commands = retryCommands.isEmpty() ? initialCommands
					: retryCommands;

			XMLReadNodeCallback[] callbacks = new XMLReadNodeCallback[commands
					.size()];
			for (int i = 0; i < commands.size(); i++) {
				XMLReadNodeCommand command = commands.get(i);

				assert !(command.getIsDigest() == XMLReadNodeCommand.DIGEST);
				logger.debug("Command/ConsistencyLevel is {}/{}", command,
						consistency_level);

				List<InetAddress> endpoints = StorageService.instance
						.getLiveNaturalEndpoints(SysConfig.DEFAULT_KEYSPACE,
								command.getKey());
				DatabaseDescriptor.getEndpointSnitch().sortByProximity(
						FBUtilities.getBroadcastAddress(), endpoints);

				XMLNodeDigestResolver resolver = new XMLNodeDigestResolver(
						command.getColID(), command.getXmlID(),
						command.getNodeID());
				XMLReadNodeCallback callback = getXMLReadNodeCallBack(resolver,
						SysConfig.XMLREAD_CONSISTENCY_LEVEL, command, endpoints);
				callback.assureSufficientLiveNodes();
				assert !callback.endpoints.isEmpty();
				callbacks[i] = callback;

				// The data-request message is sent to dataPoint, the node that
				// will actually get the data for us
				InetAddress dataPoint = callback.endpoints.get(0);
				if (dataPoint.equals(FBUtilities.getBroadcastAddress())) {
					logger.debug("reading data locally");
					StageManager.getStage(Stage.XML_READ_NODE).execute(
							new LocalXMLReadRunnable(command, callback));
				} else {
					logger.debug("reading data from {}", dataPoint);
					MessagingService.instance().sendRR(command, dataPoint,
							callback);
				}

				if (callback.endpoints.size() == 1)
					continue;

				// send the other endpoints a digest request
				XMLReadNodeCommand digestCommand = command.copy();
				digestCommand.setIsDigest(XMLReadNodeCommand.DIGEST);
				MessageProducer producer = null;
				for (InetAddress digestPoint : callback.endpoints.subList(1,
						callback.endpoints.size())) {
					if (digestPoint.equals(FBUtilities.getBroadcastAddress())) {
						logger.debug("reading digest locally");
						StageManager.getStage(Stage.XML_READ_NODE).execute(
								new LocalXMLReadRunnable(digestCommand,
										callback));
					} else {
						logger.debug("reading digest from {}", digestPoint);
						// (We lazy-construct the digest Message object since it
						// may not be necessary if we
						// are doing a local digest read, or no digest reads at
						// all.)
						if (producer == null)
							producer = new CachingMessageProducer(digestCommand);
						MessagingService.instance().sendRR(producer,
								digestPoint, callback);
					}
				}
			}

			for (int i = 0; i < commands.size(); i++) {
				XMLReadNodeCallback handler = callbacks[i];
				try {
					long startTime2 = System.currentTimeMillis();
					String xmlConent = handler.get();
					if (xmlConent != null)
						ret.add(xmlConent);

					if (logger.isDebugEnabled())
						logger.debug("Read: "
								+ (System.currentTimeMillis() - startTime2)
								+ " ms.");
				} catch (TimeoutException ex) {
					if (logger.isDebugEnabled())
						logger.debug("Read timeout: {}", ex.toString());
					throw ex;
				} catch (DigestMismatchException ex) {
					if (logger.isDebugEnabled())
						logger.debug("Digest mismatch: {}", ex.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (retryCommands.size() != 0)
				retryCommands.clear();
			// TODO read results from these mismatched nodes
		} while (!retryCommands.isEmpty());
		return ret;
	}

	private static XMLReadNodeCallback getXMLReadNodeCallBack(
			XMLNodeDigestResolver resolver, ConsistencyLevel consistencyLevel,
			XMLReadNodeCommand command, List<InetAddress> endpoints) {
		return new XMLReadNodeCallback(resolver, consistencyLevel, command,
				endpoints);
	}

	public int nextDocID() {
		// TODO
		return 0;
	}

	public static AbstractType getColKeyValidator() {
		return COLKEYVALIDATOR;
	}

	/**
	 * invoking this methods will delete the xmlnode from all replicas lock
	 * mechanism: when delete a node of a xml document, the meta data of the
	 * document is retrieved first, then we try to gain a lock on it. only when
	 * it successes, we can delete the node
	 * 
	 * @param xmlNodeIDsPairs
	 *            xml id to node ids list map
	 * @throws TimeoutException
	 * @throws UnavailableException
	 */
	public int deleteNodeRemote(
			Map<Integer, List<XMLNodeForMutation>> xmlNodesPair)
			throws UnavailableException, TimeoutException {
		List<RowMutation> mutations = new LinkedList<RowMutation>();

		List<XMLMetaData> metaList = new ArrayList<XMLMetaData>();
		List<Integer> mutateCounts = new ArrayList<Integer>();
		try {
			for (Entry<Integer, List<XMLNodeForMutation>> entry : xmlNodesPair
					.entrySet()) {
				int xmlID = entry.getKey();
				XMLMetaData metaTemp = XMLDBCatalogManager.instance()
						.getXMLMetaDataByID(getMetaData().getColID(), xmlID);

				if (lockXMLDoc(metaTemp) == false)
					continue;
				metaList.add(metaTemp);
				String key = compositeColKey(getMetaData().getColID(), xmlID);
				RowMutation mutation = new RowMutation(
						SysConfig.DEFAULT_KEYSPACE,
						COLKEYVALIDATOR.fromString(key));

				for (XMLNodeForMutation node : entry.getValue()) {
					node.delete(SysConfig.DEFAULT_KEYSPACE, getMetaData()
							.getCFName(), mutation);
				}
				mutation.setColID(getMetaData().getColID());
				mutation.setCommand(XUpdateCommand.delete);
				mutations.add(mutation);
			}

			StorageProxy.mutate(mutations,
					SysConfig.XMLSTORE_CONSISTENCY_LEVEL, mutateCounts);
			int ret = 0;
			// update the metadata about the number of deleted nodes
			for (int i = 0; i < metaList.size(); i++) {
				metaList.get(i).decreXMLNodeNum(mutateCounts.get(i));
				ret += mutateCounts.get(i);
			}
			return ret;
		} finally {
			unlockXMLDoc(metaList);
		}
	}

	public void collectDeleteNodes(NodeProxy proxy, Stack<Integer> deleteNodes) {
		if (!proxy.isLoaded()) {
			XMLNode node = fetchNodeCacheLocal(proxy.getXMLID(),
					proxy.getNodeID());
			if (node != null)
				proxy.fillNode(node);
		}

		if (proxy.isLoaded() && proxy.getNodeType() == XMLNode.ELEMENTNODE) {
			for (XMLNode node : proxy.getAttributeNodes()) {
				collectDeleteNodes((NodeProxy) node, deleteNodes);
			}

			for (XMLNode node : proxy.getChilds()) {
				collectDeleteNodes((NodeProxy) node, deleteNodes);
			}
		}
		deleteNodes.add(proxy.getNodeID());
	}

	public int deleteNodeLocal(int xmlID, int nodeID, long timeStamp)
			throws IOException {
		Stack<Integer> nodeStack = new Stack<Integer>();
		NodeProxy nodeProxy = new NodeProxy(xmlID, nodeID, NodeProxyType.local);
		collectDeleteNodes(nodeProxy, nodeStack);

		// delete the node and its child nodes
		int ret = nodeStack.size();
		RowMutation mutation = new RowMutation(SysConfig.DEFAULT_KEYSPACE,
				COLKEYVALIDATOR.fromString(Integer.toString(xmlID)));
		while (!nodeStack.isEmpty()) {
			mutation.delete(
					new QueryPath(getMetaData().getCFName(), SPNAMETYPE
							.fromString(nodeStack.pop().toString()), null),
					timeStamp);
		}
		mutation.setCommand(XUpdateCommand.delete);
		mutation.apply();

		// update the parent node of nodeID
		mutation = new RowMutation(SysConfig.DEFAULT_KEYSPACE,
				COLKEYVALIDATOR.fromString(Integer.toString(xmlID)));

		NodeProxy pNode = (NodeProxy) nodeProxy.getParent();

		pNode.fillNode(fetchNodeCacheLocal(pNode.getXMLID(), pNode.getNodeID()));

		if (nodeProxy.getNodeType() == XMLNodeImpl.ELEMENTNODE)
			pNode.deleteChild(nodeProxy);
		else
			pNode.deleteAttribute(nodeProxy);

		CFMetaData cfMeta = cfStore.getCfStore().metadata;
		mutation.add(XMLNodeImpl.serializer().serialize(
				SysConfig.DEFAULT_KEYSPACE, cfMeta.cfName, (XMLNode) pNode));

		mutation.setCommand(XUpdateCommand.update);
		mutation.apply();
		pNode.unloadNodeSelf();

		// update the cache
		nodeProxy.unloadNode();
		for (Integer delNodeID : nodeStack) {
			invalidateCache(CacheType.LOCAL, xmlID, delNodeID);
		}
		return ret;
	}

	/**
	 * the super column schema: super column name: nodeID. sub column: column
	 * name:
	 * 
	 * @param node
	 * @throws TimeoutException
	 * @throws UnavailableException
	 */
	public void storeElementNode(XMLMetaData xmlMeta, ElementNode node)
			throws UnavailableException, TimeoutException {
		RowMutation mutation = ElementNodeMutation(xmlMeta, node, null);
		StorageProxy.mutate(Arrays.asList(mutation),
				SysConfig.XMLSTORE_CONSISTENCY_LEVEL);
	}

	public RowMutation ElementNodeMutation(XMLMetaData xmlMeta,
			ElementNode node, RowMutation mutation) {
		ColumnFamily cf = XMLNodeImpl.serializer().serialize(
				SysConfig.DEFAULT_KEYSPACE, getMetaData().getCFName(),
				(XMLNode) node);

		String key = compositeColKey(xmlMeta.getColID(), xmlMeta.getXMLID());
		if (mutation == null)
			mutation = new RowMutation(SysConfig.DEFAULT_KEYSPACE,
					COLKEYVALIDATOR.fromString(key));
		ColumnFamily temp = mutation.getColumnFamily(cf.id());
		if (temp != null)
			temp.resolve(cf);
		else {
			mutation.add(cf);
			mutation.setColID(getMetaData().getColID());
			mutation.setCommand(XUpdateCommand.store);
		}
		return mutation;
	}

	public RowMutation AttribNodeMutation(XMLMetaData xmlMeta, XMLNode node,
			RowMutation mutation) {
		ColumnFamily cf = XMLNodeImpl.serializer().serialize(
				SysConfig.DEFAULT_KEYSPACE, getMetaData().getCFName(), node);

		String key = compositeColKey(xmlMeta.getColID(), xmlMeta.getXMLID());
		if (mutation == null)
			mutation = new RowMutation(SysConfig.DEFAULT_KEYSPACE,
					COLKEYVALIDATOR.fromString(key));
		ColumnFamily temp = mutation.getColumnFamily(cf.id());
		if (temp != null)
			temp.resolve(cf);
		else {
			mutation.add(cf);
			mutation.setColID(getMetaData().getColID());
			mutation.setCommand(XUpdateCommand.store);
		}
		return mutation;
	}

	public void storeAttribNode(XMLMetaData xmlMeta, AttributeNode node)
			throws UnavailableException, TimeoutException {
		RowMutation mutation = AttribNodeMutation(xmlMeta, (XMLNode) node, null);
		StorageProxy.mutate(Arrays.asList(mutation),
				SysConfig.XMLSTORE_CONSISTENCY_LEVEL);
	}

	public static String compositeColKey(int colID, int xmlDocID) {
		return Integer.toString(xmlDocID);
	}

	static <T> ReadCallback<T> getReadCallback(IResponseResolver<T> resolver,
			IReadCommand command, ConsistencyLevel consistencyLevel,
			List<InetAddress> endpoints) {
		if (consistencyLevel == ConsistencyLevel.LOCAL_QUORUM
				|| consistencyLevel == ConsistencyLevel.EACH_QUORUM) {
			return new DatacenterReadCallback<T>(resolver, consistencyLevel,
					command, endpoints);
		}
		return new ReadCallback<T>(resolver, consistencyLevel, command,
				endpoints);
	}

	public XMLNode fetchNodeCacheLocal(int XMLID, int nodeID) {
		XMLNode ret = fetchNodeCacheLocal(XMLID, nodeID, null);
		if (ret == null) {
			ByteBuffer key = Int32Type.instance.fromString(Integer
					.toString(XMLID));
			ByteBuffer spName = Int32Type.instance.fromString(Integer
					.toString(nodeID));

			return fetchNodeCacheLocal(XMLID, nodeID,
					cfStore.get(key, spName, null));
		} else {
			return ret;
		}
	}

	/**
	 * read the node from the cache. if the node is not in the cache, read it
	 * from the disk.
	 * 
	 * @param XMLID
	 * @param nodeID
	 * @param row
	 * @return null if the node doesn't exist in cache
	 */
	public XMLNode fetchNodeCacheLocal(int XMLID, int nodeID, Row row) {
		XMLNode ret = readCache(CacheType.LOCAL, XMLID, nodeID);
		if (ret == null && row != null && row.cf != null) {
			ByteBuffer spName = Int32Type.instance.fromString(Integer
					.toString(nodeID));

			IColumn spCol = row.cf.getColumn(spName);
			if (spCol == null)
				return null;
			ret = XMLNodeImpl.serializer().deserialize(XMLID,
					(SuperColumn) spCol);
			updateCache(CacheType.LOCAL, XMLID, nodeID, ret);
		}
		return ret;
	}

	public void updateCache(CacheType type, int XMLID, int nodeID, XMLNode node) {
		ICache<Pair<Integer, Integer>, XMLNode> cache = cacheManager.get(type);
		if (cache != null) {
			cache.put(new Pair<Integer, Integer>(XMLID, nodeID), node);
		}
	}

	public void invalidateCache(CacheType type, int XMLID, int nodeID) {
		ICache<Pair<Integer, Integer>, XMLNode> cache = cacheManager.get(type);
		if (cache != null) {
			cache.remove(new Pair<Integer, Integer>(XMLID, nodeID));
		}
	}

	public XMLNode readCache(CacheType type, int XMLID, int nodeID) {
		ICache<Pair<Integer, Integer>, XMLNode> cache = cacheManager.get(type);
		if (cache == null)
			return null;

		return cache.get(new Pair<Integer, Integer>(XMLID, nodeID));
	}

	public ICache getCache(CacheType cacheType_) {
		return cacheManager.get(cacheType_);
	}

	public InvertIndexDecorator getInvertIndex(boolean recursive) {
		InvertIndexDecorator ret = new InvertIndexDecorator();
		if (recursive) {
			getInvertIndex(ret);
		} else {
			ret.addInvertIndex(getMetaData().getColID(),
					xmlIndexManager.getTextInvertIndex());
		}
		return ret;
	}

	private void getInvertIndex(InvertIndexDecorator decorator) {
		decorator.addInvertIndex(getMetaData().getColID(),
				xmlIndexManager.getTextInvertIndex());
		for (String childColName : getMetaData().getChildColNames()) {
			try {
				CollectionStore colStore = XMLDBStore.instance().getCollection(
						getMetaData().getCollectionID(childColName));
				colStore.getInvertIndex(decorator);
			} catch (XMLDBException e) {
				e.printStackTrace();
			}
		}
	}

	public void setInvertIndex(IInvertIndex invertIndex) {
		throw new RuntimeException("not implemented");
	}

	public BtreeDecorator getBtreeIndex(ValueType valueType, boolean recursive) {
		BtreeDecorator ret = new BtreeDecorator();
		if (recursive) {
			getBtreeIndex(valueType, ret);
		} else {
			if (xmlIndexManager.getBtreeIndex(valueType) != null)
				ret.addBtree(getMetaData().getColID(),
						xmlIndexManager.getBtreeIndex(valueType));
		}
		return ret;
	}

	/**
	 * recursively add the child getBtreeIndex into the decorator
	 * 
	 * @param decorator
	 */
	private void getBtreeIndex(ValueType valueType, BtreeDecorator decorator) {
		if (xmlIndexManager.getBtreeIndex(valueType) != null)
			decorator.addBtree(getMetaData().getColID(),
					xmlIndexManager.getBtreeIndex(valueType));
		for (String childColName : getMetaData().getChildColNames()) {
			try {
				CollectionStore colStore = XMLDBStore.instance().getCollection(
						getMetaData().getCollectionID(childColName));
				if (colStore == null)
					continue;
				colStore.getBtreeIndex(valueType, decorator);
			} catch (XMLDBException e) {
				e.printStackTrace();
			}
		}
	}

	public CollectionMetaData getMetaData() {
		return XMLDBCatalogManager.instance().getCollectionMeta(colID);
	}

	public void setMetaData(CollectionMetaData metaData) {
		this.colID = metaData.getColID();
		// this.metaData = metaData;
	}

	// TODO(xiafan): the following three lock and unlock methods should be
	// placed into a seperate class
	// which is responsible for managing the lock
	/**
	 * put a lock on the xml document
	 * 
	 * @param docID
	 * @return
	 */
	boolean lockXMLDoc(XMLMetaData metaTemp) {
		if (metaTemp == null)
			return false;
		int retryCount = 0;
		int lockTime = 0;
		do {
			retryCount++;
			lockTime = new Random().nextInt();
			lockTime = Math.abs(lockTime) % SysConfig.XML_TRYLOCK_TIME;

			try {
				if (metaTemp.lock.tryLock(lockTime, TimeUnit.MILLISECONDS))
					break;
			} catch (InterruptedException e) {
				// TODO just continue to the next iteration
			}
		} while (retryCount < SysConfig.FAILURE_RETRY);

		if (retryCount == SysConfig.FAILURE_RETRY)
			return false;
		return true;
	}

	void unlockXMLDoc(XMLMetaData metaTemp) {
		metaTemp.lock.unlock();
	}

	void unlockXMLDoc(List<XMLMetaData> docs) {
		for (XMLMetaData doc : docs)
			doc.lock.unlock();
	}

	public static final int IDLE_PARSERS = 1000;
	public static final int MAX_PARSERS = 10000;
	private int parserNum = 0;

	public synchronized XPathParser getXPathParserTool() {
		if (parserPool.size() == 0) {
			if (parserNum > MAX_PARSERS)
				return null;
			parserNum++;
			XPathParser ret = new XPathParser(System.in, SysConfig.ENCODING);
			if (ret.connect())
				return ret;
			else
				return null;
		} else {
			XPathParser ret = parserPool.remove(0);
			XPathParser parser = null;
			while (parserPool.size() > IDLE_PARSERS) {
				parser = parserPool.remove(0);
				try {
					parser.close();
				} catch (IOException e) {
					parserPool.add(parser);
					break;
				}
			}
			return ret;
		}
	}

	public synchronized void putXPathParser(XPathParser parser) {
		if (parserPool.size() > IDLE_PARSERS) {
			parserNum--;
			try {
				parser.close();
			} catch (IOException e) {
				// TODO 这样处理是否正确？
			}
		} else {
			parserPool.add(parser);
		}
	}

	@Override
	public double getAvgRetrieveLocalLatency() {
		long n = retrieveLocalLatencyTracker.getOpCount();
		long latency = retrieveLocalLatencyTracker.getTotalLatencyMicros();
		if (n == 0)
			return 0;
		return (float) latency / (float) n;
	}

	@Override
	public double getAvgAssembleXMLLatency() {
		long n = assembleXMLLatencyTracker.getOpCount();
		long latency = assembleXMLLatencyTracker.getTotalLatencyMicros();
		if (n == 0)
			return 0;
		return (float) latency / (float) n;
	}

	@Override
	public double getAvgRtLocalLatency() {
		long n = rtLocalCfGetSpLatencyTracker.getOpCount();
		long latency = rtLocalCfGetSpLatencyTracker.getTotalLatencyMicros();
		if (n == 0)
			return 0;
		return (float) latency / (float) n;
	}

	@Override
	public double getAvgRetrieveRemoteLatency() {
		long n = retrieveRemoteLatencyTracker.getOpCount();
		long latency = retrieveRemoteLatencyTracker.getTotalLatencyMicros();
		if (n == 0)
			return 0;
		return (float) latency / (float) n;
	}

	@Override
	public double getAvgXupdateLatency() {
		long n = xupdateLatencyTracker.getOpCount();
		long latency = xupdateLatencyTracker.getTotalLatencyMicros();
		if (n == 0)
			return 0;
		return (float) latency / (float) n;
	}

	@Override
	public long getTotalXupdateLatency() {
		return xupdateLatencyTracker.getTotalLatencyMicros();
	}
}
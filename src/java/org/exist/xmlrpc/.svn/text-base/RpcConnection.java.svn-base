package org.exist.xmlrpc;

import org.apache.cassandra.config.Config;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.cassandra.utils.LatencyTracker;
import org.apache.cassandra.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.XMLDBException;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.XMLDBStore;
import imc.disxmldb.cassandra.verbhandler.XPathQueryCommand;
import imc.disxmldb.cassandra.verbhandler.XPathResult;
import imc.disxmldb.cassandra.verbhandler.XPathResult.XPathResultType;
import imc.disxmldb.config.CollectionMetaData;
import imc.disxmldb.config.SysConfig;
import imc.disxmldb.config.XMLDBCatalogManager;
import imc.disxmldb.config.XMLMetaData;
import imc.disxmldb.config.XMLSchemaMetaData;
import imc.disxmldb.index.invertindex.CFInvertIndex;
import imc.disxmldb.index.invertindex.IInvertIndex;
import imc.disxmldb.security.Permission;
import imc.disxmldb.security.PermissionDeniedException;
import imc.disxmldb.security.User;
import imc.disxmldb.security.XMLDBSecurityManager;
import imc.disxmldb.util.CassandraSchemaProxy;

import org.exist.xmldb.XmldbURI;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;
import java.util.zip.DeflaterOutputStream;

public class RpcConnection implements RpcAPI, RpcConnectionMBean {

	private static Logger logger = LoggerFactory.getLogger(RpcConnection.class);

	private final static int MAX_DOWNLOAD_CHUNK_SIZE = 0x40000;

	private final static String DEFAULT_ENCODING = SysConfig.ENCODING;

	private static final LatencyTracker getDocumentLatencyTracker = new LatencyTracker();

	static {
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		try {
			server.registerMBean(new RpcConnection(null, null), new ObjectName(
					"org.exist.xmlrpc:type=RpcConnection"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected XmldbRequestProcessorFactory factory;

	protected User user;

	/**
	 * Creates a new <code>RpcConnection</code> instance.
	 * 
	 * @exception XMLDBException
	 *                if an error occurs
	 */
	public RpcConnection(XmldbRequestProcessorFactory factory, User user) {
		super();
		this.factory = factory;
		this.user = user;
	}

	public boolean shutdown() throws PermissionDeniedException {
		return shutdown(0);
	}

	public boolean shutdown(long delay) throws PermissionDeniedException {
		if (!user.hasDbaRole())
			throw new PermissionDeniedException("not allowed to shut down"
					+ "the database");
		if (delay > 0) {
			TimerTask task = new TimerTask() {
				public void run() {
					// TODO stop the database
				}
			};
			Timer timer = new Timer();
			timer.schedule(task, delay);
		} else {
			// TODO stop the database
		}
		return true;
	}

	/**
	 * write the cache to disk
	 */
	@Override
	public boolean sync() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isXACMLEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte[] getDocument(String name, String encoding, int prettyPrint)
			throws XMLDBException {
		return getDocument(name, encoding, prettyPrint, null);
	}

	@Override
	public byte[] getDocument(String name, String encoding, int prettyPrint,
			String stylesheet) throws XMLDBException {
		XmldbURI uri;
		long startTime = System.currentTimeMillis();
		try {
			uri = XmldbURI.xmldbUriFor(name);
			String colPath = uri.removeLastSegment().getCollectionPath();
			XMLMetaData metaData = XMLDBCatalogManager.instance()
					.getXMLMetaData(colPath, uri.lastSegment().toString());
			if (metaData == null) {
				throw new URISyntaxException(name, "doesn't exist");
			}
			CollectionStore colStore = XMLDBStore.instance().getCollection(
					colPath);
			List<Pair<Integer, Integer>> nodes = new LinkedList<Pair<Integer, Integer>>();
			nodes.add(new Pair<Integer, Integer>(metaData.getXMLID(),
					CollectionStore.ROOT_XML_NODEID));
			List<String> ret = colStore.retrieve(nodes);
			if (ret != null && ret.size() > 0)
				return ret.get(0).getBytes(encoding);
			else
				return new byte[0];
		} catch (URISyntaxException e) {
			throw new XMLDBException(ErrorCodes.INVALID_URI, name);
		} catch (UnsupportedEncodingException e) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR,
					"encoding is not supported");
		} catch (UnavailableException e) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR,
					"no data node is available");
		} catch (TimeoutException e) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR,
					"reading data from data node timeouts");
		} finally {
			getDocumentLatencyTracker.addMicro(System.currentTimeMillis()
					- startTime);
		}

	}

	@Override
	public byte[] getDocument(String name, HashMap parameters)
			throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDocumentAsString(String name, int prettyPrint)
			throws XMLDBException {
		return getDocumentAsString(name, prettyPrint, null);
	}

	@Override
	public String getDocumentAsString(String name, int prettyPrint,
			String stylesheet) throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDocumentAsString(String name, HashMap parameters)
			throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap getDocumentData(String name, HashMap parameters)
			throws XMLDBException {
		long startTime = System.currentTimeMillis();
		XmldbURI uri;
		HashMap result = new HashMap();
		String encoding = (String) parameters.get(OutputKeys.ENCODING);
		if (encoding == null)
			encoding = SysConfig.ENCODING;
		try {
			uri = XmldbURI.xmldbUriFor(name);
			String colPath = uri.removeLastSegment().getCollectionPath();
			XMLMetaData metaData = XMLDBCatalogManager.instance()
					.getXMLMetaData(colPath, uri.lastSegment().toString());
			if (metaData == null) {
				throw new URISyntaxException(name, "doesn't exist");
			}
			CollectionStore colStore = XMLDBStore.instance().getCollection(
					colPath);

			List<Pair<Integer, Integer>> nodes = new LinkedList<Pair<Integer, Integer>>();
			nodes.add(new Pair<Integer, Integer>(metaData.getXMLID(),
					CollectionStore.ROOT_XML_NODEID));
			List<String> ret = colStore.retrieve(nodes);
			if (ret != null && ret.size() > 0)
				result.put("data", ret.get(0).getBytes(encoding));
			else
				result.put("data", new byte[0]);
			result.put("offset", new Integer(0));
			return result;
		} catch (URISyntaxException e) {
			throw new XMLDBException(ErrorCodes.INVALID_URI, name);
		} catch (UnsupportedEncodingException e) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR,
					"encoding is not supported");
		} catch (UnavailableException e) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR,
					"service unavailable, no data node is available");
		} catch (TimeoutException e) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR,
					"read data timeout " + e.getMessage());
		} finally {
			getDocumentLatencyTracker.addMicro(System.currentTimeMillis()
					- startTime);
		}

	}

	@Override
	public HashMap getNextChunk(String handle, int offset)
			throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap getNextExtendedChunk(String handle, String offset)
			throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getBinaryResource(String name) throws XMLDBException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasDocument(String name) throws XMLDBException,
			URISyntaxException {
		try {
			XmldbURI uri = XmldbURI.xmldbUriFor(name);
			String colPath = uri.removeLastSegment().getCollectionPath();
			XMLDBCatalogManager.instance().getXMLMetaData(colPath,
					uri.lastSegment().toString());
		} catch (XMLDBException ex) {
			if (ex.errorCode == ErrorCodes.NO_SUCH_RESOURCE
					|| ex.errorCode == ErrorCodes.NO_SUCH_COLLECTION)
				return false;
		}
		return true;
	}

	@Override
	public boolean hasCollection(String name) throws XMLDBException,
			URISyntaxException {
		try {
			XmldbURI uri = XmldbURI.xmldbUriFor(name);
			String colPath = uri.removeLastSegment().getCollectionPath();
			XMLDBCatalogManager.instance().getCollectionID(colPath);
		} catch (XMLDBException ex) {
			if (ex.errorCode == ErrorCodes.NO_SUCH_COLLECTION)
				return false;
		}
		return true;
	}

	@Override
	public Vector getDocumentListing() throws XMLDBException {
		// TODO Auto-generated method stub
		return new Vector<String>();
	}

	@Override
	public Vector getDocumentListing(String collection) throws XMLDBException,
			URISyntaxException {
		CollectionMetaData colMeta = null;
		if (XMLDBStore.instance().isMutatable())
			colMeta = XMLDBCatalogManager.instance().getCollectionMeta(
					collection);
		else
			colMeta = XMLDBCatalogManager.instance().getCollectionMetaFromDB(
					collection);
		if (colMeta == null)
			return new Vector<String>();
		Vector<String> ret = new Vector<String>(colMeta.getXMLDocNames());
		return ret;
	}

	@Override
	public HashMap listDocumentPermissions(String name) throws XMLDBException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap listCollectionPermissions(String name)
			throws XMLDBException, URISyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap getCollectionDesc(String rootCollection)
			throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap describeCollection(String collectionName)
			throws XMLDBException {
		logger.info("describleCollection : " + collectionName);
		CollectionMetaData metaData = null;
		if (XMLDBStore.instance().isMutatable())
			metaData = XMLDBCatalogManager.instance().getCollectionMeta(
					collectionName);
		else
			metaData = XMLDBCatalogManager.instance().getCollectionMetaFromDB(
					collectionName);

		HashMap desc = new HashMap();
		if (metaData == null)
			throw new XMLDBException(ErrorCodes.INVALID_COLLECTION,
					"no such collection:" + collectionName);
		// SimpleDateFormat formatter = new
		// SimpleDateFormat("yyyy MM dd HH mm ss");
		List<String> cols = new LinkedList(metaData.getChildColNames());
		desc.put("collections", cols);
		desc.put("name", metaData.getName());
		desc.put("created", new Date(metaData.getCreateDate()));
		desc.put("owner", metaData.getPermission().getOwner());
		desc.put("group", metaData.getPermission().getOwnerGroup());
		desc.put("permissions", new Integer(metaData.getPermission()
				.getPermissions()));
		return desc;
	}

	@Override
	public HashMap describeResource(String resourceName) throws XMLDBException {

		String colPath = resourceName.substring(0,
				resourceName.lastIndexOf('/'));
		String xmlDocName = resourceName.substring(
				resourceName.lastIndexOf('/') + 1, resourceName.length());
		XMLMetaData meta = XMLDBCatalogManager.instance().getXMLMetaData(
				colPath, xmlDocName);

		HashMap hash = new HashMap();
		hash.put("name", resourceName);
		// if (meta.getPermission() == null) {
		hash.put("owner", "admin");
		hash.put("group", "admin");
		hash.put("permissions", new Integer(0755));
		// }
		hash.put("type", "XMLResource");
		// hash.put("content-length", new Integer(0));
		// hash.put("mime-type", "xml");
		hash.put("created", new Date(meta.getCreateDate()));
		hash.put("modified", new Date(meta.getModifyDate()));
		return hash;
	}

	@Override
	public int getResourceCount(String collectionName) throws XMLDBException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte[] retrieve(String doc, String id) throws XMLDBException {
		XmldbURI uri = null;
		try {
			uri = XmldbURI.xmldbUriFor(doc);

			String colPath = uri.removeLastSegment().getCollectionPath();
			CollectionStore store = XMLDBStore.instance()
					.getCollection(colPath);

			List<Pair<Integer, Integer>> nodes = new LinkedList<Pair<Integer, Integer>>();
			nodes.add(new Pair<Integer, Integer>(XMLDBCatalogManager.instance()
					.getXMLMetaData(colPath, uri.lastSegment().toString())
					.getXMLID(), Integer.parseInt(id)));
			List<String> ret = store.retrieve(nodes);
			if (ret != null && ret.size() > 0)
				return ret.get(0).getBytes(SysConfig.ENCODING);
			else
				return new byte[0];
		} catch (UnsupportedEncodingException e) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR,
					"the xml content can not convert to " + SysConfig.ENCODING);
		} catch (UnavailableException e) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR,
					"no data node is available");
		} catch (TimeoutException e) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR,
					"reading data from data node timeouts");
		} catch (URISyntaxException e) {
			throw new XMLDBException(ErrorCodes.INVALID_URI, doc);
		}
	}

	@Override
	public byte[] retrieve(String doc, String id, HashMap parameters)
			throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap retrieveFirstChunk(String doc, String id, HashMap parameters)
			throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String retrieveAsString(String doc, String id, HashMap parameters)
			throws XMLDBException, URISyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] retrieveAll(int resultId, HashMap parameters)
			throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap retrieveAllFirstChunk(int resultId, HashMap parameters)
			throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap compile(byte[] xquery, HashMap parameters) throws Exception {
		// TODO Auto-generated method stub
		return new HashMap();
	}

	@Override
	public HashMap queryP(byte[] xpath, HashMap parameters)
			throws XMLDBException, URISyntaxException {
		return queryP(xpath, null, null, true, parameters);
	}

	@Override
	public HashMap queryP(byte[] xpath, boolean recursive, HashMap parameters)
			throws XMLDBException, URISyntaxException {
		return queryP(xpath, null, null, recursive, parameters);
	}

	@Override
	public HashMap queryP(byte[] xpath, String docName, String s_id,
			boolean recursive, HashMap parameters) throws XMLDBException,
			URISyntaxException {
		String colName = (String) parameters.get("colPath");
		CollectionStore colStore = XMLDBStore.instance().getCollection(colName);
		if (colStore == null) {
			throw new XMLDBException(ErrorCodes.INVALID_RESOURCE);
		}
		// judge the permission
		if (colStore.getMetaData().getPermission()
				.validate(user, XMLDBSecurityManager.READ_PERMISSION) == false) {
			throw new XMLDBException(ErrorCodes.PERMISSION_DENIED);
		}

		XPathResult result = null;
		/* try { */
		try {
			byte flag = XPathQueryCommand.installFlag((byte) 0,
					XPathQueryCommand.XPATH_READFULL);
			if (recursive)
				flag = XPathQueryCommand.installFlag((byte) flag,
						XPathQueryCommand.XPATH_RECURSIVE);
			result = colStore.XPath(xpath, flag);
		} catch (Exception e) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR, e.getMessage());
		}
		/*
		 * } catch (UnavailableException e) { throw new
		 * XMLDBException(ErrorCodes.VENDOR_ERROR, e.getMessage()); }
		 */
		List<String> xmlParts = new LinkedList<String>();
		xmlParts.addAll(result.result());
		/*
		 * if (result != null) { if (result.type == XPathResultType.xmlParts) {
		 * for (Entry<Integer, Map<Integer, List<String>>> entry :
		 * result.xmlParts.XMLParts .entrySet()) { for (List<String> xml :
		 * entry.getValue().values()) { xmlParts.addAll(xml); } } } else if
		 * (result.type == XPathResultType.FunctionReturn) {
		 * xmlParts.addAll(result.getFuncRet().result()); } }
		 */
		HashMap ret = new HashMap();
		ret.put("id", 1);
		ret.put("hash", xmlParts.hashCode());
		ret.put("results", xmlParts);
		return ret;
	}

	@Override
	public byte[] query(byte[] xquery, int howmany, int start,
			HashMap parameters) throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap querySummary(String xquery) throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String printDiagnostics(String query, HashMap parameters)
			throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createResourceId(String collection) throws XMLDBException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return "1";
	}

	@Override
	public boolean parse(byte[] xmlData, String docName) throws XMLDBException,
			URISyntaxException {
		return parse(xmlData, docName, "");
	}

	@Override
	public boolean parse(byte[] xmlData, String docName, int overwrite,
			int splitted) throws XMLDBException, URISyntaxException {
		return parse(xmlData, docName, "", overwrite, splitted);
	}

	@Override
	public boolean parse(byte[] xmlData, String docName, int overwrite,
			int splitted, Date created, Date modified) throws XMLDBException,
			URISyntaxException {
		return parse(xmlData, docName, overwrite, splitted, created, modified);
	}

	@Override
	public boolean parse(String xml, String docName, int overwrite, int splitted)
			throws XMLDBException, URISyntaxException {
		return parse(xml, docName, "", overwrite, splitted);
	}

	@Override
	public boolean parse(String xml, String docName) throws XMLDBException,
			URISyntaxException {
		return parse(xml, docName, "");
	}

	@Override
	public boolean parse(byte[] xmlData, String docName, String schemaName)
			throws XMLDBException, URISyntaxException {
		return parse(xmlData, docName, schemaName, 0, (byte) 0);
	}

	@Override
	public boolean parse(byte[] xmlData, String docName, String schemaName,
			int overwrite, int splitted) throws XMLDBException,
			URISyntaxException {
		return parse(xmlData, docName, schemaName, overwrite, splitted, null,
				null);
	}

	/**
	 * @param xmlData
	 * 
	 * @param docName
	 *            the full path to the xml doc
	 */
	@Override
	public boolean parse(byte[] xmlData, String docName, String schemaName,
			int overwrite, int splitted, Date created, Date modified)
			throws XMLDBException, URISyntaxException {
		/*
		 * TODO should we constrain the encoding of xmlData to UTF8 the
		 * schemaName should be checked to make sure the schema exists
		 */

		logger.debug(docName + " schema: " + schemaName + " is stored");

		XmldbURI uri = XmldbURI.xmldbUriFor(docName);
		String colPath = uri.removeLastSegment().getCollectionPath();
		XMLMetaData metaData = XMLDBCatalogManager.instance().newXMLDoc(
				colPath, uri.lastSegment().toString(), schemaName, created,
				modified);
		// if the xml doc is bigger than 2M, it will be splitted

		metaData.setSplitted((byte) splitted);
		CollectionStore colStore = XMLDBStore.instance().getCollection(colPath);
		boolean stored = false;
		try {
			colStore.storeXML(metaData, new String(xmlData, SysConfig.ENCODING));
			stored = true;
		} catch (UnsupportedEncodingException e) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR,
					"the xml data can not be encoded in " + SysConfig.ENCODING
							+ " or " + SysConfig.ENCODING
							+ " is not supported in server");
		} finally {
			if (!stored)
				XMLDBCatalogManager.instance().removeXMLDoc(docName);
		}
		return true;
	}

	@Override
	public boolean parse(String xml, String docName, String schemaName)
			throws XMLDBException, URISyntaxException {
		return parse(xml, docName, schemaName, 0, (byte) 0);
	}

	@Override
	public boolean parse(String xml, String docName, String schemaName,
			int overwrite, int splitted) throws XMLDBException,
			URISyntaxException {

		try {
			return parse(xml.getBytes(DEFAULT_ENCODING), docName, schemaName,
					0, splitted, null, null);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public String upload(byte[] chunk, int length) throws XMLDBException,
			IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String upload(String file, byte[] chunk, int length)
			throws XMLDBException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String uploadCompressed(byte[] data, int length)
			throws XMLDBException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String uploadCompressed(String file, byte[] data, int length)
			throws XMLDBException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean parseLocal(String localFile, String docName,
			boolean replace, String mimeType) throws XMLDBException,
			SAXException, URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean parseLocalExt(String localFile, String docName,
			boolean replace, String mimeType, boolean treatAsXML)
			throws XMLDBException, SAXException, URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean parseLocal(String localFile, String docName,
			boolean replace, String mimeType, Date created, Date modified)
			throws XMLDBException, SAXException, URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean parseLocalExt(String localFile, String docName,
			boolean replace, String mimeType, boolean treatAsXML, Date created,
			Date modified) throws XMLDBException, SAXException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean storeBinary(byte[] data, String docName, String mimeType,
			boolean replace) throws XMLDBException, URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean storeBinary(byte[] data, String docName, String mimeType,
			boolean replace, Date created, Date modified)
			throws XMLDBException, URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remove(String docName) throws XMLDBException,
			URISyntaxException {
		if (!XMLDBStore.instance().isMutatable()) {
			throw new XMLDBException(ErrorCodes.PERMISSION_DENIED,
					"this node is a slave node, which is not permited to create collections");
		}
		XmldbURI uri = XmldbURI.xmldbUriFor(docName);

		CollectionStore store = XMLDBStore.instance().getCollection(
				uri.removeLastSegment().getCollectionPath());
		store.removeXMLResource(uri.lastSegment().toString());
		XMLDBCatalogManager.instance().removeXMLDoc(uri.getCollectionPath());
		return true;
	}

	@Override
	public boolean removeCollection(String name) throws XMLDBException,
			URISyntaxException {
		if (!XMLDBStore.instance().isMutatable()) {
			throw new XMLDBException(ErrorCodes.PERMISSION_DENIED,
					"this node is a slave node, which is not permited to create collections");
		}
		return XMLDBStore.instance().removeCollection(name);

	}

	@Override
	public boolean createCollection(String colURI) throws XMLDBException,
			PermissionDeniedException {
		return createCollection(colURI, null);
	}

	@Override
	public boolean createCollection(String colURI, Date created)
			throws XMLDBException, PermissionDeniedException {
		if (!XMLDBStore.instance().isMutatable()) {
			throw new XMLDBException(ErrorCodes.PERMISSION_DENIED,
					"this node is a slave node, which is not permited to create collections");
		}
		try {
			XMLDBStore.instance().createCollection(
					XmldbURI.xmldbUriFor(colURI).getCollectionPath(), user,
					created);
			return true;
		} catch (URISyntaxException e) {
			throw new XMLDBException(ErrorCodes.INVALID_URI, e.getClass() + ":"
					+ e.getMessage());
		}
	}

	@Override
	public boolean configureCollection(String collection, String configuration)
			throws XMLDBException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int executeQuery(byte[] xpath, String encoding, HashMap parameters)
			throws XMLDBException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int executeQuery(byte[] xpath, HashMap parameters)
			throws XMLDBException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int executeQuery(String xpath, HashMap parameters)
			throws XMLDBException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public HashMap execute(String path, HashMap parameters)
			throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap querySummary(int resultId) throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap getPermissions(String resource) throws XMLDBException,
			URISyntaxException {
		HashMap result = new HashMap();
		result.put("owner", "admin");
		result.put("group", "admin");
		result.put("permissions", new Integer(1));
		return result;
	}

	@Override
	public int getHits(int resultId) throws XMLDBException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte[] retrieve(int resultId, int num, HashMap parameters)
			throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap retrieveFirstChunk(int resultId, int num, HashMap parameters)
			throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setUser(String name, String passwd, Vector groups,
			String home) throws XMLDBException {
		if (XMLDBSecurityManager.getInstance().superUser(user)) {
			User user = new User(name, passwd);
			for (Object grp : groups) {
				user.addGroup((String) grp);
			}
			return XMLDBSecurityManager.getInstance().createUser(user);
		}
		return false;
	}

	@Override
	public boolean setUser(String name, String passwd, Vector groups)
			throws XMLDBException {
		return setUser(name, passwd, groups, null);

	}

	@Override
	public boolean setPermissions(String resource, String permissions)
			throws XMLDBException, URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setPermissions(String resource, int permissions)
			throws XMLDBException, URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setPermissions(String resource, String owner,
			String ownerGroup, String permissions) throws XMLDBException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setPermissions(String resource, String owner,
			String ownerGroup, int permissions) throws XMLDBException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean lockResource(String path, String userName)
			throws XMLDBException, URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean unlockResource(String path) throws XMLDBException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String hasUserLock(String path) throws XMLDBException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap getUser(String name) throws XMLDBException {
		User user = XMLDBSecurityManager.getInstance().fetchUser(name);
		HashMap ret = new HashMap();

		if (user != null) {
			ret.put("name", user.getName());
			ret.put("groups", user.getGroups());
		}
		return ret;
	}

	@Override
	public Vector getUsers() throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeUser(String name) throws XMLDBException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Vector getGroups() throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector getIndexedElements(String collectionName, boolean inclusive)
			throws XMLDBException, URISyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector scanIndexTerms(String collectionName, String start,
			String end, boolean inclusive) throws XMLDBException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector scanIndexTerms(String xpath, String start, String end)
			throws XMLDBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean releaseQueryResult(int handle) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean releaseQueryResult(int handle, int hash) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int xupdate(String collectionName, byte[] xupdate)
			throws XMLDBException, SAXException {
		return xupdate(collectionName, xupdate, true);
	}

	@Override
	public int xupdate(String collectionName, byte[] xupdate, boolean recursive)
			throws XMLDBException, SAXException {
		// TODO: judge whether this server is a master server

		if (!XMLDBStore.instance().isMutatable()) {
			throw new XMLDBException(ErrorCodes.PERMISSION_DENIED,
					"this node is a slave node, which is not permited to handle xupdate queries");
		}

		CollectionStore colStore = XMLDBStore.instance().getCollection(
				collectionName);
		try {
			return colStore.XUpdate(xupdate, recursive);
		} catch (Exception e) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR, "cause by:"
					+ e.toString() + " message:" + e.getMessage());
		}
	}

	@Override
	public int xupdateResource(String resource, byte[] xupdate)
			throws XMLDBException, SAXException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int xupdateResource(String resource, byte[] xupdate, String encoding)
			throws XMLDBException, SAXException, URISyntaxException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Date getCreationDate(String collectionName) throws XMLDBException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector getTimestamps(String documentName) throws XMLDBException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean copyCollection(String name, String namedest)
			throws XMLDBException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List getDocumentChunk(String name, HashMap parameters)
			throws XMLDBException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getDocumentChunk(String name, int start, int stop)
			throws XMLDBException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean moveCollection(String collectionPath,
			String destinationPath, String newName) throws XMLDBException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean moveResource(String docPath, String destinationPath,
			String newName) throws XMLDBException, URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean copyCollection(String collectionPath,
			String destinationPath, String newName) throws XMLDBException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean copyResource(String docPath, String destinationPath,
			String newName) throws XMLDBException, URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reindexCollection(String name) throws XMLDBException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean backup(String userbackup, String password,
			String destcollection, String collection) throws XMLDBException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean dataBackup(String dest) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValid(String name) throws XMLDBException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Vector getDocType(String documentName) throws XMLDBException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setDocType(String documentName, String doctypename,
			String publicid, String systemid) throws XMLDBException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean storeSchema(String schemaName, String schema,
			boolean overwrite) {
		XMLSchemaMetaData meta = XMLDBCatalogManager.instance().newSchema(
				schemaName, schema, overwrite);
		return true;
	}

	@Override
	public double getDocumentAvgLatency() {
		long n = getDocumentLatencyTracker.getOpCount();
		long latency = getDocumentLatencyTracker.getTotalLatencyMicros();
		if (n == 0)
			return 0;
		return (float) latency / (float) n;
	}

}

/**
 *@author:xiafan xiafan68@gmail.com
 *@version: 2011-10-3 0.1
 */
package imc.disxmldb.config;

import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import imc.disxmldb.XMLDBStore;
import imc.disxmldb.security.Permission;
import imc.disxmldb.security.PermissionDeniedException;
import imc.disxmldb.security.PermissionFactory;
import imc.disxmldb.security.User;
import imc.disxmldb.security.XMLDBSecurityManager;
import imc.disxmldb.util.ColumnFamilyStoreSerializable;
import imc.disxmldb.util.ColumnFamilyStoreProxy;

import org.apache.cassandra.config.Schema;
import org.apache.cassandra.config.Config.XMLServerMode;
import org.apache.cassandra.db.RowMutation;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.Pair;
import org.exist.xmldb.XmldbURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.XMLDBException;

/**
 * the catalog meta data is stored in the keyspace xmldb_sys_catalog. This class
 * is used to manage the cache of the meta data of collection, xml doc and
 * schema. It will periodically resynchronize the cache of these meta data.
 * 
 * @author Administrator
 * 
 */
public class XMLDBCatalogManager implements XMLDBCatalogManagerMBean {
	private static Logger logger = LoggerFactory
			.getLogger(XMLDBCatalogManager.class);

	public static final ConsistencyLevel CATALOG_CONSISTENCYLEVEL = ConsistencyLevel.QUORUM;

	private static XMLDBCatalogManager instance = new XMLDBCatalogManager();

	public CollectionMetaData rootCollection = null;

	public ConcurrentMap<Integer, CollectionMetaData> colID2MetaData = new ConcurrentHashMap<Integer, CollectionMetaData>();
	public ConcurrentMap<Pair<Integer, Integer>, XMLMetaData> xmlID2MetaData = new ConcurrentHashMap<Pair<Integer, Integer>, XMLMetaData>();
	public ConcurrentMap<String, XMLSchemaMetaData> schemaName2MetaData = new ConcurrentHashMap<String, XMLSchemaMetaData>();

	public final ColumnFamilyStoreProxy colCatalogStore = new ColumnFamilyStoreProxy(
			SysConfig.SYSCATALOG, SysConfig.SYSCATALOG_COLLECTION);
	public final ColumnFamilyStoreProxy xmlCatalogStore = new ColumnFamilyStoreProxy(
			SysConfig.SYSCATALOG, SysConfig.SYSCATALOG_XML);
	public final ColumnFamilyStoreProxy schemaCatalogStore = new ColumnFamilyStoreProxy(
			SysConfig.SYSCATALOG, SysConfig.SYSCATALOG_SCHEMA);

	/*
	 * used to flush all dirty metadata to the columnfamily
	 */
	private ExecutorService flusher = Executors.newSingleThreadExecutor();
	private MutationFlushService metaDataFlushService = new InstantMetaDataFlushService();

	private Timer timer = null;

	public static XMLDBCatalogManager instance() {
		return instance;
	}

	private XMLDBCatalogManager() {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			mbs.registerMBean(this, new ObjectName(
					"imc.disxmldb.config:type=XMLDBCatalogManager," + "addr="
							+ FBUtilities.getBroadcastAddress()));
		} catch (Exception e) {
		}
	}

	public void unRegister() {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			mbs.unregisterMBean(new ObjectName(
					"imc.disxmldb.config:type=XMLDBCatalogManager," + "addr="
							+ FBUtilities.getBroadcastAddress()));
		} catch (Exception e) {
			// throw new RuntimeException(e);
		}
	}

	public void init() {
		logger.info("XMLDBCatalogManager init as "
				+ XMLDBStore.instance().getServerMode());
		// TODO synchronize rootCollection metaData
		// syncRootCollectionMetaData();
		if (XMLDBStore.instance().getServerMode() != XMLServerMode.master) {
			// It means this node needs to refresh this cache periodically.
			timer = new Timer();
			timer.schedule(new RefreshMetaTask(), 10000, 1000 * 60);
		}
	}

	/**
	 * 
	 * @param colPath
	 * @return
	 * @throws XMLDBException
	 */
	public int getCollectionID(String colPath) throws XMLDBException {
		if (colPath.length() == 0)
			return SysConfig.ROOT_COLLECTION_ID;
		if (colPath.startsWith("/"))
			colPath = colPath.substring(1, colPath.length());
		String[] cols = colPath.split("/");

		if (cols.length == 0
				|| (cols.length == 1 && cols[0]
						.equals(SysConfig.ROOT_COLLECTION_NAME)))
			return SysConfig.ROOT_COLLECTION_ID;

		// CollectionMetaData cur = rootCollection;
		CollectionMetaData cur = getCollectionMeta(SysConfig.ROOT_COLLECTION_ID);
		Integer pId = SysConfig.ROOT_COLLECTION_ID;
		Integer id = SysConfig.ROOT_COLLECTION_ID;
		int i = 1;
		for (i = 1; i < cols.length; i++) {
			if (cur == null) {
				fixCollectionMetaData(pId, id);
				throw new XMLDBException(ErrorCodes.NO_SUCH_COLLECTION,
						"collection " + cols[i] + "not found");
			}
			pId = id;
			id = cur.getCollectionID(cols[i]);
			if (id == null) {
				throw new XMLDBException(ErrorCodes.NO_SUCH_COLLECTION, cols[i]
						+ "doesn't exist");
			}
			cur = getCollectionMeta(id);
		}
		if (cur == null) {
			fixCollectionMetaData(pId, id);
			throw new XMLDBException(ErrorCodes.NO_SUCH_COLLECTION,
					"collection " + cols[i - 1] + "not found");
		}
		return id.intValue();
	}

	/**
	 * 
	 * @param colPath
	 *            the path from the root to the specified collection
	 * @return null if the collection doesn't exist
	 * @throws XMLDBException
	 */
	public CollectionMetaData getCollectionMeta(String colPath)
			throws XMLDBException {
		int id = getCollectionID(colPath);
		return getCollectionMeta(id);
	}

	/**
	 * 这个接口只能有rpcconnect调用，他能够反应出这个collection的当前状态，而client
	 * mode下的server缓存的只是collection的层次结构，而不需要collection本身具体的信息
	 * 
	 * @param colPath
	 * @return
	 * @throws XMLDBException
	 */
	public CollectionMetaData getCollectionMetaFromDB(String colPath)
			throws XMLDBException {
		int id = getCollectionID(colPath);
		CollectionMetaData metaData = new CollectionMetaData(id);
		metaData.deserialize(colCatalogStore);

		if (metaData == null || metaData.getName() == null) {
			return null;
		} else {
			// TODO是否可以以此契机，更新client server的元数据
			CollectionMetaData cacheMeta = this.getCollectionMeta(id);
			if (cacheMeta != null && metaData.getPermission() != null)
				cacheMeta.setPermission(metaData.getPermission());
			return metaData;
		}
	}

	/**
	 * @param colID
	 * @return null if the collection with colID doesn't exists
	 */
	public CollectionMetaData getCollectionMeta(int colID) {
		if (colID2MetaData.get(new Integer(colID)) == null) {
			syncColMetaDataByID(colID);
		}
		return colID2MetaData.get(new Integer(colID));
	}

	public XMLMetaData getXMLMetaData(String colPath, String xmlDocName)
			throws XMLDBException {
		CollectionMetaData colMeta = getCollectionMeta(colPath);
		if (colMeta == null) {
			throw new XMLDBException(ErrorCodes.INVALID_COLLECTION, colPath
					+ "doesn't exist");
		}
		Integer xmlDocID = colMeta.getXMLID(xmlDocName);
		if (xmlDocID == null) {
			throw new XMLDBException(ErrorCodes.NO_SUCH_RESOURCE, colPath + "/"
					+ xmlDocName + "doesn't exist");
		}

		return getXMLMetaDataByID(colMeta.getColID(), xmlDocID);
	}

	public XMLMetaData getXMLMetaDataByID(int colID, int xmlID) {
		Pair<Integer, Integer> key = new Pair<Integer, Integer>(colID, xmlID);
		if (xmlID2MetaData.get(key) == null) {
			syncXMLMetaData(colID, xmlID);
		}
		return xmlID2MetaData.get(new Pair<Integer, Integer>(colID, xmlID));
	}

	public XMLSchemaMetaData getSchema(String schemaName) {
		if (schemaName2MetaData.get(schemaName) == null) {
			XMLSchemaMetaData meta = new XMLSchemaMetaData(schemaName);
			meta.deserialize(schemaCatalogStore);
			if (meta.getSchemaName() == null)
				return null;
			schemaName2MetaData.putIfAbsent(schemaName, meta);
		}
		return schemaName2MetaData.get(schemaName);
	}

	public XMLSchemaMetaData newSchema(String schemaName, String schema,
			boolean overwrite) {
		XMLSchemaMetaData meta = new XMLSchemaMetaData(schemaName);
		meta.setCreateDate(System.currentTimeMillis(), false);
		meta.setModifyDate(System.currentTimeMillis(), false);
		meta.setSchema(schema, false);
		if (overwrite) {
			schemaName2MetaData.put(schemaName, meta);
			// meta.serialize(schemaCatalogStore);
			flushDirtySchemaMetaData(meta);
			return meta;
		} else {
			XMLSchemaMetaData retMeta = schemaName2MetaData.putIfAbsent(
					schemaName, meta);
			if (retMeta == null)
				flushDirtySchemaMetaData(meta);
			return retMeta;
		}
	}

	public CollectionMetaData newCollection(String colPath, User user, Date date)
			throws XMLDBException, PermissionDeniedException {
		if (user == null) {
			throw new PermissionDeniedException();
		}

		CollectionMetaData parent = getCollectionMeta(colPath.substring(0,
				colPath.lastIndexOf("/")));

		user = XMLDBSecurityManager.getInstance().fetchUser(user);
		if (user == null
				|| parent.getPermission() == null
				|| !parent.getPermission().validate(user,
						XMLDBSecurityManager.WRITE_PERMISSION)) {
			throw new PermissionDeniedException();
		}

		Permission perm = PermissionFactory.getPermission(user.getName(),
				user.getPrimaryGroup(),
				XMLDBSecurityManager.DEFAULT_DIR_PERMISSION);
		CollectionMetaData ret = new CollectionMetaData(nextCollectionID(),
				colPath.substring(colPath.lastIndexOf("/") + 1,
						colPath.length()), perm);

		boolean success = parent.addCollection(ret.getName(), ret.getColID());
		if (!success) {
			throw new XMLDBException(ErrorCodes.INVALID_COLLECTION,
					"collection " + ret.getName() + " already exists");
		} else {
			if (colID2MetaData.putIfAbsent(ret.getColID(), ret) == null) {
				ret.setpColID(parent.getColID());
				if (date == null) {
					ret.setCreateDate(System.currentTimeMillis());
					ret.setModifyDate(System.currentTimeMillis());
				} else {
					ret.setCreateDate(date.getTime());
					ret.setModifyDate(date.getTime());
				}
				flushDirtyColMetaData(ret);
				// ret.serialize(colCatalogStore);
			}
			ret = colID2MetaData.get(ret.getColID());
			return ret;
		}
	}

	public void removeCollectionInCache(int colID) {
		CollectionMetaData child = getCollectionMeta(colID);
		if (child != null) {
			colID2MetaData.remove(colID);
			int pColID = child.getpColID();
			CollectionMetaData pCol = getCollectionMeta(pColID);
			if (pCol != null)
				pCol.removeCollectionClient(colID);
		}
	}

	/**
	 * only remove the data in the column family colcatalog;
	 * 
	 * @param colID
	 * @throws XMLDBException
	 */
	private void removeCollectionInDB(int colID) throws XMLDBException {
		if (XMLDBStore.instance().isMutatable() == false)
			return;
		RowMutation mutation = new RowMutation(SysConfig.SYSCATALOG,
				Int32Type.instance.fromString(Integer.toString(colID)));
		mutation.delete(new QueryPath(SysConfig.SYSCATALOG_COLLECTION, null,
				null), System.currentTimeMillis());
		flushMetaData(mutation);
	}

	/**
	 * remove the cache and persistent metadata of colID. TODO 应该递归删除colID
	 * 
	 * @param colID
	 * @return
	 * @throws URISyntaxException
	 * @throws XMLDBException
	 */
	public boolean removeCollection(int pColID, int colID)
			throws URISyntaxException, XMLDBException {
		CollectionMetaData child = getCollectionMeta(colID);
		if (child != null) {
			String childPath = child.getFullName();
			for (String xmlName : child.getXMLDocNames())
				removeXMLDoc(childPath + "/" + xmlName);
		}
		if (child != null)
			pColID = child.getColID();
		CollectionMetaData parent = getCollectionMeta(pColID);
		boolean del = true;
		if (parent != null) {
			String name = parent.getColName(colID);
			if (name != null)
				del = parent.removeCollection(name);
		}
		if (del) {
			removeCollectionInDB(colID);
			colID2MetaData.remove(colID);
			return true;
		} else {
			return false;
		}
	}

	public boolean removeCollection(String colPath) throws URISyntaxException,
			XMLDBException {
		logger.info("removeCollection " + colPath);
		XmldbURI uri = XmldbURI.xmldbUriFor(colPath);
		String colPar = uri.removeLastSegment().getCollectionPath();
		String colChild = uri.lastSegment().toString();

		CollectionMetaData parent = XMLDBCatalogManager.instance()
				.getCollectionMeta(colPar);
		Integer id = parent.getCollectionID(colChild);
		if (id == null)
			return true;

		CollectionMetaData child = getCollectionMeta(id);
		if (child == null) {
			throw new XMLDBException(ErrorCodes.INVALID_COLLECTION, colChild
					+ " not found");
		}
		// first remove the xmlmetadatas belong to the collection from the
		// underlying backups
		String childPath = child.getFullName();
		for (String xmlName : child.getXMLDocNames())
			removeXMLDoc(childPath + "/" + xmlName);

		boolean del = parent.removeCollection(colChild);
		if (del) {
			removeCollectionInDB(id);
			colID2MetaData.remove(id);

			return true;
		} else {
			return false;
		}
	}

	/**
	 * remove the metadata of the xmlDoc
	 * 
	 * @param xmlDoc
	 * @return
	 * @throws URISyntaxException
	 * @throws XMLDBException
	 */
	public boolean removeXMLDoc(String xmlDoc) throws URISyntaxException,
			XMLDBException {
		XmldbURI uri = XmldbURI.xmldbUriFor(xmlDoc);
		String colPar = uri.removeLastSegment().getCollectionPath();
		String xmlName = uri.lastSegment().toString();

		CollectionMetaData parent = XMLDBCatalogManager.instance()
				.getCollectionMeta(colPar);
		Integer id = parent.getXMLID(xmlName);
		if (id == null)
			return true;
		XMLMetaData xmlMeta = XMLDBCatalogManager.instance.getXMLMetaDataByID(
				parent.getColID(), id);

		// first remove the xml part of this xml document
		for (Integer xmlID : xmlMeta.getXMLPartIDs()) {
			removeXMLMetaData(parent.getColID(), xmlID);
		}

		// then remove the metadata and cache
		boolean del = parent.removeXMLDoc(xmlName, id);
		if (del) {
			removeXMLMetaData(parent.getColID(), xmlMeta.getXMLID());
			return true;
		} else {
			return false;
		}
	}

	public static boolean removeXMLMetaData(int colID, int xmlID)
			throws XMLDBException {
		RowMutation mutation = new RowMutation(SysConfig.SYSCATALOG,
				XMLMetaData.composeKey(colID, xmlID));
		mutation.delete(new QueryPath(SysConfig.SYSCATALOG_XML, null, null),
				System.currentTimeMillis());
		return XMLDBCatalogManager.instance().flushMetaData(mutation);
	}

	/**
	 * there is no need to overwrite the metadata, if an xml is needed to be
	 * overwrited, this methods will just return the previous metadata;
	 * 
	 * @param colPath
	 * @param xmlDocName
	 * @param schemaName
	 * @param created
	 * @param modified
	 * @return
	 * @throws XMLDBException
	 */
	public XMLMetaData newXMLDoc(String colPath, String xmlDocName,
			String schemaName, Date created, Date modified)
			throws XMLDBException {
		CollectionMetaData colMeta = getCollectionMeta(colPath);
		XMLMetaData xmlMeta = new XMLMetaData(colMeta.getColID(),
				colMeta.getNextXMLID());
		xmlMeta.setSchemaName(schemaName);
		xmlMeta.setName(xmlDocName);

		if (colMeta.addXMLdoc(xmlMeta.getName(), xmlMeta.getXMLID()) == false) {
			// this means an xml doc with the name already exists in the system,
			// this also ensures no two metadata with the same xml doc name
			// could exist in the system
			/*
			 * Pair<Integer, Integer> key = new Pair<Integer, Integer>(
			 * colMeta.getColID(), colMeta.getXMLID(xmlDocName)); if
			 * (xmlID2MetaData.get(key) == null) syncXMLMetaData(key.left,
			 * key.right); return xmlID2MetaData.get(key);
			 */
			throw new XMLDBException(ErrorCodes.INVALID_RESOURCE, xmlDocName
					+ " already exists");
		} else {
			colMeta.increXmlNum();
		}

		if (created == null)
			xmlMeta.setCreateDate(System.currentTimeMillis());
		else
			xmlMeta.setCreateDate(created.getTime());
		if (modified == null)
			xmlMeta.setModifyDate(System.currentTimeMillis());
		else
			xmlMeta.setModifyDate(modified.getTime());
		Pair<Integer, Integer> key = new Pair<Integer, Integer>(
				xmlMeta.getColID(), xmlMeta.getXMLID());
		xmlID2MetaData.putIfAbsent(key, xmlMeta);
		flushDirtyXMLMetaData(xmlMeta);
		return xmlID2MetaData.get(key);
	}

	public XMLMetaData newXMLPart(XMLMetaData parent) {
		CollectionMetaData colMeta = getCollectionMeta(parent.getColID());
		XMLMetaData ret = new XMLMetaData(parent.getColID(),
				colMeta.getNextXMLID());
		ret.setName(parent.getName() + "." + ret.getXMLID());
		ret.setSchemaName(parent.getSchemaName());
		ret.setpXMLID(parent.getXMLID());
		ret.setNamespaceMappings(parent.getNamespaceMappings());

		parent.addXMLPart(ret.getName(), ret.getXMLID());
		// colMeta.addXMLdoc(ret.getName(), ret.getXMLID());
		Pair<Integer, Integer> key = new Pair<Integer, Integer>(ret.getColID(),
				ret.getXMLID());
		xmlID2MetaData.putIfAbsent(key, ret);
		flushDirtyXMLMetaData(ret);
		return ret;
	}

	private int nextCollectionID() {
		if (rootCollection == null)
			syncColMetaDataByID(SysConfig.ROOT_COLLECTION_ID);
		return rootCollection.getNextColID();
	}

	/**
	 * resynchronize all the cache, which is achieved by clear all the local
	 * cache
	 */
	public void reSyncAll() {
		logger.info("reSyncAll");
		rootCollection = null;
		colID2MetaData.clear();
		xmlID2MetaData.clear();
		schemaName2MetaData.clear();
	}

	/**
	 * @param XMLDocID
	 */
	public void syncXMLMetaData(int colID, int XMLDocID) {
		XMLMetaData metaData = new XMLMetaData(colID, XMLDocID);
		metaData.deserialize(xmlCatalogStore);
		XMLMetaData pre = xmlID2MetaData.putIfAbsent(
				new Pair<Integer, Integer>(colID, XMLDocID), metaData);
		if (pre != null && pre.isDirty()) {
			this.flushDirtyXMLMetaData(pre);
		} 
		XMLMetaData meta = xmlID2MetaData.get(new Pair<Integer, Integer>(colID,
				XMLDocID));

	}

	/**
	 * synchronize the CollectionMetaData of colPath
	 * 
	 * @param colPath
	 * @throws XMLDBException
	 * 
	 */
	public void syncColMetaData(String colPath) throws XMLDBException {
		Integer colID = getCollectionID(colPath);
		syncColMetaDataByID(colID);
	}

	/**
	 * TODO: how to determine which data is more newer
	 * 
	 * @param colID
	 */
	public void syncColMetaDataByID(int colID) {
		CollectionMetaData metaData;
		if (colID == SysConfig.ROOT_COLLECTION_ID) {
			syncRootCollectionMetaData();
			metaData = rootCollection;
		} else {
			metaData = new CollectionMetaData(colID);
			metaData.deserialize(colCatalogStore);
		}
		if (metaData == null || metaData.getName() == null) {
			return;
		}
		if (colID2MetaData.putIfAbsent(metaData.getColID(), metaData) == null) {
			if (!metaData.isRootCol()) {
				CollectionMetaData parent = this.getCollectionMeta(metaData
						.getpColID());
				if (parent != null)
					parent.addCollection(metaData.getName(),
							metaData.getColID());
			}
		}
	}

	private synchronized void syncRootCollectionMetaData() {
		CollectionMetaData metaData = new CollectionMetaData(
				SysConfig.ROOT_COLLECTION_ID);
		metaData.deserialize(colCatalogStore);
		if (metaData.getName() == null && XMLDBStore.instance().isMutatable()) {
			metaData = new CollectionMetaData(SysConfig.ROOT_COLLECTION_ID,
					SysConfig.ROOT_COLLECTION_NAME,
					XMLDBSecurityManager.DEFAULT_ROOTCOL_PERM);
		}

		if (metaData.getName() != null
				&& colID2MetaData.putIfAbsent(metaData.getColID(), metaData) == null) {
			rootCollection = metaData;
			// rootCollection.serialize(colCatalogStore);
			flushDirtyColMetaData(metaData);
		}
	}

	public void flushDirtyXMLMetaData(XMLMetaData metaData) {
		if (XMLDBStore.instance().isMutatable())
			metaData.serialize(xmlCatalogStore);
	}

	public void flushDirtyColMetaData(CollectionMetaData metaData) {
		if (XMLDBStore.instance().isMutatable())
			metaData.serialize(colCatalogStore);
	}

	public void flushDirtySchemaMetaData(XMLSchemaMetaData metaData) {
		if (XMLDBStore.instance().isMutatable())
			metaData.serialize(schemaCatalogStore);
	}

	/**
	 * for client mode server, this should always return true
	 * 
	 * @param rm
	 * @return
	 */
	public boolean flushMetaData(RowMutation rm) {
		if (XMLDBStore.instance().isMutatable() == false)
			return true;
		return metaDataFlushService.flush(rm, CATALOG_CONSISTENCYLEVEL);
	}

	public void shutdown() {
		flusher.shutdown();
		metaDataFlushService.shutdown();
		if (timer != null)
			timer.cancel();
		unRegister();
	}

	/**
	 * this class is used to submit task that will flush the dirty data to the
	 * underlying storage
	 * 
	 * @author Administrator
	 * 
	 */
	private class FlushRunnable implements Runnable {
		ColumnFamilyStoreSerializable data = null;
		ColumnFamilyStoreProxy store = null;

		public FlushRunnable(ColumnFamilyStoreSerializable data,
				ColumnFamilyStoreProxy store) {
			this.data = data;
			this.store = store;
		}

		@Override
		public void run() {
			data.serialize(store);
		}
	}

	/**
	 * This is a timer task used to refresh the catalog data in the client mode
	 * server. It implements this by deleting the metadata.
	 * 
	 * @author xiafan
	 * 
	 */
	private class RefreshMetaTask extends TimerTask {

		@Override
		public void run() {
			Random rand = new Random();
			// the update of collection meta data is triggered by the dropCf
			// info as client
			// only needs to know the structure of the collections
			/*
			 * Set<Integer> colIDs = colID2MetaData.keySet(); List<Integer>
			 * delColIDs = new LinkedList<Integer>(); for (Integer colID :
			 * colIDs) { if (rand.nextFloat() < 0.01f) { delColIDs.add(colID); }
			 * }
			 * 
			 * for (Integer colID : delColIDs) colID2MetaData.remove(colID);
			 */

			Set<Pair<Integer, Integer>> XMLDocIDs = xmlID2MetaData.keySet();
			List<Pair<Integer, Integer>> delDocIDs = new LinkedList<Pair<Integer, Integer>>();
			for (Pair<Integer, Integer> xmlID : delDocIDs) {
				if (rand.nextFloat() < 0.01f) {
					delDocIDs.add(xmlID);
				}
			}

			for (Pair<Integer, Integer> colID : delDocIDs)
				xmlID2MetaData.remove(colID);

		}
	}

	public void onBecomeMaster() {
		logger.info("become master");
		timer.cancel();
		reSyncAll();
	}

	public void onBecomeSlave() {
		logger.info("become slave");
		timer = new Timer();
		timer.schedule(new RefreshMetaTask(), 10000, 1000);
	}

	@Override
	public void repairCollectionMetadata(int colID) {
		if (colID2MetaData.containsKey(colID)) {
			flushDirtyColMetaData(colID2MetaData.get(colID));
		}
	}

	/**
	 * check if the collection has been deleted. if true, delete the metadata in
	 * cache. The persistant data should also be deleted if this is the master.
	 * 
	 * @param colID
	 */
	public void fixCollectionMetaData(int pColID, int colID) {
		logger.info("fixCollectionMetaData for " + colID);

		Integer id = Schema.instance.getId(SysConfig.DEFAULT_KEYSPACE,
				CollectionMetaData.getCFName(colID));
		if (id == null) {
			// the collection has been deleted
			if (XMLDBStore.instance().isMutatable()) {
				try {
					removeCollection(pColID, colID);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("fixCollectionMetaData for " + colID
							+ " error: " + e.getMessage());
				}
			} else {
				removeCollectionInCache(colID);
			}
		}
	}
}

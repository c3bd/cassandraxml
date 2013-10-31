/**
 * @author:xiafan xiafan68@gmail.com
 * @version: 2011-9-27 0.1
 */
package imc.disxmldb;

import java.net.InetAddress;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.Config.XMLServerMode;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.thrift.CassandraDaemon;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.SchemaDisagreementException;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.thrift.TException;
import org.exist.xmldb.XmldbURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.XMLDBException;

import imc.disxmldb.config.CollectionMetaData;
import imc.disxmldb.config.SysConfig;
import imc.disxmldb.config.XMLDBCatalogManager;
import imc.disxmldb.config.XMLSchemaMetaData;
import imc.disxmldb.gms.IGMSObserver;
import imc.disxmldb.gms.ZooKeeperBasedGMS;
import imc.disxmldb.security.PermissionDeniedException;
import imc.disxmldb.security.User;
import imc.disxmldb.security.XMLDBSecurityManager;
import imc.disxmldb.util.CassandraSchemaProxy;

/**
 * this class uses the singleton design pattern to define a single instance of
 * the XML database server. This class manages the meta data and the actual data
 * store. The creation and deletion of collections should be executed through
 * this class, which will ensure the consistency between the schema and the
 * actual data in the store.
 * 
 */
public class XMLDBStore implements IGMSObserver {
	private static Logger logger = LoggerFactory.getLogger(XMLDBStore.class);
	// private ThreadLocal<UserSession> session = new
	// ThreadLocal<UserSession>();
	private XMLDBCatalogManager catalogManager = XMLDBCatalogManager.instance();
	private ConcurrentMap<Integer, CollectionStore> colStoreMap = new ConcurrentHashMap<Integer, CollectionStore>();
	private static XMLDBStore instance = new XMLDBStore();
	private InetAddress localAddr = FBUtilities.getLocalAddress();
	private XMLServerMode serverMode = XMLServerMode.starting;

	public static XMLDBStore instance() {
		return instance;
	}

	private XMLDBStore() {
	}

	public void init() {
		if (DatabaseDescriptor.ZooKeeperEnabled()) {
			ZooKeeperBasedGMS.getInstance().init();
			ZooKeeperBasedGMS.getInstance().register(this);
			if (DatabaseDescriptor.getServerMode() == XMLServerMode.master) {
				ZooKeeperBasedGMS.getInstance().electAsMaster();
			}
		} else {
			serverMode = DatabaseDescriptor.getServerMode();
		}
		catalogManager.init();
	}

	public void shutdown() {
		for (CollectionStore colStore : colStoreMap.values()) {
			colStore.close();
		}
		catalogManager.shutdown();
	}

	/**
	 * 
	 * @param schemaName
	 *            the name of the schema, which is the identifier to used to
	 *            specify the schema
	 * @param schema
	 *            the content of the schema file
	 * @return
	 */
	public XMLSchemaMetaData createSchema(String schemaName, String schema,
			boolean overwrite) {
		return catalogManager.newSchema(schemaName, schema, overwrite);
	}

	public int createCollection(String colPath, User user)
			throws XMLDBException, PermissionDeniedException {
		return createCollection(colPath, user, new Date());
	}

	/**
	 * create metadata and columnfamily for the new collection
	 * 
	 * @param colPath
	 *            the name of the collection being created, the same collection
	 *            should not exist
	 * @return -1 if create fails non-negative the id of the collection created
	 * @throws XMLDBException
	 */
	public int createCollection(String colPath, User user, Date date)
			throws XMLDBException, PermissionDeniedException {
		logger.debug("XMLDBStore.createCollection " + colPath);
		CollectionMetaData meta = catalogManager.newCollection(colPath, user,
				date);
		CFMetaData cfMeta = CassandraSchemaProxy
				.createCollectionCFMetaData(meta);
		try {
			CassandraSchemaProxy.system_add_column_family(cfMeta);
		} catch (InvalidRequestException e) {
			throw new XMLDBException(ErrorCodes.INVALID_COLLECTION, e);
		} catch (SchemaDisagreementException e) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR, e);
		}
		return meta.getColID();
	}

	/**
	 * remove the metadata and the column family
	 * 
	 * @param colName
	 * @return
	 * @throws URISyntaxException
	 * @throws XMLDBException
	 */
	public boolean removeCollection(String colName) throws URISyntaxException,
			XMLDBException {
		CollectionMetaData meta = XMLDBCatalogManager.instance()
				.getCollectionMeta(colName);

		if (colName.endsWith("/"))
			colName = colName.substring(0, colName.length() - 1);
		for (String name : meta.getChildColNames()) {
			removeCollection(colName + "/" + name);
		}

		String cfName = meta.getCFName();
		CollectionStore colStore = getCollection(colName);

		if (colStore != null) {
			colStore.unRegister();
		}

		boolean del = XMLDBCatalogManager.instance().removeCollection(colName);
		if (del == false) {
			return false;
		} else {
			try {
				CassandraSchemaProxy.system_drop_column_family(
						SysConfig.DEFAULT_KEYSPACE, cfName);
			} catch (Exception e) {
				// TODO how to handle this case
				e.printStackTrace();
			}
			return true;
		}
	}
	
	public void removeCollectionHandler(int colID) {
		colStoreMap.remove(colID);
	}

	/**
	 * remove the metadata and data related
	 * 
	 * @param xmlPath
	 * @return
	 * @throws URISyntaxException
	 * @throws XMLDBException
	 */
	public boolean removeXMLResource(String xmlPath) throws URISyntaxException,
			XMLDBException {
		// first remove the actural data
		XmldbURI uri = XmldbURI.xmldbUriFor(xmlPath);
		String colPath = uri.removeLastSegment().getCollectionPath();
		CollectionStore colStore = instance.getCollection(colPath);
		if (colStore == null)
			throw new XMLDBException(ErrorCodes.INVALID_COLLECTION,
					"colPath doesn't exist");

		colStore.removeXMLResource(xmlPath);

		// then remove the metadata
		XMLDBCatalogManager.instance().removeXMLDoc(xmlPath);
		return true;
	}

	/**
	 * get the CollectionStore of colName under the root collection
	 * 
	 * @param colName
	 *            collection name, which can be a path
	 * @return null if the collection does't exist
	 * @throws XMLDBException
	 */
	public CollectionStore getCollection(String colPath) throws XMLDBException {
		int colID = catalogManager.getCollectionID(colPath);
		return getCollection(colID);
	}

	public CollectionStore getCollection(int colID) throws XMLDBException {
		Integer id = new Integer(colID);
		if (colStoreMap.get(id) == null) {
			CollectionMetaData meta = catalogManager.getCollectionMeta(colID);
			if (meta == null) {
				throw new XMLDBException(ErrorCodes.INVALID_COLLECTION,
						meta.getName() + " doesn't exist");
			}
			try {
				CollectionStore store = new CollectionStore(meta);
				colStoreMap.putIfAbsent(id, store);
			} catch (Exception ex) {
				logger.error(ex
						+ "; the metadata and the actural data is inconsistent, which may be caused by exception shutdown of the server");
				//TODO is this approach proper?
				XMLDBCatalogManager.instance().fixCollectionMetaData(-1, colID);
				return null;
			}
		}
		return colStoreMap.get(id);
	}

	public Collection<ColumnFamilyStore> getIndexBackedByCFStores(int colID) {
		return null;
	}

	public InetAddress getLocalAddr() {
		return localAddr;
	}

	public void setLocalAddr(InetAddress localAddr) {
		this.localAddr = localAddr;
	}

	public XMLServerMode getServerMode() {
		return serverMode;
	}

	public void setServerMode(XMLServerMode serverMode) {
		this.serverMode = serverMode;
	}

	/**
	 * whether this node can execute update query
	 * @return
	 */
	public boolean isMutatable() {
		return getServerMode() == XMLServerMode.master;
	}
	
	/**
	 * decide whether this node should take part in the election of master
	 */
	@Override
	public void onMasterExit() {
		if (DatabaseDescriptor.getServerMode() != XMLServerMode.slave) {
			logger.info("this node is taking part in the election of master node");
			ZooKeeperBasedGMS.getInstance().electAsMaster();
		}
	}

	/**
	 * check if this node should change its role in the cluster
	 */
	@Override
	public void onMasterNodeChange(InetAddress masterAddr) {
		logger.info(masterAddr + " becomes the new master");
		if (localAddr.equals(masterAddr)) {
			XMLDBCatalogManager.instance().onBecomeMaster();
			serverMode = XMLServerMode.master;
		} else if (serverMode == XMLServerMode.master) {
			serverMode = XMLServerMode.secondary_master;
		} else if (serverMode == XMLServerMode.starting) {
			serverMode = DatabaseDescriptor.getServerMode();
			if (serverMode == XMLServerMode.master)
				serverMode = XMLServerMode.secondary_master;
		}
	}

	/**
	 * it means this node should not perform any write operations if this node
	 * is a master node
	 */
	@Override
	public void onZooKeeperLost() {
		logger.warn("this node lost connection to the zookeeper cluster");
		if (serverMode == XMLServerMode.master) {
			serverMode = XMLServerMode.secondary_master;
		}
	}
}

/**
 *@author:xiafan xiafan68@gmail.com
 *@version: 2011-10-2 0.1
 */
package imc.disxmldb.config;

import imc.disxmldb.XMLDBStore;
import imc.disxmldb.dom.XMLNodeImpl;
import imc.disxmldb.dom.typesystem.ValueType;
import imc.disxmldb.security.Permission;
import imc.disxmldb.security.PermissionFactory;
import imc.disxmldb.security.UnixStylePermissionSerializer;
import imc.disxmldb.security.XMLDBSecurityManager;
import imc.disxmldb.security.UnixStylePermission;
import imc.disxmldb.util.ColumnFamilyStoreSerializable;
import imc.disxmldb.util.ColumnFamilyStoreProxy;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.RowMutation;
import org.apache.cassandra.db.SuperColumn;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.db.marshal.IntegerType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.service.StorageProxy;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.cassandra.utils.ByteBufferUtil;

/**
 * This class is the class used to manage the meta data needs by a collection.
 * In order to ensure all modifications to the meta data will be persisted, all
 * functions that modify the state of this collection will be persisted
 * immediately. Besides, the serialization of the class is executed atomic,
 * which guarantees the deserialization process won't read partial data.
 */
public class CollectionMetaData implements ColumnFamilyStoreSerializable {

	public static final String COLDATA_CFNAME_PREFIX = "xmlCollection_";
	public static final ByteBuffer BASEINFO = UTF8Type.instance
			.fromString("baseinfo");
	public static final ByteBuffer CHILDCOLLECTION_SP = UTF8Type.instance
			.fromString("childcol");
	public static final ByteBuffer CHILDXML_SP = UTF8Type.instance
			.fromString("childxml");
	public static final ByteBuffer PARTITION_PATH_SP = UTF8Type.instance
			.fromString("ppath");
	public static final ByteBuffer INDEXTYPE_SP = UTF8Type.instance
			.fromString("idxType");

	public static final ByteBuffer BASEINFO_NAME = UTF8Type.instance
			.fromString("name");
	public static final ByteBuffer BASEINFO_CREATEDATE = UTF8Type.instance
			.fromString("createdate");
	public static final ByteBuffer BASEINFO_MODIFYDATE = UTF8Type.instance
			.fromString("modifydate");
	public static final ByteBuffer BASEINFO_PCOLID = UTF8Type.instance
			.fromString("pcolid");
	public static final ByteBuffer BASEINFO_MAXCOLID = UTF8Type.instance
			.fromString("maxcolid");
	public static final ByteBuffer BASEINFO_XMLNUM = UTF8Type.instance
			.fromString("xmlnum");
	public static final ByteBuffer BASEINFO_MAXXMLID = UTF8Type.instance
			.fromString("maxXMLID");

	private boolean isDirty = true;

	// this field should not be changed after it has been intialized
	private final int colID;
	private final ByteBuffer key;
	private int pColID = -1;
	private String name = null;

	private Integer xmlNum = new Integer(0);

	/*
	 * only the root Collection should use the field, it can be used to generate
	 * the collection ID, which should be synchronized to the columfamily in
	 * real time
	 */
	private Integer maxColID = new Integer(0);
	// the following field should be get from the columnfamily each time
	private Integer maxXMLID = new Integer(0);
	private Permission perm = null;
	private long createDate = System.currentTimeMillis();
	private long modifyDate = System.currentTimeMillis();

	private ConcurrentMap<String, Integer> childColMap = new ConcurrentHashMap<String, Integer>();
	private ConcurrentMap<String, Integer> childXMLMap = new ConcurrentHashMap<String, Integer>();
	private ConcurrentSkipListSet<ValueType> indexTypes = new ConcurrentSkipListSet<ValueType>();

	// map the partition path to the xml docIDs
	private ConcurrentMap<String, ConcurrentSkipListSet<Integer>> path2XMLIDMap = new ConcurrentHashMap<String, ConcurrentSkipListSet<Integer>>();

	/**
	 * this constructor is used to create a metadata that will use the colID to
	 * fetch content from the server
	 * 
	 * @param colID
	 */
	public CollectionMetaData(int colID) {
		this.colID = colID;
		this.key = Int32Type.instance.fromString(Integer.toString(colID));
		indexTypes.add(ValueType.STRING);
	}

	public CollectionMetaData(int colID, String colName, Permission perm) {
		this.colID = colID;
		this.name = colName;
		this.key = Int32Type.instance.fromString(Integer.toString(colID));
		this.perm = perm;
		indexTypes.add(ValueType.STRING);
	}

	/**
	 * 
	 * @return -1 if persistence fails
	 */
	public int getNextXMLID() {
		synchronized (maxXMLID) {
			maxXMLID++;
			RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
			rm.add(new QueryPath(SysConfig.SYSCATALOG_COLLECTION, BASEINFO,
					BASEINFO_MAXXMLID), ByteBufferUtil.bytes(maxXMLID
					.intValue()), System.currentTimeMillis());
			boolean ret = XMLDBCatalogManager.instance().flushMetaData(rm);
			if (ret)
				return maxXMLID;
			else
				return -1;
		}
	}

	/**
	 * 
	 * @return -1 if persistence fails
	 */
	public int getNextColID() {
		synchronized (maxColID) {
			maxColID++;
			RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
			rm.add(new QueryPath(SysConfig.SYSCATALOG_COLLECTION, BASEINFO,
					BASEINFO_MAXCOLID), ByteBufferUtil.bytes(maxColID
					.intValue()), System.currentTimeMillis());
			boolean ret = XMLDBCatalogManager.instance().flushMetaData(rm);
			if (ret)
				return maxColID;
			else
				return -1;
		}
	}

	public Integer getCollectionID(String colName) {
		if (childColMap.get(colName) == null) {
			// Synchronize the data
			// fix the bug that when the root db is synchronized,
			// It will cause the new created collection never read by slave node
			ColumnFamilyStoreProxy store = XMLDBCatalogManager.instance().colCatalogStore;
			Row row = store.get(key.duplicate(), CHILDCOLLECTION_SP,
					UTF8Type.instance.fromString(colName),
					XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL);
			if (row != null && row.cf != null && !row.cf.isMarkedForDelete()) {
				SuperColumn sp = (SuperColumn) row.cf
						.getColumn(CHILDCOLLECTION_SP);
				if (sp == null)
					return null;
				IColumn col = sp.getColumn(UTF8Type.instance
						.fromString(colName));
				if (col != null) {
					Integer id = new Integer(ByteBufferUtil.toInt(col.value()));
					childColMap.putIfAbsent(colName, id);
				}
			}
		}
		return childColMap.get(colName);
	}

	public Integer getXMLID(String xmlDocName) {
		Integer ret = childXMLMap.get(xmlDocName);

		if (ret != null) {
			return ret;
		} else if (xmlDocName.matches("\\.\\d+")) {
			String partName = xmlDocName;
			xmlDocName = xmlDocName.substring(0, xmlDocName.lastIndexOf('.'));
			Integer pData = getXMLID(xmlDocName);
			if (pData == null)
				return -1;
			else {
				XMLMetaData p = XMLDBCatalogManager.instance()
						.getXMLMetaDataByID(colID, pData);
				if (p == null)
					return -1;
				else {
					return p.getXMLPartID(partName);
				}
			}

		}

		return -1;
	}

	public int getColID() {
		return colID;
	}

	/**
	 * remove the metadata of child collection
	 * 
	 * @param name
	 * @return
	 */
	public boolean removeCollection(String name) {
		RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
		rm.delete(new QueryPath(SysConfig.SYSCATALOG_COLLECTION,
				CHILDCOLLECTION_SP, UTF8Type.instance.fromString(name)), System
				.currentTimeMillis());
		boolean ret = XMLDBCatalogManager.instance().flushMetaData(rm);
		if (ret) {
			childColMap.remove(name);
		}
		return ret;
	}

	 public String getColName(int colID) {
		 for (Iterator<Entry<String, Integer>> iter = childColMap.entrySet()
					.iterator(); iter.hasNext();) {
				Entry<String, Integer> entry = iter.next();
				if (entry.getValue() == colID) {
					return entry.getKey();
				}
			}
		 return null;
	 }
	
	public void removeCollectionClient(int childID) {
		for (Iterator<Entry<String, Integer>> iter = childColMap.entrySet()
				.iterator(); iter.hasNext();) {
			Entry<String, Integer> entry = iter.next();
			if (entry.getValue() == childID) {
				iter.remove();
				break;
			}
		}
	}

	/**
	 * only register the name and id pair into this collection
	 * 
	 * @param name
	 * @param id
	 * @return false if the collection already exists or writing the data fails;
	 */
	public boolean addCollection(String name, Integer id) {
		Integer ret = childColMap.putIfAbsent(name, id);
		if (ret != null)
			return false;

		RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
		rm.add(new QueryPath(SysConfig.SYSCATALOG_COLLECTION,
				CHILDCOLLECTION_SP, UTF8Type.instance.fromString(name)),
				ByteBufferUtil.bytes(id.intValue()), System.currentTimeMillis());

		boolean succ = XMLDBCatalogManager.instance().flushMetaData(rm);
		if (succ == false) {
			ret = childColMap.get(name);
			if (ret.equals(id))
				childColMap.remove(name);
		}
		return succ;
	}

	/**
	 * 
	 * @param name
	 *            the name of the xml doc
	 * @param id
	 *            the id of the xml doc
	 * @return false if the doc
	 */
	public boolean addXMLdoc(String name, Integer id) {
		Integer ret = childXMLMap.putIfAbsent(name, id);
		if (ret != null)
			return false;

		RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
		rm.add(new QueryPath(SysConfig.SYSCATALOG_COLLECTION, CHILDXML_SP,
				UTF8Type.instance.fromString(name)), ByteBufferUtil.bytes(id.intValue()),
				System.currentTimeMillis());

		boolean succ = XMLDBCatalogManager.instance().flushMetaData(rm);
		if (succ == false) {
			ret = childXMLMap.get(name);
			if (ret.equals(id))
				childXMLMap.remove(name);
		}

		return succ;
	}

	public boolean removeXMLDoc(String name, Integer id) {
		childXMLMap.remove(name);
		RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
		rm.delete(new QueryPath(SysConfig.SYSCATALOG_COLLECTION, CHILDXML_SP,
				UTF8Type.instance.fromString(name)), System.currentTimeMillis());

		boolean succ = XMLDBCatalogManager.instance().flushMetaData(rm);
		if (succ == false) {
			childXMLMap.putIfAbsent(name, id);
		}
		return succ;
	}

	public int getpColID() {
		return pColID;
	}

	public void setpColID(int pColID) {
		this.pColID = pColID;
		RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
		rm.add(new QueryPath(SysConfig.SYSCATALOG_COLLECTION, BASEINFO,
				BASEINFO_PCOLID), ByteBufferUtil.bytes(pColID), System
				.currentTimeMillis());
		XMLDBCatalogManager.instance().flushMetaData(rm);
	}

	public String getName() {
		return name;
	}

	public int getXmlNum() {
		return xmlNum;
	}

	public void increXmlNum() {
		synchronized (xmlNum) {
			xmlNum++;
			RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
			rm.add(new QueryPath(SysConfig.SYSCATALOG_COLLECTION, BASEINFO,
					BASEINFO_XMLNUM), ByteBufferUtil.bytes(xmlNum.intValue()),
					System.currentTimeMillis());
			XMLDBCatalogManager.instance().flushMetaData(rm);
		}
	}

	public void decreXmlNum() {
		synchronized (xmlNum) {
			xmlNum--;
			RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
			rm.add(new QueryPath(SysConfig.SYSCATALOG_COLLECTION, BASEINFO,
					BASEINFO_XMLNUM), ByteBufferUtil.bytes(xmlNum.intValue()),
					System.currentTimeMillis());
			XMLDBCatalogManager.instance().flushMetaData(rm);
		}
	}

	public void setXmlNum(int xmlNum_) {
		synchronized (xmlNum) {
			this.xmlNum = new Integer(xmlNum_);

			RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
			rm.add(new QueryPath(SysConfig.SYSCATALOG_COLLECTION, BASEINFO,
					BASEINFO_XMLNUM), ByteBufferUtil.bytes(xmlNum.intValue()),
					System.currentTimeMillis());
			if (XMLDBCatalogManager.instance().flushMetaData(rm) == false) {
				// TODO(xiafan): this failure mutations should be recorded into
				// logs to
				// handle in future
			}
		}
	}

	/**
	 * the xml identified by xmlDocID is partitioned by partitionPath
	 * 
	 * @param partitionPath
	 * @param xmlDocID
	 */
	public void addXMLPartitionPath(String partitionPath, int xmlDocID) {
		ConcurrentSkipListSet<Integer> xmlDocIDs = path2XMLIDMap
				.get(partitionPath);
		if (xmlDocIDs == null) {
			xmlDocIDs = new ConcurrentSkipListSet<Integer>();
			xmlDocIDs.add(xmlDocID);
			xmlDocIDs = path2XMLIDMap.putIfAbsent(partitionPath, xmlDocIDs);
			// check again
			if (xmlDocIDs != null)
				xmlDocIDs.add(xmlDocID);
			else
				xmlDocIDs = path2XMLIDMap.get(partitionPath);
		} else {
			xmlDocIDs.add(xmlDocID);
		}

		synchronized (xmlDocIDs) {
			try {
				RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(bos);
				for (Integer xmlID : xmlDocIDs) {
					dos.writeInt(xmlID);
				}
				rm.add(new QueryPath(SysConfig.SYSCATALOG_COLLECTION,
						PARTITION_PATH_SP, UTF8Type.instance
								.fromString(partitionPath)), ByteBuffer
						.wrap(bos.toByteArray()), System.currentTimeMillis());

				boolean succ = XMLDBCatalogManager.instance().flushMetaData(rm);
				if (succ == false) {
					// setDirty(true, true);
				}
			} catch (IOException e) {
				/*
				 * TODO this error is fatal, how to handle it so that no
				 * metadata is lost
				 */
				throw new RuntimeException(e);
			}
		}
	}

	public List<Integer> getXMLDocByPartitionPath(String partitionPath) {
		ConcurrentSkipListSet<Integer> xmlDocIDs = path2XMLIDMap
				.get(partitionPath);
		if (xmlDocIDs != null) {
			return new LinkedList<Integer>(xmlDocIDs);
		} else {
			return new LinkedList<Integer>();
		}
	}

	public long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
		RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
		rm.add(new QueryPath(SysConfig.SYSCATALOG_COLLECTION, BASEINFO,
				BASEINFO_CREATEDATE), ByteBufferUtil.bytes(createDate), System
				.currentTimeMillis());
		XMLDBCatalogManager.instance().flushMetaData(rm);
	}

	public long getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(long modifyDate) {
		this.modifyDate = modifyDate;
		RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
		rm.add(new QueryPath(SysConfig.SYSCATALOG_COLLECTION, BASEINFO,
				BASEINFO_MODIFYDATE), ByteBufferUtil.bytes(modifyDate), System
				.currentTimeMillis());
		XMLDBCatalogManager.instance().flushMetaData(rm);
	}

	public Permission getPermission() {
		return perm;
	}

	public void setPermission(Permission perm) {
		this.perm = perm;
		RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
		UnixStylePermissionSerializer.write(perm, rm,
				SysConfig.SYSCATALOG_COLLECTION);
		// perm.write(rm, SysConfig.SYSCATALOG_COLLECTION);
		XMLDBCatalogManager.instance().flushMetaData(rm);
	}

	public boolean isDirty() {
		return isDirty;
	}

	/**
	 * when the data of this object is changed, this function should be called
	 * at the end
	 * 
	 * @param isDirty
	 */
	/*
	 * public void setDirty(boolean isDirty, boolean flush) { if (isDirty ==
	 * false) { this.isDirty = false; } else if (isDirty == true && this.isDirty
	 * == false) { this.isDirty = isDirty; if (flush)
	 * XMLDBCatalogManager.instance().flushDirtyColMetaData(this); } }
	 */

	public void addIndex(ValueType valueType) {
		RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
		rm.add(new QueryPath(SysConfig.SYSCATALOG_COLLECTION, INDEXTYPE_SP,
				Int32Type.instance.decompose(valueType.ordinal())),
				ByteBufferUtil.EMPTY_BYTE_BUFFER, System.currentTimeMillis());

		if (XMLDBCatalogManager.instance().flushMetaData(rm))
			indexTypes.add(valueType);
	}

	public Set<ValueType> getIndexTypes() {
		return indexTypes;
	}

	public Set<String> getChildColNames() {
		return childColMap.keySet();
	}

	public Collection<Integer> getXMLDocIDs() {
		return childXMLMap.values();
	}

	public Collection<String> getXMLDocNames() {
		return childXMLMap.keySet();
	}

	public String getCFName() {
		return getCFName(colID);
	}

	public static String getCFName(int colID_) {
		return COLDATA_CFNAME_PREFIX + colID_;
	}

	public String getBtreeSuffix(ValueType type) {
		return "btree_" + colID + "_" + type;
	}

	public String getInvertIndexName(String type) {
		return "invert_" + type + "_" + colID;
	}

	public String getBtreeSuffix(String type) {
		return "btree_" + colID + "_" + type;
	}

	public String getFullName() {
		String ret = name;
		int curColID = pColID;
		while (curColID != -1) {
			CollectionMetaData metaData = XMLDBCatalogManager.instance()
					.getCollectionMeta(curColID);
			ret = metaData.getName() + "/" + ret;
			curColID = metaData.getpColID();
		}
		ret = "/" + ret;
		return ret;
	}

	@Override
	public void serialize(ColumnFamilyStoreProxy cfStore) {
		if (XMLDBStore.instance().isMutatable() == false)
			return;
		// setDirty(false, false);
		ByteBuffer key = Int32Type.instance.fromString(Integer.toString(colID));
		RowMutation mutation = cfStore.incre_insert(key.duplicate(), BASEINFO,
				BASEINFO_NAME, ByteBufferUtil.bytes(name), null);

		/*
		 * cfStore.incre_insert(key, BASEINFO, BASEINFO_NAME,
		 * ByteBufferUtil.bytes(name), mutation);
		 */

		cfStore.incre_insert(key, BASEINFO, BASEINFO_PCOLID,
				ByteBufferUtil.bytes(pColID), mutation);

		cfStore.incre_insert(key, BASEINFO, BASEINFO_XMLNUM,
				ByteBufferUtil.bytes(xmlNum), mutation);

		cfStore.incre_insert(key, BASEINFO, BASEINFO_MAXCOLID,
				ByteBufferUtil.bytes(maxColID.intValue()), mutation);

		cfStore.incre_insert(key, BASEINFO, BASEINFO_MAXXMLID,
				ByteBufferUtil.bytes(maxXMLID.intValue()), mutation);

		cfStore.incre_insert(key, BASEINFO, BASEINFO_CREATEDATE,
				ByteBufferUtil.bytes(createDate), mutation);

		cfStore.incre_insert(key, BASEINFO, BASEINFO_MODIFYDATE,
				ByteBufferUtil.bytes(modifyDate), mutation);

		if (perm != null)
			// mutation = perm.write(mutation, cfStore.cfName);
			mutation = UnixStylePermissionSerializer.write(perm, mutation,
					SysConfig.SYSCATALOG_COLLECTION);

		for (Map.Entry<String, Integer> xmlDoc : childXMLMap.entrySet()) {
			cfStore.incre_insert(key, CHILDXML_SP,
					UTF8Type.instance.fromString(xmlDoc.getKey()),
					ByteBufferUtil.bytes(xmlDoc.getValue()), mutation);
		}

		for (Map.Entry<String, Integer> col : childColMap.entrySet()) {
			cfStore.incre_insert(key.duplicate(), CHILDCOLLECTION_SP,
					UTF8Type.instance.fromString(col.getKey()),
					ByteBufferUtil.bytes(col.getValue()), mutation);
		}

		// serialize the parition path
		try {
			for (Entry<String, ConcurrentSkipListSet<Integer>> entry : path2XMLIDMap
					.entrySet()) {

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(bos);
				for (Integer xmlID : entry.getValue()) {
					dos.writeInt(xmlID);
				}

				cfStore.incre_insert(key.duplicate(), PARTITION_PATH_SP,
						UTF8Type.instance.fromString(entry.getKey()),
						ByteBuffer.wrap(bos.toByteArray()), mutation);
			}
		} catch (IOException e) {
			/*
			 * TODO this error is fatal, how to handle it so that no metadata is
			 * lost
			 */
			throw new RuntimeException(e);
		}
		cfStore.mutate(mutation, XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL);
		/*
		 * cfStore.batch_mutate(mutations,
		 * XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL);
		 */
	}

	@Override
	public void deserialize(ColumnFamilyStoreProxy cfStore) {
		// setDirty(false, false);
		ByteBuffer key = ByteBufferUtil.bytes(colID);

		/*
		 * List<Row> rows = cfStore.getSuperColumn(key.duplicate(), BASEINFO,
		 * null, XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL);
		 */
		Row row = cfStore.get(key.duplicate(), null, null,
				XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL);
		if (row == null || row.cf == null)
			return;

		IColumn col = row.cf.getColumn(BASEINFO);
		if (col != null) {
			try {
				if (col.getSubColumn(BASEINFO_NAME) != null)
					name = ByteBufferUtil.string(col
							.getSubColumn(BASEINFO_NAME).value());
				if (col.getSubColumn(BASEINFO_PCOLID) != null)
					pColID = ByteBufferUtil.toInt(col.getSubColumn(
							BASEINFO_PCOLID).value());
				if (col.getSubColumn(BASEINFO_CREATEDATE) != null)
					createDate = ByteBufferUtil.toLong(col.getSubColumn(
							BASEINFO_CREATEDATE).value());
				if (col.getSubColumn(BASEINFO_MODIFYDATE) != null)
					modifyDate = ByteBufferUtil.toLong(col.getSubColumn(
							BASEINFO_MODIFYDATE).value());
				if (col.getSubColumn(BASEINFO_MAXXMLID) != null)
					maxXMLID = new Integer(col.getSubColumn(BASEINFO_MAXXMLID)
							.value().get());
				if (col.getSubColumn(BASEINFO_MAXCOLID) != null)
					maxColID = new Integer(ByteBufferUtil.toInt(col
							.getSubColumn(BASEINFO_MAXCOLID).value()));
				if (col.getSubColumn(BASEINFO_XMLNUM) != null)
					xmlNum = new Integer(ByteBufferUtil.toInt(col.getSubColumn(
							BASEINFO_XMLNUM).value()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// name = null;
				e.printStackTrace();
			}
		}

		perm = PermissionFactory.getPermission();
		perm = UnixStylePermissionSerializer.read(perm, row);
		// perm.read(row);

		childXMLMap.clear();
		col = row.cf.getColumn(CHILDXML_SP);
		if (col != null) {
			try {
				String XMLName = null;
				Integer id = null;
				for (IColumn subCol : col.getSubColumns()) {
					XMLName = ByteBufferUtil.string(subCol.name());
					id = new Integer(ByteBufferUtil.toInt(subCol.value()));
					childXMLMap.put(XMLName, id);
				}
			} catch (CharacterCodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		childColMap.clear();
		col = row.cf.getColumn(CHILDCOLLECTION_SP);
		if (col != null) {
			try {
				String colName = null;
				Integer id = null;
				for (IColumn subCol : col.getSubColumns()) {
					colName = ByteBufferUtil.string(subCol.name());
					id = new Integer(ByteBufferUtil.toInt(subCol.value()));
					childColMap.put(colName, id);
				}
			} catch (CharacterCodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		col = row.cf.getColumn(PARTITION_PATH_SP);
		if (col != null) {
			path2XMLIDMap.clear();
			try {
				String colName = null;
				Integer id = null;
				for (IColumn subCol : col.getSubColumns()) {
					colName = ByteBufferUtil.string(subCol.name());
					ConcurrentSkipListSet<Integer> xmlDocs = new ConcurrentSkipListSet<Integer>();
					while (subCol.value().remaining() > 0) {
						xmlDocs.add(subCol.value().getInt());
					}
					path2XMLIDMap.put(colName, xmlDocs);
				}
			} catch (CharacterCodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		col = row.cf.getColumn(INDEXTYPE_SP);
		if (col != null) {
			indexTypes.clear();
			for (IColumn subCol : col.getSubColumns()) {
				indexTypes.add(ValueType.getValueType(Int32Type.instance
						.compose(subCol.name())));
			}
		}

	}

	public boolean isRootCol() {
		return pColID == -1;
	}

}

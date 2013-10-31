/**
 *@author:xiafan xiafan68@gmail.com
 *@version: 2011-10-3 0.1
 */
package imc.disxmldb.config;

import imc.disxmldb.util.ColumnFamilyStoreSerializable;
import imc.disxmldb.util.ColumnFamilyStoreProxy;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.RowMutation;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.db.marshal.MarshalException;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.service.StorageProxy;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.cassandra.utils.ByteBufferUtil;

/**
 * this meta data is stored in keyspace:xmldb_sys_catalog, columnfamily:
 * xmlcatalog. besides, maxNodeID is stored in a special columnfamily
 * 
 * @author Administrator
 * 
 */
public class XMLMetaData implements ColumnFamilyStoreSerializable {
	public static final ByteBuffer BASEINFO = SysConfig.XMLCf.getComparatorFor(
			null).fromString("baseinfo");
	public static final ByteBuffer XMLPART_MAPPING = SysConfig.XMLCf
			.getComparatorFor(ByteBufferUtil.EMPTY_BYTE_BUFFER).fromString(
					"xmlpart_mapping");
	public static final ByteBuffer XMLNAMESPACE_MAPPING_SP = SysConfig.XMLCf
			.getComparatorFor(ByteBufferUtil.EMPTY_BYTE_BUFFER).fromString(
					"namesp_mapping");
	public static final ByteBuffer XMLPART_SP = SysConfig.XMLCf
			.getComparatorFor(ByteBufferUtil.EMPTY_BYTE_BUFFER).fromString(
					"xmlpartsid");
	public static final ByteBuffer XMLSCHEMAMAP_SP = SysConfig.XMLCf
			.getComparatorFor(ByteBufferUtil.EMPTY_BYTE_BUFFER).fromString(
					"schemamap");

	public static final ByteBuffer BASEINFO_NAME = SysConfig.XMLCf
			.getComparatorFor(ByteBufferUtil.EMPTY_BYTE_BUFFER).fromString(
					"name");
	public static final ByteBuffer BASEINFO_SCHEMANAME = SysConfig.XMLCf
			.getComparatorFor(ByteBufferUtil.EMPTY_BYTE_BUFFER).fromString(
					"schemaname");
	public static final ByteBuffer BASEINFO_CREATEDATE = SysConfig.XMLCf
			.getComparatorFor(ByteBufferUtil.EMPTY_BYTE_BUFFER).fromString(
					"createdate");
	public static final ByteBuffer BASEINFO_MODIFYDATE = SysConfig.XMLCf
			.getComparatorFor(ByteBufferUtil.EMPTY_BYTE_BUFFER).fromString(
					"modifydate");
	public static final ByteBuffer BASEINFO_SPLITTED = SysConfig.XMLCf
			.getComparatorFor(ByteBufferUtil.EMPTY_BYTE_BUFFER).fromString(
					"splitted");
	public static final ByteBuffer BASEINFO_PXMLID = SysConfig.XMLCf
			.getComparatorFor(ByteBufferUtil.EMPTY_BYTE_BUFFER).fromString(
					"pxmlid");
	public static final ByteBuffer BASEINFO_ROOTXMLPARTID = SysConfig.XMLCf
			.getComparatorFor(ByteBufferUtil.EMPTY_BYTE_BUFFER).fromString(
					"rootxmlpartid");
	public static final ByteBuffer BASEINFO_MAXNODEID = SysConfig.XMLCf
			.getComparatorFor(ByteBufferUtil.EMPTY_BYTE_BUFFER).fromString(
					"maxnodeid");
	public static final ByteBuffer BASEINFO_XMLNODENUM = SysConfig.XMLCf
			.getComparatorFor(ByteBufferUtil.EMPTY_BYTE_BUFFER).fromString(
					"xmlnodenum");
	public static final ColumnFamilyStoreProxy metaStore = new ColumnFamilyStoreProxy(
			SysConfig.SYSCATALOG, SysConfig.SYSCATALOG_XML,
			DataFlushServiceFactory.instance.getMetaDataFlushService());

	public static final int XMLROOT_NODEID = 0;

	public Lock lock = new ReentrantLock();
	private final int XMLID;
	private final int colID;
	private int pXMLID = -1; // the document id of the parent
	private String name = null;
	private String schemaName = "";
	private long createDate = System.currentTimeMillis();
	private long modifyDate = System.currentTimeMillis();

	private byte splitted = 0;
	private AtomicInteger maxNodeID = new AtomicInteger(-1); // used to
																// generator
																// nodeIDs. It
																// is often
																// incremented
																// repeatly but
																// flushed at
																// end.
	private Integer xmlNodeNum = new Integer(0); // increment by a number and
													// stored in the db, which
													// is
	// an atomic unit
	private int rootXMLPartID = -1; // the current implementation replicate the
									// common part to all xml parts;
	private ConcurrentHashMap<String, Integer> childXMLMappings = new ConcurrentHashMap<String, Integer>();
	private volatile Integer lastXMLPartID = null;

	private boolean isDirty = false;
	private String partitionPath = null;

	private Map<String, String> namespaceMappings = new ConcurrentHashMap<String, String>();

	private ByteBuffer key = null;

	public XMLMetaData(int colID, int XMLID) {
		this.colID = colID;
		this.XMLID = XMLID;
		key = SysConfig.xmlCatalogKeyValidator.fromString(Integer
				.toString(colID) + ":" + Integer.toString(XMLID));
	}

	public ByteBuffer composeKey() {
		return key;
	}

	public static ByteBuffer composeKey(int colID, int xmlID) {
		return SysConfig.xmlCatalogKeyValidator.fromString(Integer
				.toString(colID) + ":" + Integer.toString(xmlID));
	}

	public Collection<Integer> getXMLPartIDs() {
		return childXMLMappings.values();
	}

	public Integer getXMLPartID(String name) {
		return childXMLMappings.get(name);
	}

	/*
	 * public boolean addXMLPart(Integer id) { XMLPartIDs.add(id); //
	 * XMLPartNum++; setDirty(true, true); return true; }
	 */

	public boolean addXMLPart(String name, Integer id) {
		Integer ret = childXMLMappings.putIfAbsent(name, id);
		if (ret == null) {
			RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
			rm.add(new QueryPath(SysConfig.SYSCATALOG_XML, XMLPART_MAPPING,
					UTF8Type.instance.fromString(name)), ByteBufferUtil
					.bytes(id), System.currentTimeMillis());

			boolean succ = XMLDBCatalogManager.instance().flushMetaData(rm);
			if (succ == false) {
				childXMLMappings.remove(name);
				return false;
			}

			if (lastXMLPartID == null
					|| lastXMLPartID.intValue() < id.intValue())
				lastXMLPartID = id.intValue();
			return true;
		} else {
			return false;
		}
	}

	public Integer getLastXMLPartID() {
		return lastXMLPartID;
	}

	/**
	 * only one thread can invoke this function
	 * 
	 * @return
	 */
	public synchronized Integer getLastEffectPartID() {
		XMLMetaData ret = null;
		Integer docID = getLastXMLPartID();
		if (docID == null
				|| ((ret = XMLDBCatalogManager.instance().getXMLMetaDataByID(
						colID, docID)) == null)
				|| (ret.getMaxNodeID() >= SysConfig.SPLIT_THRESHOLD)) {
			// new an xmlpart
			ret = XMLDBCatalogManager.instance().newXMLPart(this);
			return ret.getXMLID();
		}
		return ret.getXMLID();
	}

	public boolean isDirty() {
		return isDirty;
	}

	/*
	 * public void setDirty(boolean isDirty, boolean flush) { if (isDirty ==
	 * false) { this.isDirty = isDirty; } else if (isDirty == true &&
	 * this.isDirty == false) { this.isDirty = isDirty; if (flush)
	 * XMLDBCatalogManager.instance().flushDirtyXMLMetaData(this); } }
	 */

	public int getXMLID() {
		return XMLID;
	}

	public int getColID() {
		return colID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
		rm.add(new QueryPath(SysConfig.SYSCATALOG_XML, BASEINFO, BASEINFO_NAME),
				ByteBufferUtil.bytes(name), System.currentTimeMillis());
		XMLDBCatalogManager.instance().flushMetaData(rm);
	}

	public long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
		RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
		rm.add(new QueryPath(SysConfig.SYSCATALOG_XML, BASEINFO,
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
		rm.add(new QueryPath(SysConfig.SYSCATALOG_XML, BASEINFO,
				BASEINFO_MODIFYDATE), ByteBufferUtil.bytes(modifyDate), System
				.currentTimeMillis());
		XMLDBCatalogManager.instance().flushMetaData(rm);
	}

	public byte getSplitted() {
		return splitted;
	}

	public void setSplitted(byte splitted) {
		this.splitted = splitted;
		RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
		rm.add(new QueryPath(SysConfig.SYSCATALOG_XML, BASEINFO,
				BASEINFO_SPLITTED), ByteBufferUtil.bytes(splitted), System
				.currentTimeMillis());
		XMLDBCatalogManager.instance().flushMetaData(rm);
	}

	public int getpXMLID() {
		return pXMLID;
	}

	public void setpXMLID(int pXMLID) {
		this.pXMLID = pXMLID;
		RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
		rm.add(new QueryPath(SysConfig.SYSCATALOG_XML, BASEINFO,
				BASEINFO_PXMLID), ByteBufferUtil.bytes(pXMLID), System
				.currentTimeMillis());
		XMLDBCatalogManager.instance().flushMetaData(rm);
	}

	public int getMaxNodeID() {
		return maxNodeID.get();
	}

	public void setMaxNodeID(int maxNodeID) {
		this.maxNodeID.set(maxNodeID);
		RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
		rm.add(new QueryPath(SysConfig.SYSCATALOG_XML, BASEINFO,
				BASEINFO_MAXNODEID), ByteBufferUtil.bytes(maxNodeID), System
				.currentTimeMillis());
		XMLDBCatalogManager.instance().flushMetaData(rm);
	}

	public int increAndGetMaxNodeID() {
		int ret = maxNodeID.incrementAndGet();
		return ret;
	}

	public boolean syncMaxNodeID() {
		RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
		rm.add(new QueryPath(SysConfig.SYSCATALOG_XML, BASEINFO,
				BASEINFO_MAXNODEID), ByteBufferUtil.bytes(maxNodeID.get()),
				System.currentTimeMillis());
		return XMLDBCatalogManager.instance().flushMetaData(rm);
	}

	public int getRootXMLPartID() {
		return rootXMLPartID;
	}

	public void setRootXMLPartID(int rootXMLPartID) {
		this.rootXMLPartID = rootXMLPartID;
		RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
		rm.add(new QueryPath(SysConfig.SYSCATALOG_XML, BASEINFO,
				BASEINFO_ROOTXMLPARTID), ByteBufferUtil.bytes(rootXMLPartID),
				System.currentTimeMillis());
		XMLDBCatalogManager.instance().flushMetaData(rm);
	}

	/*
	 * public String getPartitionPath() { return partitionPath; }
	 * 
	 * public void setPartitionPath(String partitionPath, boolean flush) {
	 * this.partitionPath = partitionPath; RowMutation rm = new
	 * RowMutation(SysConstant.SYSCATALOG, key); rm.add(new
	 * QueryPath(SysConstant.SYSCATALOG_XML, BASEINFO, BASEINFO_MAXNODEID),
	 * ByteBufferUtil .bytes(maxNodeID.get()), System.currentTimeMillis()); try
	 * { StorageProxy.mutate(Arrays.asList(rm),
	 * XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL); } catch (Exception e) {
	 * setDirty(true, true); } }
	 */

	public void addNamespaceMappings(String prefix, String uri) {
		namespaceMappings.put(uri, prefix);
		RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
		rm.add(new QueryPath(SysConfig.SYSCATALOG_XML, XMLNAMESPACE_MAPPING_SP,
				SysConfig.XMLCf.getComparatorFor(
						ByteBufferUtil.EMPTY_BYTE_BUFFER).fromString(prefix)),
				ByteBufferUtil.bytes(uri), System.currentTimeMillis());
		if (XMLDBCatalogManager.instance().flushMetaData(rm) == false) {
			namespaceMappings.remove(uri);
		}
	}

	public Map<String, String> getNamespaceMappings() {
		return namespaceMappings;
	}

	public String getFullName() {
		CollectionMetaData meta = XMLDBCatalogManager.instance()
				.getCollectionMeta(colID);
		return meta.getFullName() + "/" + name;
	}

	public boolean shouldSplit() {
		return xmlNodeNum > SysConfig.SPLIT_THRESHOLD;
	}

	public boolean shouldSplit(int incre) {
		return xmlNodeNum + incre > SysConfig.SPLIT_THRESHOLD;
	}

	public void increXMLNodeNum(int incre) {
		synchronized (xmlNodeNum) {
			xmlNodeNum = new Integer(xmlNodeNum + incre);
			if (metaStore.insert(key, BASEINFO, BASEINFO_XMLNODENUM,
					ByteBufferUtil.bytes(xmlNodeNum),
					XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL) == false)
				isDirty = true;
		}
	}

	public void decreXMLNodeNum(int decre) {
		synchronized (xmlNodeNum) {
			xmlNodeNum = new Integer(xmlNodeNum - decre);
			if (metaStore.insert(key, BASEINFO, BASEINFO_XMLNODENUM,
					ByteBufferUtil.bytes(xmlNodeNum),
					XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL) == false)
				isDirty = true;
		}
	}

	@Override
	public void serialize(ColumnFamilyStoreProxy cfStore) {
		RowMutation mutation = null;
		try {
			ByteBuffer key = composeKey();
			mutation = cfStore.incre_insert(key.duplicate(), BASEINFO,
					BASEINFO_NAME, ByteBufferUtil.bytes(name), null);
			mutation = cfStore.incre_insert(key.duplicate(), BASEINFO,
					BASEINFO_SCHEMANAME, ByteBufferUtil.bytes(schemaName),
					mutation);
			mutation = cfStore.incre_insert(key.duplicate(), BASEINFO,
					BASEINFO_CREATEDATE, ByteBufferUtil.bytes(createDate),
					mutation);
			mutation = cfStore.incre_insert(key.duplicate(), BASEINFO,
					BASEINFO_MODIFYDATE, ByteBufferUtil.bytes(modifyDate),
					mutation);
			mutation = cfStore
					.incre_insert(key.duplicate(), BASEINFO, BASEINFO_SPLITTED,
							ByteBufferUtil.bytes(splitted), mutation);
			mutation = cfStore.incre_insert(key.duplicate(), BASEINFO,
					BASEINFO_PXMLID, ByteBufferUtil.bytes(pXMLID), mutation);
			mutation = cfStore.incre_insert(key.duplicate(), BASEINFO,
					BASEINFO_ROOTXMLPARTID, ByteBufferUtil.bytes(rootXMLPartID),
					mutation);
			mutation = cfStore.incre_insert(key.duplicate(), BASEINFO,
					BASEINFO_MAXNODEID, ByteBufferUtil.bytes(maxNodeID.get()),
					mutation);
			mutation = cfStore.incre_insert(key.duplicate(), BASEINFO,
					BASEINFO_XMLNODENUM, ByteBufferUtil.bytes(xmlNodeNum),
					mutation);

			/*
			 * for (Integer id : XMLPartIDs) { cfStore.insert(key.duplicate(),
			 * XMLPART_SP, SysConstant.XMLCf
			 * .getComparatorFor(ByteBufferUtil.EMPTY_BYTE_BUFFER)
			 * .fromString(id.toString()), ByteBufferUtil.EMPTY_BYTE_BUFFER,
			 * XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL); }
			 */

			/*
			 * for (Map.Entry<String, String> entry :
			 * namespaceMappings.entrySet()) { cfStore.insert( key.duplicate(),
			 * XMLSCHEMAMAP_SP, SysConstant.XMLCf.getComparatorFor(
			 * ByteBufferUtil.EMPTY_BYTE_BUFFER).fromString( entry.getKey()),
			 * UTF8Type.instance.fromString(entry.getValue()),
			 * XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL); }
			 */

		} catch (Exception e) {
			/*
			 * TODO this error is fatal, how to handle it so that no metadata is
			 * lost
			 */
			throw new RuntimeException(e);
		}
		cfStore.mutate(mutation, XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL);
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setNamespaceMappings(Map<String, String> namespaceMappings) {
		this.namespaceMappings = namespaceMappings;
		RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
		for (Entry<String, String> entry : namespaceMappings.entrySet()) {
			rm.add(new QueryPath(SysConfig.SYSCATALOG_XML, XMLSCHEMAMAP_SP,
					SysConfig.XMLCf.getComparatorFor(
							ByteBufferUtil.EMPTY_BYTE_BUFFER).fromString(
							entry.getValue())), ByteBufferUtil.bytes(entry
					.getKey()), System.currentTimeMillis());
		}
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
		RowMutation rm = new RowMutation(SysConfig.SYSCATALOG, key);
		rm.add(new QueryPath(SysConfig.SYSCATALOG_XML, BASEINFO,
				BASEINFO_SCHEMANAME), ByteBufferUtil.bytes(schemaName), System
				.currentTimeMillis());
		XMLDBCatalogManager.instance().flushMetaData(rm);
	}

	@Override
	public void deserialize(ColumnFamilyStoreProxy cfStore) {
		synchronized (this) {
			ByteBuffer key = composeKey();
			List<Row> rows = cfStore.getSuperColumn(key.duplicate(), BASEINFO,
					null, XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL);
			if (rows.size() == 0) {
				return;
			} else {
				for (Row row : rows) {
					if (row.cf == null)
						continue;
					IColumn col = row.cf.getColumn(BASEINFO);
					try {
						name = ByteBufferUtil.string(col.getSubColumn(
								BASEINFO_NAME).value());
						schemaName = ByteBufferUtil.string(col.getSubColumn(
								BASEINFO_SCHEMANAME).value());
						createDate = ByteBufferUtil.toLong(col.getSubColumn(
								BASEINFO_CREATEDATE).value());
						modifyDate = ByteBufferUtil.toLong(col.getSubColumn(
								BASEINFO_MODIFYDATE).value());
						splitted = col.getSubColumn(BASEINFO_SPLITTED).value()
								.get();
						pXMLID = ByteBufferUtil.toInt(col.getSubColumn(
								BASEINFO_PXMLID).value());
						maxNodeID.set(ByteBufferUtil.toInt(col.getSubColumn(
								BASEINFO_MAXNODEID).value()));
						rootXMLPartID = ByteBufferUtil.toInt(col.getSubColumn(
								BASEINFO_ROOTXMLPARTID).value());
						xmlNodeNum = ByteBufferUtil.toInt(col.getSubColumn(
								BASEINFO_XMLNODENUM).value());
					} catch (CharacterCodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			rows = cfStore.getSuperColumn(key, XMLPART_MAPPING, null,
					XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL);

			if (rows == null || rows.size() == 0) {
				return;
			} else {
				int maxID = -1;
				childXMLMappings.clear();
				for (Row row : rows) {
					if (row.cf == null)
						continue;

					IColumn col = row.cf.getColumn(XMLPART_MAPPING);

					for (IColumn subColumn : col.getSubColumns()) {
						int id = ByteBufferUtil.toInt(subColumn.value());
						childXMLMappings.put(
								UTF8Type.instance.getString(subColumn.name()),
								id);
						if (id > maxID)
							maxID = id;
					}
				}

				if (maxID != -1)
					lastXMLPartID = maxID;
				/*
				 * XMLPartIDs.clear(); for (Row row : rows) { if (row.cf ==
				 * null) continue; IColumn col = row.cf.getColumn(XMLPART_SP);
				 * try { for (IColumn subCol : col.getSubColumns()) {
				 * 
				 * XMLPartIDs.add(Integer.parseInt(ByteBufferUtil
				 * .string(subCol.name())));
				 * 
				 * } } catch (NumberFormatException e) { // TODO Auto-generated
				 * catch block e.printStackTrace(); } catch
				 * (CharacterCodingException e) { // TODO Auto-generated catch
				 * block e.printStackTrace(); } }
				 */
			}
		}
	}

}

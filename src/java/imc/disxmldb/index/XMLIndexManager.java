package imc.disxmldb.index;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.XMLDBStore;
import imc.disxmldb.config.CollectionMetaData;
import imc.disxmldb.config.SysConfig;
import imc.disxmldb.dom.XMLNode;
import imc.disxmldb.dom.XMLNodeImpl;
import imc.disxmldb.dom.cache.CacheManager.CacheType;
import imc.disxmldb.dom.typesystem.ValueType;
import imc.disxmldb.index.btree.ComposeKeyBtree;
import imc.disxmldb.index.btree.BtreeFactory;
import imc.disxmldb.index.btree.CFBasedBtree;
import imc.disxmldb.index.btree.IBtree;
import imc.disxmldb.index.invertindex.CFInvertIndex;
import imc.disxmldb.index.invertindex.IInvertIndex;
import imc.disxmldb.util.CassandraSchemaProxy;
import imc.disxmldb.util.LocalCFStoreProxy;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cassandra.config.Config.XMLServerMode;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.SuperColumn;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.db.commitlog.ReplayPosition;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.WrappedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.base.XMLDBException;

/**
 * currently there are element tag index, attribute tag index, string value
 * index, filesize index and inverted index. This class is responsible for the
 * creation and maintenance of those indices. When an XML Node is inserted,
 * updated or deleted, this class determines which indices to update. It is also
 * responsible for the deletion of index files on the disk once they need to be
 * deleted.
 * 
 */
public class XMLIndexManager {
	private static final String NODETAG_INDEX_SUFFIX = "nodeTag";
	private static final String ATTRTAG_INDEX_SUFFIX = "attrTag";
	private static final Logger logger = LoggerFactory
			.getLogger(XMLIndexManager.class);

	private ColumnFamilyStore baseCfs = null;
	private int colID = -1;
	private CollectionMetaData metaData = null;
	private CollectionStore colStore = null;
	private IInvertIndex textInvertIndex = null;

	private IBtree nodeTagIndex = null;
	private IBtree attrTagIndex = null;

	private Map<ValueType, IBtree> btreeIndexes = null;

	public XMLIndexManager(ColumnFamilyStore cfStore) {
		this.baseCfs = cfStore;
		init();
	}

	public void init() {

		String columnFamilyName = baseCfs.getColumnFamilyName();
		colID = Integer.parseInt(columnFamilyName.substring(
				columnFamilyName.lastIndexOf('_') + 1,
				columnFamilyName.length()));
		metaData = new CollectionMetaData(colID);

		ColumnFamilyStore invertIndexCf = CassandraSchemaProxy
				.createInvertIndexCFMetaData(
						baseCfs.table,
						baseCfs.columnFamily + "."
								+ metaData.getInvertIndexName("default"));
		LocalCFStoreProxy cfProxy = new LocalCFStoreProxy(invertIndexCf.table,
				invertIndexCf);
		textInvertIndex = new CFInvertIndex(cfProxy);

		initBtree();
	}

	public void initBtree() {
		/*
		 * * the following index should be initialized to created on the disk
		 */
		btreeIndexes = new HashMap<ValueType, IBtree>();

		String[] dataDirs = DatabaseDescriptor.getAllDataFileLocations();
		List<String> btreeFiles = searchBtreeIndex(dataDirs,
				SysConfig.DEFAULT_KEYSPACE);
		for (String btreeFile : btreeFiles) {
			String typeStr = btreeFile.substring(
					btreeFile.lastIndexOf('_') + 1, btreeFile.length());
			try {
				ValueType valueType = ValueType.valueOf(typeStr);
				if (ValueType.isFileSizeType(valueType))
					valueType = ValueType.FILESIZE;
				btreeIndexes.put(valueType, createBtree(valueType));
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
		if (btreeIndexes.get(ValueType.NODETAG) == null) {
			btreeIndexes.put(ValueType.NODETAG, createBtree(ValueType.NODETAG));
		}

		if (btreeIndexes.get(ValueType.ATTRTAG) == null) {
			btreeIndexes.put(ValueType.ATTRTAG, createBtree(ValueType.ATTRTAG));
		}
	}

	public IInvertIndex getTextInvertIndex() {
		return textInvertIndex;
	}

	public IBtree createBtree(ValueType valueType) {
		IBtree ret = null;
		try {
			String[] dataDirs = DatabaseDescriptor.getAllDataFileLocations();

			String btreeIndexDir = checkDir(dataDirs,
					SysConfig.DEFAULT_KEYSPACE,
					metaData.getBtreeSuffix(valueType));
			if (btreeIndexDir == null) {
				// this is the first the invert index is created

				btreeIndexDir = dataDirs[0] + File.separator
						+ SysConfig.DEFAULT_KEYSPACE + File.separator
						+ metaData.getBtreeSuffix(valueType);
				File file = new File(btreeIndexDir);
				file.mkdirs();
			}
			ret = BtreeFactory.createBtree(btreeIndexDir,
					DatabaseDescriptor.getBtreeCacheSize(), valueType);
			// metaData.addIndex(valueType);
		} catch (Exception ex) {

		}
		return ret;
	}

	public CollectionStore getColStore() {
		if (colStore == null) {
			try {
				colStore = XMLDBStore.instance().getCollection(colID);
			} catch (XMLDBException e) {
			}
		}
		return colStore;

	}

	/**
	 * check whether dirName exists under dirs and is a directory
	 * 
	 * @param dirs
	 * @param dirName
	 * @return
	 */
	private String checkDir(String[] dirs, String keyspace, String dirName) {
		for (String dataDir : dirs) {
			File dataPath = new File(dataDir + File.separator + keyspace,
					dirName);
			if (dataPath.exists() && dataPath.isDirectory()) {
				return dataPath.getAbsolutePath();
			}
		}
		return null;
	}

	private List<String> searchBtreeIndex(String[] dirs, String keyspace) {
		List<String> ret = new LinkedList<String>();
		for (String dataDir : dirs) {
			File dataPath = new File(dataDir + File.separator + keyspace);
			File[] files = dataPath.listFiles(new FileFilter() {
				Pattern pattern = Pattern.compile("btree_"
						+ metaData.getColID() + "_.+");

				@Override
				public boolean accept(File pathname) {
					Matcher matcher = pattern.matcher(pathname.getName());
					if (matcher.matches() && pathname.isDirectory())
						return true;
					return false;
				}
			});
			for (File file : files) {
				ret.add(file.getAbsolutePath());
			}
		}
		return ret;
	}

	/**
	 * Drops and adds new indexes associated with the underlying CF
	 * 
	 * @throws IOException
	 */
	public void reload() throws IOException {

	}

	/**
	 * Removes an existing index
	 * 
	 * @param column
	 *            the indexed column to remove
	 * @throws IOException
	 */
	public void removeIndexedColumn(ByteBuffer column) throws IOException {
		// nothing to do
	}

	/**
	 * Remove all underlying index data
	 * 
	 * @throws IOException
	 */
	public void removeAllIndexes() throws IOException {
		if (attrTagIndex != null) {
			attrTagIndex.removeDataFiles();
		}
		if (nodeTagIndex != null)
			nodeTagIndex.removeDataFiles();

		if (textInvertIndex != null)
			textInvertIndex.removeDataFiles();

		for (IBtree btree : btreeIndexes.values())
			btree.removeDataFiles();

	}

	public void applyIndexUpdates(ByteBuffer rowKey, ColumnFamily cf,
			SortedSet<ByteBuffer> mutatedIndexedColumns,
			ColumnFamily oldIndexedColumns) throws IOException {
		int xmlID = Integer.parseInt(CollectionStore.COLKEYVALIDATOR
				.getString(rowKey));

		// 对整个文档进行删除的情况
		if (cf.isMarkedForDelete()) {
			// 如果节点已经存在，那么需要判断内容是否比较新
			if (oldIndexedColumns != null)
				for (IColumn col : oldIndexedColumns.getSortedColumns()) {
					// 表示当前更新比当前节点的内容更加新
					if (col.timestamp() < cf.getMarkedForDeleteAt())
						deleteXMLNode(xmlID, (SuperColumn) col);
				}
			return;
		}
		/**
		 * when indexes needs to be updated: 1. delete a node, all indexes need
		 * to be updated 2. update the text value of a node, inverted index and
		 * value index need to be updated. 3. udpate the range, all indexes
		 * needs to be updated 4. others, none indexes need to be updated.
		 */
		for (IColumn col : cf.getSortedColumns()) {
			SuperColumn oldSp = null;
			if (oldIndexedColumns != null)
				oldSp = (SuperColumn) oldIndexedColumns.getColumn(col.name());
			// in this case oldIndexedColumns conains all the information about
			// a node
			if (col.isMarkedForDelete()) {
				// 这是一个删除操作
				if (oldSp != null
						&& col.getMarkedForDeleteAt() > oldSp.maxTimestamp())
					// deleteXMLNode(xmlID, oldSp);
					deleteXMLNode(xmlID, (SuperColumn) oldSp);// TODO需要确认这里能否成功
			} else {
				// 这是一个节点插入或者文本值更新的操作
				SuperColumn sp = (SuperColumn) col;
				if (oldSp == null) {
					// 直接向索引中添加索引项
					insertXMLNode(xmlID, sp);
				} else if (sp.timestamp() > oldSp.timestamp()) {
					// only update the text
					ValueType newType = ValueType.getValueType(ByteBufferUtil
							.toInt(sp.getColumn(CollectionStore.VALUETYPE)
									.value()));

					// 判断更新是否已经过时
					if (oldSp.isMarkedForDelete()) {
						insertXMLNode(xmlID, sp);
					} else if (oldSp.getColumn(CollectionStore.VALUE) == null
							|| oldSp.getColumn(CollectionStore.VALUE)
									.timestamp() < sp.getColumn(
									CollectionStore.VALUE).timestamp())
						updateXMLNodeValue(xmlID, oldSp,
								sp.getColumn(CollectionStore.VALUE).value(),
								newType);
				}
				/*
				 * else { //TODO暂不考虑这种情况 deleteXMLNode(xmlID, oldSp);
				 * insertXMLNode(xmlID, sp); }
				 */
			}
		}
	}

	/**
	 * 
	 * @param xmlID
	 *            the xml document id of the node
	 * @param col
	 *            the previous xml node in SuperColumn form
	 * @param text
	 *            the updated text value
	 */
	private void updateXMLNodeValue(int xmlID, SuperColumn col,
			ByteBuffer text, ValueType newType) {
		// get a xml node
		XMLNode node = XMLNodeImpl.serializer().deserialize(xmlID, col);
		if (XMLDBStore.instance().getServerMode() != XMLServerMode.starting)
			getColStore().invalidateCache(CacheType.LOCAL, xmlID, node.getId());
		// update index for the value
		boolean previousEmpty = node.getValue().equals(
				ByteBufferUtil.EMPTY_BYTE_BUFFER);

		IBtree btree = null;
		if (!previousEmpty && node.getValueType() != ValueType.UNKNOW) {
			btree = getBtreeIndex(node.getValueType());
			btree.remove(
					node.getValueStr(),
					new Node(xmlID, node.getId(), node.getRange(), node
							.getLevel()));
		}

		if (!text.equals(ByteBufferUtil.EMPTY_BYTE_BUFFER)) {
			btree = getBtreeIndex(newType);
			btree.put(
					ValueType.getValidator(newType).compose(text).toString(),
					new Node(xmlID, node.getId(), node.getRange(), node
							.getLevel()));

		}

		if (newType == ValueType.STRING) {
			if (!previousEmpty) {
				textInvertIndex.remove(node.getValueStr(), xmlID, node.getId(),
						node.getRange());
			}
			if (!text.equals(ByteBufferUtil.EMPTY_BYTE_BUFFER))
				textInvertIndex.put(node.getValidator().getString(text), xmlID,
						node.getId(), node.getRange(), node.getLevel());
		}
	}

	private void deleteXMLNode(int xmlID, SuperColumn col) {
		// get a xml node
		XMLNode node = XMLNodeImpl.serializer().deserialize(xmlID, col);
		if (XMLDBStore.instance().getServerMode() != XMLServerMode.starting)
			getColStore().invalidateCache(CacheType.LOCAL, xmlID, node.getId());
		// modeified by chengcheng 11.10.11 //
		// if (!col.isMarkedForDelete()) {
		if (node.getNodeType() == XMLNode.ELEMENTNODE) {
			btreeIndexes.get(ValueType.NODETAG).remove(
					node.getTagName(),
					new Node(xmlID, node.getId(), node.getRange(), node
							.getLevel()));
		} else {
			btreeIndexes.get(ValueType.ATTRTAG).remove(
					node.getTagName(),
					new Node(xmlID, node.getId(), node.getRange(), node
							.getLevel()));
		}
		// }

		// update index for the value
		if (!node.getValue().equals(ByteBufferUtil.EMPTY_BYTE_BUFFER)) {
			IBtree btree = getBtreeIndex(node.getValueType());
			if (btree != null && !node.getValueStr().isEmpty())
				btree.remove(node.getValueStr(), new Node(xmlID, node.getId(),
						node.getRange(), node.getLevel()));

			if (node.getValueType() == ValueType.STRING
					&& !node.getValueStr().isEmpty())
				textInvertIndex.remove(node.getValueStr(), xmlID, node.getId(),
						node.getRange());
		}
	}

	private void insertXMLNode(int xmlID, SuperColumn col) {
		// get a xml node
		int nodeID = Integer.parseInt(CollectionStore.SPNAMETYPE.getString(col
				.name()));
		XMLNode node = XMLNodeImpl.serializer().deserialize(xmlID, col);
		if (XMLDBStore.instance().getServerMode() != XMLServerMode.starting)
			getColStore().invalidateCache(CacheType.LOCAL, xmlID, node.getId());
		// modeified by chengcheng 11.10.11 //
		if (!col.isMarkedForDelete()) {
			if (node.getNodeType() == XMLNodeImpl.ELEMENTNODE) {
				btreeIndexes.get(ValueType.NODETAG).put(
						node.getTagName(),
						new Node(xmlID, node.getId(), node.getRange(), node
								.getLevel()));
			} else {
				btreeIndexes.get(ValueType.ATTRTAG).put(
						node.getTagName(),
						new Node(xmlID, node.getId(), node.getRange(), node
								.getLevel()));
			}
		}

		// update index for the value
		if (!node.getValue().equals(ByteBufferUtil.EMPTY_BYTE_BUFFER)) {
			IBtree btree = getBtreeIndex(node.getValueType());
			if (btree != null && !node.getValueStr().isEmpty())
				btree.put(
						node.getValueStr(),
						new Node(xmlID, nodeID, node.getRange(), node
								.getLevel()));
			if (node.getValueType() == ValueType.STRING
					&& !node.getValueStr().isEmpty())
				textInvertIndex.put(node.getValueStr(), xmlID, nodeID,
						node.getRange(), node.getLevel());
		}
	}

	public IBtree getBtreeIndex(ValueType valueType) {
		// all kinds of filesize btree is supported by the same btree index
		if (ValueType.isFileSizeType(valueType))
			valueType = ValueType.FILESIZE;

		if (btreeIndexes == null) {
			initBtree();
		}

		IBtree btree = btreeIndexes.get(valueType);
		if (btree == null) {
			btree = createBtree(valueType);
			if (btree != null)
				btreeIndexes.put(valueType, btree);
			else {
				for (int i = 0; i < 10 && btree == null; i++) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
					btree = btreeIndexes.get(valueType);
				}
			}
		}
		return btree;
	}

	/**
	 * Rename all underlying index files
	 * 
	 * @param newCfName
	 *            the new index Name
	 */
	public void renameIndexes(String newCfName) throws IOException {
		// if we should rename the index?
	}

	/**
	 * Flush all indexes to disk
	 * 
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public void flushIndexes() throws IOException {
		if (attrTagIndex != null)
			attrTagIndex.flush();
		if (nodeTagIndex != null)
			nodeTagIndex.flush();

		if (textInvertIndex != null)
			textInvertIndex.flush();

		for (IBtree btree : btreeIndexes.values())
			btree.flush();
	}

	/**
	 * @return all built indexes (ready to use), it should only return column
	 *         indexes?
	 */
	public List<String> getBuiltIndexes() {
		return new ArrayList<String>();
	}

	/**
	 * @return all CFS from indexes which use a backing CFS internally (KEYS)
	 */
	public Collection<ColumnFamilyStore> getIndexesBackedByCfs() {
		ArrayList<ColumnFamilyStore> cfsList = new ArrayList<ColumnFamilyStore>();
		cfsList.add(((CFInvertIndex) textInvertIndex).getLocalCFStoreProxy()
				.getCfStore());
		/*
		 * cfsList.add(((CFBasedBtree) nodeTagIndex).getLocalCFStoreProxy()
		 * .getCfStore());
		 */
		return cfsList;
	}

	/**
	 * Delete all columns from all indexes for this row
	 * 
	 * @param key
	 *            the row key
	 * @param indexedColumnsInRow
	 *            all column names in row
	 */
	// public void deleteFromIndexes(DecoratedKey<?> key, List<IColumn>
	// indexedColumnsInRow) throws IOException
	public void deleteFromIndex(DecoratedKey<?> key,
			List<IColumn> indexedColumnsInRow, List<IColumn> allColumns) {
		int xmlID = Integer.parseInt(CollectionStore.COLKEYVALIDATOR
				.getString(key.key));

		for (IColumn col : allColumns) {
			deleteXMLNode(xmlID, (SuperColumn) col);
		}
	}

	public void flushAndSignal(final CountDownLatch latch,
			ExecutorService flushwriter, ReplayPosition ctx) {
		flushwriter.execute(new WrappedRunnable() {
			public void runMayThrow() throws IOException {
				baseCfs.flushLock.lock();
				try {
					if (!baseCfs.isDropped()) {
						// we should avoid the cfs flush here, or it will cause
						// dead lock
						for (IBtree btree : btreeIndexes.values())
							btree.flush();
						// nodeTagIndex.flush();
						// attrTagIndex.flush();
						// textInvertIndex.flush();
					}
				} catch (Exception ex) {
					logger.error(ex.toString());
				} finally {
					baseCfs.flushLock.unlock();
					latch.countDown();
				}
			}
		});
	}
}

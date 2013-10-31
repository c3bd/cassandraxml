/**
 * @author chengchengyu
 * 需要修改回去为简单的key
 */
package imc.disxmldb.index.btree;

import imc.disxmldb.dom.typesystem.ValueType;
import imc.disxmldb.index.ComparatorByRangeFirst;
import imc.disxmldb.index.Node;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.filter.IdentityXMLFilter;
import imc.disxmldb.index.filter.IXMLFilter;
import imc.disxmldb.util.FileUtil;
import imc.disxmldb.xpath.XPathSequence;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.LatencyTracker;
import org.apache.cassandra.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.bind.tuple.DoubleBinding;
import com.sleepycat.bind.tuple.FloatBinding;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.EnvironmentMutableConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * This class use the Berkeley DB to implement to IBtree interface.
 */
public class ComposeKeyBtree implements IBtree, ComposeKeyBtreeMBean {
	private static Logger logger = LoggerFactory
			.getLogger(ComposeKeyBtree.class);

	private LatencyTracker putLatencyTracker = new LatencyTracker();
	private LatencyTracker retrievelLatencyTracker = new LatencyTracker();
	private LatencyTracker removeLatencyTracker = new LatencyTracker();

	// ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
	DbEnv dbenv;
	AbstractType validator = null;

	public DbEnv getDbenv() {
		return dbenv;
	}

	public void setDbenv(DbEnv dbenv) {
		this.dbenv = dbenv;
	}

	String dbpath;

	public ComposeKeyBtree(String dbpath, int cacheSize, ValueType type) {
		validator = ValueType.getValidator(type);
		init(dbpath, cacheSize, type);
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {

			String regPath = null;
			if (dbpath.lastIndexOf(":") < 0)
				regPath = dbpath;
			else
				regPath = dbpath.substring(dbpath.indexOf(':') + 1);
			mbs.registerMBean(this, new ObjectName(
					"imc.disxmldb.index:type=Btree," + "dbPath=" + regPath));
		} catch (Exception e) {
			// throw new RuntimeException(e);
		}
	}

	private void unRegister() {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {

			String regPath = null;
			if (dbpath.lastIndexOf(":") < 0)
				regPath = dbpath;
			else
				regPath = dbpath.substring(dbpath.indexOf(':') + 1);
			mbs.unregisterMBean(new ObjectName("imc.disxmldb.index:type=Btree,"
					+ "dbPath=" + regPath));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void init(String dbpath, int cacheSize, ValueType type) {
		this.dbpath = dbpath;
		this.dbenv = new DbEnv();
		dbenv.getMyDbConfig().setBtreeComparator(
				(Class<? extends Comparator<byte[]>>) ValueType.getComparator(
						type).getClass());
		dbenv.setup(new File(dbpath), false, true, cacheSize);
	}

	@Override
	public void close() {
		this.dbenv.flush();
		this.dbenv.close();
		unRegister();
	}

	public void put(String key, Node node) {
		DatabaseEntry theKey = new DatabaseEntry();
		theKey = composeKey(key, node.getXmlDocID());
		put(theKey, node);
	}

	public void put(DatabaseEntry theKey, Node node) {
		long startTime = System.currentTimeMillis();
		DatabaseEntry theData = new DatabaseEntry();

		Cursor cursor = dbenv.getNodeDb().openCursor(null, null);
		OperationStatus retVal = null;

		try {
			dbenv.getNodeBinding().objectToEntry(node, theData);
			/*
			 * dbenv.getNodeBinding().objectToEntry( new
			 * NodeUnit(node.getNodeID(), node.getRange(), node.getLevel()),
			 * theData);
			 */
			if (cursor.getSearchBoth(theKey, theData, LockMode.DEFAULT) == OperationStatus.NOTFOUND)
				retVal = cursor.put(theKey, theData);
		} catch (Exception dbe) {
			try {
				System.out.println("Error putting entry " + theKey.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			// txn.abort();
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		// txn.commit();
		putLatencyTracker.addMicro(System.currentTimeMillis() - startTime);
	}

	/**
	 * the List<IndexArrayUnit> should be ordered by docID, NodeUnit.range[0],
	 * NodeUnit.range[1]
	 * 
	 * @param text
	 * @return
	 */

	public TreeMap<Integer, List<NodeUnit>> get(String key, IXMLFilter filter) {
		DatabaseEntry theKey = new DatabaseEntry();

		theKey = composeKey(key, -1);
		return get(theKey, filter);
	}

	public TreeMap<Integer, List<NodeUnit>> get(String key) {
		return get(key, IdentityXMLFilter.instance);
	}

	public TreeMap<Integer, List<NodeUnit>> get(DatabaseEntry theKey,
			IXMLFilter filter) {
		long startTime = System.currentTimeMillis();
		TreeMap<Integer, List<NodeUnit>> imap = new TreeMap<Integer, List<NodeUnit>>();

		DatabaseEntry theData = new DatabaseEntry();

		Cursor cursor = dbenv.getNodeDb().openCursor(null, null);
		// Pair<Integer, ByteBuffer> oKeyPair = deComposeKey(Arrays.copyOf(
		// theKey.getData(), theKey.getData().length));

		try {
			OperationStatus retVal = cursor.getSearchKey(theKey, theData,
					LockMode.DEFAULT);

			while (retVal == OperationStatus.SUCCESS) {
				Node myNode = (Node) dbenv.getNodeBinding().entryToObject(
						theData);
				if (!filter.filter(myNode)) {
					List<NodeUnit> ln = imap.get(myNode.getXmlDocID());
					if (ln != null) {
						ln.add(new NodeUnit(myNode.getNodeID(), myNode
								.getRange(), myNode.getLevel()));
					} else {
						ln = new ArrayList<NodeUnit>();
						ln.add(new NodeUnit(myNode.getNodeID(), myNode
								.getRange(), myNode.getLevel()));
						imap.put(myNode.getXmlDocID(), ln);
					}
				}
				retVal = cursor.getNextDup(theKey, theData, LockMode.DEFAULT);
			}
			/*
			 * OperationStatus retVal = cursor.getSearchKeyRange(theKey,
			 * theData, LockMode.DEFAULT); // 如果返回不成功的话，那只能说明key对应的索引项不存在 while
			 * (retVal == OperationStatus.SUCCESS) {
			 * 
			 * Pair<Integer, ByteBuffer> nKeyPair = deComposeKey(theKey
			 * .getData()); // 如果找到的索引项和当前key不相等，那只能说明扫描已经超过当前索引项 assert
			 * validator.compare(oKeyPair.right, nKeyPair.right) <= 0; if
			 * (validator.compare(oKeyPair.right, nKeyPair.right) < 0) break;
			 * 
			 * boolean shouldNext = true; while (retVal ==
			 * OperationStatus.SUCCESS) { NodeUnit mynode = (NodeUnit)
			 * dbenv.getNodeBinding() .entryToObject(theData); // judge whether
			 * the doc should be returned if (!filter.filter(nKeyPair.left,
			 * null)) { List<NodeUnit> ln = imap.get(nKeyPair.left); if (ln !=
			 * null) { ln.add(mynode); } else { ln = new ArrayList<NodeUnit>();
			 * ln.add(mynode); imap.put(nKeyPair.left, ln); } retVal =
			 * cursor.getNextDup(theKey, theData, LockMode.DEFAULT); } else {
			 * shouldNext = false; break; } } if (shouldNext) retVal =
			 * cursor.getNext(theKey, theData, LockMode.DEFAULT); else retVal =
			 * cursor.getNextNoDup(theKey, theData, LockMode.DEFAULT); }
			 */
		} finally {
			cursor.close();
			cursor = null;
			retrievelLatencyTracker.addMicro(System.currentTimeMillis()
					- startTime);
		}
		return imap;
	}

	public TreeMap<Integer, List<NodeUnit>> get(DatabaseEntry theKey) {
		return get(theKey, IdentityXMLFilter.instance);
	}

	public TreeMap<Integer, List<NodeUnit>> getLesser(String key) {
		return getLesser(key, IdentityXMLFilter.instance);
	}

	public TreeMap<Integer, List<NodeUnit>> getLesserOrEqual(String key) {
		return getLesserOrEqual(key, IdentityXMLFilter.instance);
	}

	public TreeMap<Integer, List<NodeUnit>> getLesser(String key,
			IXMLFilter filter) {
		return getLesserIntern(key, filter, false);
	}

	public TreeMap<Integer, List<NodeUnit>> getLesserOrEqual(String key,
			IXMLFilter filter) {
		return getLesserIntern(key, filter, true);
	}

	public TreeMap<Integer, List<NodeUnit>> getLesserIntern(String key,
			IXMLFilter filter, boolean inclusion) {
		long startTime = System.currentTimeMillis();

		TreeMap<Integer, List<NodeUnit>> imap = new TreeMap<Integer, List<NodeUnit>>();

		DatabaseEntry theData = new DatabaseEntry();

		DatabaseEntry foundKey = composeKey(key, -1);
		ByteBuffer byteKey = validator.fromString(key);
		Cursor cursor = dbenv.getNodeDb().openCursor(null, null);
		LockMode lockMode = LockMode.DEFAULT;
		try {
			OperationStatus retVal = cursor.getSearchKeyRange(foundKey,
					theData, LockMode.DEFAULT);
			// 返回false只有可能当前key是所有索引项里面最大的
			if (retVal != OperationStatus.SUCCESS) {
				retVal = cursor.getLast(foundKey, theData, lockMode);
			}

			while (retVal == OperationStatus.SUCCESS) {
				Pair<Integer, ByteBuffer> nKeyPair = deComposeKey(foundKey
						.getData());
				if (!inclusion) {
					if (validator.compare(byteKey, nKeyPair.right) == 0)
						retVal = cursor.getPrevNoDup(foundKey, theData,
								lockMode);
					inclusion = true;
					continue;
				}
				if (validator.compare(byteKey, nKeyPair.right) < 0) {
					retVal = cursor.getPrevNoDup(foundKey, theData, lockMode);
					continue;
				}

				Node myNode = (Node) dbenv.getNodeBinding().entryToObject(
						theData);
				if (!filter.filter(myNode)) {
					List<NodeUnit> ln = imap.get(myNode.getXmlDocID());
					if (ln != null) {
						NodeUnit tempNode = new NodeUnit(myNode);
						int index = Collections.binarySearch(ln, tempNode,
								ComparatorByRangeFirst.instance);
						if (index < 0)
							ln.add(Math.abs(index) - 1, tempNode);
					} else {
						ln = new ArrayList<NodeUnit>();
						ln.add(new NodeUnit(myNode.getNodeID(), myNode
								.getRange(), myNode.getLevel()));
						imap.put(myNode.getXmlDocID(), ln);
					}
				}
				retVal = cursor.getPrev(foundKey, theData, LockMode.DEFAULT);
			}

			/*
			 * // 返回false只有可能当前key是所有索引项里面最大的 if (retVal !=
			 * OperationStatus.SUCCESS) { retVal = cursor.getLast(foundKey,
			 * theData, LockMode.DEFAULT); } while (retVal ==
			 * OperationStatus.SUCCESS) { if (inclusion) { while (retVal ==
			 * OperationStatus.SUCCESS) { // 读取完key对应的所有索引项 Pair<Integer,
			 * ByteBuffer> nKeyPair = deComposeKey(foundKey .getData()); int
			 * comp = validator.compare(byteKey, nKeyPair.right); assert comp >=
			 * 0; if (comp == 0) imap.putAll(get(key, filter)); else break;
			 * retVal = cursor.getPrevNoDup(foundKey, theData,
			 * LockMode.DEFAULT); } inclusion = false; // no need to go into
			 * this part }
			 * 
			 * Pair<Integer, ByteBuffer> nKeyPair = null;
			 * 
			 * while (retVal == OperationStatus.SUCCESS) { nKeyPair =
			 * deComposeKey(foundKey.getData()); enterMap(imap, nKeyPair.left,
			 * theData, filter); retVal = cursor.getPrevDup(foundKey, theData,
			 * LockMode.DEFAULT); } retVal = cursor.getPrevNoDup(foundKey,
			 * theData, LockMode.DEFAULT); }
			 */
		} catch (Exception willNeverOccur) {
		} finally {
			cursor.close();
			cursor = null;
			retrievelLatencyTracker.addMicro(System.currentTimeMillis()
					- startTime);
		}

		return imap;
	}

	public TreeMap<Integer, List<NodeUnit>> getGreater(String key) {
		return getGreater(key, IdentityXMLFilter.instance);
	}

	public TreeMap<Integer, List<NodeUnit>> getGreater(String key,
			IXMLFilter filter) {
		return getGreaterIntern(key, filter, false);
	}

	public TreeMap<Integer, List<NodeUnit>> getGreaterOrEqual(String key) {
		return getGreaterOrEqual(key, IdentityXMLFilter.instance);
	}

	public TreeMap<Integer, List<NodeUnit>> getGreaterOrEqual(String key,
			IXMLFilter filter) {
		return getGreaterIntern(key, filter, true);
	}

	private TreeMap<Integer, List<NodeUnit>> getGreaterIntern(String key,
			IXMLFilter filter, boolean inclusion) {
		long startTime = System.currentTimeMillis();

		TreeMap<Integer, List<NodeUnit>> imap = new TreeMap<Integer, List<NodeUnit>>();

		DatabaseEntry theData = new DatabaseEntry();

		DatabaseEntry foundKey = composeKey(key, -1);
		ByteBuffer byteKey = validator.fromString(key);

		Cursor cursor = dbenv.getNodeDb().openCursor(null, null);
		int temp = -1;

		try {
			// 首先找到第一个大于等于key的索引项,有可能返回的索引项不是当前key的，那么只需要直接退出。如果直接没找到，那么直接返回空结果
			OperationStatus retVal = cursor.getSearchKeyRange(foundKey,
					theData, LockMode.DEFAULT);

			while (retVal == OperationStatus.SUCCESS) {
				if (!inclusion) {
					int comp = 0;
					// 如果不需要包含key对应的索引列表，那么需要skip掉key对应的所有索引列表
					Pair<Integer, ByteBuffer> nKeyPair = deComposeKey(foundKey
							.getData());
					comp = validator.compare(byteKey, nKeyPair.right);
					if (comp == 0) {
						retVal = cursor.getNextNoDup(foundKey, theData,
								LockMode.DEFAULT);
					}
					inclusion = true;
					continue;
				}

				// 读取完一个索引项和一个文档id对应的索引列表
				Node myNode = (Node) dbenv.getNodeBinding().entryToObject(
						theData);
				if (!filter.filter(myNode)) {
					List<NodeUnit> ln = imap.get(myNode.getXmlDocID());
					if (ln != null) {
						NodeUnit tempNode = new NodeUnit(myNode);
						int index = Collections.binarySearch(ln, tempNode,
								ComparatorByRangeFirst.instance);
						if (index < 0)
							ln.add(Math.abs(index) - 1, tempNode);
					} else {
						ln = new ArrayList<NodeUnit>();
						ln.add(new NodeUnit(myNode.getNodeID(), myNode
								.getRange(), myNode.getLevel()));
						imap.put(myNode.getXmlDocID(), ln);
					}
				}
				retVal = cursor.getNext(foundKey, theData, LockMode.DEFAULT);
			}

			/*
			 * while (retVal == OperationStatus.SUCCESS) { if (!inclusion) { int
			 * comp = 0; // 如果不需要包含key对应的索引列表，那么需要skip掉key对应的所有索引列表 while
			 * (retVal == OperationStatus.SUCCESS) { Pair<Integer, ByteBuffer>
			 * nKeyPair = deComposeKey(foundKey .getData()); comp =
			 * validator.compare(byteKey, nKeyPair.right); if (comp == 0) {
			 * retVal = cursor.getNextNoDup(foundKey, theData,
			 * LockMode.DEFAULT); } else break; } inclusion = true; }
			 * Pair<Integer, ByteBuffer> nKeyPair = null;
			 * 
			 * while (retVal == OperationStatus.SUCCESS) { //
			 * 读取完一个索引项和一个文档id对应的索引列表 nKeyPair =
			 * deComposeKey(foundKey.getData()); enterMap(imap, nKeyPair.left,
			 * theData, filter); retVal = cursor.getNextDup(foundKey, theData,
			 * LockMode.DEFAULT); } retVal = cursor.getNext(foundKey, theData,
			 * LockMode.DEFAULT); }
			 */
		} finally {
			cursor.close();
			cursor = null;
			retrievelLatencyTracker.addMicro(System.currentTimeMillis()
					- startTime);
		}

		return imap;
	}

	public void remove(String key, Node node) {
		DatabaseEntry theKey = new DatabaseEntry();
		theKey = composeKey(key, node.getXmlDocID());
		remove(theKey, node);
	}

	public void remove(DatabaseEntry theKey, Node node) {
		long startTime = System.currentTimeMillis();
		// Transaction txn = dbenv.getEnv().beginTransaction(null, null);

		DatabaseEntry theData = new DatabaseEntry();

		// Cursor cursor = dbenv.getNodeDb().openCursor(txn, null);
		Cursor cursor = dbenv.getNodeDb().openCursor(null, null);
		try {
			dbenv.getNodeBinding().objectToEntry(node, theData);
			OperationStatus ops = cursor.getSearchBoth(theKey, theData,
					LockMode.DEFAULT);
			if (ops == OperationStatus.SUCCESS) {
				cursor.delete();
			} else {
				System.out.println("deleting failed" + ops.toString());
			}
		} catch (DatabaseException dbe) {
			try {
				System.out.println("Error removing entry " + theKey.toString());
			} catch (Exception willNeverOccur) {
			}
			// txn.abort();
			throw dbe;
		} finally {
			cursor.close();
			cursor = null;
		}
		// txn.commit();
		removeLatencyTracker.addMicro(System.currentTimeMillis() - startTime);
	}

	public void adjustCache(long cacheSize) {
		EnvironmentMutableConfig conf = new EnvironmentMutableConfig();
		conf.setCacheSize(cacheSize);
		dbenv.getEnv().setMutableConfig(conf);
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		dbenv.flush();
	}

	@Override
	public long getNCacheMiss() {
		return dbenv.getEnv().getStats(null).getNCacheMiss();
	}

	@Override
	public long getTotalCacheSize() {
		return dbenv.getEnv().getStats(null).getCacheTotalBytes();
	}

	@Override
	public double getAvgRetrievelLatency() {
		long n = retrievelLatencyTracker.getOpCount();
		long latency = retrievelLatencyTracker.getTotalLatencyMicros();
		if (n == 0) {
			return 0;
		}
		return (float) latency / (float) n;
	}

	@Override
	public double getRecentRetrievelLatency() {
		return retrievelLatencyTracker.getRecentLatencyMicros();
	}

	@Override
	public double getAvgRemoveLatency() {
		long n = removeLatencyTracker.getOpCount();
		long latency = removeLatencyTracker.getTotalLatencyMicros();
		if (n == 0) {
			return 0;
		}
		return (float) latency / (float) n;
	}

	@Override
	public double getRecentRemoveLatency() {
		return removeLatencyTracker.getRecentLatencyMicros();
	}

	@Override
	public double getAvgPutLatency() {
		long n = putLatencyTracker.getOpCount();
		long latency = putLatencyTracker.getTotalLatencyMicros();
		if (n == 0) {
			return 0;
		}
		return (float) latency / (float) n;
	}

	@Override
	public double getRecentPutLatency() {
		return putLatencyTracker.getRecentLatencyMicros();
	}

	@Override
	public void removeDataFiles() {
		close();
		if (FileUtil.removeFile(dbpath) == false) {
			logger.warn(dbpath + " fails to be deleted");
		}
	}

	@Override
	public long estimateKeyResultNumB(String key, int replicas) {
		assert replicas > 0;
		DatabaseEntry theKey = new DatabaseEntry();
		Cursor cursor = null;
		try {
			theKey = composeKey(key, -1);
			// ByteBuffer byteKey = validator.fromString(key);
			DatabaseEntry theData = new DatabaseEntry();
			cursor = dbenv.getNodeDb().openCursor(null, null);
			OperationStatus retVal = cursor.getSearchKey(theKey, theData,
					LockMode.DEFAULT);
			int count = 0;
			if (retVal == OperationStatus.SUCCESS)
				count = cursor.count();
			return count / replicas;
		} catch (Exception e) {
			// e.printStackTrace();
		} finally {
			cursor.close();
			cursor = null;
		}
		return 0;
	}

	@Override
	public long estimateKeyResultNum(String key, int replicas) {
		assert replicas > 0;
		DatabaseEntry theKey = new DatabaseEntry();
		Cursor cursor = dbenv.getNodeDb().openCursor(null, null);
		try {
			theKey = composeKey(key, -1);
			// ByteBuffer byteKey = validator.fromString(key);
			DatabaseEntry theData = new DatabaseEntry();
			OperationStatus retVal = cursor.getSearchKey(theKey, theData,
					LockMode.DEFAULT);
			long count = 0;
			if (retVal == OperationStatus.SUCCESS)
				count = cursor.countEstimate();
			return count / replicas;
		} catch (Exception e) {
			// e.printStackTrace();
		} finally {
			cursor.close();
			cursor = null;
		}
		return 0;
	}

	/**
	 * 判断这个索引项是否出现在索引列表里
	 */
	@Override
	public boolean contains(String key, Node node) {
		DatabaseEntry theKey = composeKey(key, node.getXmlDocID());
		DatabaseEntry theData = new DatabaseEntry();

		dbenv.getNodeBinding().objectToEntry(node, theData);
		/*
		 * dbenv.getNodeBinding() .objectToEntry( new NodeUnit(node.getNodeID(),
		 * node.getRange(), node.getLevel()), theData);
		 */
		Cursor cursor = dbenv.getNodeDb().openCursor(null, null);
		try {
			OperationStatus retVal = cursor.getSearchBoth(theKey, theData,
					LockMode.DEFAULT);
			return retVal == OperationStatus.SUCCESS;
		} finally {
			cursor.close();
			cursor = null;
		}
	}

	/**
	 * 
	 * @param key
	 * @param child
	 * @param level
	 * @param filter
	 * @return null means the XML doc for this node doesn't exist in the index
	 */
	public List<NodeUnit> getParentNode(String key, Node child, int level,
			IXMLFilter filter) {
		List<NodeUnit> ret = new ArrayList<NodeUnit>();
		DatabaseEntry theKey = composeKey(key, child.getXmlDocID());
		ByteBuffer byteKey = validator.fromString(key);
		DatabaseEntry theData = new DatabaseEntry();
		dbenv.getNodeBinding().objectToEntry(child, theData);
		/*
		 * dbenv.getNodeBinding() .objectToEntry( new NodeUnit(node.getNodeID(),
		 * node.getRange(), node.getLevel()), theData);
		 */
		LockMode lockMode = LockMode.DEFAULT;
		Cursor cursor = null;
		try {
			cursor = dbenv.getNodeDb().openCursor(null, null);
			OperationStatus retVal = cursor.getSearchBothRange(theKey, theData,
					lockMode);
			// 如果没找到的话，可能是该节点的父节点是倒排列表中的最后一项
			if (retVal != OperationStatus.SUCCESS) {
				retVal = cursor.getSearchKey(theKey, theData, lockMode);
				if (retVal == OperationStatus.SUCCESS) {
					retVal = cursor.getNextNoDup(theKey, theData, lockMode);
					if (retVal != OperationStatus.SUCCESS) {
						retVal = cursor.getLast(theKey, theData, lockMode);
					} else {
						retVal = cursor.getPrev(theKey, theData, lockMode);
					}
				} else {
					// 说明没有对应的key的索引项
					return ret;
				}

			}

			Pair<Integer, ByteBuffer> nKeyPair = deComposeKey(theKey.getData());
			// 说明不是一个索引项
			if (validator.compare(byteKey, nKeyPair.right) != 0)
				return ret;

			while (retVal == OperationStatus.SUCCESS) {
				Node retNode = (Node) dbenv.getNodeBinding().entryToObject(
						theData);
				// 如果是一个索引项，那也肯定会遭到下一个XML文档中去
				if (retNode.getXmlDocID() < child.getXmlDocID())
					break;
				else if (retNode.getXmlDocID() == child.getXmlDocID()) {

					if (judgeNode(retNode, child, level)
							&& !filter.filter(retNode))
						ret.add(new NodeUnit(retNode.getNodeID(), retNode
								.getRange(), retNode.getLevel()));
					else if (retNode.getRange()[1] <= child.getRange()[0]) {
						// no need to search any more
						break;
					}
				}
				retVal = cursor.getPrevDup(theKey, theData, lockMode);
			}

			/*
			 * // 如果没找到的话，可能是该节点的父节点是倒排列表中的最后一项 if (retVal !=
			 * OperationStatus.SUCCESS) { // 找该索引项的下一个索引项，有可能当前索引项已经是最有一个索引项
			 * theKey = composeKey(key, node.getXmlDocID() + 1); retVal =
			 * cursor.getSearchKeyRange(theKey, theData, lockMode); if (retVal
			 * != OperationStatus.SUCCESS) { // 那么可以 认为当前索引项是最后一个索引项 retVal =
			 * cursor.getLast(theKey, theData, lockMode); } else { retVal =
			 * cursor.getPrev(theKey, theData, lockMode); } }
			 * 
			 * Pair<Integer, ByteBuffer> nKeyPair =
			 * deComposeKey(theKey.getData()); // 说明不是一个索引项 if
			 * (validator.compare(byteKey, nKeyPair.right) != 0 ||
			 * !nKeyPair.left.equals(node.getXmlDocID())) return ret;
			 * 
			 * // 如果到这里retVal成功了，那么可以向后扫描列表来定位父节点 while (retVal ==
			 * OperationStatus.SUCCESS) { nKeyPair =
			 * deComposeKey(theKey.getData()); NodeUnit retNode = (NodeUnit)
			 * dbenv.getNodeBinding() .entryToObject(theData);
			 * 
			 * if (judgeNode(retNode, node, level) &&
			 * !filter.filter(nKeyPair.left, retNode)) ret.add(new
			 * NodeUnit(retNode.getNodeID(), retNode .getRange(),
			 * retNode.getLevel())); else if (retNode.getRange()[1] <=
			 * node.getRange()[0]) { // no need to search any more break; }
			 * retVal = cursor.getPrevDup(theKey, theData, LockMode.DEFAULT); }
			 */
			return ret;
		} finally {
			cursor.close();
			cursor = null;
		}
	}

	private boolean judgeNode(NodeUnit parent, Node child, int level) {
		if (parent.getRange()[0] < child.getRange()[0]
				&& child.getRange()[1] < parent.getRange()[1]) {
			if (level < XPathSequence.ASCIENT_DESCIENT_LEVEL
					&& parent.getLevel() + level == child.getLevel())
				return true;
			else if (level >= XPathSequence.ASCIENT_DESCIENT_LEVEL
					&& parent.getLevel() < child.getLevel()) {
				return true;
			}
		}
		return false;
	}

	private boolean judgeNode(Node parent, NodeUnit child, int level) {
		if (parent.getRange()[0] < child.getRange()[0]
				&& child.getRange()[1] < parent.getRange()[1]) {
			if (level < XPathSequence.ASCIENT_DESCIENT_LEVEL
					&& parent.getLevel() + level == child.getLevel())
				return true;
			else if (level >= XPathSequence.ASCIENT_DESCIENT_LEVEL
					&& parent.getLevel() < child.getLevel()) {
				return true;
			}
		}
		return false;
	}

	private boolean judgeNode(Node parent, Node child, int level) {
		if (parent.getRange()[0] < child.getRange()[0]
				&& child.getRange()[1] < parent.getRange()[1]) {
			if (level < XPathSequence.ASCIENT_DESCIENT_LEVEL
					&& parent.getLevel() + level == child.getLevel())
				return true;
			else if (level >= XPathSequence.ASCIENT_DESCIENT_LEVEL
					&& parent.getLevel() < child.getLevel()) {
				return true;
			}
		}
		return false;
	}

	public DatabaseEntry composeKey(String key, int XMLDocID) {
		ByteBuffer keyBuf = validator.fromString(key);
		/*
		 * byte[] keyBytes = ByteBufferUtil.getArray(keyBuf); ByteBuffer buffer
		 * = ByteBuffer.allocate(keyBytes.length + 4); buffer.mark();
		 * buffer.putInt(XMLDocID); buffer.put(keyBytes); buffer.rewind();
		 */
		return new DatabaseEntry(ByteBufferUtil.getArray(keyBuf));
	}

	public Pair<Integer, ByteBuffer> deComposeKey(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		// int docId = buffer.getInt();
		return new Pair<Integer, ByteBuffer>(-1, buffer);
	}

	@Override
	public List<NodeUnit> getChildNode(String key, Node parent, int level,
			IXMLFilter filter) {
		List<NodeUnit> ret = new ArrayList<NodeUnit>();
		DatabaseEntry theKey = composeKey(key, parent.getXmlDocID());
		ByteBuffer byteKey = validator.fromString(key);
		DatabaseEntry theData = new DatabaseEntry();
		dbenv.getNodeBinding().objectToEntry(parent, theData);
		/*
		 * dbenv.getNodeBinding().objectToEntry( new
		 * NodeUnit(parent.getNodeID(), parent.getRange(), parent.getLevel()),
		 * theData);
		 */LockMode lockMode = LockMode.DEFAULT;
		Cursor cursor = null;
		try {
			cursor = dbenv.getNodeDb().openCursor(null, null);
			OperationStatus retVal = cursor.getSearchBothRange(theKey, theData,
					lockMode);
			// 如果没找到的话，那就是没有对应的子节点
			if (retVal != OperationStatus.SUCCESS) {
				return ret;
			}
			Pair<Integer, ByteBuffer> nKeyPair = deComposeKey(theKey.getData());
			Node retNode = (Node) dbenv.getNodeBinding().entryToObject(theData);
			// 说明不是一个索引项或者不是同一个文档的
			if (validator.compare(byteKey, nKeyPair.right) != 0
					|| retNode.getXmlDocID() != parent.getXmlDocID())
				return ret;

			// 如果到这里retVal成功了，那么可以向后扫描列表来定位父节点
			while (retVal == OperationStatus.SUCCESS) {
				nKeyPair = deComposeKey(theKey.getData());
				retNode = (Node) dbenv.getNodeBinding().entryToObject(theData);

				if (retNode.getXmlDocID() > parent.getXmlDocID())
					break;

				if (judgeNode(parent, retNode, level))
					ret.add(new NodeUnit(retNode.getNodeID(), retNode
							.getRange(), retNode.getLevel()));
				else if (retNode.getRange()[0] >= parent.getRange()[1]) {
					// no need to search any more
					break;
				}
				retVal = cursor.getNextDup(theKey, theData, LockMode.DEFAULT);
			}

			return ret;
		} finally {
			cursor.close();
			cursor = null;
		}
	}
}
package imc.disxmldb.index.btree;

import imc.disxmldb.dom.typesystem.ValueType;
import imc.disxmldb.index.Node;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.cache.IndexCache;
import imc.disxmldb.index.filter.IXMLFilter;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.sleepycat.je.DatabaseEntry;

public class CacheBtree extends ComposeKeyBtree {
	public static final int POSTER_LIMIT = 100000;
	IndexCache cache = new IndexCache();
	ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

	public CacheBtree(String dbpath, int cacheSize, ValueType valueType) {
		super(dbpath, cacheSize, valueType);
	}

	@Override
	public void put(DatabaseEntry theKey_, Node node_) {
		try {
			rwLock.readLock().lock();
			super.put(theKey_, node_);
			if (cache.contains(theKey_)) {
				cache.put(theKey_, node_);
			}
		} finally {
			rwLock.readLock().unlock();
		}
	}

	@Override
	public TreeMap<Integer, List<NodeUnit>> get(DatabaseEntry key_,
			IXMLFilter filter_) {
		if (cache.contains(key_)) {
			return cache.get(key_, filter_);
		} else {
			TreeMap<Integer, List<NodeUnit>> ret = null;

			if (rwLock.writeLock().tryLock()) {
				try {
					ret = super.get(key_, filter_);
					if (isCachable(key_, ret)) {
						cache.put(key_, ret);
					}
				} finally {
					rwLock.writeLock().unlock();
				}
			} else {
				// avoid the case when the system is busy with update queries or
				// read queries
				ret = super.get(key_, filter_);
			}

			return ret;
		}
	}

	@Override
	public void remove(DatabaseEntry theKey_, Node node_) {
		try {
			rwLock.readLock().lock();
			super.remove(theKey_, node_);
			if (cache.contains(theKey_)) {
				cache.remove(theKey_, node_);
			}
		} finally {
			rwLock.readLock().unlock();
		}
	}

	private int calcResultSize(TreeMap<Integer, List<NodeUnit>> ret_) {
		int count = 0;
		for (List<NodeUnit> entry : ret_.values()) {
			count += entry.size();
		}
		return count;
	}

	private boolean isCachable(DatabaseEntry key_,
			TreeMap<Integer, List<NodeUnit>> ret_) {
		if (calcResultSize(ret_) > POSTER_LIMIT)
			return true;
		else
			return false;
	}
}

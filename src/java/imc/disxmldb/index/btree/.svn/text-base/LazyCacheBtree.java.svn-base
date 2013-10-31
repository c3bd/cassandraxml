package imc.disxmldb.index.btree;

import imc.disxmldb.dom.typesystem.ValueType;
import imc.disxmldb.index.Node;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.cache.IndexCache;
import imc.disxmldb.index.filter.IXMLFilter;

import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.sleepycat.je.DatabaseEntry;

public class LazyCacheBtree extends ComposeKeyBtree {
	public static final int POSTER_LIMIT = 50000;
	IndexCache cache = new IndexCache();
	UpdateSpeed speed = new UpdateSpeed();
	Random rand = new Random();

	public LazyCacheBtree(String dbpath, int cacheSize,ValueType valueType) {
		super(dbpath, cacheSize, valueType);
	}

	@Override
	public void put(DatabaseEntry theKey_, Node node_) {
		super.put(theKey_, node_);
		speed.reportUpdate();
	}

	@Override
	public TreeMap<Integer, List<NodeUnit>> get(DatabaseEntry key_,
			IXMLFilter filter_) {
		TreeMap<Integer, List<NodeUnit>> ret = null;
		if (cache.contains(key_)) {
			ret = cache.get(key_, filter_);
			if (speed.getSpeed() > 0 || rand.nextFloat() < 0.001) {
				cache.remove(key_);
			}
		} else {
			ret = super.get(key_, filter_);
			if (isCachable(key_, ret)) {
				cache.put(key_, ret);
			}
		}
		return ret;
	}

	@Override
	public void remove(DatabaseEntry theKey_, Node node_) {
		super.remove(theKey_, node_);
		speed.reportUpdate();
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

	private static class UpdateSpeed {
		AtomicInteger opCount = new AtomicInteger(0);
		public void reportUpdate() {
			opCount.incrementAndGet();
		}

		public int getSpeed() {
			return opCount.getAndSet(0);
		}
	}
}
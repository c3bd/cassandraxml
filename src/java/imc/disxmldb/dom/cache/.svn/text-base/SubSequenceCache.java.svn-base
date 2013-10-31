package imc.disxmldb.dom.cache;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import imc.disxmldb.index.btree.IndexQueryResult;

import org.apache.cassandra.cache.ICache;
import org.apache.cassandra.config.DatabaseDescriptor;


import com.google.common.collect.MapMaker;


public class SubSequenceCache implements ICache<Integer, IndexQueryResult> {
	private static final int maxLimit = 1000000;
	private ConcurrentMap<Integer, IndexQueryResult> map = null;
	private AtomicInteger cacheID = new AtomicInteger(0);
	/**
	 * the maximumSize limits the parallel subsequence queries to maxLimit
	 * 
	 * @param cacheSize
	 * @param maxCacheSize
	 */
	public SubSequenceCache() {
		map = new MapMaker()
				.concurrencyLevel(64).expireAfterWrite(DatabaseDescriptor.getRpcTimeout(),
						TimeUnit.MILLISECONDS).initialCapacity(128)
				.maximumSize(maxLimit).makeMap();

	}

	public int nextCacheID() {
		return cacheID.getAndIncrement();
	}
	
	@Override
	public int capacity() {
		return map.size();
	}

	@Override
	public void setCapacity(int capacity) {

	}

	@Override
	public void put(Integer key, IndexQueryResult value) {
		map.put(key, value);
	}

	@Override
	public IndexQueryResult get(Integer key) {
		return map.get(key);
	}

	@Override
	public void remove(Integer key) {
		map.remove(key);

	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public void clear() {
		map.clear();

	}

	@Override
	public Set<Integer> keySet() {
		return map.keySet();
	}

	@Override
	public Set<Integer> hotKeySet(int n) {
		return null;
	}

	@Override
	public boolean isPutCopying() {
		return false;
	}

}

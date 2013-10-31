package imc.disxmldb.dom.cache;

import imc.disxmldb.dom.XMLNode;

import java.util.Set;

import org.apache.cassandra.cache.ConcurrentLinkedHashCache;
import org.apache.cassandra.cache.ICache;
import org.apache.cassandra.utils.Pair;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;

/**
 * DomNodeCache manages the cache of XMLNode, which can reduce the cost of deserialization of XML nodes.
 * The key of this cache is the pair consists of XML Doc ID and XML Node ID. 
 * It doesn't maintain the global cache of XML nodes, but the cache of a single collection in the XML Store.
 * @author xiafan
 *
 */
public class DomNodeCache implements ICache<Pair<Integer, Integer>, XMLNode> {
	public static int CACHE_SIZE = 2000;
	public static int MAX_CACHE_SIZE = 10000;
	public static int DEFAULT_CONCURENCY_LEVEL = 128;
	
	private ICache<Pair<Integer, Integer>, XMLNode> iCache = null;

	public DomNodeCache(int cacheSize, int maxCacheSize) {
		 ConcurrentLinkedHashMap<Pair<Integer, Integer>, XMLNode> map = new ConcurrentLinkedHashMap.Builder<Pair<Integer, Integer>, XMLNode>()
         .weigher(Weighers.<XMLNode>singleton())
         .initialCapacity(cacheSize)
         .maximumWeightedCapacity(maxCacheSize)
         .concurrencyLevel(DEFAULT_CONCURENCY_LEVEL)
         .build();
		this.iCache = new ConcurrentLinkedHashCache<Pair<Integer, Integer>, XMLNode>(map);
	}

	@Override
	public int capacity() {
		return iCache.capacity();
	}

	@Override
	public void setCapacity(int capacity) {
		iCache.setCapacity(capacity);
	}

	@Override
	public int size() {
		return iCache.size();
	}

	@Override
	public void clear() {
		iCache.clear();
	}

	@Override
	public Set keySet() {
		return iCache.keySet();
	}

	@Override
	public Set hotKeySet(int n) {
		return iCache.hotKeySet(n);
	}

	@Override
	public boolean isPutCopying() {
		return iCache.isPutCopying();
	}

	@Override
	public void put(Pair<Integer, Integer> key, XMLNode value) {
		/**
		 * TODO this methods needs to be modified
		 */
		iCache.put(key, value);
	}

	@Override
	public XMLNode get(Pair<Integer, Integer> key) {
		return iCache.get(key);
	}

	@Override
	public void remove(Pair<Integer, Integer> key) {
		iCache.remove(key);
	}

}

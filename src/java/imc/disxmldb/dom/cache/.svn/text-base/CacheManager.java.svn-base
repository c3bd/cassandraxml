package imc.disxmldb.dom.cache;

import java.util.HashMap;

import org.apache.cassandra.cache.ICache;

/**
 * this class is the facet that includes all kinds of cache methods.
 * @author xiafan
 *
 */
public class CacheManager {
	public enum CacheType {REMOTE, LOCAL, SUBSEQ};
	private HashMap<CacheType, ICache> caches = new HashMap<CacheType, ICache>();
	
	public void register(CacheType type, ICache cache) {
		caches.put(type, cache);
	}
	
	public ICache get(CacheType type) {
		return caches.get(type);
	}
}

package imc.disxmldb.index.cache;

import imc.disxmldb.index.ComparatorByRangeFirst;
import imc.disxmldb.index.Node;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.filter.IXMLFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class IndexCache implements IIndexCache {
	private ConcurrentMap<Object, PosterList> cache = new ConcurrentHashMap<Object, PosterList>();
	int capacity = 1000;
	
	@Override
	public void put(Object key_, Node node_) {
		if (cache.containsKey(key_)) {
			// only when the cache contains key, we will update the cache
			// the posterList can only be put into this cache as a whole using
			// the other methods
			PosterList list = cache.get(key_);
			if (list != null)
				list.add(node_);
		}

	}

	@Override
	public void remove(Object key_, Node node_) {
		if (cache.containsKey(key_)) {
			PosterList list = cache.get(key_);
			if (list != null)
				list.remove(node_);
		}

	}
	
	@Override
	public void remove(Object key) {
		cache.remove(key);
	}

	@Override
	public TreeMap<Integer, List<NodeUnit>> get(Object key_, IXMLFilter filter_) {
		TreeMap<Integer, List<NodeUnit>> ret = new TreeMap<Integer, List<NodeUnit>>();
		if (cache.containsKey(key_)) {
			PosterList list = cache.get(key_);
			if (list != null)
			ret = list.asView(filter_);
		}
		return ret;
	}

	private static class PosterList {
		ConcurrentMap<Integer, ConcurrentSkipListSet<NodeUnit>> posterList = new ConcurrentSkipListMap<Integer, ConcurrentSkipListSet<NodeUnit>>();

		public PosterList(TreeMap<Integer, List<NodeUnit>> posterList_) {
			for (Entry<Integer, List<NodeUnit>> entry : posterList_.entrySet()) {
				ConcurrentSkipListSet<NodeUnit> newList = new ConcurrentSkipListSet<NodeUnit>(new ComparatorByRangeFirst());
				newList.addAll(entry.getValue());
				posterList.put(entry.getKey(), newList);
			}
		}
		
		public void add(Node node_) {
			NodeUnit unit = new NodeUnit(node_.getNodeID(), node_.getRange(),
					node_.getLevel());
			ConcurrentSkipListSet<NodeUnit> list = null;
			if (posterList.containsKey(node_.getXmlDocID()) == false) {
				list = new ConcurrentSkipListSet<NodeUnit>(
						new ComparatorByRangeFirst());
				posterList.putIfAbsent(node_.getXmlDocID(), list);
			}
			list = posterList.get(node_.getXmlDocID());
			list.add(unit);
		}

		public void remove(Node node_) {
			if (posterList.containsKey(node_.getXmlDocID())) {
				NodeUnit unit = new NodeUnit(node_.getNodeID(), node_.getRange(),
						node_.getLevel());
				posterList.get(node_.getXmlDocID()).remove(unit);
			}
		}

		public TreeMap<Integer, List<NodeUnit>> asView(IXMLFilter filter_) {
			TreeMap<Integer, List<NodeUnit>> ret = new TreeMap<Integer, List<NodeUnit>>();
			for (Entry<Integer, ConcurrentSkipListSet<NodeUnit>> entry : posterList
					.entrySet()) {
				if (!filter_.filter(entry.getKey(), null)) {
					ret.put(entry.getKey(),
							new ArrayList<NodeUnit>(entry.getValue()));
				}
			}
			return ret;
		}
	}

	/**
	 * It guarantees multiple thread safety
	 */
	@Override
	public void put(Object key_, TreeMap<Integer, List<NodeUnit>> posterList_) {
		cache.putIfAbsent(key_, new PosterList(posterList_));
		if (cache.size() > capacity) {
			Random rand = new Random();
			for (Object key : cache.keySet()) {
				if (rand.nextFloat() > 0.5)
				{
					cache.remove(key);
					break;
				}
			}
		}
	}

	@Override
	public boolean contains(Object key) {
		return cache.containsKey(key);
	}


}

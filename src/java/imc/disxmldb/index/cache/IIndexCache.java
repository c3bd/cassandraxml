package imc.disxmldb.index.cache;

import java.util.List;
import java.util.TreeMap;

import imc.disxmldb.index.Node;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.filter.IXMLFilter;

public interface IIndexCache {
	public boolean contains(Object key);
	public void put(Object key, TreeMap<Integer, List<NodeUnit>> posterList);
	public void put(Object key, Node node);
	public void remove(Object key, Node node);
	public void remove(Object key);
	public TreeMap<Integer, List<NodeUnit>> get(Object key, IXMLFilter filter);
}

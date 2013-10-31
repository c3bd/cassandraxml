package imc.disxmldb.index.btree;

import imc.disxmldb.index.ComparatorByRangeFirst;
import imc.disxmldb.index.NodeUnit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * This class is used to encapsulate the query results read from all those
 * indices.
 * 
 */
public class IndexQueryResult {
	Map<Integer, TreeMap<Integer, List<NodeUnit>>> results = new HashMap<Integer, TreeMap<Integer, List<NodeUnit>>>();

	/**
	 * add the result returned by the btree of collection colID
	 * 
	 * @param colID
	 * @param result
	 */
	public void addResult(Integer colID, TreeMap<Integer, List<NodeUnit>> result) {
		if (results.get(colID) == null)
			results.put(colID, result);
		else {
			TreeMap<Integer, List<NodeUnit>> thisResult = results.get(colID);
			for (Entry<Integer, List<NodeUnit>> entry : result.entrySet()) {
				if (thisResult.get(entry.getKey()) == null)
					thisResult.put(entry.getKey(), entry.getValue());
				else {
					List<NodeUnit> nodes = thisResult.get(entry.getKey());
					for (NodeUnit node : entry.getValue()) {
						int index = Collections.binarySearch(nodes, node,
								ComparatorByRangeFirst.instance);
						if (index < 0)
							nodes.add(Math.abs(index) - 1, node);
					}
				}
			}
		}
	}

	/**
	 * just overwrite the current posterList
	 * 
	 * @param colID
	 * @param result
	 */
	public void addResult(Integer colID, IndexQueryResult result) {
		if (result.getColResult(colID) != null)
			results.put(colID, result.getColResult(colID));
	}

	/**
	 * return the iterator that is used to iterate through the collections
	 * 
	 * @return
	 */
	public Iterator<Integer> colIterator() {
		return results.keySet().iterator();
	}

	public boolean contains(Integer colID) {
		return results.containsKey(colID);
	}

	public TreeMap<Integer, List<NodeUnit>> getColResult(Integer colID) {
		return results.get(colID);
	}

	public int size() {
		int count = 0;
		for (TreeMap<Integer, List<NodeUnit>> posterList : results.values()) {
			for (List<NodeUnit> list : posterList.values())
				count += list.size();
		}
		return count;
	}

	public void filterOutRoot() {
		for (TreeMap<Integer, List<NodeUnit>> posterList : results.values()) {
			for (List<NodeUnit> entry : posterList.values()) {
				for (Iterator<NodeUnit> iter = entry.iterator(); iter.hasNext();) {
					NodeUnit unit = iter.next();
					if (unit.getLevel() != 1) {
						iter.remove();
					}
				}
			}
		}
	}

	public void merge(IndexQueryResult ret) {
		for (Iterator<Integer> colIter = ret.colIterator(); colIter.hasNext();) {
			int colID = colIter.next();
			addResult(colID, ret.getColResult(colID));
		}

	}
}
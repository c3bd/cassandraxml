/**
*@author:xiafan xiafan68@gmail.com
*@version: 2011-9-27 0.1
*/
package imc.disxmldb.index.btree;

import imc.disxmldb.index.Node;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.filter.IXMLFilter;

import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

/**
 * define the interface needed by an B+tree
 *
 */
public interface IBtree {
	/**
	 * put key/node to btree
	 * @param String,Node
	 * @return
	 */
	public void put(String key, Node node);
//	public void put(float key, Node node);
//	public void put(double key, Node node);

	
	/**
	 * 
	 * the List<IndexArrayUnit> should be ordered by docID, NodeUnit.range[0], NodeUnit.range[1]
	 * @param text
	 * @return List<IndexArrayUnit>
	 */
	public TreeMap<Integer, List<NodeUnit>> get(String key, IXMLFilter filter);
	public TreeMap<Integer, List<NodeUnit>> getLesser(String key);
	public TreeMap<Integer, List<NodeUnit>> getLesserOrEqual(String key);
	public TreeMap<Integer, List<NodeUnit>> getLesser(String key, IXMLFilter filter);
	public TreeMap<Integer, List<NodeUnit>> getLesserOrEqual(String key,
			IXMLFilter filter);
	
	public TreeMap<Integer, List<NodeUnit>> getGreater(String key);
	public TreeMap<Integer, List<NodeUnit>> getGreaterOrEqual(String key);
	public TreeMap<Integer, List<NodeUnit>> getGreater(String key, IXMLFilter filter);
	public TreeMap<Integer, List<NodeUnit>> getGreaterOrEqual(String key,IXMLFilter filter);
	
	public TreeMap<Integer, List<NodeUnit>> get(String key);
	
	public boolean contains(String key, Node node);
	public List<NodeUnit> getParentNode(String key, Node node, int level,
			IXMLFilter filter) ;
	public long estimateKeyResultNum(String key, int replicas);
	
	/**
	 * remove record from btree by key/node
	 * @param String,Node
	 * @return
	 */
	public void remove(String key, Node node) ;
	
	public void adjustCache(long cacheSize);
	
	public void close() throws IOException;
	
	public void flush() throws IOException;
	
	public void removeDataFiles();


	public List<NodeUnit> getChildNode(String stepName, Node node, int level,
			IXMLFilter filter);


	long estimateKeyResultNumB(String key, int replicas);
}

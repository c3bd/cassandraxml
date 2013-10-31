/**
 *  @author xiafan
 */
package imc.disxmldb.index.btree;

import imc.disxmldb.index.Node;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.filter.IdentityXMLFilter;
import imc.disxmldb.index.filter.IXMLFilter;

import java.util.LinkedList;
import java.util.List;
import org.apache.cassandra.utils.Pair;

/**
 * a decorator for B+tree that enable querying recursively into the child
 * collection of current collection.
 * 
 */
public class BtreeDecorator {
	private List<Pair<Integer, IBtree>> baseBtreeList = null;

	public BtreeDecorator() {
		baseBtreeList = new LinkedList<Pair<Integer, IBtree>>();
	}

	public BtreeDecorator(List<Pair<Integer, IBtree>> baseBtreeList) {
		this.baseBtreeList = baseBtreeList;
	}

	public void addBtree(Integer id, IBtree btree) {
		baseBtreeList.add(new Pair<Integer, IBtree>(id, btree));
	}

	/**
	 * 
	 * the List<IndexArrayUnit> should be ordered by docID, NodeUnit.range[0],
	 * NodeUnit.range[1]
	 * 
	 * @param text
	 * @return List<IndexArrayUnit>
	 */
	public IndexQueryResult get(String key, IXMLFilter filter) {
		IndexQueryResult result = new IndexQueryResult();
		for (Pair<Integer, IBtree> entry : baseBtreeList) {
			if (entry.right != null)
				result.addResult(entry.left, entry.right.get(key, filter));
		}
		return result;
	}

	public IndexQueryResult get(String key) {
		IndexQueryResult result = new IndexQueryResult();
		for (Pair<Integer, IBtree> entry : baseBtreeList) {
			if (entry.right != null)
				result.addResult(entry.left, entry.right.get(key));
		}
		return result;
	}

	public IndexQueryResult getLesser(String key) {
		return getLesser(key, IdentityXMLFilter.instance);
	}

	public IndexQueryResult getLesserOrEqual(String key) {
		return getLesserOrEqual(key, IdentityXMLFilter.instance);
	}

	public IndexQueryResult getLesser(String key, IXMLFilter filter) {
		IndexQueryResult result = new IndexQueryResult();
		for (Pair<Integer, IBtree> entry : baseBtreeList) {
			if (entry.right != null)
				result.addResult(entry.left, entry.right.getLesser(key, filter));
		}
		return result;
	}

	public IndexQueryResult getLesserOrEqual(String key, IXMLFilter filter) {
		IndexQueryResult result = new IndexQueryResult();
		for (Pair<Integer, IBtree> entry : baseBtreeList) {
			if (entry.right != null)
				result.addResult(entry.left,
						entry.right.getLesserOrEqual(key, filter));
		}
		return result;
	}

	public IndexQueryResult getGreater(String key) {
		return getGreater(key, IdentityXMLFilter.instance);
	}

	public IndexQueryResult getGreaterOrEqual(String key) {
		return getGreaterOrEqual(key, IdentityXMLFilter.instance);
	}

	public IndexQueryResult getGreater(String key, IXMLFilter filter) {
		IndexQueryResult result = new IndexQueryResult();
		for (Pair<Integer, IBtree> entry : baseBtreeList) {
			if (entry.right != null)
				result.addResult(entry.left,
						entry.right.getGreater(key, filter));
		}
		return result;
	}

	public IndexQueryResult getGreaterOrEqual(String key, IXMLFilter filter) {
		IndexQueryResult result = new IndexQueryResult();
		for (Pair<Integer, IBtree> entry : baseBtreeList) {
			if (entry.right != null)
				result.addResult(entry.left,
						entry.right.getGreaterOrEqual(key, filter));
		}
		return result;
	}

	public int estimateKeyResultNum(String key, int replicas) {
		int count = 0;
		for (Pair<Integer, IBtree> entry : baseBtreeList) {
			if (entry.right != null)
				count += entry.right.estimateKeyResultNum(key, replicas);
		}
		return count;
	}

	public boolean contains(String key, Node node) {
		for (Pair<Integer, IBtree> entry : baseBtreeList) {
			if (entry.right != null)
				if (entry.right.contains(key, node))
					return true;
		}
		return false;
	}

	/**
	 * there should be only one btree in this collection
	 * 
	 * @param key
	 * @param node
	 * @param level
	 * @param filter
	 * @return
	 */
	public List<NodeUnit> getParentNode(String key, Node node, int level,
			IXMLFilter filter) {
		for (Pair<Integer, IBtree> entry : baseBtreeList) {
			if (entry.right != null)
				return entry.right.getParentNode(key, node, level, filter);
			return null;
		}
		return null;
	}

	public List<NodeUnit> getChildNode(String stepName, Node node, int level,
			IXMLFilter filter) {
		for (Pair<Integer, IBtree> entry : baseBtreeList) {
			if (entry.right != null)
				return entry.right.getChildNode(stepName, node, level, filter);
			return null;
		}
		return null;
	}
}

package imc.disxmldb.index.invertindex;

import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.filter.IXMLFilter;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 * The interface encapsulate the algorithm used to retrieve data from the invert
 * index.
 * 
 */
public interface ICFRetrievalStrategy {
	public TreeMap<Integer, List<NodeUnit>> retrieve(Set<String> set,
			SortedSet<ByteBuffer> xmlDocs, IXMLFilter filter);
}

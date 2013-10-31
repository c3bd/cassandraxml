/**
*@author: xiafan xiafan68@gmail.com
*@version: 2011-9-27 0.1
*/
package imc.disxmldb.index.invertindex;

import imc.disxmldb.dom.XMLNode;
import imc.disxmldb.dom.XMLNodeImpl;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.filter.IXMLFilter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 * define the interface needed by an invert index
 */
public interface IInvertIndex {
	
		public void put(int xmlDocID, XMLNode node);
		/**
		 * the List<IndexArrayUnit> should be ordered by docID, NodeUnit.range[0], NodeUnit.range[1]
		 * @param text
		 * @return
		 */
		public TreeMap<Integer, List<NodeUnit>> get(String text);
		
		public TreeMap<Integer, List<NodeUnit>> get(Set<String> set,
				SortedSet<ByteBuffer> xmlDocs, IXMLFilter filter);
		/**
		 * 
		 * @param set
		 * @param xmlDocs
		 * 			if xmlDocs is not null, it will be used to filter out the posterlist with the set
		 * @return
		 */
		public TreeMap<Integer, List<NodeUnit>> get(Set<String> set, SortedSet<ByteBuffer> xmlDocs);
		
		public TreeMap<Integer, List<NodeUnit>> get(String text, IXMLFilter filter);
		
		public void put(String text, int xmlDocID, int nodeID, double[] range, int level);
		public void remove(String text, int xmlDocID, int nodeID, double[] range);
		
		public void flush() throws IOException;
		
		public void close() throws IOException;
		
		public void removeDataFiles() throws IOException;

}

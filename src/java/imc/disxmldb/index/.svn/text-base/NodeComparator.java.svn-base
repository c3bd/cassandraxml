/**
 * @author chengchengyu
 */
package imc.disxmldb.index;

import java.util.Comparator;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.je.DatabaseEntry;

/**
 * This class implements the comparator used to compare the object of Node
 * class. It will be used to sort the poster list of the B+tree.
 * 
 * @author xiafan
 * 
 */
public class NodeComparator implements Comparator<byte[]> {

	public NodeComparator() {
	}

	public int compare(byte[] o1, byte[] o2) {

		DatabaseEntry dbe1 = new DatabaseEntry(o1);
		DatabaseEntry dbe2 = new DatabaseEntry(o2);

		NodeBinding nodeBinding = new NodeBinding();

		Node node1 = (Node) nodeBinding.entryToObject(dbe1);
		Node node2 = (Node) nodeBinding.entryToObject(dbe2);

		int comp = node1.getXmlDocID() - node2.getXmlDocID();
		if (comp == 0) {
			if (node1.getNodeID() == node2.getNodeID())
				return 0;
			else {
				if (node1.getRange()[0] > node2.getRange()[0])
					return 1;
				else if (node1.getRange()[0] < node2.getRange()[0])
					return -1;
				else
					return 0;

			}
		} else {
			return comp < 0? -1 : 1;
		}
	}
}

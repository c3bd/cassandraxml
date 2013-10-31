/**
 * @author chengchengyu
 */
package imc.disxmldb.index;

import java.util.Comparator;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.je.DatabaseEntry;

/**
 * This class implements the comparator to compare two Node object. It first
 * compares by the node ID of two Node and then by the range encoding
 * 
 */
public class NodeIDComparator implements Comparator<NodeUnit> {

	public NodeIDComparator() {
	}

	public int compare(NodeUnit o1, NodeUnit o2) {

		if (o1.getRange()[1] == o1.getRange()[1])
			if (o1.getRange()[0] == o1.getRange()[0])
				if (o1.getNodeID() == o2.getNodeID())
					return 0;
				else
					return o1.getNodeID() > o2.getNodeID() ? 1 : -1;
			else
				return o1.getRange()[0] > o2.getRange()[0] ? 1 : -1;
		else
			return o1.getRange()[1] > o2.getRange()[1] ? 1 : -1;
	}

}

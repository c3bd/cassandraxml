package imc.disxmldb.index;

import java.util.Comparator;
/**
 * ComparatorByRangeFirst compares two XML nodes according to the range encoding
 *
 */
public class ComparatorByRangeFirst implements Comparator<NodeUnit>{
	public static ComparatorByRangeFirst instance = new ComparatorByRangeFirst();
	/**
	 * assume if the ids of two nodes are the same, two nodes are the same 
	 */
	@Override
	public int compare(NodeUnit o1, NodeUnit o2) {
		if (o1.getNodeID() == o2.getNodeID())
			return 0;
		if (o1.getRange()[0] == o2.getRange()[0]) {
			if (o1.getRange()[1] == o2.getRange()[1]) {
				return o1.getNodeID() - o2.getNodeID();
			} else {
				return (o1.getRange()[1] > o2.getRange()[1])? 1 : -1;
			} 
		} else{
			return (o1.getRange()[0] > o2.getRange()[0]) ? 1 : -1;
		}
	}
}

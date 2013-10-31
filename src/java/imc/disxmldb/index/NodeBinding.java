/**
 * @author chengchengyu
 */
package imc.disxmldb.index;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * This class defines the serialization and deserialization behavior to store a
 * node into the B+tree
 * 
 * @author xiafan
 * 
 */
public class NodeBinding extends TupleBinding<Node> {

	@Override
	public Node entryToObject(TupleInput ti) {
		int xmlID = ti.readInt();
		int nodeID = ti.readInt();
		double[] range = new double[2];
		range[0] = ti.readDouble();
		range[1] = ti.readDouble();
		int level = ti.readInt();
		return new Node(xmlID, nodeID, range, level);
	}

	@Override
	public void objectToEntry(Node node, TupleOutput to) {
		to.writeInt(node.getXmlDocID());
		to.writeInt(node.getNodeID());
		double[] range = node.getRange();
		to.writeDouble(range[0]);
		to.writeDouble(range[1]);
		to.writeInt(node.getLevel());
	}

}
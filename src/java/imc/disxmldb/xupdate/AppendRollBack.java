package imc.disxmldb.xupdate;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.cassandra.thrift.UnavailableException;
/**
 * @author xiafan
 */
import imc.disxmldb.CollectionStore;
import imc.disxmldb.config.XMLMetaData;
import imc.disxmldb.dom.ElementNode;
import imc.disxmldb.dom.XMLNode;
import imc.disxmldb.dom.XMLNodeForMutation;

/**
 * rollback all the append operations, which will delete all the appended nodes
 * and update the root node of the appended nodes
 */
public class AppendRollBack {
	private CollectionStore colStore = null;
	private List<XMLNode> nodes = null;
	private XMLMetaData meta = null;
	private ElementNode parent = null;

	/**
	 * 
	 * @param colStore
	 * @param nodes
	 */
	public AppendRollBack(CollectionStore colStore, XMLMetaData meta,
			ElementNode parent, List<XMLNode> nodes) {
		this.colStore = colStore;
		this.meta = meta;
		this.parent = parent;
		this.nodes = nodes;
	}

	public void rollback() {
		// delete the appended nodes
		HashMap<Integer, List<XMLNodeForMutation>> xmlNodeIDsPair = new HashMap<Integer, List<XMLNodeForMutation>>();
		try {
			colStore.deleteNodeRemote(xmlNodeIDsPair);
		} catch (Exception e) {
			// TODO then this operation should be stored and executed in future
		}

		// update the parent node
		if (parent != null) {
			for (XMLNode node : nodes) {
				if (node instanceof ElementNode)
					parent.deleteChild(node);
				else
					parent.deleteAttribute(node);
			}
		}
		try {
			colStore.storeElementNode(meta, parent);
		} catch (UnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

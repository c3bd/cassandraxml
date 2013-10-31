package imc.disxmldb.cassandra.verbhandler.result;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.cassandra.dht.AbstractBounds;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.xmldb.api.base.XMLDBException;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.XMLDBStore;
import imc.disxmldb.cassandra.verbhandler.XPathQueryCommand;
import imc.disxmldb.cassandra.verbhandler.XPathResult.XPathResultType;
import imc.disxmldb.dom.XMLNodeForMutation;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.btree.IndexQueryResult;
import imc.disxmldb.util.IXPathResult;

public class XMLNodeForMutations implements IXPathResult<XMLNodeForMutations> {
	public Map<Integer, Map<Integer, List<XMLNodeForMutation>>> nodes = new TreeMap<Integer, Map<Integer, List<XMLNodeForMutation>>>();

	public void addNodeSketch(IndexQueryResult result_) {
		Iterator<Integer> colIter = result_.colIterator();
		while (colIter.hasNext()) {
			int colID = colIter.next();
			CollectionStore colStore;
			try {
				colStore = XMLDBStore.instance().getCollection(colID);
			} catch (XMLDBException e) {
				continue;
			}
			if (colStore == null)
				continue;
			TreeMap<Integer, List<NodeUnit>> nodes = result_
					.getColResult(colID);
			for (Entry<Integer, List<NodeUnit>> XMLEntry : nodes.entrySet()) {
				int XMLID = XMLEntry.getKey();
				for (NodeUnit nodeUnit : XMLEntry.getValue()) {
					addXMLNodeSketch(
							colID,
							XMLID,
							colStore.retrieveNodeSketch(XMLID,
									nodeUnit.getNodeID()));
				}
			}

		}
	}

	public void addXMLNodeSketch(Integer colID, Integer xmlID,
			XMLNodeForMutation sketch) {
		if (sketch == null)
			return;
		if (nodes.containsKey(colID)) {
			Map<Integer, List<XMLNodeForMutation>> xml2NodeSketchs = nodes
					.get(colID);
			if (xml2NodeSketchs.get(xmlID) != null) {
				xml2NodeSketchs.get(xmlID).add(sketch);
			} else {
				List<XMLNodeForMutation> nodes = new LinkedList<XMLNodeForMutation>();
				nodes.add(sketch);
				xml2NodeSketchs.put(xmlID, nodes);
			}
		} else {
			HashMap<Integer, List<XMLNodeForMutation>> xml2NodeSketchs = new HashMap<Integer, List<XMLNodeForMutation>>();
			nodes.put(colID, xml2NodeSketchs);
			List<XMLNodeForMutation> nodes = new LinkedList<XMLNodeForMutation>();
			nodes.add(sketch);
			xml2NodeSketchs.put(xmlID, nodes);
		}
	}

	@Override
	public ByteBuffer getDigest() {
		// TODO Auto-generated method stub
		return ByteBufferUtil.EMPTY_BYTE_BUFFER;
	}

	@Override
	public void serialize(XMLNodeForMutations data, DataOutputStream dos,
			int version) throws IOException {
		dos.writeInt(data.nodes.size());
		for (Entry<Integer, Map<Integer, List<XMLNodeForMutation>>> colEntry : data.nodes
				.entrySet()) {
			dos.writeInt(colEntry.getKey());
			Map<Integer, List<XMLNodeForMutation>> xml2NodeIDs = colEntry
					.getValue();
			dos.writeInt(xml2NodeIDs.size());
			for (Entry<Integer, List<XMLNodeForMutation>> entry : xml2NodeIDs
					.entrySet()) {
				dos.writeInt(entry.getKey());
				dos.writeInt(entry.getValue().size());
				for (XMLNodeForMutation node : entry.getValue()) {
					XMLNodeForMutation.serialize(node, dos, version);
				}
			}
		}
	}

	@Override
	public XMLNodeForMutations deserialize(DataInputStream dis, int version)
			throws IOException {
		assert nodes != null;
		nodes.clear();

		int colCount = dis.readInt();
		for (int l = 0; l < colCount; l++) {
			int colID = dis.readInt();
			HashMap<Integer, List<XMLNodeForMutation>> xml2Nodes = new HashMap<Integer, List<XMLNodeForMutation>>();
			int mapLen = dis.readInt();
			for (int i = 0; i < mapLen; i++) {
				int xmlID = dis.readInt();
				int listLen = dis.readInt();
				List<XMLNodeForMutation> value = new LinkedList<XMLNodeForMutation>();
				for (int k = 0; k < listLen; k++) {
					value.add(XMLNodeForMutation.deserialize(dis, version));
				}
				xml2Nodes.put(xmlID, value);
			}
			nodes.put(colID, xml2Nodes);
		}
		return this;
	}

	@Override
	public void merge(int index_, XMLNodeForMutations other_) {
		for (Entry<Integer, Map<Integer, List<XMLNodeForMutation>>> colEntry : other_.nodes
				.entrySet()) {
			if (nodes.containsKey(colEntry.getKey())) {
				Map<Integer, List<XMLNodeForMutation>> xml2Nodes = nodes
						.get(colEntry.getKey());
				for (Entry<Integer, List<XMLNodeForMutation>> entry : colEntry
						.getValue().entrySet()) {
					if (xml2Nodes.containsKey(entry.getKey())) {
						xml2Nodes.get(entry.getKey()).addAll(entry.getValue());
					} else {
						xml2Nodes.put(entry.getKey(), entry.getValue());
					}
				}
			} else {
				nodes.put(colEntry.getKey(), colEntry.getValue());
			}

		}

	}

	@Override
	public void postProcess() {
	}

	@Override
	public String postProcess(XPathQueryCommand cmd, byte[] cmdb,
			CollectionStore colStore, List<AbstractBounds> ranges) {
		return null;
	}

	@Override
	public List<String> result() {
		return null;
	}

	@Override
	public void setIndex(int index_) {

	}

	@Override
	public XPathResultType getType() {
		return XPathResultType.NodeSketch;
	}

	@Override
	public int size() {
		return nodes.size();
	}

}

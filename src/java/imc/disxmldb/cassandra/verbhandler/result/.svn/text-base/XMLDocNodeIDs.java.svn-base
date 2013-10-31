package imc.disxmldb.cassandra.verbhandler.result;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.cassandra.verbhandler.XPathQueryCommand;
import imc.disxmldb.cassandra.verbhandler.XPathResult.XPathResultType;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.btree.IndexQueryResult;
import imc.disxmldb.util.IXPathResult;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.cassandra.dht.AbstractBounds;
import org.apache.cassandra.utils.FBUtilities;

public class XMLDocNodeIDs implements IXPathResult<XMLDocNodeIDs> {
	// colID: {xmlDocID: [nodeID]}
	public Map<Integer, Map<Integer, List<Integer>>> nodeIDs = new TreeMap<Integer, Map<Integer, List<Integer>>>();

	@Override
	public int size() {
		return nodeIDs.size();
	}

	public void addNodeID(Integer colID, Integer xmlID, Integer nodeID) {
		if (nodeIDs.containsKey(colID)) {
			Map<Integer, List<Integer>> xml2NodeIDs = nodeIDs.get(colID);
			if (xml2NodeIDs.get(xmlID) == null) {
				List<Integer> nodes = new LinkedList<Integer>();
				nodes.add(nodeID);
				xml2NodeIDs.put(xmlID, nodes);
			} else {
				xml2NodeIDs.get(xmlID).add(nodeID);
			}
		} else {
			HashMap<Integer, List<Integer>> xml2NodeIDs = new HashMap<Integer, List<Integer>>();
			nodeIDs.put(colID, xml2NodeIDs);
			List<Integer> nodes = new LinkedList<Integer>();
			nodes.add(nodeID);
			xml2NodeIDs.put(xmlID, nodes);
		}
	}

	@Override
	public void merge(int index_, XMLDocNodeIDs other) {
		for (Entry<Integer, Map<Integer, List<Integer>>> colEntry : other.nodeIDs
				.entrySet()) {
			if (nodeIDs.containsKey(colEntry.getKey())) {
				Map<Integer, List<Integer>> xml2NodeIDs = nodeIDs
						.get(colEntry.getKey());
				for (Entry<Integer, List<Integer>> entry : colEntry
						.getValue().entrySet()) {
					if (xml2NodeIDs.containsKey(entry.getKey())) {
						xml2NodeIDs.get(entry.getKey()).addAll(
								entry.getValue());
					} else {
						xml2NodeIDs.put(entry.getKey(), entry.getValue());
					}
				}
			} else {
				nodeIDs.put(colEntry.getKey(), colEntry.getValue());
			}

		}
	}

	@Override
	public ByteBuffer getDigest() {
		MessageDigest digester = FBUtilities.threadLocalMD5Digest();
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(aos);

		try {
			for (Entry<Integer, Map<Integer, List<Integer>>> colEntry : nodeIDs
					.entrySet()) {
				dos.writeInt(colEntry.getKey());
				Map<Integer, List<Integer>> xml2NodeIDs = colEntry
						.getValue();
				for (Entry<Integer, List<Integer>> entry : xml2NodeIDs
						.entrySet()) {
					dos.writeInt(entry.getKey());
					for (Integer nodeID : entry.getValue()) {
						dos.writeInt(nodeID);
					}
				}
			}
			digester.digest(aos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ByteBuffer.wrap(digester.digest());
	}

	@Override
	public void serialize(XMLDocNodeIDs data, DataOutputStream dos,
			int version) throws IOException {
		dos.writeInt(data.nodeIDs.size());
		for (Entry<Integer, Map<Integer, List<Integer>>> colEntry : data.nodeIDs
				.entrySet()) {
			dos.writeInt(colEntry.getKey());
			Map<Integer, List<Integer>> xml2NodeIDs = colEntry.getValue();
			dos.writeInt(xml2NodeIDs.size());
			for (Entry<Integer, List<Integer>> entry : xml2NodeIDs
					.entrySet()) {
				dos.writeInt(entry.getKey());
				dos.writeInt(entry.getValue().size());
				for (Integer nodeID : entry.getValue()) {
					dos.writeInt(nodeID);
				}
			}
		}
	}

	@Override
	public XMLDocNodeIDs deserialize(DataInputStream dis, int version)
			throws IOException {
		assert nodeIDs != null;
		nodeIDs.clear();

		int colCount = dis.readInt();
		for (int l = 0; l < colCount; l++) {
			int colID = dis.readInt();
			HashMap<Integer, List<Integer>> xml2NodeIDs = new HashMap<Integer, List<Integer>>();
			int mapLen = dis.readInt();
			for (int i = 0; i < mapLen; i++) {
				int xmlID = dis.readInt();
				int listLen = dis.readInt();
				List<Integer> value = new LinkedList<Integer>();
				for (int k = 0; k < listLen; k++) {
					value.add(dis.readInt());
				}
				xml2NodeIDs.put(xmlID, value);
			}
			nodeIDs.put(colID, xml2NodeIDs);
		}
		return this;
	}

	public void addNodeID(IndexQueryResult result) {
		for (Iterator<Integer> colIter = result.colIterator(); colIter
				.hasNext();) {
			int colID = colIter.next();
			Map<Integer, List<NodeUnit>> qRet = result.getColResult(colID);
			for (Entry<Integer, List<NodeUnit>> entry : qRet.entrySet()) {
				for (NodeUnit node : entry.getValue()) {
					addNodeID(colID, entry.getKey(), node.getNodeID());
				}
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
		return XPathResultType.NodeIDs;
	}
}

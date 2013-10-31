package imc.disxmldb.cassandra.verbhandler.result;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.XMLDBStore;
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
import org.xmldb.api.base.XMLDBException;

public class XMLDocXMLParts implements IXPathResult<XMLDocXMLParts> {
	public Map<Integer, Map<Integer, List<String>>> XMLParts = new TreeMap<Integer, Map<Integer, List<String>>>();

	@Override
	public ByteBuffer getDigest() {
		MessageDigest digester = FBUtilities.threadLocalMD5Digest();
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(aos);

		try {
			for (Entry<Integer, Map<Integer, List<String>>> colEntry : XMLParts
					.entrySet()) {
				dos.writeInt(colEntry.getKey());
				Map<Integer, List<String>> xml2XMLParts = colEntry
						.getValue();
				for (Entry<Integer, List<String>> entry : xml2XMLParts
						.entrySet()) {
					dos.writeInt(entry.getKey());
					for (String part : entry.getValue()) {
						dos.writeUTF(part);
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
	public void merge(int index_, XMLDocXMLParts other) {
		for (Entry<Integer, Map<Integer, List<String>>> colEntry : other.XMLParts
				.entrySet()) {
			if (XMLParts.containsKey(colEntry.getKey())) {
				Map<Integer, List<String>> xml2Parts = XMLParts
						.get(colEntry.getKey());
				for (Entry<Integer, List<String>> entry : colEntry
						.getValue().entrySet()) {
					if (xml2Parts.containsKey(entry.getKey())) {
						xml2Parts.get(entry.getKey()).addAll(
								entry.getValue());
					} else {
						xml2Parts.put(entry.getKey(), entry.getValue());
					}
				}
			} else {
				XMLParts.put(colEntry.getKey(), colEntry.getValue());
			}

		}
	}

	/**
	 * this function will only retrieve text locally
	 * 
	 * @param result_
	 * @param colStore
	 */
	public void addXMLParts(IndexQueryResult result_) {
		for (Iterator<Integer> colIter = result_.colIterator(); colIter
				.hasNext();) {
			int colID = colIter.next();
			CollectionStore colStore = null;
			try {
				colStore = XMLDBStore.instance().getCollection(colID);
			} catch (XMLDBException e) {
			}
			if (colStore == null)
				continue;
			Map<Integer, List<NodeUnit>> qRet = result_.getColResult(colID);
			for (Entry<Integer, List<NodeUnit>> entry : qRet.entrySet()) {
				for (NodeUnit node : entry.getValue()) {
					addXMLParts(
							colID,
							entry.getKey(),
							colStore.retrieveLocal(entry.getKey(),
									node.getNodeID()));
				}
			}
		}
	}

	public void addXMLParts(Integer colID, Integer xmlID, String xmlPart) {
		if (xmlPart == null || xmlPart.isEmpty())
			return;
		if (XMLParts.containsKey(colID)) {
			Map<Integer, List<String>> xml2NodeIDs = XMLParts.get(colID);
			if (xml2NodeIDs.get(xmlID) != null) {
				xml2NodeIDs.get(xmlID).add(xmlPart);
			} else {
				List<String> nodes = new LinkedList<String>();
				nodes.add(xmlPart);
				xml2NodeIDs.put(xmlID, nodes);
			}
		} else {
			HashMap<Integer, List<String>> xml2NodeIDs = new HashMap<Integer, List<String>>();
			XMLParts.put(colID, xml2NodeIDs);
			List<String> nodes = new LinkedList<String>();
			nodes.add(xmlPart);
			xml2NodeIDs.put(xmlID, nodes);
		}
	}

	public void addXMLParts(Integer key, Map<Integer, List<String>> retText) {
		if (XMLParts.get(key) != null) {
			XMLParts.get(key).putAll(retText);
		} else {
			XMLParts.put(key, retText);
		}
	}

	@Override
	public List<String> result() {
		List<String> ret = new LinkedList<String>();
		for (Map<Integer, List<String>> posterList : XMLParts.values()) {
			for (List<String> list : posterList.values()) {
				ret.addAll(list);
			}
		}
		return ret;
	}

	@Override
	public int size() {
		int ret = 0;
		for (Map<Integer, List<String>> posterList : XMLParts.values()) {
			for (List<String> list : posterList.values()) {
				ret += list.size();
			}
		}
		return ret;
	}
	
	@Override
	public void serialize(XMLDocXMLParts xmlParts_, DataOutputStream dos,
			int version) throws IOException {
		dos.writeInt(xmlParts_.XMLParts.size());
		for (Entry<Integer, Map<Integer, List<String>>> colEntry : xmlParts_.XMLParts
				.entrySet()) {
			dos.writeInt(colEntry.getKey());
			Map<Integer, List<String>> xml2Parts = colEntry.getValue();
			dos.writeInt(xml2Parts.size());
			for (Entry<Integer, List<String>> entry : xml2Parts.entrySet()) {
				dos.writeInt(entry.getKey());
				dos.writeInt(entry.getValue().size());
				for (String part : entry.getValue()) {
					dos.writeUTF(part);
				}
			}
		}
	}

	public XMLDocXMLParts deserialize(DataInputStream dis, int version)
			throws IOException {
		assert XMLParts != null;
		XMLParts.clear();
		int colCount = dis.readInt();
		for (int l = 0; l < colCount; l++) {
			int colID = dis.readInt();
			HashMap<Integer, List<String>> xml2NodeIDs = new HashMap<Integer, List<String>>();
			int mapLen = dis.readInt();
			for (int i = 0; i < mapLen; i++) {
				int xmlID = dis.readInt();
				int listLen = dis.readInt();
				List<String> value = new LinkedList<String>();
				for (int k = 0; k < listLen; k++) {
					value.add(dis.readUTF());
				}
				xml2NodeIDs.put(xmlID, value);
			}
			XMLParts.put(colID, xml2NodeIDs);
		}
		return this;
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
	public void setIndex(int index_) {
	}

	@Override
	public XPathResultType getType() {
		return XPathResultType.xmlParts;
	}
}

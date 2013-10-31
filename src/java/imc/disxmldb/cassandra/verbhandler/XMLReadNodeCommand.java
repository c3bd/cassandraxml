/**
 *@author:xiafan xiafan68@gmail.com
 *@version: 2011-10-8 0.1
 */
package imc.disxmldb.cassandra.verbhandler;

import imc.disxmldb.CollectionStore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.ReadCommand;
import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.io.util.DataOutputBuffer;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessageProducer;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;

public class XMLReadNodeCommand implements MessageProducer {
	public static final byte DIGEST = 1;
	public static final byte NONDIGEST = 0;
	private static XMLReadNodeCommandSerializer serializer = new XMLReadNodeCommandSerializer();
	
	private int colID = -1;
	private int xmlID = -1;
	private int nodeID = 0;
	private byte isDigest = 0;

	public static XMLReadNodeCommandSerializer serializer() {
		return serializer;
	}
	
	public XMLReadNodeCommand(int colID, int xmlID, int nodeID, byte isDigest) {
		this.colID = colID;
		this.xmlID = xmlID;
		this.nodeID = nodeID;
		this.isDigest = isDigest;
	}

	public ByteBuffer getKey() {
		return CollectionStore.COLKEYVALIDATOR.fromString(Integer.toString(xmlID));
	}
	@Override
	public Message getMessage(Integer version) throws IOException {
		DataOutputBuffer dob = new DataOutputBuffer();
		serializer.serialize(this, dob, version);
		Message message = new Message(FBUtilities.getBroadcastAddress(),
				StorageService.Verb.XML_READ_NODE, Arrays.copyOf(dob.getData(),
						dob.getLength()), version);
		return message;
	}

	public int getColID() {
		return colID;
	}

	public void setColID(int colID) {
		this.colID = colID;
	}

	public int getXmlID() {
		return xmlID;
	}

	public void setXmlID(int xmlID) {
		this.xmlID = xmlID;
	}

	public byte getIsDigest() {
		return isDigest;
	}

	public void setIsDigest(byte isDigest) {
		this.isDigest = isDigest;
	}

	public int getNodeID() {
		return nodeID;
	}

	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	public XMLReadNodeCommand copy() {
		XMLReadNodeCommand ret = new XMLReadNodeCommand(this.colID, this.xmlID, this.nodeID, this.isDigest);
		return ret;
	}
}

class XMLReadNodeCommandSerializer implements ICompactSerializer<XMLReadNodeCommand> {

	@Override
	public void serialize(XMLReadNodeCommand t, DataOutputStream dos,
			int version) throws IOException {
		dos.writeInt(t.getColID());
		dos.writeInt(t.getXmlID());
		dos.writeInt(t.getNodeID());
		dos.writeByte(t.getIsDigest());
	}

	@Override
	public XMLReadNodeCommand deserialize(DataInputStream dis, int version)
			throws IOException {
		return new XMLReadNodeCommand(dis.readInt(), dis.readInt(), dis.readInt(), dis.readByte());
	}
	
}
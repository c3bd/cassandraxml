package imc.disxmldb.cassandra.verbhandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.cassandra.dht.AbstractBounds;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.io.util.DataOutputBuffer;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessageProducer;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.service.StorageService;

public class XPathQueryCommand implements MessageProducer{
	public static final byte XPATH_RECURSIVE = 1;
	public static final byte XPATH_READFULL = 2;
	public static final byte XPATH_DIGEST = 4;
	public static final byte XPATH_NODEONLY = 8;
	public static final byte XPATH_NODEID = 16;
	
	private static final XPathQueryCommandSerializer serializer = new XPathQueryCommandSerializer();
	public AbstractBounds range = null;
	public int colID = 0;
	public byte[] command = null;
	public byte flag = 0;
	/*public boolean readFull = false;
	public boolean recursive = true;
	public boolean isDigest = false;*/
	
	public XPathQueryCommand(int colID_, byte[] command_, byte flag_) {
		this.colID = colID_;
		this.command = command_;
		this.flag = flag_;
	}
	/*
	public XPathQueryCommand(int colID, byte[] command, boolean readFull, boolean isDigest, boolean recursive) {
		this.colID = colID;
		this.command = command;
			
		this.readFull = readFull;
		this.isDigest = isDigest;
		this.recursive = recursive;
	}*/
	
	public static XPathQueryCommandSerializer serializer() {
		return serializer;
	}
	
	public XPathQueryCommand copy() {
		//XPathQueryCommand ret = new XPathQueryCommand(colID, command, readFull, isDigest, recursive);
		XPathQueryCommand ret = new XPathQueryCommand(colID, command, flag);
		ret.range = range;
		return ret;
	}
	
	@Override
	public Message getMessage(Integer version) throws IOException {
		DataOutputBuffer dob = new DataOutputBuffer();
		serializer.serialize(this, dob, version);
		Message message = new Message(FBUtilities.getBroadcastAddress(),
				StorageService.Verb.XML_XQUERY, Arrays.copyOf(dob.getData(),
						dob.getLength()), version);
		return message;
	}
	
	public boolean isDigest() {
		return (flag & XPATH_DIGEST) != 0;
	}
	
	public boolean isReadFull() {
		return (flag & XPATH_READFULL) != 0;
	}
	
	public boolean isReadNodeID() {
		return (flag & XPATH_NODEID) != 0;
	}
	
	public boolean isReadNodeOnly() {
		return (flag & XPATH_NODEONLY) != 0;
	}
	
	public boolean isRecursive() {
		return (flag & XPATH_RECURSIVE) != 0;
	}
	public static byte installFlag(byte flag_, byte flagToInstall_) {
		return (byte) (flag_|flagToInstall_);
	}
	
	public static byte unInstallFlag(byte flag_, byte flagToInstall) {
		return (byte)(flag_ & ~flagToInstall);
	}
}

class XPathQueryCommandSerializer implements ICompactSerializer<XPathQueryCommand> {

	@Override
	public void serialize(XPathQueryCommand t, DataOutputStream dos, int version)
			throws IOException {
		AbstractBounds.serializer().serialize(t.range, dos);
		dos.writeInt(t.colID);
		dos.writeInt(t.command.length);
		dos.write(t.command);
		dos.writeByte(t.flag);
		/*dos.writeBoolean(t.readFull);
		dos.writeBoolean(t.isDigest);
		dos.writeBoolean(t.recursive);*/
	}

	@Override
	public XPathQueryCommand deserialize(DataInputStream dis, int version)
			throws IOException {
		AbstractBounds range = AbstractBounds.serializer().deserialize(dis);
		int colID = dis.readInt();
		int len = dis.readInt();
		byte[] command = new byte[len];
		dis.read(command);
		//XPathQueryCommand ret = new XPathQueryCommand(colID, command, dis.readBoolean(), dis.readBoolean(), dis.readBoolean());
		XPathQueryCommand ret = new XPathQueryCommand(colID, command, dis.readByte());
		ret.range = range;
		return ret;
	}
	
}
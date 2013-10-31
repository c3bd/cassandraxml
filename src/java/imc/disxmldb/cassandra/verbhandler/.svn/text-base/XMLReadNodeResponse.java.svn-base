/**
 *@author:xiafan xiafan68@gmail.com
 *@version: 2011-10-8 0.1
 */
package imc.disxmldb.cassandra.verbhandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.utils.ByteBufferUtil;

public class XMLReadNodeResponse {
	String xmlContent = null;
	ByteBuffer digest = null;
	private static final XMLReadNodeResponseSerializer serializer = new XMLReadNodeResponseSerializer();

	public static XMLReadNodeResponseSerializer serializer() {
		return serializer;
	}

	public XMLReadNodeResponse(String xmlContent) {
		this.xmlContent = xmlContent;
	}

	public XMLReadNodeResponse(ByteBuffer digest) {
		this.digest = digest;
	}

	public String getXmlContent() {
		return xmlContent;
	}

	public void setXmlContent(String xmlContent) {
		this.xmlContent = xmlContent;
	}

	public ByteBuffer getDigest() {
		return digest;
	}

	public void setDigest(ByteBuffer digest) {
		this.digest = digest;
	}

}

class XMLReadNodeResponseSerializer implements
		ICompactSerializer<XMLReadNodeResponse> {

	@Override
	public void serialize(XMLReadNodeResponse t, DataOutputStream dos,
			int version) throws IOException {
		
		if (t.getXmlContent() == null) {
			dos.writeByte(XMLReadNodeCommand.DIGEST);
			ByteBufferUtil.writeWithLength(t.getDigest(), dos);
		} else {
			dos.writeByte(XMLReadNodeCommand.NONDIGEST);
			dos.writeUTF(t.getXmlContent());
		}

	}

	@Override
	public XMLReadNodeResponse deserialize(DataInputStream dis, int version)
			throws IOException {
		// TODO Auto-generated method stub
		byte isDigest = dis.readByte();
		if (isDigest == XMLReadNodeCommand.DIGEST) {
			return new XMLReadNodeResponse(ByteBufferUtil.readWithLength(dis));
		} else {
			return new XMLReadNodeResponse(dis.readUTF());
		}
	}

}
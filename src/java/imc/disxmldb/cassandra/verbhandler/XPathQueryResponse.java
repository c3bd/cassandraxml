package imc.disxmldb.cassandra.verbhandler;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.utils.ByteBufferUtil;

public class XPathQueryResponse {
	public static final XPathQueryResponseSerializer serializer = new XPathQueryResponseSerializer();
	public XPathResult result = null;

	public boolean isDigest = false;
	public ByteBuffer digest = null;

	public XPathQueryResponse(boolean isDigest) {
		this.isDigest = isDigest;
	}
}

class XPathQueryResponseSerializer implements
		ICompactSerializer<XPathQueryResponse> {

	@Override
	public void serialize(XPathQueryResponse t, DataOutputStream dos,
			int version) throws IOException {
		dos.writeBoolean(t.isDigest);
		if (t.isDigest) {
			ByteBufferUtil.writeWithShortLength(t.digest, dos);
		} else {
			t.result.serializer.serialize(t.result, dos, version);
		}
	}

	@Override
	public XPathQueryResponse deserialize(DataInputStream dis, int version)
			throws IOException {
		boolean isDigest = dis.readBoolean();
		XPathQueryResponse ret = new XPathQueryResponse(isDigest);
		if (isDigest) {
			ret.digest = ByteBufferUtil.readWithShortLength(dis);
		} else {
			ret.result = XPathResult.serializer.deserialize(dis, version);
		}
		return ret;
	}
}
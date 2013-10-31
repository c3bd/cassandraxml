package imc.disxmldb.cassandra.verbhandler.result;

import imc.disxmldb.cassandra.verbhandler.XPathResult.XPathResultType;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;

import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.utils.FBUtilities;

/**
 * This class is used to store the results of the min function in xpath.
 * It contains the serialization and deserialization of the value.
 */
public class MinResult extends FunctionReturn{
	public double min = 0.0d;
	
	public MinResult() {
		funcType = XPathResultType.Min;
	}
	
	@Override
	public void merge(int index_, FunctionReturn other) {
		if (min > ((MinResult)other).min )
			min = ((MinResult)other).min;
	}

	@Override
	public List<String> result() {
		List<String> ret = new LinkedList<String>();
		ret.add(Double.toString(min));
		return ret;
	}

	@Override
	public ByteBuffer getDigest() {
		MessageDigest digester = FBUtilities.threadLocalMD5Digest();
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(aos);

		try {
			dos.writeDouble(min);
			digester.digest(aos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ByteBuffer.wrap(digester.digest());
	}
}

class MinResultSerializer extends FunctionReturnSerializer{

	@Override
	public void serialize(FunctionReturn t, DataOutputStream dos, int version)
			throws IOException {
		//dos.writeInt(t.funcType.ordinal());
		dos.writeDouble(((MinResult)t).min);
	}

	@Override
	public MinResult deserialize(DataInputStream dis, int version)
			throws IOException {
		MinResult ret = new MinResult();
		ret.min = dis.readDouble();
		return ret;
	}
}
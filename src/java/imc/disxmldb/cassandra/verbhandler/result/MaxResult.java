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
 * This class is used to store the results of the max function in xpath. 
 * It contains the serialization and deserialization of the value. The merge function will be 
 * invoked on the coordinator node, which just select the max value of all the results returned by
 * the task nodes.
 */
public class MaxResult extends FunctionReturn {
	public double max = 0.0;

	public MaxResult() {
		funcType = XPathResultType.Max;
	}
	
	@Override
	public void merge(int index_, FunctionReturn other) {
		// TODO Auto-generated method stub
		if (max < ((MaxResult) other).max) {
			max = ((MaxResult) other).max;
		}
	}

	@Override
	public List<String> result() {
		List<String> ret = new LinkedList<String>();
		ret.add(Double.toString(max));
		return ret;
	}

	@Override
	public ByteBuffer getDigest() {
		MessageDigest digester = FBUtilities.threadLocalMD5Digest();
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(aos);

		try {
			dos.writeDouble(max);
			digester.digest(aos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ByteBuffer.wrap(digester.digest());
	}
}

class MaxResultSerializer extends FunctionReturnSerializer {

	@Override
	public void serialize(FunctionReturn t, DataOutputStream dos, int version)
			throws IOException {
		//dos.writeInt(t.funcType.ordinal());
		dos.writeDouble(((MaxResult)t).max);
	}

	@Override
	public MaxResult deserialize(DataInputStream dis, int version)
			throws IOException {
		MaxResult ret = new MaxResult();
		ret.max = dis.readDouble();
		return ret;
	}
}

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
 * This class is used to store the results of the sum function in xpath.
 *  It contains the serialization and deserialization of the value. The merge function will
 *  just add up the sum result of all the returned value.
 */
public class SumResult extends FunctionReturn {
	public double sum = 0.0;

	public SumResult() {
		funcType = XPathResultType.Sum;
	}

	@Override
	public void merge(int index_, FunctionReturn other) {
		sum += ((SumResult)other).sum;
	}

	@Override
	public List<String> result() {
		List<String> ret = new LinkedList<String>();
		ret.add(Double.toString(sum));
		return ret;
	}

	@Override
	public ByteBuffer getDigest() {
		MessageDigest digester = FBUtilities.threadLocalMD5Digest();
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(aos);

		try {
			dos.writeDouble(sum);
			digester.digest(aos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ByteBuffer.wrap(digester.digest());
	}
}

class SumResultSerializer extends FunctionReturnSerializer {

	@Override
	public void serialize(FunctionReturn t, DataOutputStream dos, int version)
			throws IOException {
		//dos.writeInt(t.funcType.ordinal());
		dos.writeDouble(((SumResult)t).sum);
	}

	@Override
	public SumResult deserialize(DataInputStream dis, int version)
			throws IOException {
		SumResult ret = new SumResult();
		ret.sum = dis.readDouble();
		return ret;
	}
}
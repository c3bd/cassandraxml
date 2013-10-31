/**
 *  @author xiafan xiafan68@gmail.com
 */
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
import java.util.Map.Entry;

import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.utils.FBUtilities;
/**
 * This class is used to store the results of an average function. As the average result should be 
 * aggregated on the coordinator node at last, this class doesn't store the real average numerical
 * value, but the count and sum of the results.This file also contains the serialization
 * and deserialization of this class.
 */
public class AvgResult extends FunctionReturn {
	public double sum = 0;
	public int count = 0;

	public AvgResult() {
		funcType = XPathResultType.Average;
	}

	@Override
	public void merge(int index_, FunctionReturn other_) {
		sum += ((AvgResult) other_).sum;
		count += ((AvgResult) other_).count;
	}

	@Override
	public List<String> result() {
		List<String> ret = new LinkedList<String>();
		if (count == 0)
			ret.add("0");
		else {
			ret.add(Double.toString(sum / count));
		}
		return ret;
	}

	@Override
	public ByteBuffer getDigest() {
		MessageDigest digester = FBUtilities.threadLocalMD5Digest();
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(aos);

		try {
			dos.writeDouble(sum);
			dos.writeInt(count);
			digester.digest(aos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ByteBuffer.wrap(digester.digest());
	}
}

class AvgResultSerializer extends FunctionReturnSerializer {

	@Override
	public void serialize(FunctionReturn t, DataOutputStream dos, int version)
			throws IOException {
		//dos.writeInt(t.funcType.ordinal());
		dos.writeDouble(((AvgResult) t).sum);
		dos.writeInt(((AvgResult) t).count);
	}

	@Override
	public AvgResult deserialize(DataInputStream dis, int version)
			throws IOException {
		AvgResult ret = new AvgResult();
		ret.sum = dis.readDouble();
		ret.count = dis.readInt();
		return ret;
	}

}
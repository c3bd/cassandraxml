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
 * This class is used to store the results of the count function in xpath. 
 * It contains the serialization and deserialization of the value. The merge function
 * will be invoked on the coordinator, which just add up all the count results to reveal
 * the final result.
 */
public class CountResult extends FunctionReturn {
	public static CountResultSerializer serializer = new CountResultSerializer();
	public int count = 0;
	
	public CountResult() {
		funcType = XPathResultType.Count;
	}
	
	@Override
	public void merge(int index_,FunctionReturn other) {
		count += ((CountResult)other).count;
	}

	@Override
	public List<String> result() {
		List<String> ret = new LinkedList<String>();
		ret.add(Integer.toString(count));
		return ret;
	}

	@Override
	public ByteBuffer getDigest() {
		MessageDigest digester = FBUtilities.threadLocalMD5Digest();
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(aos);

		try {
			dos.writeInt(count);
			digester.digest(aos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ByteBuffer.wrap(digester.digest());
	}
	@Override
	public String toString() {
		return Integer.toString(count);
	}
}
class CountResultSerializer extends FunctionReturnSerializer  {

	@Override
	public void serialize(FunctionReturn t, DataOutputStream dos, int version)
			throws IOException {
		//dos.writeInt(t.funcType.ordinal());
		dos.writeInt(((CountResult)t).count);
	}

	@Override
	public CountResult deserialize(DataInputStream dis, int version)
			throws IOException {
		CountResult ret = new CountResult();
		ret.count = dis.readInt();
		return ret;
	}	
}
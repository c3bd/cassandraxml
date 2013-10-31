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
 * This class is used to store the results of the text function in xpath. It
 * contains the serialization and deserialization of the value.
 */
public class TextResult extends FunctionReturn {

	public List<String> texts = new LinkedList<String>();

	public TextResult() {
		funcType = XPathResultType.Text;
	}

	@Override
	public void merge(int index_, FunctionReturn other) {
		texts.addAll(((TextResult) other).texts);
	}

	@Override
	public List<String> result() {
		return texts;
	}

	@Override
	public int size() {
		return (texts == null ? 0 : texts.size());
	}

	@Override
	public ByteBuffer getDigest() {
		MessageDigest digester = FBUtilities.threadLocalMD5Digest();
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(aos);

		try {
			dos.writeInt(texts.hashCode());
			digester.digest(aos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ByteBuffer.wrap(digester.digest());
	}

}

class TextResultSerializer extends FunctionReturnSerializer {

	@Override
	public void serialize(FunctionReturn t, DataOutputStream dos, int version)
			throws IOException {
		TextResult temp = (TextResult) t;
		//dos.writeInt(t.funcType.ordinal());
		dos.writeInt(temp.texts.size());
		for (String text : temp.texts) {
			dos.writeUTF(text);
		}
	}

	@Override
	public TextResult deserialize(DataInputStream dis, int version)
			throws IOException {
		TextResult ret = new TextResult();
		ret.texts = new LinkedList<String>();
		int count = dis.readInt();
		for (int i = 0; i < count; i++) {
			ret.texts.add(dis.readUTF());
		}

		return ret;
	}

}
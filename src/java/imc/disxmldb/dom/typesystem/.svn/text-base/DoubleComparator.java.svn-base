package imc.disxmldb.dom.typesystem;

import java.nio.ByteBuffer;
import java.util.Comparator;

import org.apache.cassandra.db.marshal.DoubleType;


public class DoubleComparator implements Comparator<byte[]> {

	@Override
	public int compare(byte[] o1, byte[] o2) {
		ByteBuffer a = ByteBuffer.wrap(o1);
		ByteBuffer b = ByteBuffer.wrap(o2);
		/*int aID = a.getInt();
		int bID = b.getInt();
		int ret = DoubleType.instance.compare(a, b);
		if (ret == 0) {
			int comp = aID - bID;
			if (comp == 0)
				return 0;
			else 
				return (comp < 0)? -1: 1;
		} else 
			return ret;*/
		return DoubleType.instance.compare(a, b);
	}
}
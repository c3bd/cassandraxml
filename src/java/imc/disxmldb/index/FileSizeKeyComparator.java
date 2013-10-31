/**
 * @author chengchengyu
 */
package imc.disxmldb.index;

import java.util.Comparator;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.je.DatabaseEntry;

/**
 * This comparator is used by the B+tree to compare the key. This comparator is
 * defined for the type FileSize type.
 * 
 */
public class FileSizeKeyComparator implements Comparator<byte[]> {

	public FileSizeKeyComparator() {
	}

	public int compare(byte[] o1, byte[] o2) {

		String e1 = new String(o1);

		String e2 = new String(o2);
		Double a1 = Double.valueOf(e1);
		Double a2 = Double.valueOf(e2);

		return a1.compareTo(a2);

	}

}

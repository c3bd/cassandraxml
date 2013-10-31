package imc.disxmldb.security;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.RowMutation;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.io.util.FastByteArrayOutputStream;

public class UserSerializer {
	private final static ByteBuffer GROUP = UTF8Type.instance.fromString("grp");
	private final static ByteBuffer NAME = UTF8Type.instance.fromString("name");
	private final static ByteBuffer PASS = UTF8Type.instance.fromString("psd");
	// private final static String USER_ID = "uid";
	
	public static void write(User user, RowMutation rm, String cf) {
		assert rm != null && cf != null;
		long timestamp = System.currentTimeMillis();
		// rm.add(new QueryPath(cf, NAME, null),
		// UTF8Type.instance.fromString(user), timestamp);
		rm.add(new QueryPath(cf, null, PASS),
				UTF8Type.instance.fromString(user.getPassword()), timestamp);

		FastByteArrayOutputStream aos = new FastByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(aos);
		try {
			dos.writeInt(user.getGroups().length);
			for (String grp : user.getGroups()) {
				dos.writeUTF(grp);
			}
		} catch (IOException e) {
		}
		ByteBuffer grpBuffer = ByteBuffer.wrap(aos.toByteArray());
		rm.add(new QueryPath(cf, null, GROUP), grpBuffer, timestamp);
	}

	public static void read(User user, Row row) {
		assert row != null;
		if (row.cf == null)
			return;

		IColumn col = row.cf.getColumn(PASS);
		if (col != null) {
			user.setPassword(UTF8Type.instance.getString(col.value()));
		}

		col = row.cf.getColumn(GROUP);
		if (col != null) {
			ByteArrayInputStream ain = new ByteArrayInputStream(col.value()
					.duplicate().array());
			DataInputStream din = new DataInputStream(ain);
			int count;
			try {
				count = din.readInt();
				String[] groups = new String[count];
				for (int i = 0; i < count; i++) {
					groups[i] = din.readUTF();
				}
				user.setGroups(groups);
			} catch (IOException e) {

			}

		}
	}
}

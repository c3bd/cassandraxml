package imc.disxmldb.security;

import java.nio.ByteBuffer;

import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.RowMutation;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.db.marshal.UTF8Type;

public class UnixStylePermissionSerializer {
	public static final ByteBuffer SPNAME = UTF8Type.instance.fromString("permsp");
	public static final ByteBuffer USER = UTF8Type.instance.fromString("user");
	public static final ByteBuffer GROUP = UTF8Type.instance.fromString("grp");
	public static final ByteBuffer PERM = UTF8Type.instance.fromString("perm");
	
	public static RowMutation write(Permission perm, RowMutation mutation, String cf) {
		mutation.add(new QueryPath(cf, SPNAME, USER), UTF8Type.instance.fromString(perm.getOwner()), System.currentTimeMillis());
		mutation.add(new QueryPath(cf, SPNAME, GROUP), UTF8Type.instance.fromString(perm.getOwnerGroup()), System.currentTimeMillis());
		ByteBuffer permByte = ByteBuffer.allocate(1);
		permByte.put((byte) perm.getPermissions());
		permByte.rewind();
		mutation.add(new QueryPath(cf, SPNAME, PERM), permByte, System.currentTimeMillis());
		return mutation;
	}
	
	public static Permission read(Permission perm, Row row) {
		assert row != null;
		if (perm == null)
			perm = new UnixStylePermission();
		IColumn col = row.cf.getColumn(SPNAME);
		if (col != null) {
			if (col.getSubColumn(USER) != null) {
				perm.setOwner(UTF8Type.instance.getString(col.getSubColumn(USER).value()));
			}
			
			if (col.getSubColumn(GROUP) != null) {
				perm.setGroup(UTF8Type.instance.getString(col.getSubColumn(GROUP).value()));
			}
			
			if (col.getSubColumn(PERM) != null) {
				perm.setPermissions(col.getSubColumn(PERM).value().duplicate().get());
			}
		} else
			return null;
		return perm;
	}

}

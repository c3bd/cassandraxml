package imc.disxmldb.dom;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.dom.typesystem.ValueType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.RowMutation;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.utils.ByteBufferUtil;

public class XMLNodeForMutation {
	public String tagName = null; // 在删除时被用到，用于更新attr或者ele索引
	public int id; // node id
	public double[] range = new double[2];
	public ValueType valueType = ValueType.UNKNOW;
	public ByteBuffer value = ByteBufferUtil.EMPTY_BYTE_BUFFER;
	public int level = 0;
	protected final byte nodeType;

	public XMLNodeForMutation(byte NodeType_) {
		nodeType = NodeType_;
	}

	/**
	 * 
	 * @param keyspace_
	 * @param cfName_
	 * @param node_
	 * @param rm_
	 *            不能为空
	 * @return
	 */
	public void udpate(ColumnFamily cf_) {
		long timestamp = System.currentTimeMillis();
		String cfName = cf_.metadata().cfName;
		ByteBuffer spName = CollectionStore.SPNAMETYPE.fromString(Integer
				.toString(id));
		cf_.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.NODETYPE), ByteBuffer
				.wrap(new byte[] { nodeType }), timestamp);
		cf_.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.DEPTH), ByteBufferUtil.bytes(level),
				timestamp);
		cf_.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.TAGNAME), UTF8Type.instance
				.fromString(tagName), timestamp);
		cf_.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.VALUE), value, timestamp);
		cf_.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.VALUETYPE), ByteBufferUtil
				.bytes(valueType.ordinal()), timestamp);
		cf_.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.LOWERRANGE), ByteBufferUtil
				.bytes(range[0]), timestamp);
		cf_.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.UPPERRANGE), ByteBufferUtil
				.bytes(range[1]), timestamp);
	}

	public RowMutation delete(String keyspace, String cfName, RowMutation rm_) {
		long timestamp = System.currentTimeMillis();
		ByteBuffer spName = CollectionStore.SPNAMETYPE.fromString(Integer
				.toString(id));
		/*rm_.delete(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.NODETYPE), timestamp);
		rm_.delete(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.DEPTH), timestamp);
		rm_.delete(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.TAGNAME), timestamp);
		rm_.delete(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.VALUE), timestamp);
		rm_.delete(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.VALUETYPE), timestamp);
		rm_.delete(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.LOWERRANGE), timestamp);
		rm_.delete(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.UPPERRANGE), timestamp);*/
		rm_.delete(new QueryPath(cfName, spName.duplicate()), timestamp);
		return rm_;
	}

	public static void serialize(XMLNodeForMutation node, DataOutputStream dos, int version)
			throws IOException {
		dos.writeByte(node.nodeType);
		dos.writeUTF(node.tagName);
		dos.writeInt(node.id);
		dos.writeInt(node.valueType.ordinal());
		byte[] valueBytes = ByteBufferUtil.getArray(node.value);
		dos.writeInt(valueBytes.length);
		dos.write(ByteBufferUtil.getArray(node.value));
		dos.writeInt(node.level);
		dos.writeDouble(node.range[0]);
		dos.writeDouble(node.range[1]);
	}

	public static XMLNodeForMutation deserialize(DataInputStream dis, int version)
			throws IOException {
		byte nodeType_ = dis.readByte();
		XMLNodeForMutation ret = new XMLNodeForMutation(nodeType_);
		ret.tagName = dis.readUTF();
		ret.id = dis.readInt();
		ret.valueType = ValueType.VALUETYPES[dis.readInt()];
		byte[] values = new byte[dis.readInt()];
		dis.read(values);
		ret.value = ByteBuffer.wrap(values);

		ret.level = dis.readInt();
		ret.range[0] = dis.readDouble();
		ret.range[1] = dis.readDouble();
		return ret;
	}
	
	@Override
	public String toString() {
		return String.format("ragName:%s;id:%d;range[0]:%f,range[1]:%f,%d", tagName, id, range[0],range[1], level);
	}
}

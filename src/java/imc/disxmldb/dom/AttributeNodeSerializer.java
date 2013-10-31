package imc.disxmldb.dom;

import java.nio.ByteBuffer;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.dom.typesystem.ValueType;

import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.SuperColumn;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.utils.ByteBufferUtil;

/**
 * This class defines the serialization and deserialization
 * behave of attribute node
 *
 */
public class AttributeNodeSerializer extends XMLNodeSerializer {
	@Override
	public ColumnFamily serialize(String keyspace, String cfName, XMLNode node) {
		long startTime = System.currentTimeMillis();
		ColumnFamily cf = ColumnFamily.create(keyspace, cfName);
		ByteBuffer spName = CollectionStore.SPNAMETYPE.fromString(Integer
				.toString(node.getId()));
		long timestamp = System.currentTimeMillis();

		if (node.getParent() != null)
			cf.addColumn(new QueryPath(cfName, spName.duplicate(),
					CollectionStore.PARENTID), ByteBufferUtil.bytes(node
					.getParent().getId()), timestamp);
		
		cf.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.NODETYPE), ByteBuffer
				.wrap(new byte[] { node.getNodeType() }), timestamp);

		cf.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.DEPTH), ByteBufferUtil.bytes(node.getLevel()), timestamp);
		if (node.getTagName() != null)
			cf.addColumn(new QueryPath(cfName, spName.duplicate(),
					CollectionStore.TAGNAME), UTF8Type.instance
					.fromString(node.getTagName()), timestamp);

		cf.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.VALUE), node.getValue(), timestamp);

		cf.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.VALUETYPE), ByteBufferUtil.bytes(node.getValueType()
				.ordinal()), timestamp);

		cf.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.LOWERRANGE), ByteBufferUtil.bytes(node.getRange()[0]),
				timestamp);
		cf.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.UPPERRANGE), ByteBufferUtil.bytes(node.getRange()[1]),
				timestamp);
		serializeLatencyTracker.addMicro(System.currentTimeMillis() - startTime);
		return cf;
	}
	@Override
	public XMLNode deserialize(int XMLID, SuperColumn spCol) {
		if (spCol == null)
			return null;
		
		assert spCol.getSubColumn(CollectionStore.NODETYPE).value().duplicate().get() == XMLNodeImpl.ATTRIBUTENODE;
		
		XMLNodeImpl node = new AttributeNodeImpl(null, 0, 0, null);
		long startTime = System.currentTimeMillis();
		IColumn subCol = null;
		node.id = Integer.parseInt(CollectionStore.SPNAMETYPE.getString(spCol.name()));
		
		if ((subCol = spCol.getSubColumn(CollectionStore.PARENTID)) != null) {
			node.parent = new ElementNodeImpl(null, subCol.value().duplicate().getInt(),
					node.level - 1, null);
		}
		
		if ((subCol = spCol.getSubColumn(CollectionStore.TAGNAME)) != null)
			node.tagName = UTF8Type.instance.getString(subCol.value().duplicate());

		if ((subCol = spCol.getSubColumn(CollectionStore.VALUETYPE)) != null) {
			int valueTypeIndex = ByteBufferUtil.toInt(subCol.value().duplicate());
			node.valueType = ValueType.getValueType(valueTypeIndex);
		}

		if ((subCol = spCol.getSubColumn(CollectionStore.DEPTH)) != null)
			node.level = ByteBufferUtil.toInt(subCol.value().duplicate());

		if ((subCol = spCol.getSubColumn(CollectionStore.VALUE)) != null)
			node.value = subCol.value().duplicate();

		if ((subCol = spCol.getSubColumn(CollectionStore.LOWERRANGE)) != null)
			node.range[0] = ByteBufferUtil.toDouble(subCol.value().duplicate());

		if ((subCol = spCol.getSubColumn(CollectionStore.UPPERRANGE)) != null)
			node.range[1] = ByteBufferUtil.toDouble(subCol.value().duplicate());
		
		deserializeLatencyTracker.addMicro(System.currentTimeMillis() - startTime);
		return node;
	}
}

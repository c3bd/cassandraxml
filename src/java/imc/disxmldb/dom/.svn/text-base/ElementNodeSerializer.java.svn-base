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
 * this class defines the behave of serialization and deserialization of element
 * node
 * 
 */
public class ElementNodeSerializer extends XMLNodeSerializer {

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
				CollectionStore.NODETYPE), ByteBuffer.wrap(new byte[] { node
				.getNodeType() }), timestamp);

		cf.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.DEPTH), ByteBufferUtil.bytes(node.getLevel()),
				timestamp);
		if (node.getTagName() != null)
			cf.addColumn(new QueryPath(cfName, spName.duplicate(),
					CollectionStore.TAGNAME), UTF8Type.instance.fromString(node
					.getTagName()), timestamp);

		cf.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.VALUE), node.getValue(), timestamp);

		cf.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.VALUETYPE), ByteBufferUtil.bytes(node
				.getValueType().ordinal()), timestamp);

		cf.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.LOWERRANGE), ByteBufferUtil.bytes(node
				.getRange()[0]), timestamp);
		cf.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.UPPERRANGE), ByteBufferUtil.bytes(node
				.getRange()[1]), timestamp);
		serializeLatencyTracker
				.addMicro(System.currentTimeMillis() - startTime);

		ElementNode eleNode = (ElementNode) node;

		// serialize the attribute nodes
		cf.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.ATTRNUM), ByteBufferUtil.bytes(eleNode
				.getAttrNum()), timestamp);
		ByteBuffer attrsBuffer = ByteBuffer.allocate(eleNode.getAttrNum()
				* Integer.SIZE / 8);
		for (XMLNode attr : eleNode.getAttributeNodes()) {
			attrsBuffer.putInt(attr.getId());
		}
		attrsBuffer.rewind();
		cf.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.ATTRLIST), attrsBuffer, timestamp);

		// serialize the child nodes
		cf.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.CHILDNUM), ByteBufferUtil.bytes(eleNode
				.getChilds().size()), timestamp);
		ByteBuffer childsBuffer = ByteBuffer.allocate(eleNode.getChildNum()
				* Integer.SIZE / 8);
		for (XMLNode child : eleNode.getChilds()) {
			childsBuffer.putInt(child.getId());
		}
		childsBuffer.rewind();
		cf.addColumn(new QueryPath(cfName, spName.duplicate(),
				CollectionStore.CHILDLIST), childsBuffer, timestamp);
		return cf;
	}

	@Override
	public XMLNode deserialize(int XMLID, SuperColumn spCol) {
		if (spCol == null)
			return null;
		assert spCol.getSubColumn(CollectionStore.NODETYPE).value().duplicate()
				.get() == XMLNodeImpl.ELEMENTNODE;

		ElementNodeImpl node = new ElementNodeImpl(null, 0, 0, null);
		long startTime = System.currentTimeMillis();
		IColumn subCol = null;
		node.id = Integer.parseInt(CollectionStore.SPNAMETYPE.getString(spCol
				.name()));

		if ((subCol = spCol.getSubColumn(CollectionStore.PARENTID)) != null) {
			/*
			 * node.parent = new ElementNodeImpl(null,
			 * subCol.value().duplicate().getInt(), node.level - 1, null);
			 */
			node.setParent(new NodeProxy(XMLID, subCol.value().duplicate()
					.getInt()));
		}

		if ((subCol = spCol.getSubColumn(CollectionStore.TAGNAME)) != null)
			node.tagName = UTF8Type.instance.getString(subCol.value()
					.duplicate());

		if ((subCol = spCol.getSubColumn(CollectionStore.VALUETYPE)) != null) {
			int valueTypeIndex = ByteBufferUtil.toInt(subCol.value()
					.duplicate());
			node.valueType = ValueType.getValueType(valueTypeIndex);
		}

		if ((subCol = spCol.getSubColumn(CollectionStore.DEPTH)) != null)
			node.level = ByteBufferUtil.toInt(subCol.value());

		if ((subCol = spCol.getSubColumn(CollectionStore.VALUE)) != null)
			node.value = subCol.value().duplicate();

		if ((subCol = spCol.getSubColumn(CollectionStore.LOWERRANGE)) != null)
			node.range[0] = ByteBufferUtil.toDouble(subCol.value());

		if ((subCol = spCol.getSubColumn(CollectionStore.UPPERRANGE)) != null)
			node.range[1] = ByteBufferUtil.toDouble(subCol.value());

		// deserialize the attribute
		if (spCol.getSubColumn(CollectionStore.ATTRNUM) != null) {
			int attrNum = ByteBufferUtil.toInt(spCol.getSubColumn(
					CollectionStore.ATTRNUM).value());
			ByteBuffer attrBuffer = spCol
					.getSubColumn(CollectionStore.ATTRLIST).value().duplicate();
			for (int i = 0; i < attrNum; i++) {
				// node.addAttribute(new AttributeNodeImpl(null,
				// attrBuffer.getInt(), 0, null));
				node.addAttribute(new NodeProxy(XMLID, attrBuffer.getInt()));
			}
		}

		// deserialize the child xmlnode
		if (spCol.getSubColumn(CollectionStore.CHILDNUM) != null) {
			int childNum = ByteBufferUtil
					.toInt(spCol.getSubColumn(CollectionStore.CHILDNUM).value()
							.duplicate());
			ByteBuffer childBuffer = spCol
					.getSubColumn(CollectionStore.CHILDLIST).value()
					.duplicate();
			for (int i = 0; i < childNum; i++) {
				node.addChild(new NodeProxy(XMLID, childBuffer.getInt()));
			}
		}

		deserializeLatencyTracker.addMicro(System.currentTimeMillis()
				- startTime);
		return node;
	}
}
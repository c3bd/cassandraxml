package imc.disxmldb.dom;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.util.IXMLNodeSerializer;
import imc.disxmldb.util.XMLNodeSerializerMBean;

import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.SuperColumn;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.LatencyTracker;

/**
 * The facet class integrates all the serialization class of XML nodes
 * 
 */
public class XMLNodeSerializer implements IXMLNodeSerializer,
		XMLNodeSerializerMBean {

	public static final LatencyTracker serializeLatencyTracker = new LatencyTracker();
	public static final LatencyTracker deserializeLatencyTracker = new LatencyTracker();

	private static HashMap<Byte, XMLNodeSerializer> serializers = new HashMap<Byte, XMLNodeSerializer>();
	static {
		serializers.put(XMLNodeImpl.ATTRIBUTENODE,
				new AttributeNodeSerializer());
		serializers.put(XMLNodeImpl.ELEMENTNODE, new ElementNodeSerializer());
	}

	/*
	 * static { MBeanServer server = ManagementFactory.getPlatformMBeanServer();
	 * try { server.registerMBean(new XMLNodeSerializer(), new
	 * ObjectName("imc.disxmldb.dom:type=XMLNodeSerializer")); } catch
	 * (Exception e) { throw new RuntimeException(e); } }
	 */
	public XMLNodeSerializer() {

	}

	@Override
	public ColumnFamily serialize(String keyspace, String cfName, XMLNode node) {
		return serializers.get(node.getNodeType()).serialize(keyspace, cfName,
				node);
	}

	@Override
	public XMLNode deserialize(int XMLID, SuperColumn spCol) {
		if (spCol == null)
			return null;
		if (spCol.getSubColumn(CollectionStore.NODETYPE).value().duplicate()
				.get() == XMLNodeImpl.ATTRIBUTENODE) {
			return serializers.get(XMLNodeImpl.ATTRIBUTENODE).deserialize(
					XMLID, spCol);
		} else {
			return serializers.get(XMLNodeImpl.ELEMENTNODE).deserialize(XMLID,
					spCol);
		}
	}

	@Override
	public long getSerializeLatency() {
		return serializeLatencyTracker.getTotalLatencyMicros();
	}

	@Override
	public long getDeserializeLatency() {
		return deserializeLatencyTracker.getTotalLatencyMicros();
	}
}
package imc.disxmldb.util;

import imc.disxmldb.dom.XMLNode;

import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.SuperColumn;

/**
 * the interface for an XML node to serialize to and deserialize from an column family
 * @author xiafan
 *
 */
public interface IXMLNodeSerializer {
	public ColumnFamily serialize(String keyspace, String cfName, XMLNode node);
	public XMLNode deserialize(int XMLID, SuperColumn spCol);
}

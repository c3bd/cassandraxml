/**
 *@author:xiafan xiafan68@gmail.com
 *@version: 2011-10-26 0.1
 */
package imc.disxmldb.util;

import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.SuperColumn;

/**
 * define the interface to serialize and deserialize data to or from Column
 * family
 */
public interface ColumnFamilySerializable {
	public ColumnFamily serialize(String keyspace, String cfName);

	public void deserialize(SuperColumn spCol);
}

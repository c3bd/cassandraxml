/**
 *@author:xiafan xiafan68@gmail.com
 *@version: 2011-10-22 0.1
 */
package imc.disxmldb;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import imc.disxmldb.dom.AttributeNode;
import imc.disxmldb.dom.AttributeNodeImpl;
import imc.disxmldb.dom.XMLNode;
import imc.disxmldb.dom.typesystem.ValueType;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.invertindex.CFInvertIndex;
import imc.disxmldb.util.CassandraSchemaProxy;
import imc.disxmldb.util.LocalCFStoreProxy;

import org.apache.cassandra.config.Schema;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Table;
import org.junit.Test;

public class CFInvertIndexTest extends CFInvertIndexCleaner {
	private String[] dicts = { "CFInvertIndexTest", "CFInvertIndexCleaner",
			"performanceTest", "LocalCFStoreProxy", "CFInvertIndex", "String",
			"texts", "node", "AttributeNode", "setValueType", "setValue",
			"setRange", "testGet", "CassandraSchemaProxy" };

	@Test
	public void performanceTest() {
		ColumnFamilyStore cfStore = CassandraSchemaProxy
				.createInvertIndexCFMetaData(keyspace, "testindex");
		LocalCFStoreProxy proxy = new LocalCFStoreProxy(Table.open(keyspace),
				cfStore);
		CFInvertIndex inv = new CFInvertIndex(proxy);

		int count = 10000;
		Random rand = new Random();
		for (int i = 0; i < count; i++) {
			String dict = "";
			int idx = Math.abs(rand.nextInt());
			for (int b = 0; b < idx % dicts.length; b++) {
				int idx1 = Math.abs(rand.nextInt());
				dict += dicts[idx1 % dicts.length] + " ";
			}
			inv.put(dict, rand.nextInt(), rand.nextInt(),
					new double[] { rand.nextDouble(), rand.nextDouble() }, 0);
		}
		System.out.println("put " + count + " records latency: ");

	}

	@Test
	public void testGet() {
		ColumnFamilyStore cfStore = CassandraSchemaProxy
				.createInvertIndexCFMetaData(keyspace, "testindex");
		LocalCFStoreProxy proxy = new LocalCFStoreProxy(Table.open(keyspace),
				cfStore);
		CFInvertIndex inv = new CFInvertIndex(proxy);
		String[] texts = new String[] { "this is a test",
				"this is ForwardStep", "forwardAxis 测试到排索引NodeTest" };
		AttributeNode[] node = new AttributeNode[3];
		node[0] = new AttributeNodeImpl("root", 0, 0, null);
		((XMLNode) node[0]).setValueType(ValueType.STRING);
		((XMLNode) node[0]).setValue(texts[0]);
		((XMLNode) node[0]).setRange(new double[] { 0.0, 100.0 });

		node[1] = new AttributeNodeImpl("child1", 0, 1, null);
		((XMLNode) node[1]).setValueType(ValueType.STRING);
		((XMLNode) node[1]).setValue(texts[2]);
		((XMLNode) node[1]).setRange(new double[] { 10.0, 80.0 });

		node[2] = new AttributeNodeImpl("child1", 0, 1, null);
		((XMLNode) node[2]).setValueType(ValueType.STRING);
		((XMLNode) node[2]).setValue(texts[2]);
		((XMLNode) node[2]).setRange(new double[] { 111.0, 122.0 });

		for (int i = 0; i < 3; i++) {
			inv.put(((XMLNode) node[i]).getValueStr(), 0,
					((XMLNode) node[i]).getId(),
					((XMLNode) node[i]).getRange(),
					((XMLNode) node[i]).getLevel());
		}
		TreeMap<Integer, List<NodeUnit>> ret = inv.get("this is a test");
		try {
			inv.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ret = inv.get("索引");
		System.out.println(inv.get("this is a root node"));
	}

	@Test
	public void testRemove() {
		ColumnFamilyStore cfStore = CassandraSchemaProxy
				.createInvertIndexCFMetaData(keyspace, "testindex");
		LocalCFStoreProxy proxy = new LocalCFStoreProxy(Table.open(keyspace),
				cfStore);
		CFInvertIndex inv = new CFInvertIndex(proxy);
		String[] texts = new String[] { "this is a test",
				"this is ForwardStep ForwardAxis NodeTest  AbbreviatedForwardStep" };
		AttributeNode[] node = new AttributeNode[3];
		node[0] = new AttributeNodeImpl("root", 0, 0, null);
		((XMLNode) node[0]).setValueType(ValueType.STRING);
		((XMLNode) node[0]).setValue(texts[0]);
		((XMLNode) node[0]).setRange(new double[] { 0.0, 100.0 });

		node[1] = new AttributeNodeImpl("child1", 0, 1, null);
		((XMLNode) node[1]).setValueType(ValueType.STRING);
		((XMLNode) node[1]).setValue(texts[1]);
		((XMLNode) node[1]).setRange(new double[] { 10.0, 80.0 });

		node[2] = new AttributeNodeImpl("child1", 0, 1, null);
		((XMLNode) node[2]).setValueType(ValueType.STRING);
		((XMLNode) node[2]).setValue(texts[0]);
		((XMLNode) node[2]).setRange(new double[] { 111.0, 122.0 });

		for (int i = 0; i < 3; i++) {
			inv.put(((XMLNode) node[i]).getValueStr(), 0,
					((XMLNode) node[i]).getId(),
					((XMLNode) node[i]).getRange(),
					((XMLNode) node[i]).getLevel());
		}
		System.out.println(inv.get("this is a test"));
		System.out.println(inv.get("this is a root node"));
		inv.remove(((XMLNode) node[0]).getValueStr(), 0,
				((XMLNode) node[1]).getId(), ((XMLNode) node[1]).getRange());
		System.out.println(inv.get("this is a test"));
	}
}

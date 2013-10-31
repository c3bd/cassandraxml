package imc.disxmldb.util;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.XMLDBStore;
import imc.disxmldb.cassandra.verbhandler.XPathResult;

import org.apache.cassandra.thrift.CassandraDaemon;
import org.exist.xmldb.RemoteXMLResource;
import org.junit.Test;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XUpdateQueryService;

public class XUpdateTest {
	private static final String DATADIR = "/home/xiafan/xmldata";

	@Test
	public void testUpdateText() throws Exception {
		String[] args = { "-Djava.rmi.server.hostname=mc1",
				"-Dcom.sun.management.jmxremote.port=7791",
				"-Dcom.sun.management.jmxremote.ssl=false",
				"-Dcom.sun.management.jmxremote.authenticate=false",
				"-Xmx8192M", "-Xms8192M" };
		CassandraDaemon.main(args);
		String xupdate = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
				+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
				+ "<xu:update select=\"/basicfeature/node/id\">"
				+ 1
				+ "</xu:update></xu:modifications>";
		CollectionStore colStore = XMLDBStore.instance().getCollection(0);
		colStore.XUpdate(xupdate.getBytes(), false);
	}

	@Test
	public void testDelete() throws XMLDBException, IOException {
		String[] args = { "-Djava.rmi.server.hostname=mc1",
				"-Dcom.sun.management.jmxremote.port=7791",
				"-Dcom.sun.management.jmxremote.ssl=false",
				"-Dcom.sun.management.jmxremote.authenticate=false",
				"-Xmx8192M", "-Xms8192M" };
		CassandraDaemon.main(args);
		String xupdate = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
				+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
				+ "<xu:update select=\"/basicfeature/node/id\">"
				+ 1
				+ "</xu:update></xu:modifications>";
		CollectionStore colStore = XMLDBStore.instance().getCollection(0);
		XPathResult result;/*
							 * colStore.XPathLocal(
							 * "/basicfeature/node".getBytes(), false);
							 */
		int xmlDocID = 1;
		/*
		 * for (Entry<Integer, List<Integer>> entry : result.nodeIDs.xml2NodeIDs
		 * .entrySet()) { xmlDocID = entry.getKey(); for (Integer nodeID :
		 * entry.getValue()) { colStore.deleteNodeLocal(entry.getKey(), nodeID,
		 * System.currentTimeMillis()); } }
		 */
		String doc = colStore.retrieveLocal(xmlDocID, 0);
		System.out.println("doc after nodes deleted: " + doc);
	}

	@Test
	public void testAppendEmptyDoc() throws XMLDBException {
		String driver = "org.exist.xmldb.DatabaseImpl";
		Class cl;
		try {
			cl = Class.forName(driver);
			Database database = (Database) cl.newInstance();
			database.setProperty("creat-database", "true");
			DatabaseManager.registerDatabase(database);
			String textType = "css";
			Collection col = DatabaseManager.getCollection(TestUtilities.uri, "admin",
					"admin");
			XUpdateQueryService update = (XUpdateQueryService) col.getService(
					"XUpdateQueryService", "1.0");
			update.setProperty("pretty", "true");
			update.setProperty("encoding", "ISO-8859-1");

			// store an empty xml doc
			Resource x = col.createResource("testdoc.xml", "XMLResource");
			//((RemoteXMLResource) x).setSchemaName("bf.xsd");
			((RemoteXMLResource) x).setSplitted((byte) 1);
			x.setContent("<basicfeature></basicfeature>");
			col.storeResource(x);

			for (int i = 0; i < 3; i++) {
				String node = "<node id=\"" + i + "\">"
						+ "<bfid>0300010000000007200</bfid>"
						+ "<TimeUpload>2010��10��10��/TimeUpload>"
						+ "<Sharer>audr</Sharer>"
						+ "<FileName>������Ƶ.avi</FileName>"
						+ "<FileSize>1.5 MB</FileSize>" + "</node>";
				String update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
						+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
						+ "<xu:append select=\"/basicfeature\">"
						+ node
						+ "</xu:append></xu:modifications>";
				update.update(update_code);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

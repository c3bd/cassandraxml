package imc.disxmldb.xupdate;

import static org.junit.Assert.assertEquals;
import imc.disxmldb.util.TestUtilities;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.modules.XPathQueryService;
import org.xmldb.api.modules.XUpdateQueryService;

public class RollbackTest {
	public static String colName = "rollBackCol";
	public static String fullColName = "/db/" + colName;
	public static String docName = "rollBackXML.xml";
	public static String emptyDoc = "<basicfeature></basicfeature>";

	@Before
	public void prepare() throws InterruptedException {
		TestUtilities.prepareCollection(fullColName);
		Thread.sleep(1000);
		TestUtilities.prepareDoc(fullColName, docName, emptyDoc, (byte) 1);
	}

	@Test
	public void rollBackTest() {
		try {
			Collection col = TestUtilities.getCollection(fullColName);
			XUpdateQueryService update = (XUpdateQueryService) col.getService(
					"XUpdateQueryService", "1.0");
			update.setProperty("pretty", "true");
			update.setProperty("encoding", "ISO-8859-1");
			// append three nodes in a xupdate expression
			String node = "";
			for (int i = 0; i < 10; i++) {
				node += "<node id=\"" + i + "\">"
						+ "<bfid>0300010000000007200</bfid>"
						+ "<TimeUpload>2010年11月09日</TimeUpload>"
						+ "<Sharer>audr</Sharer>"
						+ "<FileName>世界经济能否在明年上半年复苏.avi</FileName>"
						+ "<FileSize>1.5 MB</FileSize>" + "</node>";
			}

			String update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:append select=\"/basicfeature\">"
					+ node
					+ "</xu:append></xu:modifications>";
			update.update(update_code);

			// query to check if they have been stored and indexed
			XPathQueryService query = (XPathQueryService) col.getService(
					"XPathQueryService", "1.0");
			String query_code = "/basicfeature/node[./FileSize='1.5MB']";
			ResourceSet retSet = query.query(query_code);
			assertEquals(retSet.getSize(), 10);

			// append a erroneous fragment
			node  = "<node id=\"100\">" + "<bfid>0300010000000007200</bfid>"
					+ "<TimeUpload>2010年11月09日</TimeUpload>"
					+ "<Sharer>&lt;audr</Sharer>"
					+ "<FileName>世界经济能否在明年上半年复苏.avi</FileName>"
					+ "<FileSize>1.5 MB</FileSize>" + "</node>";

			update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:append select=\"/basicfeature\">"
					+ node
					+ "</xu:append></xu:modifications>";
			try {
				update.update(update_code);
			} catch (Exception e) {
				// TODO: handle exception
			}
			// query to test if the rollback functions well
			query_code = "/basicfeature/node";
			retSet = query.query(query_code);
			assertEquals(retSet.getSize(), 11);
			ResourceIterator iter = retSet.getIterator();
			System.out.println(query_code+" count: " + retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@After
	public void after() {
		TestUtilities.removeCollection(fullColName);
	}
}

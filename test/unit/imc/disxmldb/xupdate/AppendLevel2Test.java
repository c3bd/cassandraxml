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

public class AppendLevel2Test {
	public static String colName = "testCol";
	public static String fullColName = "/db/" + colName;
	public static String docName = "testXML.xml";
	public static String emptyDoc = "<number><text></text></number>";

	@Before
	public void prepare() throws InterruptedException {
		TestUtilities.prepareCollection(fullColName);
		Thread.sleep(1000);
		TestUtilities.prepareDoc(fullColName, docName, emptyDoc, (byte) 1);
	}

	@Test
	public void appendMultiNodesATime() {
		try {
			Collection col = TestUtilities.getCollection(fullColName);
			XUpdateQueryService update = (XUpdateQueryService) col.getService(
					"XUpdateQueryService", "1.0");
			update.setProperty("pretty", "true");
			update.setProperty("encoding", "ISO-8859-1");
			// append three nodes in a xupdate expression
			String node = "";
			float fileSize = 1.1f;
			String[] date = new String[] { "2010年11月09日", "2010年11月09日",
					"2010年11月10日" };

			node = "<consumption><count>0</count></consumption>";
			String update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:append select=\"/number/text\">"
					+ node
					+ "</xu:append></xu:modifications>";
			System.out.println("execute " + update_code);
			System.out.println("updated nodes: " + update.update(update_code));
			Thread.sleep(500);

			node = "<curlture><count>0</count></curlture>";
			update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:append select=\"/number/text\">"
					+ node
					+ "</xu:append></xu:modifications>";
			System.out.println("execute " + update_code);
			System.out.println("updated nodes: " + update.update(update_code));
			Thread.sleep(500);

			// query to check if they have been stored and indexed
			XPathQueryService query = (XPathQueryService) col.getService(
					"XPathQueryService", "1.0");
			String query_code = "/number/text/consumption/count";
			ResourceSet retSet = query.query(query_code);
			assertEquals(retSet.getSize(), 1);
			ResourceIterator iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			query_code = "/number/text/consumption/count";
			retSet = query.query(query_code);
			assertEquals(retSet.getSize(), 1);
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@After
	public void clean() {
		TestUtilities.removeCollection(fullColName);
	}
}

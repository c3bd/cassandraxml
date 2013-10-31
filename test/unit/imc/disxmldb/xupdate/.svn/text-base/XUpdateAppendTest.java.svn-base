package imc.disxmldb.xupdate;

import static org.junit.Assert.*;
import imc.disxmldb.util.TestUtilities;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.modules.XPathQueryService;
import org.xmldb.api.modules.XUpdateQueryService;

public class XUpdateAppendTest {
	public static String colName = "testCol";
	public static String fullColName = "/db/" + colName;
	public static String docName = "testXML.xml";
	public static String emptyDoc = "<basicfeature></basicfeature>";

	@Before
	public void prepare() throws InterruptedException {
		TestUtilities.prepareCollection(fullColName);
		Thread.sleep(1000);
		TestUtilities.prepareDoc(fullColName, docName, emptyDoc, (byte) 1);
	}

	@Test
	public void appendEmptyDoc() {
		try {
			Collection col = TestUtilities.getCollection(fullColName);
			XUpdateQueryService update = (XUpdateQueryService) col.getService(
					"XUpdateQueryService", "1.0");
			update.setProperty("pretty", "true");
			update.setProperty("encoding", "ISO-8859-1");
			String node = "<node id=\"1\">"
					+ "<bfid>0300010000000007201</bfid>"
					+ "<TimeUpload>2010年11月09日</TimeUpload>"
					+ "<Sharer>audr</Sharer>"
					+ "<FileName>世界经济能否在明年上半年复苏.avi</FileName>"
					+ "<FileSize>1.5 MB</FileSize>" + "</node>";

			String update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:append select=\"/basicfeature\">"
					+ node
					+ "</xu:append></xu:modifications>";
			// System.out.println("execute " + update_code);
			System.out.println("updated nodes:" + update.update(update_code));
			Thread.sleep(50);

			XPathQueryService query = (XPathQueryService) col.getService(
					"XPathQueryService", "1.0");
			String query_code = "/basicfeature/node/@id";
			ResourceSet retSet = query.query(query_code);
			assertEquals(retSet.getSize(), 1);

			System.out.println("after append one node");
			ResourceIterator iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			node = "<node id=\"2\">" + "<bfid>0300010000000007202</bfid>"
					+ "<TimeUpload>2010年11月09日</TimeUpload>"
					+ "<Sharer>audr</Sharer>" + "<FileName>大国崛起.avi</FileName>"
					+ "<FileSize>600.5 MB</FileSize>" + "</node>";
			update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:append select=\"/basicfeature\">"
					+ node
					+ "</xu:append></xu:modifications>";
			// System.out.println("execute " + update_code);
			update.update(update_code);
			Thread.sleep(50);

			query_code = "/basicfeature/node/@id";
			retSet = query.query(query_code);
			assertEquals(retSet.getSize(), 2);
			System.out.println("after append another node");
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			node = "<road><count>0</count></road>";
			update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:append select=\"/basicfeature\">"
					+ node
					+ "</xu:append></xu:modifications>";
			update.update(update_code);
			Thread.sleep(50);

			query_code = "/basicfeature/node/FileName/count()";
			retSet = query.query(query_code);
			assertEquals(retSet.getSize(), 1);

			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			query_code = "/basicfeature/node/FileName/text()";
			retSet = query.query(query_code);
			assertEquals(retSet.getSize(), 2);

			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			for (int i = 0; i < 3; i++) {
				node += "<node id=\"" + i + "\">" + "<bfid>030001000000000720"
						+ i + "</bfid>" + "<TimeUpload>" + date[i]
						+ "</TimeUpload>" + "<Sharer>audr</Sharer>"
						+ "<FileName>世界经济能否在明年上半年复苏.avi</FileName>"
						+ "<FileSize>" + fileSize + " MB</FileSize>"
						+ "</node>";
				fileSize += 10.0f;
			}

			String update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:append select=\"/basicfeature\">"
					+ node
					+ "</xu:append></xu:modifications>";
			System.out.println("execute " + update_code);
			System.out.println("updated nodes: " + update.update(update_code));
			Thread.sleep(500);

			// query to check if they have been stored and indexed
			XPathQueryService query = (XPathQueryService) col.getService(
					"XPathQueryService", "1.0");
			String query_code = "/basicfeature/node/bfid";
			ResourceSet retSet = query.query(query_code);
			assertEquals(retSet.getSize(), 3);
			System.out.println("after append three nodes");
			ResourceIterator iter = retSet.getIterator();
			System.out.println("query:" + query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			for (int i = 0; i < 3; i++) {
				query_code = "/basicfeature/node[./@id=\"" + i + "\"]/bfid";
				retSet = query.query(query_code);
				assertEquals(retSet.getSize(), 1);
				System.out.println("query: " + query_code + " result size:"
						+ retSet.getSize());
				iter = retSet.getIterator();
				while (iter.hasMoreResources()) {
					System.out.println(iter.nextResource().getContent());
				}
			}

			for (int i = 3; i < 6; i++) {
				query_code = "/basicfeature/node[./@id=\"" + i + "\"]/bfid";
				retSet = query.query(query_code);
				assertEquals(retSet.getSize(), 0);
				System.out.println("query: " + query_code + " result size:"
						+ retSet.getSize());
				iter = retSet.getIterator();
				while (iter.hasMoreResources()) {
					System.out.println(iter.nextResource().getContent());
				}
			}

			query_code = "/basicfeature/node[./TimeUpload='2010年11月09日']/bfid";
			retSet = query.query(query_code);
			assertEquals(retSet.getSize(), 2);
			System.out.println("query: " + query_code + " result size:"
					+ retSet.getSize());
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			query_code = "/basicfeature/node[./contains(./FileName, '世界经济')]/bfid";
			retSet = query.query(query_code);
			assertEquals(retSet.getSize(), 3);
			System.out.println("query: " + query_code + " result size:"
					+ retSet.getSize());
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			query_code = "/basicfeature/node[./contains(./FileName, '大国崛起')]/bfid";
			retSet = query.query(query_code);
			assertEquals(retSet.getSize(), 0);
			System.out.println("query: " + query_code + " result size:"
					+ retSet.getSize());
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			// append another three nodes
			node = "";
			for (int i = 3; i < 6; i++) {
				node += "<node id=\"" + i + "\">" + "<bfid>030001000000000720"
						+ i + "</bfid>" + "<TimeUpload>" + date[i - 3]
						+ "</TimeUpload>" + "<Sharer>audr</Sharer>"
						+ "<FileName>大国崛起.avi</FileName>" + "<FileSize>"
						+ fileSize + " MB</FileSize>" + "</node>";
				fileSize += 10.0f;
			}

			update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:append select=\"/basicfeature\">"
					+ node
					+ "</xu:append></xu:modifications>";
			System.out.println("execute " + update_code);
			update.update(update_code);

			query_code = "/basicfeature/node/bfid";
			retSet = query.query(query_code);
			assertEquals(retSet.getSize(), 6);
			System.out.println("after append another three nodes");
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			for (int i = 0; i < 6; i++) {
				query_code = "/basicfeature/node[./@id=\"" + i + "\"]/bfid";
				retSet = query.query(query_code);
				assertEquals(retSet.getSize(), 1);
				System.out.println("query: " + query_code + " result size:"
						+ retSet.getSize());
				iter = retSet.getIterator();
				while (iter.hasMoreResources()) {
					System.out.println(iter.nextResource().getContent());
				}
			}

			query_code = "/basicfeature/node[./TimeUpload='2010年11月09日']/bfid";
			retSet = query.query(query_code);
			assertEquals(retSet.getSize(), 4);
			System.out.println("query: " + query_code + " result size:"
					+ retSet.getSize());
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			query_code = "/basicfeature/node[./contains(./FileName, '世界经济')]/bfid";
			retSet = query.query(query_code);
			assertEquals(retSet.getSize(), 3);
			System.out.println("query: " + query_code + " result size:"
					+ retSet.getSize());
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			query_code = "/basicfeature/node[./contains(./FileName, '大国崛起')]/bfid";
			retSet = query.query(query_code);
			assertEquals(retSet.getSize(), 3);
			System.out.println("query: " + query_code + " result size:"
					+ retSet.getSize());
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			/*
			 * node = "<road><count>0</count></road>"; update_code =
			 * "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" " +
			 * "xmlns:xu=\"http://www.xmldb.org/xupdate\">" +
			 * "<xu:append select=\"/basicfeature\">" + node +
			 * "</xu:append></xu:modifications>"; update.update(update_code);
			 * 
			 * query_code = "/basicfeature/node/FileName/count()"; retSet =
			 * query.query(query_code); assertEquals(retSet.getSize(), 1);
			 * 
			 * iter = retSet.getIterator(); while (iter.hasMoreResources()) {
			 * System.out.println(iter.nextResource().getContent()); }
			 * 
			 * query_code = "/basicfeature/node/FileName/text()"; retSet =
			 * query.query(query_code); assertEquals(retSet.getSize(), 2);
			 * 
			 * iter = retSet.getIterator(); while (iter.hasMoreResources()) {
			 * System.out.println(iter.nextResource().getContent()); }
			 */
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void appendThirdLevelTest() {
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
			for (int i = 0; i < 1; i++) {
				node += "<image><road><count>0</count></road></image>";
				fileSize += 10.0f;
			}

			String update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:append select=\"/basicfeature\">"
					+ node
					+ "</xu:append></xu:modifications>";
			System.out.println("execute " + update_code);
			System.out.println("updated nodes: " + update.update(update_code));

			XPathQueryService query = (XPathQueryService) col.getService(
					"XPathQueryService", "1.0");
			String query_code = "/basicfeature/image";
			ResourceSet retSet = query.query(query_code);
			System.out.println("after append three nodes");
			ResourceIterator iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			node = "<sign><count>0</count></sign>";
			update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:append select=\"/basicfeature/image/\">"
					+ node
					+ "</xu:append></xu:modifications>";
			System.out.println("execute " + update_code);
			System.out.println("updated nodes: " + update.update(update_code));
			query_code = "/basicfeature/image";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

		} catch (Exception e) {

		}
	}

	@After
	public void clean() {
		TestUtilities.removeCollection(fullColName);
	}
}

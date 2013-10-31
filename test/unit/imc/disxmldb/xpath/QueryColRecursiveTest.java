package imc.disxmldb.xpath;

import static org.junit.Assert.assertEquals;
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

public class QueryColRecursiveTest {
	public static String colName = "childCollection";
	public static String rootColName = "/db/pcol/";
	public static String fullColName = rootColName + colName;
	public static String docName = "testXML.xml";
	public static String emptyDoc = "<basicfeature></basicfeature>";

	@Before
	public void prepare() {
		TestUtilities.prepareCollection(rootColName);
		TestUtilities.prepareCollection(fullColName);
		TestUtilities.prepareDoc(rootColName, docName, emptyDoc, (byte) 1);
		TestUtilities.prepareDoc(fullColName, docName, emptyDoc, (byte) 1);
	}

	@After
	public void clean() {
		TestUtilities.removeCollection(fullColName);
		TestUtilities.removeCollection(rootColName);
	}

	@Test
	public void testXUpdate() {
		String driver = "org.exist.xmldb.DatabaseImpl";
		Class cl;
		try {
			cl = Class.forName(driver);
			Database database = (Database) cl.newInstance();
			database.setProperty("creat-database", "true");
			DatabaseManager.registerDatabase(database);
			String textType = "css";
			Collection pCol = TestUtilities.getCollection(rootColName);
			Collection col = TestUtilities.getCollection(fullColName);

			XUpdateQueryService pUpdate = (XUpdateQueryService) pCol
					.getService("XUpdateQueryService", "1.0");
			XUpdateQueryService update = (XUpdateQueryService) col.getService(
					"XUpdateQueryService", "1.0");

			update.setProperty("pretty", "true");
			update.setProperty("encoding", "ISO-8859-1");

			String update_code = null;
			// append xml node
			for (int i = 0; i < 3; i++) {
				String node = "<node id=\"" + i + "\">"
						+ "<bfid>0300010000000007200</bfid>"
						+ "<TimeUpload>2010年10月10日</TimeUpload>"
						+ "<Sharer>audr</Sharer>"
						+ "<FileName>测试文件.avi</FileName>"
						+ "<FileSize>1.5 MB</FileSize>" + "</node>";
				update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
						+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
						+ "<xu:append select=\"/basicfeature\">"
						+ node
						+ "</xu:append></xu:modifications>";
				System.out.println(update_code);
				update.update(update_code);
				pUpdate.update(update_code);
				Thread.sleep(50);
			}
			Thread.sleep(1000);
			// test child function
			XPathQueryService query = (XPathQueryService) col.getService(
					"XPathQueryService", "1.0");
			String query_code = "/basicfeature/node/FileName/text()";
			ResourceSet retSet = query.query(query_code);
			assertEquals(6, retSet.getSize());
			
			Thread.sleep(1000);
			query_code = "/basicfeature/node[./contains(./FileName, '测试文件.avi')]";
			retSet = query.query(query_code);
			assertEquals(6, retSet.getSize());
			System.out.println(query_code);
			ResourceIterator iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			query_code = "/basicfeature/node[./FileName = '测试文件.avi']";
			retSet = query.query(query_code);
			System.out.println(query_code);
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			assertEquals(6, retSet.getSize());

			
			query_code = "/basicfeature/node[./FileSize = '1.5 MB']";
			retSet = query.query(query_code);
			System.out.println(query_code);
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			assertEquals(6, retSet.getSize());
			
			// test query recursively parent collection
			System.out.println("query on parent recursively");
			XPathQueryService pQuery = (XPathQueryService) pCol.getService(
					"XPathQueryService", "1.0");
			query_code = "/basicfeature/node/FileName/text()";
			retSet = pQuery.query(query_code);
			assertEquals(9, retSet.getSize());
			
			query_code = "/basicfeature/node[./contains(./FileName, '测试文件.avi')]";
			retSet = pQuery.query(query_code);
			//the result should be doubled
			assertEquals(9, retSet.getSize());
			System.out.println(query_code);
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			// test equal
			query_code = "/basicfeature/node[./FileName = '测试文件.avi']";
			retSet = pQuery.query(query_code);
			System.out.println(query_code);
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			assertEquals(9, retSet.getSize());

			query_code = "/basicfeature/node[./FileSize = '1.5 MB']";
			retSet = pQuery.query(query_code);
			System.out.println(query_code);
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			assertEquals(9, retSet.getSize());
			
			//after an update on the child collection
			update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:update select=\"/basicfeature/node/FileName\">"
					+ "被修改文件.avi" + "</xu:update></xu:modifications>";
			update.update(update_code);

			//the query on the parent should return 3 items
			query_code = "/basicfeature/node[./FileName = '测试文件.avi']";
			retSet = pQuery.query(query_code);
			System.out.println(query_code);
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			assertEquals(3, retSet.getSize());

			query_code = "/basicfeature/node[./contains(./FileName, '测试文件.avi')]";
			retSet = pQuery.query(query_code);
			//the result should be doubled
			assertEquals(3, retSet.getSize());
			System.out.println(query_code);
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

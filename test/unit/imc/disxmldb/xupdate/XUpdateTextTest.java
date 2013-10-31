package imc.disxmldb.xupdate;

import imc.disxmldb.util.TestUtilities;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.modules.XPathQueryService;
import org.xmldb.api.modules.XUpdateQueryService;

public class XUpdateTextTest {
	public static String colName = "updateTextTest";
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
	public void updateTextTest() {
		try {
			Collection col = TestUtilities.getCollection(fullColName);
			XUpdateQueryService update = (XUpdateQueryService) col.getService(
					"XUpdateQueryService", "1.0");
			update.setProperty("pretty", "true");
			update.setProperty("encoding", "ISO-8859-1"); 

			// append nodes
			String node = "";
			int count = 3;
			for (int i = 0; i < count; i++) {
				node += "<node id=\"" + i + "\">"
						+ "<bfid>0300010000000007200</bfid>"
						+ "<TimeUpload>2010年11月09日</TimeUpload>"
						+ "<Sharer>audr</Sharer>"
						+ "<User_Comment>test</User_Comment>"
						+ "<Keywords>a b</Keywords>" + "<File>"
						+ "<FileName>世界经济能否在明年上半年复苏.avi</FileName>"
						+ "<FileSize>1.5 MB</FileSize>" + "</File>" + "</node>";
			}
			String update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:append select=\"/basicfeature\">"
					+ node
					+ "</xu:append></xu:modifications>";
			System.out.println("execute " + update_code);
			update.update(update_code);
			Thread.sleep(500);
			
			// query to check if they have been stored and indexed
			XPathQueryService query = (XPathQueryService) col.getService(
					"XPathQueryService", "1.0");
			String query_code = null;
			ResourceSet retSet = null;
			ResourceIterator iter = null;

			for (int i = 0; i < count; i++) {
				query_code = "/basicfeature/node[./@id='" + i + "']//FileName";
				retSet = query.query(query_code);
				iter = retSet.getIterator();
				System.out.println("query_code: " + query_code + "; size:"
						+ retSet.getSize());
				Assert.assertEquals(1, retSet.getSize());
				while (iter.hasMoreResources()) {
					System.out.println(iter.nextResource().getContent());
				}
			}
			// update the text
			update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:update select=\"/basicfeature/node[./@id='1']//FileName\">"
					+ "我更新了.avi"
					+ "</xu:update>"
					+ "<xu:update select=\"/basicfeature/node[./@id='1']//User_Comment\">"
					+ "new comment"
					+ "</xu:update>"
					+ "<xu:update select=\"/basicfeature/node[./@id='2']//FileName\">"
					+ "我更新了.avi" + "</xu:update></xu:modifications>";
			System.out.println("updated nodes:" + update.update(update_code));
			System.out.println(update_code);
			Thread.sleep(500);
			
			// test invert index
			query = (XPathQueryService) col.getService("XPathQueryService",
					"1.0");
			query_code = "/basicfeature/node[./@id='1']//FileName/text()";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			
			System.out.println(query_code + "; count: "
					+ retSet.getSize());
			Assert.assertEquals(1, retSet.getSize());
			while (iter.hasMoreResources()) {
				Resource resource = iter.nextResource();
				System.out.println(resource.getContent());
				Assert.assertEquals("我更新了.avi", resource.getContent());
			}

			query = (XPathQueryService) col.getService("XPathQueryService",
					"1.0");
			query_code = "/basicfeature/node[./@id='1']//User_Comment/text()";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			System.out.println(query_code + "; count: "
					+ retSet.getSize());
			Assert.assertEquals(1, retSet.getSize());
			while (iter.hasMoreResources()) {
				Resource resource = iter.nextResource();
				System.out.println(resource.getContent());
				Assert.assertEquals("new comment", resource.getContent());
			}

			// test invert index
			query_code = "/basicfeature/node[./contains(.//FileName, '我更新了')]//FileName";
			retSet = query.query(query_code);
			Assert.assertEquals(2, retSet.getSize());
			iter = retSet.getIterator();
			System.out.println(query_code + "  select by FileName text");
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			query_code = "/basicfeature/node[.//FileName='我更新了.avi']//FileName";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 2);
			iter = retSet.getIterator();
			System.out.println(query_code + "  select by FileName text");
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			// test update of user_comment
			query_code = "/basicfeature/node[.//User_Comment='new comment']/User_Comment";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 1);
			iter = retSet.getIterator();
			System.out.println(query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/basicfeature/node[./contains(.//User_Comment,'new comment')]/User_Comment";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 1);
			iter = retSet.getIterator();
			System.out.println(query_code + "  result size:" + retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:update select=\"/basicfeature/node[./Sharer='audr']//Keywords\">"
					+ "新的关键词"
					+ "</xu:update></xu:modifications>";
			update.update(update_code);
			System.out.println("execute " + update_code);
			Thread.sleep(500);
			
			query_code = "/basicfeature/node[.//Keywords='新的关键词']/Keywords";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 3);
			iter = retSet.getIterator();
			System.out.println(query_code + " result size:" + retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/basicfeature/node[./contains(./Keywords,'关键词')]/Keywords";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 3);
			iter = retSet.getIterator();
			System.out.println(query_code + " result size:" + retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@After
	public void clean() {
		TestUtilities.removeCollection(fullColName);
	}
}

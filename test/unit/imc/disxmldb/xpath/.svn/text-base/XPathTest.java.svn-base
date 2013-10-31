package imc.disxmldb.xpath;

import java.util.Random;

import junit.framework.Assert;
import imc.disxmldb.util.TestUtilities;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.modules.XPathQueryService;
import org.xmldb.api.modules.XUpdateQueryService;

public class XPathTest {
	public static String colName = "testCol";
	public static String fullColName = "/db/" + colName;
	public static String docName = "testXML.xml";
	public static String emptyDoc = "<basicfeature></basicfeature>";

	@Before
	public void prepare() {
		TestUtilities.prepareCollection(fullColName);
		String node = "";
		float fileSize = 1.1f;
		for (int i = 0; i < 10; i++) {
			node += "<node id=\"" + i + "\" name=\"activity" + i + "\">"
					+ "<bfid>0300010000000007200</bfid>" + "<general>"
					+ "<TimeUpload>2010年11月09日</TimeUpload>" + ""
					+ "<Sharer>audr</Sharer>" + "</general>"
					+ "<FileName>世界经济能否在明年上半年复苏.avi</FileName>"
					+ "<FileSize>" + fileSize + "MB</FileSize>" + "</node>";
			fileSize += 1.0;
		}
		emptyDoc = "<basicfeature>" + node + "</basicfeature>";
		TestUtilities.prepareDoc(fullColName, docName, emptyDoc, (byte) 1);
		
		
	}

	@Test
	public void IntegerTypeTest() {
		try {
			Collection col = TestUtilities.getCollection(fullColName);
			XUpdateQueryService update = (XUpdateQueryService) col.getService(
					"XUpdateQueryService", "1.0");
			update.setProperty("pretty", "true");
			update.setProperty("encoding", "ISO-8859-1");
			XPathQueryService query = (XPathQueryService) col.getService(
					"XPathQueryService", "1.0");
			String query_code = "/basicfeature/node/count()";
			ResourceSet retSet = query.query(query_code);
			ResourceIterator iter = retSet.getIterator();
			Assert.assertEquals(1, retSet.getSize());
			
			// int type test
			query_code = "/basicfeature/node[./@id=7]//Sharer";
			retSet = query.query(query_code);
			Assert.assertEquals(1, retSet.getSize());
			iter = retSet.getIterator();
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test() {
		try {
			Collection col = TestUtilities.getCollection(fullColName);
			XUpdateQueryService update = (XUpdateQueryService) col.getService(
					"XUpdateQueryService", "1.0");
			update.setProperty("pretty", "true");
			update.setProperty("encoding", "ISO-8859-1");

		/*	// append a node
			String node = "";
			float fileSize = 1.1f;
			for (int i = 0; i < 10; i++) {
				node += "<node id=\"" + i + "\" name=\"activity" + i + "\">"
						+ "<bfid>0300010000000007200</bfid>" + "<general>"
						+ "<TimeUpload>2010年11月09日</TimeUpload>" + ""
						+ "<Sharer>audr</Sharer>" + "</general>"
						+ "<FileName>世界经济能否在明年上半年复苏.avi</FileName>"
						+ "<FileSize>" + fileSize + "MB</FileSize>" + "</node>";
				fileSize += 1.0;
			}

			String update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:append select=\"/basicfeature\">"
					+ node
					+ "</xu:append></xu:modifications>";
			update.update(update_code);*/

			XPathQueryService query = (XPathQueryService) col.getService(
					"XPathQueryService", "1.0");
			String query_code = "/basicfeature/node/count()";
			ResourceSet retSet = query.query(query_code);
			ResourceIterator iter = retSet.getIterator();
			Assert.assertEquals(1, retSet.getSize());
			System.out.println(query_code + +retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			query = (XPathQueryService) col.getService("XPathQueryService",
					"1.0");
			query_code = "/basicfeature/node[./@id=7]";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			Assert.assertEquals(1, retSet.getSize());
			System.out.println(query_code + +retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			// test get child nodes
			query = (XPathQueryService) col.getService("XPathQueryService",
					"1.0");
			query_code = "/basicfeature/node[./@id='7']//Sharer";
			retSet = query.query(query_code);
			Assert.assertEquals(1, retSet.getSize());
			iter = retSet.getIterator();
			System.out.println(query_code + "select by id = '7' count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			query_code = "/basicfeature/node[./@name='activity1']";
			retSet = query.query(query_code);
			Assert.assertEquals(1, retSet.getSize());
			iter = retSet.getIterator();
			System.out.println(query_code
					+ "select by name='activity1' count: " + retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			// test btree index and equal function
			query_code = "/basicfeature/node[.//FileName ='世界经济能否在明年上半年复苏.avi']";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 10);
			iter = retSet.getIterator();
			System.out.println(query_code + "select by FileName text");
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			// test invert index and contains function
			query_code = "/basicfeature/node[./contains(.//FileName, '世界经济能否在明年上半年复苏.avi')]";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 10);
			iter = retSet.getIterator();
			System.out.println("query: " + query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			// test fetch attribute
			query_code = "/basicfeature/node[./contains(.//FileName, '世界经济能否在明年上半年复苏.avi')]/@id";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 10);
			iter = retSet.getIterator();
			System.out.println("query: " + query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			// test text() function
			query_code = "/basicfeature/node[./contains(.//FileName, '世界经济能否在明年上半年复苏.avi')]/FileSize/text()";
			retSet = query.query(query_code);
			Assert.assertEquals(10, retSet.getSize());
			iter = retSet.getIterator();
			System.out.println("query: " + query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			// test count() function
			query_code = "/basicfeature/node[./contains(.//FileName, '世界经济能否在明年上半年复苏.avi')]/count()";
			retSet = query.query(query_code);
			Assert.assertEquals(1, retSet.getSize());
			iter = retSet.getIterator();
			System.out.println("query: " + query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			// test subsequence() function
			query_code = "/basicfeature/node[./contains(.//FileName, '世界经济能否在明年上半年复苏.avi')]/subsequence(2,4)";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 4);
			iter = retSet.getIterator();
			System.out.println("query: " + query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			// test low bound overflows
			query_code = "/basicfeature/node[./contains(.//FileName, '世界经济能否在明年上半年复苏.avi')]/subsequence(10 ,4)";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 0);
			iter = retSet.getIterator();
			System.out.println("query: " + query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			// test upper bound overflows
			query_code = "/basicfeature/node[./contains(.//FileName, '世界经济能否在明年上半年复苏.avi')]/subsequence(8 ,4)";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 2);
			iter = retSet.getIterator();
			System.out.println("query: " + query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			// test multiple predicate
			query_code = "/basicfeature/node[./contains(.//FileName, '世界经济能否在明年上半年复苏.avi')][./@id='1']";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 1);
			iter = retSet.getIterator();
			System.out.println("query: " + query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			// filesize type test
			query_code = "/basicfeature/node[./FileSize < '2.2MB']";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 2);
			iter = retSet.getIterator();
			System.out.println("query: " + query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			// int type test
			query_code = "/basicfeature/node[./@id=7]//Sharer";
			retSet = query.query(query_code);
			Assert.assertEquals(1, retSet.getSize());
			iter = retSet.getIterator();
			System.out.println(query_code + "select by id = '7' count: "
					+ retSet.getSize());
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

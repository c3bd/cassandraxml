package imc.disxmldb.xupdate;

import imc.disxmldb.util.TestUtilities;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.modules.XPathQueryService;
import org.xmldb.api.modules.XUpdateQueryService;

public class XUpdateDeleteTest {
	public static final String colName = "deleteTest";
	public static final String fullColName = "/db/" + colName;
	public static final String docName = "testDel.xml";
	public static String emptyDoc = "<basicfeature></basicfeature>";
	public static final int count = 4;
	
	@Before
	public void prepare() {
		TestUtilities.prepareCollection(fullColName);
		emptyDoc = "<basicfeature>";
		
		for (int i = 0; i < count; i++) {
			emptyDoc += "<node id=\"" + i + "\">"
					+ "<bfid>0300010000000007200</bfid>"
					+ "<TimeUpload>2010年11月09日</TimeUpload>"
					+ "<Sharer>audr</Sharer>"
					+ "<FileName>test file name.avi</FileName>"
					+ "<FileSize>1.5 MB</FileSize>" + "</node>";
		}
		emptyDoc += "</basicfeature>";
		System.out.println(emptyDoc);
		TestUtilities.prepareDoc(fullColName, docName, emptyDoc, (byte) 1);
	}

	@Test
	public void deleteTest() {
		try {
			Collection col = TestUtilities.getCollection(fullColName);
			XUpdateQueryService update = (XUpdateQueryService) col.getService(
					"XUpdateQueryService", "1.0");
			update.setProperty("pretty", "true");
			update.setProperty("encoding", "ISO-8859-1");
			String update_code = null;

			XPathQueryService query = (XPathQueryService) col.getService(
					"XPathQueryService", "1.0");
			String query_code = null;
			ResourceSet retSet = null;
			ResourceIterator iter = null;

			// show the content of the database before test
			query_code = "/basicfeature/node/@id";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), count);
			iter = retSet.getIterator();
			System.out.println(query_code + " result size:" + retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println("\t" + iter.nextResource().getContent());
			}
			
			query_code = "/basicfeature/node[./FileSize='1.5 MB']/@id";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), count);
			System.out.println(query_code + " result size:" + retSet.getSize());
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println("\t" + iter.nextResource().getContent());
			}

			query_code = "/basicfeature/node[contains(./FileName, 'test file name.avi')]/@id";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), count);
			System.out.println(query_code + " result size:" + retSet.getSize());
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println("\t" + iter.nextResource().getContent());
			}

			// query the node by the deleted id to check if the indexex have
			// been updated
			// delete 2 nodes
			update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:remove select=\"/basicfeature/node[./@id='1']\"/>"
					+ "<xu:remove select=\"/basicfeature/node[./@id='2']\"/></xu:modifications>";
			System.out.println("deleted nodes: " + update.update(update_code));
			System.out.println("execute " + update_code);
			Thread.sleep(50);

			// query all the node nodes
			query_code = "/basicfeature/node/@id";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 2);
			iter = retSet.getIterator();
			System.out.println(query_code + " result size:" + retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println("\t" + iter.nextResource().getContent());
			}

			// query the node by the deleted id to check if the indexex have
			// been updated
			/*
			 * query_code = "/basicfeature/node[./@id='1']"; retSet =
			 * query.query(query_code); Assert.assertEquals(retSet.getSize(),
			 * 0); System.out.println(
			 * "querying the node by the deleted id returns 0 result");
			 * 
			 * query_code = "/basicfeature/node[./@id='3']"; retSet =
			 * query.query(query_code); Assert.assertEquals(retSet.getSize(),
			 * 1);
			 * System.out.println("querying the node by the  id returns1 result"
			 * );
			 */

			// query the node by the FileSize to check if the btree index has
			// been updated
			query_code = "/basicfeature/node[./FileSize='1.5 MB']/@id";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 2);
			System.out.println(query_code + " result size:" + retSet.getSize());
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println("\t" + iter.nextResource().getContent());
			}
			// query the node by the FileSize to check if the btree index has
			// been updated
			query_code = "/basicfeature/node[contains(./FileName, 'test file name.avi')]/@id";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 2);
			System.out.println(query_code + " result size:" + retSet.getSize());
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println("\t" + iter.nextResource().getContent());
			}
			
			update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:remove select=\"/basicfeature/node[./TimeUpload='2010年11月09日']\"/>"
					+ "</xu:modifications>";
			System.out.println("deleted nodes: " + update.update(update_code));
			System.out.println("execute " + update_code);
			Thread.sleep(50);

			// query all the node nodes
			query_code = "/basicfeature/node";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 0);
			iter = retSet.getIterator();
			System.out.println(query_code + " result size:" + retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println("\t" + iter.nextResource().getContent());
			}

			// query the node by the FileSize to check if the btree index has
			// been updated
			query_code = "/basicfeature/node[./FileSize='1.5 MB']/@id";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 0);
			System.out.println(query_code + " result size:" + retSet.getSize());
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println("\t" + iter.nextResource().getContent());
			}
			// query the node by the FileSize to check if the btree index has
			// been updated
			query_code = "/basicfeature/node[contains(./FileName, 'test file name.avi')]/@id";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 0);
			System.out.println(query_code + " result size:" + retSet.getSize());
			iter = retSet.getIterator();
			while (iter.hasMoreResources()) {
				System.out.println("\t" + iter.nextResource().getContent());
			}
			

		} catch (Exception e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@After
	public void clean() {
		TestUtilities.removeCollection(fullColName);
	}
}

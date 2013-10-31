package imc.disxmldb.storexml;

import imc.disxmldb.util.TestUtilities;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XPathQueryService;
import org.xmldb.api.modules.XUpdateQueryService;

public class StoreSplittedXMLTest {
	public static String colName = "testSplitted";
	public static String fullColName = "/db/" + colName;
	public static String docName = "testSplitted.xml";
	public static String emptyDoc = "<number type='sub'><image></image></number>";

	@Before
	public void prepare() {
		TestUtilities.prepareCollection(fullColName);
		TestUtilities.prepareDoc(fullColName, docName, emptyDoc, (byte) 1);
	}

	@Test
	public void store() throws XMLDBException {
		Collection col = TestUtilities.getCollection(fullColName);
		XUpdateQueryService update = (XUpdateQueryService) col.getService(
				"XUpdateQueryService", "1.0");
		update.setProperty("pretty", "true");
		update.setProperty("encoding", "ISO-8859-1");
		String node = null;
		String update_code = null;
		int count = 36;
		for (int i = 0; i < count; i++) {
			node = "<total><count>" + i + "</count></total>";
			update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:append select=\"/number/image\">"
					+ node
					+ "</xu:append></xu:modifications>";
			update.update(update_code);
		}
		XPathQueryService query = (XPathQueryService) col.getService(
				"XPathQueryService", "1.0");
		String query_code = "/number/image";
		ResourceSet retSet = query.query(query_code);
		Assert.assertEquals(1, retSet.getSize());
		ResourceIterator iter = retSet.getIterator();
		while (iter.hasMoreResources()) {
			System.out.println(iter.nextResource().getContent());
		}

		query_code = "/number/image/total";
		retSet = query.query(query_code);
		Assert.assertEquals(count, retSet.getSize());
		iter = retSet.getIterator();
		while (iter.hasMoreResources()) {
			System.out.println(iter.nextResource().getContent());
		}

		query_code = "/number/image/total/count";
		retSet = query.query(query_code);
		Assert.assertEquals(count, retSet.getSize());
		iter = retSet.getIterator();
		while (iter.hasMoreResources()) {
			System.out.println(iter.nextResource().getContent());
		}
		
		query_code = "/number[./@type='sub']/image/total/count";
		retSet = query.query(query_code);
		Assert.assertEquals(count, retSet.getSize());
		iter = retSet.getIterator();
		System.out.println(query_code);
		while (iter.hasMoreResources()) {
			System.out.println(iter.nextResource().getContent());
		}
		
		query_code = "/number[./@type='sub']";
		retSet = query.query(query_code);
		Assert.assertEquals(1, retSet.getSize());
		iter = retSet.getIterator();
		System.out.println(query_code);
		while (iter.hasMoreResources()) {
			System.out.println(iter.nextResource().getContent());
		}
		
		query_code = "/number";
		retSet = query.query(query_code);
		Assert.assertEquals(1, retSet.getSize());
		iter = retSet.getIterator();
		System.out.println(query_code);
		while (iter.hasMoreResources()) {
			System.out.println(iter.nextResource().getContent());
		}
	}
	@After
	public void clean() {
		TestUtilities.removeCollection(fullColName);
	}
}

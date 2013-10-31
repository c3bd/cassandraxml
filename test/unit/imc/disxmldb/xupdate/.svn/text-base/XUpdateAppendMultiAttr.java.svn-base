package imc.disxmldb.xupdate;

import static org.junit.Assert.assertEquals;
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

public class XUpdateAppendMultiAttr {
	public static String colName = "testCol";
	public static String fullColName = "/db/" + colName;
	public static String docName = "testXML.xml";
	public static String emptyDoc = "<imagetype sub=\"8\"></imagetype>";

	@Before
	public void prepare() throws InterruptedException {
		TestUtilities.prepareCollection(fullColName);
		Thread.sleep(1000);
		TestUtilities.prepareDoc(fullColName, docName, emptyDoc, (byte) 1);
	}

	@Test
	public void test() {
		try {
			Collection col = TestUtilities.getCollection(fullColName);
			XUpdateQueryService update = (XUpdateQueryService) col.getService(
					"XUpdateQueryService", "1.0");
			update.setProperty("pretty", "true");
			update.setProperty("encoding", "ISO-8859-1");
			String[] name = new String[] { "consumption", "culture",
					"education", "finance", "government", "health", "military",
					"science", "sport", "tour" };
			String[] code = new String[] { "020001", "020002", "020003",
					"020004", "020005", "020006", "020007", "020008", "020009",
					"020010" };

			XPathQueryService query = (XPathQueryService) col.getService(
					"XPathQueryService", "1.0");
			for (int i = 0; i < name.length; i++) {
				String node = "<outtype name=\"" + name[i]
						+ "\"  length=\"6\" num=\"1\"><code>" + code[i]
						+ "</code></outtype>";
				String update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
						+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
						+ "<xu:append select=\"/imagetype/\">"
						+ node
						+ "</xu:append></xu:modifications>";
				System.out.println("execute " + update_code);
				System.out.println("updated nodes:"
						+ update.update(update_code));
			}

			for (int i = 0; i < name.length; i++) {
				String query_code = "/imagetype/outtype[./@name='" + name[i]
						+ "']/code/text()";
				ResourceSet retSet = query.query(query_code);
				assertEquals(retSet.getSize(), 1);
				ResourceIterator iter = retSet.getIterator();
				String retCode = (String) iter.nextResource().getContent();
				System.out.println(query_code);
				System.out.println(retCode);
				Assert.assertEquals(code[i], retCode);
			}
		} catch (Exception ex) {

		}
	}

	@After
	public void clean() {
		TestUtilities.removeCollection(fullColName);
	}
}

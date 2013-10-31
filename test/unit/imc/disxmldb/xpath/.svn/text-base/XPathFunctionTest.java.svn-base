package imc.disxmldb.xpath;

import imc.disxmldb.util.TestUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.modules.XPathQueryService;
import org.xmldb.api.modules.XUpdateQueryService;

public class XPathFunctionTest {
	public static String colName = "funcTestCol";
	public static String fullColName = "/db/" + colName;
	public static String docName = "orders.xml";
	public static String filePath = "testdata/" + docName;

	@Before
	public void prepare() throws IOException {
		TestUtilities.prepareCollection(fullColName);
		FileInputStream input;

		input = new FileInputStream(new File(filePath));
		InputStreamReader ireader = new InputStreamReader(input);
		BufferedReader reader = new BufferedReader(ireader);
		String line = null;
		StringBuilder xmlContent = new StringBuilder();
		while (null != (line = reader.readLine())) {
			xmlContent.append(line);
		}
		TestUtilities.prepareDoc(fullColName, docName, xmlContent.toString(),
				(byte) 1);
	}

	@Test
	public void XPathFuncTest() {
		try {
			Collection col = TestUtilities.getCollection(fullColName);
			XUpdateQueryService update = (XUpdateQueryService) col.getService(
					"XUpdateQueryService", "1.0");
			update.setProperty("pretty", "true");
			update.setProperty("encoding", "ISO-8859-1");
			XPathQueryService query = (XPathQueryService) col.getService(
					"XPathQueryService", "1.0");
			String query_code = "/table/T/O_TOTALPRICE/max()";
			ResourceSet retSet = query.query(query_code);
			ResourceIterator iter = retSet.getIterator();
			Assert.assertEquals(1, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			query_code = "/table/T/O_TOTALPRICE/min()";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			Assert.assertEquals(1, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/table/T/O_TOTALPRICE/avg()";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			Assert.assertEquals(1, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/table/T/O_TOTALPRICE/sum()";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			Assert.assertEquals(1, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/table/T/O_TOTALPRICE/count()";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			Assert.assertEquals(1, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			query_code = "/table/T[./O_ORDERKEY=7]/O_COMMENT/text()";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			Assert.assertEquals(1, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			Thread.sleep(500);
			query_code = "/table/T[./contains(./O_COMMENT, 'pinto beans')]/O_COMMENT";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			Assert.assertEquals(4, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/table/T[./contains(./O_COMMENT, '易碎商品')]/O_COMMENT";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			Assert.assertEquals(3, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/table/T[./contains(./O_COMMENT, '轻拿轻放。made in China')]/O_COMMENT";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			Assert.assertEquals(1, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/table/T[./contains(./O_COMMENT, 'pinto beans')]/O_COMMENT/subsequence(0,1)";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			Assert.assertEquals(1, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/table/T[./contains(./O_COMMENT, 'pinto beans')]/O_COMMENT/subsequence(0,5)";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			Assert.assertEquals(4, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/table/T[./contains(./O_COMMENT, 'pinto beans')]/O_COMMENT/subsequence(4,1)";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			Assert.assertEquals(0, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@After
	public void cleanup() {
		TestUtilities.removeCollection(fullColName);
	}
}

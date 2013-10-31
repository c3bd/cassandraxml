package imc.disxmldb.xpath;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

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

public class XPathTypeTest {
	public static String colName = "typeTestCol";
	public static String fullColName = "/db/" + colName;
	public static String docName = "synthetic.xml";
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
	public void typeTest() {
		try {
			Collection col = TestUtilities.getCollection(fullColName);
			XUpdateQueryService update = (XUpdateQueryService) col.getService(
					"XUpdateQueryService", "1.0");
			update.setProperty("pretty", "true");
			update.setProperty("encoding", "ISO-8859-1");
			XPathQueryService query = (XPathQueryService) col.getService(
					"XPathQueryService", "1.0");
			String query_code = "/basicfeature/node[./@id=1]";
			ResourceSet retSet = query.query(query_code);
			ResourceIterator iter = retSet.getIterator();
			Assert.assertEquals(1, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			query_code = "/basicfeature/node[./FileName='大国崛起.avi']/@id";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			Assert.assertEquals(1, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/basicfeature/node[./FileSize = '110k']/@id";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			Assert.assertEquals(1, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/basicfeature/node[./FileSize < '300k']/@id";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			Assert.assertEquals(4, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/basicfeature/node[./FileSize < '200k']/@id";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			Assert.assertEquals(3, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			query_code = "/basicfeature/node[./FileSize > '110k']/@id";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			Assert.assertEquals(3, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			query_code = "/basicfeature/node[./FileSize > '0k']/@id";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			Assert.assertEquals(5, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			query_code = "/basicfeature/node[./FileSize > '700k']/@id";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			Assert.assertEquals(1, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/basicfeature/node[./FileSize > '109M']/@id";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			Assert.assertEquals(1, retSet.getSize());
			System.out.println("query: " + query_code + "; count: "
					+ retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/basicfeature/node[./FileSize > '110M']/@id";
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

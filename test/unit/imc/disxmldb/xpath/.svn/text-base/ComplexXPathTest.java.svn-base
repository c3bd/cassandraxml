package imc.disxmldb.xpath;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
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

/**
 * kind of queries to be tested:
 * 1.	multiple predicates
 * 2.	nested predicates
 * 3.	predicates contains function
 * 4.	functions contains simple xpath as parameter
 * 5.	functions contains complex xpath as parameter
 * @author xiafan
 *
 */
public class ComplexXPathTest {
	public static String colName = "CXPathCol";
	public static String fullColName = "/db/" + colName;
	public static String docName = "CXPathTest.xml";
	public static String emptyDoc = "<Database></Database>";

	@Before
	public void prepare() {
		TestUtilities.prepareCollection(fullColName);
		TestUtilities.prepareDoc(fullColName, docName, emptyDoc, (byte) 1);
	}

	@Test
	public void ComplexPredicateXPathTest() {
		try {
			Collection col = TestUtilities.getCollection(fullColName);
			XUpdateQueryService update = (XUpdateQueryService) col.getService(
					"XUpdateQueryService", "1.0");
			update.setProperty("pretty", "true");
			update.setProperty("encoding", "ISO-8859-1");

			// append a node
			String node = "";
			FileReader fis = new FileReader("testdata/complex.xml");
			BufferedReader bis = new BufferedReader(fis);
			StringBuffer buffer = new StringBuffer();
			while (null != (node = bis.readLine())) {
				buffer.append(node);
			}
			
			String update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:append select=\"/Database\">"
					+ buffer.toString()
					+ "</xu:append></xu:modifications>";
			update.update(update_code);

			
			//some simple test
			XPathQueryService query = (XPathQueryService) col.getService(
					"XPathQueryService", "1.0");
			String query_code = "/Database/ProteinEntry//refinfo[./@refid='A31764']";
			ResourceSet retSet = query.query(query_code);
			ResourceIterator iter = retSet.getIterator();
			Assert.assertEquals(1, retSet.getSize());
			System.out.println("query:" + query_code +" result size: "+  + retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}

			query_code = "/Database/ProteinEntry[./@id='CCHU']/reference//title";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 4);
			iter = retSet.getIterator();
			System.out.println("query: " + query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}			

		/*	query_code = "/Database/ProteinEntry[./@id='CCHU']//refinfo";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 4);
			iter = retSet.getIterator();
			System.out.println("query: " + query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}*/
			
			//test select by child nodes
		/*	query_code = "/Database/ProteinEntry[.//uid='CCHU']";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 1);
			iter = retSet.getIterator();
			System.out.println("query: " + query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}*/
			
			query_code = "/Database/ProteinEntry[.//accession='A31764']/@id";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 1);
			iter = retSet.getIterator();
			System.out.println("query: " + query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/Database/ProteinEntry[.//header/accession='A31764']/@id";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 1);
			iter = retSet.getIterator();
			System.out.println("query: " + query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/Database/ProteinEntry[.//accession='A31764']//accession";
			retSet = query.query(query_code);
			iter = retSet.getIterator();
			System.out.println("query: " + query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/Database/ProteinEntry[.//accession='A31764'][.//accession='A05676']/@id";
			retSet = query.query(query_code);
			Assert.assertEquals(1, retSet.getSize());
			iter = retSet.getIterator();
			System.out.println("query: " + query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/Database/ProteinEntry[.//accession='A31764'][.//accession='A05671']/@id";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 0);
			iter = retSet.getIterator();
			System.out.println("query: " + query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/Database/ProteinEntry[.//refinfo[./@refid='A31764']/authors/author='Evans, M.J.']/@id";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 1);
			iter = retSet.getIterator();
			System.out.println("query: " + query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/Database/ProteinEntry[./contains(./reference/refinfo[./@refid='A94455']/citation, 'unpublished results, 1966, cited by Margoliash')]/@id";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 1);
			iter = retSet.getIterator();
			System.out.println("query: " + query_code);
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/Database/ProteinEntry[./contains(./reference/refinfo[./@refid='94455']/citation, 'unpublished results, 1966, cited by Margoliash')]/@id";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 0);
			iter = retSet.getIterator();
			System.out.println("query: " + query_code + " result size: " + retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/Database/ProteinEntry[./contains(.//title, 'cytochrome c gene: two classes of processed')]/@id";
			retSet = query.query(query_code);
			Assert.assertEquals(retSet.getSize(), 1);
			iter = retSet.getIterator();
			System.out.println("query: " + query_code);
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

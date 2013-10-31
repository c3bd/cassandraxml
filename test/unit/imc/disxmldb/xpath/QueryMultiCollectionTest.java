package imc.disxmldb.xpath;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import imc.disxmldb.util.TestUtilities;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XPathQueryService;

public class QueryMultiCollectionTest {
	public static String colName1 = "testCol1";
	public static String colName2 = "testCol2";
	public static String fullColName1 = "/db/" + colName1;
	public static String fullColName2 = "/db/" + colName2;
	public static String docName1 = "texttype1.xml";
	public static String fileName1 = "testdata/" + docName1;
	public static String docName2 = "texttype2.xml";
	public static String fileName2 = "testdata/" + docName2;

	@Before
	public void prepare() {
		TestUtilities.prepareCollection(fullColName1);
	}

	@After
	public void after() {
		TestUtilities.removeCollection(fullColName1);
	}
	
	/**
	 * the same xml document is saved under different collection. Test if query one collection will
	 * return the result multiple times
	 */
	@Test
	public void test() {
		FileInputStream input;
		try {
			input = new FileInputStream(new File(fileName1));
			InputStreamReader ireader = new InputStreamReader(input);
			BufferedReader reader = new BufferedReader(ireader);
			String line = null;
			StringBuilder xmlContent = new StringBuilder();
			while (null != (line = reader.readLine())) {
				xmlContent.append(line);
			}
			TestUtilities.prepareDoc(fullColName1, docName1,
					xmlContent.toString(), (byte) 1);

			input = new FileInputStream(new File(fileName2));
			ireader = new InputStreamReader(input);
			reader = new BufferedReader(ireader);
			line = null;
			xmlContent = new StringBuilder();
			while (null != (line = reader.readLine())) {
				xmlContent.append(line);
			}
			TestUtilities.prepareDoc(fullColName2, docName2,
					xmlContent.toString(), (byte) 1);
			
			Collection col = TestUtilities.getCollection(fullColName1);
			XPathQueryService query = (XPathQueryService) col.getService(
					"XPathQueryService", "1.0");
			String query_code = "/texttype/outtype[./code='020003']";
			ResourceSet retSet = query.query(query_code);
			ResourceIterator iter = retSet.getIterator();
			System.out.println("query: " + query_code + " count: " + retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

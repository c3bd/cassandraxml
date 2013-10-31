package imc.disxmldb.storexml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import imc.disxmldb.util.TestUtilities;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;

public class StoreTextTypeTest {
	public static String colName = "textType";
	public static String fullColName = "/db/" + colName;
	public static String docName = "texttype.xml";
	public static String fileName = "testdata/texttype1.xml";

	@Before
	public void prepare() {
		TestUtilities.prepareCollection(fullColName);
	}
	
	@Test
	public void storeAndQuery() {
		try {
			FileInputStream input = new FileInputStream(new File(fileName));
			InputStreamReader ireader = new InputStreamReader(input);
			BufferedReader reader = new BufferedReader(ireader);
			String line = null;
			StringBuilder xmlContent = new StringBuilder();
			while (null != (line = reader.readLine())) {
				xmlContent.append(line);
			}
			TestUtilities.prepareDoc(fullColName, docName, xmlContent.toString(), (byte) 1);
			
			Collection col = TestUtilities.getCollection(fullColName);
			//query the doc
			XPathQueryService query = (XPathQueryService) col.getService(
					"XPathQueryService", "1.0");
			String query_code = "/texttype/outtype[./@name = 'culture']";
			ResourceSet retSet = query.query(query_code);
			Assert.assertEquals(1, retSet.getSize());
			ResourceIterator iter = retSet.getIterator();
			System.out.println("query: " + query_code + " count: " + retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/texttype/outtype[./name = 'consumption']";
			retSet = query.query(query_code);
			Assert.assertEquals(0, retSet.getSize());
			iter = retSet.getIterator();
			System.out.println("query: " + query_code + " count: " + retSet.getSize());
	
			String code_key = "020002";
			query_code = "/texttype/outtype[./code='020002']/code/text()";
			retSet = query.query(query_code);
			Assert.assertEquals(1, retSet.getSize());
			iter = retSet.getIterator();
			System.out.println("query: " + query_code + " count: " + retSet.getSize());
			while (iter.hasMoreResources()) {
				XMLResource resource = (XMLResource) iter.nextResource();
				System.out.println(resource.getContent());
				Assert.assertEquals(code_key, resource.getContent());
			}
			
			query_code = "/texttype/outtype[./contains(./code, '020001')]";
			retSet = query.query(query_code);
			Assert.assertEquals(1, retSet.getSize());
			iter = retSet.getIterator();
			System.out.println("query: " + query_code + " count: " + retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/texttype/outtype[./contains(./@name, 'consumption')]";
			retSet = query.query(query_code);
			Assert.assertEquals(1, retSet.getSize());
			iter = retSet.getIterator();
			System.out.println("query: " + query_code + " count: " + retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/texttype/outtype[./@name='consumption']/@length/text()";
			retSet = query.query(query_code);
			Assert.assertEquals(1, retSet.getSize());
			iter = retSet.getIterator();
			System.out.println("query: " + query_code + " count: " + retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/texttype/outtype[./@name='consumption']/@length";
			retSet = query.query(query_code);
			Assert.assertEquals(1, retSet.getSize());
			iter = retSet.getIterator();
			System.out.println("query: " + query_code + " count: " + retSet.getSize());
			while (iter.hasMoreResources()) {
				System.out.println(iter.nextResource().getContent());
			}
			
			query_code = "/texttype/outtype[./code='020001']/@name/text()";
			retSet = query.query(query_code);
			Assert.assertEquals(1, retSet.getSize());
			iter = retSet.getIterator();
			System.out.println("query: " + query_code + " count: " + retSet.getSize());
			while (iter.hasMoreResources()) {
				XMLResource resource = (XMLResource) iter.nextResource();
				System.out.println(resource.getContent());
				Assert.assertEquals("consumption", resource.getContent());
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
	
	@After
	public void clear() {
		TestUtilities.removeCollection(fullColName);
	}
}

package performance;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import imc.disxmldb.util.TestUtilities;

import org.junit.Test;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XPathQueryService;

public class PerformanceTest {
	private static final String fullCol = "/db/proteindatabase";
	private static final String uri = "xmldb:exist://10.11.1.202:10002/xmlrpc/db/";
	private static final int testCount = 5; // number of test each que

	public static String[] queries = {
			"/ProteinDatabase/ProteinEntry[./@id='CCHU']",
			"/ProteinDatabase/ProteinEntry[reference/accinfo/accession='AE0077']",
			// "/ProteinDatabase/ProteinEntry//accinfo/xrefs",
			"/ProteinDatabase/ProteinEntry[reference/refinfo/authors/author='Massung, R.F.']",
			"/ProteinDatabase/ProteinEntry[organism/variety='strain Marburg']/reference/accinfo/xrefs",
			"/ProteinDatabase/ProteinEntry[reference//year='1988']/organism",
			"/ProteinDatabase/ProteinEntry[organism/source='Anabaena sp.']/reference/accinfo/xrefs",
			"/ProteinDatabase/ProteinEntry[reference/accinfo/note]",
			"/ProteinDatabase/ProteinEntry[reference/accinfo/accession='AE0077']",
			"/ProteinDatabase/ProteinEntry[reference/refinfo/year='1988']/reference/accinfo[status='preliminary']/xrefs" };
	public static String[] compareQueries = {
			"//ProteinEntry[./contains(.//title, ' complete genome sequence')]",
			"//ProteinEntry[.//accession='AE0077']",
			"//ProteinEntry//accinfo/xrefs/count()",
			"//ProteinEntry[.//author='Massung, R.F.']",
			"//ProteinEntry[.//variety='strain Marburg']//accinfo/xrefs",
			"//ProteinEntry[reference//year='1988']/organism",
			"//ProteinEntry[organism/source='Anabaena sp.']//accinfo/xrefs",
			"//ProteinEntry[//accinfo/note]",
			"//ProteinEntry[//accinfo/accession='AE0077']",
			"//ProteinEntry[//year='1988']//accinfo[status='preliminary']/xrefs"};

	public static String[] countQueries = {
			"//ProteinEntry[.//accession='AE0077']/count()",
			"//ProteinEntry//accinfo/xrefs/count()",
			"//ProteinEntry[.//author='Massung, R.F.']/count()",
			"//ProteinEntry[.//variety='strain Marburg']//accinfo/xrefs/count()",
			"//ProteinEntry[reference//year='1988']/organism/count()",
			"//ProteinEntry[organism/source='Anabaena sp.']//accinfo/xrefs/count()",
			"//ProteinEntry[//accinfo/note]/count()",
			"//ProteinEntry[//accinfo/accession='AE0077']/count()",
			"//ProteinEntry[//year='1988']//accinfo[status='preliminary']/xrefs/count()" };

	@Test
	public void performaceTest() throws XMLDBException, FileNotFoundException {
		String fileName = null;
		if (fileName != null) {
			System.setOut(new PrintStream(new FileOutputStream(fileName, true)));
		}

		Collection col = TestUtilities.getCollection(uri, fullCol);
		XPathQueryService queryClient = (XPathQueryService) col.getService(
				"XPathQueryService", "1.0");

		for (String query : compareQueries) {
			System.out.println(query);
			ResourceSet retSet = null;
			long start = System.currentTimeMillis();
			for (int i = 0; i < testCount; i++) {
				retSet = queryClient.query(query);
			}
			long end = System.currentTimeMillis();
			long count = retSet.getSize();
			System.out.println("count: " + count + ";time: " + (end - start)
					/ (testCount * 1000.0) + "s");
		}
	}

	@Test
	public void performCountTest() throws FileNotFoundException, XMLDBException {
		String fileName = null;
		if (fileName != null) {
			System.setOut(new PrintStream(new FileOutputStream(fileName, true)));
		}

		Collection col = TestUtilities.getCollection(uri, fullCol);
		XPathQueryService queryClient = (XPathQueryService) col.getService(
				"XPathQueryService", "1.0");

		for (String query : countQueries) {
			System.out.println(query);
			ResourceSet retSet = null;
			long start = System.currentTimeMillis();
			for (int i = 0; i < testCount; i++) {
				retSet = queryClient.query(query);
			}
			long end = System.currentTimeMillis();
			long count = retSet.getSize();

			ResourceIterator iter = retSet.getIterator();
			if (iter.hasMoreResources())
				System.out.println(iter.nextResource().getContent());

			System.out.println("count: " + count + ";time: " + (end - start)
					/ (testCount * 1000.0) + "s");
		}
	}
}

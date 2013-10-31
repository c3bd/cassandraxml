package imc.disxmldb.xmlrpc;

import imc.disxmldb.config.SysConfig;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Provider.Service;

import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XPathQueryService;
import org.xmldb.api.modules.XUpdateQueryService;
import org.exist.xmldb.*;

/**
 * @author:xiafan xiafan68@gmail.com
 * @version: Sep 8, 2011 0.1
 */

public class XmlRpcServerTest {
	private static final String DATADIR = "/home/xiafan/xmldata";
	private static final String uri = "xmldb:exist://mc1:8088/xmlrpc/db/";

	public static void testCreateCollection() {
		String driver = "org.exist.xmldb.DatabaseImpl";
		Class cl;
		try {
			cl = Class.forName(driver);
			Database database = (Database) cl.newInstance();
			database.setProperty("creat-database", "true");
			DatabaseManager.registerDatabase(database);
			String textType = "css";
			RemoteCollection col = (RemoteCollection) DatabaseManager
					.getCollection(uri, "admin", "admin");
			// RemoteCollectionManagementService service =
			// (RemoteCollectionManagementService)
			// col.getService("CollectionManagementService", "1.0");

			// service.createCollection(XmldbURI.xmldbUriFor("/db/child1"));
			col = (RemoteCollection) col.getChildCollection("child1");
			System.out.println(col.getName());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public static void testXUpdateAppend() {
		String driver = "org.exist.xmldb.DatabaseImpl";
		Class cl;
		try {
			cl = Class.forName(driver);
			Database database = (Database) cl.newInstance();
			database.setProperty("creat-database", "true");
			DatabaseManager.registerDatabase(database);
			String textType = "css";
			Collection col = DatabaseManager.getCollection(uri, "admin",
					"admin");
			XUpdateQueryService update = (XUpdateQueryService) col.getService(
					"XUpdateQueryService", "1.0");
			update.setProperty("pretty", "true");
			update.setProperty("encoding", "ISO-8859-1");
			/*String node = "<node id=\"7\">"
					+ "<bfid>0300010000000007200</bfid>"
					+ "<TimeUpload>2010年11月09日</TimeUpload>"
					+ "<Sharer>audr</Sharer>"
					+ "<FileName>世界经济能否在明年上半年复苏.avi</FileName>"
					+ "<FileSize>1.5 MB</FileSize>" + "</node>";
			
			String update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:append select=\"/basicfeature\">"
					+ node
					+ "</xu:append></xu:modifications>";*/
			FileReader reader = new FileReader(DATADIR + File.separator + "errorxml.xml");
			BufferedReader bfReader = new BufferedReader(reader);
			String node = "";
			String line = null;
			while (null != (line = bfReader.readLine())) {
				node += line;
			}
			String update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
				+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
				+ "<xu:append select=\"/dblp\">"
				+ node
				+ "</xu:append></xu:modifications>";
			update.update(update_code);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void testXUpdateAppendSubNode() {
		String driver = "org.exist.xmldb.DatabaseImpl";
		Class cl;
		try {
			cl = Class.forName(driver);
			Database database = (Database) cl.newInstance();
			database.setProperty("creat-database", "true");
			DatabaseManager.registerDatabase(database);
			String textType = "css";
			Collection col = DatabaseManager.getCollection(uri, "admin",
					"admin");
			XUpdateQueryService update = (XUpdateQueryService) col.getService(
					"XUpdateQueryService", "1.0");
			update.setProperty("pretty", "true");
			update.setProperty("encoding", "UTF8");
			String node ="<bfid>1</bfid>";
			
			String update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:append select=\"/basicfeature/node[./@id=\'5\']\">"
					+ node
					+ "</xu:append></xu:modifications>";
			update.update(update_code);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void testXUpdate() {
		String driver = "org.exist.xmldb.DatabaseImpl";
		Class cl;
		try {
			cl = Class.forName(driver);
			Database database = (Database) cl.newInstance();
			database.setProperty("creat-database", "true");
			DatabaseManager.registerDatabase(database);
			String textType = "css";
			Collection col = DatabaseManager.getCollection(uri, "admin",
					"admin");
			XUpdateQueryService update = (XUpdateQueryService) col.getService(
					"XUpdateQueryService", "1.0");
			update.setProperty("pretty", "true");
			update.setProperty("encoding", "ISO-8859-1");

			String update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:update select=\"/basicfeature/node/fid\">"
					+ 1
					+ "</xu:update></xu:modifications>";
			update.update(update_code);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void testXUpdateDelete() {
		String driver = "org.exist.xmldb.DatabaseImpl";
		Class cl;
		try {
			cl = Class.forName(driver);
			Database database = (Database) cl.newInstance();
			database.setProperty("creat-database", "true");
			DatabaseManager.registerDatabase(database);
			String textType = "css";
			Collection col = DatabaseManager.getCollection(uri, "admin",
					"admin");
			XUpdateQueryService update = (XUpdateQueryService) col.getService(
					"XUpdateQueryService", "1.0");
			update.setProperty("pretty", "true");
			update.setProperty("encoding", "ISO-8859-1");

			String update_code = "<?xml version=\"1.0\"?><xu:modifications version=\"1.0\" "
					+ "xmlns:xu=\"http://www.xmldb.org/xupdate\">"
					+ "<xu:remove select=\"/basicfeature/node/fid\"/></xu:modifications>";
			update.update(update_code);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void testXQuery(String xpath)
			throws UnsupportedEncodingException {
		String driver = "org.exist.xmldb.DatabaseImpl";
		Class cl;
		try {
			cl = Class.forName(driver);
			Database database = (Database) cl.newInstance();
			database.setProperty("creat-database", "true");
			DatabaseManager.registerDatabase(database);
			Collection col = DatabaseManager.getCollection(uri, "admin",
					"admin").getChildCollection("dblp");
			XPathQueryService query = (XPathQueryService) col.getService(
					"XPathQueryService", "1.0");
			String bf = null;
			int bfid = 10;
			String limition = "./bfid=\"" + bfid + "\"";
			/*
			 * String query_code = "for $i in /basicfeature/node[" + limition +
			 * "] return $i";
			 */
			// String query_code = "/basicfeature/node[./@id='1']/bfid";
			String query_code = new String(xpath.getBytes(),
					SysConfig.ENCODING);
			long start = System.currentTimeMillis();
			ResourceSet result = query.query(query_code);
			System.out.println("time costs: " + (System.currentTimeMillis() - start));
			long count = result.getSize();
			System.out.println("the count of the size is " + count);
			ResourceIterator res = result.getIterator();
			while (res.hasMoreResources()) {
				Resource r = res.nextResource();
				String number = (String) r.getContent();
				System.out.println(number);
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void storeSchema(String name) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, XMLDBException,
			IOException {
		String driver = "org.exist.xmldb.DatabaseImpl";
		Class cl = Class.forName(driver);
		Database database = (Database) cl.newInstance();
		database.setProperty("creat-database", "true");
		DatabaseManager.registerDatabase(database);
		FileInputStream fis = new FileInputStream("testdata" + File.separator
				+ name);
		InputStreamReader isr = new InputStreamReader(fis,
				System.getProperty("file.encoding"));
		BufferedReader br = new BufferedReader(isr);

		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = br.readLine()) != null) {
			sb.append(line.trim());
		}

		Collection col = DatabaseManager.getCollection(uri, "admin", "admin");
		((RemoteCollection) col).storeSchema(name, sb.toString(), true);
	}

	public static void test(String name) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, XMLDBException,
			IOException {
		String driver = "org.exist.xmldb.DatabaseImpl";
		Class cl = Class.forName(driver);
		Database database = (Database) cl.newInstance();
		database.setProperty("creat-database", "true");
		DatabaseManager.registerDatabase(database);
		Collection col = DatabaseManager.getCollection(uri, "admin", "admin");
		FileInputStream fis = new FileInputStream(DATADIR + File.separator
				+ name);
		InputStreamReader isr = new InputStreamReader(fis,
				System.getProperty("file.encoding"));
		BufferedReader br = new BufferedReader(isr);

		StringBuilder xml = new StringBuilder();
		String line = null;
		while ((line = br.readLine()) != null) {
			xml.append(line);
		}
		// StringBuilder xml = new
		// StringBuilder("<basicfeature><node id='1000'><fid>希腊公投震动市场 欧债危机再添变数</fid></node><node id='1001'><fid>this is another node </fid></node></basicfeature>");
		Resource x = col.createResource(name, "XMLResource");
		((RemoteXMLResource) x).setSplitted((byte) 1);
		x.setContent(xml.toString());
		long start = System.currentTimeMillis();
		col.storeResource(x);
		System.out.println("time costed: "
				+ (System.currentTimeMillis() - start) + "ms");
	}

	public static void getXML(String docName) throws ClassNotFoundException,
			XMLDBException, InstantiationException, IllegalAccessException {
		String driver = "org.exist.xmldb.DatabaseImpl";
		Class cl = Class.forName(driver);
		Database database = (Database) cl.newInstance();
		database.setProperty("creat-database", "true");
		DatabaseManager.registerDatabase(database);
		Collection col = DatabaseManager.getCollection(uri, "admin", "admin");

		long start = System.currentTimeMillis();
		Resource x = col.getResource(docName);
		System.out.println(x.getContent());
		System.out.println("time costed: "
				+ (System.currentTimeMillis() - start) + "ms");
	}

	public static void main(String[] args) throws ClassNotFoundException,
			XMLDBException, InstantiationException, IllegalAccessException,
			IOException, InterruptedException {

		
		//storeSchema("bf.xsd");
		//test("text1.xml");
		/*for (int i = 2; i < 3; i++) {
			test("basicfeature" + i + ".xml");
			Thread.sleep(100);
			//test("semanticfeature" + i + ".xml");
		}*/
		//
		//test("basicfeature1.xml");
		//test("basicfeature.xml");
		// test("semanticfeature.xml");
		// for ( int i = 0; i < 100; i ++)
		// test("basicfeature" + i + ".xml");
		// getXML("basicfeature.xml");
		// testXUpdate();
		// testXUpdateDelete();
		//testXQuery("/basicfeature/node[./@id='78001']");
		//testXQuery("/semanticfeature/node[contains(./General/Width/keyWord, \"摩托罗拉手机\")]/sfid");
		//testXQuery("/basicfeature/node[./@id='1']");
		//testXQuery("/semanticfeature/node[contains(./author, \"李四\")]/sfid");
		//testXQuery("/semanticfeature/node[./sfid='0300010000000005300']/General");
		// testCreateCollection();
		//testXQuery("/basicfeature/node[./contains(./filepath, 'edu.sina.com.cn')]");
		
		//testXQuery("/dblp/article[./contains(./title, 'Database')]");
		//testXQuery("/dblp/phdthesis[./contains(./title, 'Database')]/count()");
		//testXQuery("/dblp/inproceedings[./author='Scott M. Staley']");
		testXUpdateAppend();
		//testXQuery("/dblp/inproceedings[./author='Scott M. Staley']");
		//test xudpate append
		//test("beforeappend.xml");
		//testXUpdateAppend(); 
		//testXUpdateAppendSubNode();
		//getXML("basicfeature1.xml.2");

	}
}

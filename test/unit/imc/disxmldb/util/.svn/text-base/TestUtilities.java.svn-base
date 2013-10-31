package imc.disxmldb.util;

import org.exist.xmldb.RemoteCollection;
import org.exist.xmldb.RemoteCollectionManagementService;
import org.exist.xmldb.RemoteXMLResource;
import org.exist.xmldb.XmldbURI;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;

public class TestUtilities {
	//public static final String uri = "xmldb:exist://58.198.176.88:10002/xmlrpc/db/";
	public static final String uri = "xmldb:exist://localhost:10002/xmlrpc/db/";
	//public static final String uri = "xmldb:exist://10.11.1.201:10002/xmlrpc/db/";
	public static Collection getCollection(String uri_, String colName) {
		String driver = "org.exist.xmldb.DatabaseImpl";
		Class cl;
		try {
			cl = Class.forName(driver);
			Database database = (Database) cl.newInstance();
			database.setProperty("creat-database", "true");
			DatabaseManager.registerDatabase(database);
			String textType = "css";
			RemoteCollection col = (RemoteCollection) DatabaseManager
					.getCollection(uri_, "admin", "admin");

			if (colName.startsWith("/"))
				colName = colName.substring(1, colName.length());
			if (colName.endsWith("/"))
				colName = colName.substring(0, colName.length() - 1);

			String[] cols = colName.split("/");

			for (int i = 1; i < cols.length; i++) {
				col = (RemoteCollection) col.getChildCollection(cols[i]);
			}

			return col;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public static Collection getCollection(String colName) {
		return getCollection(uri, colName);
	}

	/**
	 * create a collection with path colName
	 * 
	 * @param colName
	 */
	public static void prepareCollection(String colName) {
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
			RemoteCollectionManagementService service = (RemoteCollectionManagementService) col
					.getService("CollectionManagementService", "1.0");

			service.createCollection(XmldbURI.xmldbUriFor(colName));
			// col = (RemoteCollection) col.getChildCollection(colName);
			// System.out.println(col.getName());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public static void removeCollection(String colName) {
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
			RemoteCollectionManagementService service = (RemoteCollectionManagementService) col
					.getService("CollectionManagementService", "1.0");

			service.removeCollection(XmldbURI.xmldbUriFor(colName));
			// col = (RemoteCollection) col.getChildCollection(colName);
			// System.out.println(col.getName());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public static void prepareDoc(String colName, String docName,
			String rootXML, byte splitted) {
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

			if (colName.startsWith("/"))
				colName = colName.substring(1, colName.length());
			if (colName.endsWith("/"))
				colName = colName.substring(0, colName.length() - 1);

			String[] cols = colName.split("/");

			for (int i = 1; i < cols.length; i++) {
				col = (RemoteCollection) col.getChildCollection(cols[i]);
				if (col == null)
					return;
			}

			Resource x = col.createResource(docName, "XMLResource");
			//((RemoteXMLResource) x).setSchemaName("bf.xsd");
			((RemoteXMLResource) x).setSplitted(splitted);
			x.setContent(rootXML);
			col.storeResource(x);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}

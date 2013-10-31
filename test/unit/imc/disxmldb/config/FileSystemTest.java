package imc.disxmldb.config;

import imc.disxmldb.util.TestUtilities;

import org.exist.xmldb.RemoteCollection;
import org.exist.xmldb.RemoteCollectionManagementService;
import org.exist.xmldb.XmldbURI;
import org.junit.Test;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;

public class FileSystemTest {
	@Test
	public void collectionCreateAndDelTest() {
		String driver = "org.exist.xmldb.DatabaseImpl";
		Class cl;
		try {
			cl = Class.forName(driver);
			Database database = (Database) cl.newInstance();
			database.setProperty("creat-database", "true");
			DatabaseManager.registerDatabase(database);
			String textType = "css";
			RemoteCollection col = (RemoteCollection) DatabaseManager
					.getCollection(TestUtilities.uri, "admin", "admin");
			RemoteCollectionManagementService service = (RemoteCollectionManagementService) col
					.getService("CollectionManagementService", "1.0");

			String testColName = "/db/testCol/";
			//service.removeCollection(XmldbURI.xmldbUriFor(colName));
			Collection child = service.createCollection(XmldbURI.xmldbUriFor(testColName));
			
			String[] childs = col.getChildCollections();
			service.removeCollection(XmldbURI.xmldbUriFor(testColName));
			// col = (RemoteCollection) col.getChildCollection(colName);
			// System.out.println(col.getName());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	@Test public void ColCreateSameColTest() {
		String driver = "org.exist.xmldb.DatabaseImpl";
		Class cl;
		try {
			cl = Class.forName(driver);
			Database database = (Database) cl.newInstance();
			database.setProperty("creat-database", "true");
			DatabaseManager.registerDatabase(database);
			String textType = "css";
			RemoteCollection col = (RemoteCollection) DatabaseManager
					.getCollection(TestUtilities.uri, "admin", "admin");
			RemoteCollectionManagementService service = (RemoteCollectionManagementService) col
					.getService("CollectionManagementService", "1.0");

			String testColName = "/db/testCol/";
			//service.removeCollection(XmldbURI.xmldbUriFor(colName));
			//Collection child = service.createCollection(XmldbURI.xmldbUriFor(testColName));
			
			String childColName = testColName + "childCol";
			for (int i = 0; i < 3; i++)
				service.createCollection(XmldbURI.xmldbUriFor(childColName));
			
			String[] childs = col.getChildCollections();
			for (String child : childs)
				System.out.println(child);
			service.removeCollection(XmldbURI.xmldbUriFor(childColName));
			// col = (RemoteCollection) col.getChildCollection(colName);
			// System.out.println(col.getName());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}

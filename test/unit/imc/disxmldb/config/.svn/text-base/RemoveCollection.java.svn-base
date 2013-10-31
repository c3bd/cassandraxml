package imc.disxmldb.config;

import imc.disxmldb.util.TestUtilities;

import org.exist.xmldb.RemoteCollection;
import org.exist.xmldb.RemoteCollectionManagementService;
import org.exist.xmldb.XmldbURI;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Database;

public class RemoveCollection {
	public static void main(String[] args) {
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

			String[] childs = col.getChildCollections();
			System.out.println("child collection size:" + childs.length);
			for (String child : childs) {
				System.out.println("child collection: " + child);
			}
			// service.removeCollection(XmldbURI.xmldbUriFor(colName));
			service.removeCollection(XmldbURI.xmldbUriFor(Config.testRootCol));

			childs = col.getChildCollections();
			System.out.println("child collection size:" + childs.length);
			for (String child : childs) {
				System.out.println("child collection: " + child);
			}
			// col = (RemoteCollection) col.getChildCollection(colName);
			// System.out.println(col.getName());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}

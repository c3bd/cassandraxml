package imc.disxmldb.config;

import junit.framework.Assert;
import imc.disxmldb.util.TestUtilities;

import org.exist.xmldb.RemoteCollection;
import org.exist.xmldb.RemoteCollectionManagementService;
import org.exist.xmldb.XmldbURI;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;

public class CreateCollection {
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
				Assert.assertTrue(!child.equals(Config.testRootColName));
			}
			// service.removeCollection(XmldbURI.xmldbUriFor(colName));
			RemoteCollection testCol = (RemoteCollection) service.createCollection(XmldbURI
					.xmldbUriFor(Config.testRootCol));

			childs = col.getChildCollections();
			System.out.println("child collection of " + col.getName());
			for (String child : childs) {
				System.out.println("	child collection: " + child);
			}

			System.out.println("create collection " + Config.testRootCol
					+ " again");
			try {
				service.createCollection(XmldbURI
						.xmldbUriFor(Config.testRootCol));
			} catch (Exception ex) {
				System.out.println(ex);
			}

			
			childs = testCol.getChildCollections();
			System.out.println("child collection of " + testCol.getName());
			for (String child : childs) {
				System.out.println("	child collection: " + child);
			}
			service = (RemoteCollectionManagementService) testCol.getService(
					"CollectionManagementService", "1.0");
			for (int i = 0; i < 3; i++) {
				service.createCollection(XmldbURI
						.xmldbUriFor(Config.testRootCol + "/child" + i));
			}
			
			childs = testCol.getChildCollections();
			System.out.println("child collection of " + testCol.getName());
			for (String child : childs) {
				System.out.println("	child collection: " + child);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}
}

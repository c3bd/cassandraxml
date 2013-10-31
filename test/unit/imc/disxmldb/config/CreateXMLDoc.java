package imc.disxmldb.config;

import imc.disxmldb.util.TestUtilities;
import junit.framework.Assert;

import org.exist.xmldb.RemoteCollection;
import org.exist.xmldb.RemoteCollectionManagementService;
import org.exist.xmldb.RemoteXMLResource;
import org.exist.xmldb.XmldbURI;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;

public class CreateXMLDoc {
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
			// service.removeCollection(XmldbURI.xmldbUriFor(colName));

			RemoteCollection testCol = (RemoteCollection) service
					.createCollection(XmldbURI.xmldbUriFor(Config.testRootCol));

	/*		RemoteCollection testCol = (RemoteCollection) TestUtilities
					.getCollection(Config.testRootCol);*/
			System.out.println("created");
			Resource x = testCol.createResource(Config.testXMLDocName,
					"XMLResource");
			// ((RemoteXMLResource) x).setSchemaName("bf.xsd");
			((RemoteXMLResource) x).setSplitted((byte) 1);
			x.setContent(Config.testXMLDoc);
			testCol.storeResource(x);
			String[] docs = testCol.getResources();
			for (String doc : docs) {
				System.out.println(doc);
			}

			try {
				x = testCol
						.createResource(Config.testXMLDocName, "XMLResource");
				// ((RemoteXMLResource) x).setSchemaName("bf.xsd");
				((RemoteXMLResource) x).setSplitted((byte) 1);
				x.setContent(Config.testXMLDoc);
				testCol.storeResource(x);
			} catch (Exception ex) {
				System.out.println(ex);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

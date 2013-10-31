package imc.disxmldb.security;

import junit.framework.Assert;
import imc.disxmldb.util.TestUtilities;

import org.exist.xmldb.RemoteCollection;
import org.exist.xmldb.RemoteUserManagementService;
import org.junit.Test;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Database;

public class UserModTest {
	@Test
	public void addUser() {
		String uri_ = TestUtilities.uri;
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
			RemoteUserManagementService usermod = (RemoteUserManagementService) col.getService("UserManagementService", "1");
			User user = new User("testuser", "testpwd");
			user.addGroup("testgrp");
			usermod.addUser(user);
			
			User retUser = usermod.getUser(user.getName());
			Assert.assertEquals(user.getName(), retUser.getName());
			//Assert.assertEquals(user, retUser);
			System.out.println(retUser);
			
			col = (RemoteCollection) DatabaseManager
					.getCollection(uri_, user.getName(), user.getPassword());
		} catch (Exception ex) {
			ex.printStackTrace();
			Assert.assertTrue(false);
		}
	}
}

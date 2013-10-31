package imc.disxmldb.security;

import imc.disxmldb.XMLDBStore;
import imc.disxmldb.config.SysConfig;
import imc.disxmldb.config.XMLDBCatalogManager;
import imc.disxmldb.util.ColumnFamilyStoreProxy;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.cassandra.config.Config;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.RowMutation;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.Pair;

public class XMLDBSecurityManager {
	public static int DEFAULT_DIR_PERMISSION = 0750;
	public static int DEFAULT_FILE_PERMISSION = 0774;
	public static int FULL_PERMISSION = 0777;
	// security
	public static final User ADMINISTRATOR = new User("admin", "admin", "admin");
	public static final Permission DEFAULT_ROOTCOL_PERM = PermissionFactory
			.getPermission(ADMINISTRATOR.getName(),
					ADMINISTRATOR.getPrimaryGroup(),
					XMLDBSecurityManager.FULL_PERMISSION);

	public static int ACCESS_PERMISSION = 1;// for directory, it means user can
											// dive into this directory
	public static int WRITE_PERMISSION = 2;
	public static int READ_PERMISSION = 4; // for directory, it means user can
											// read the contents of this
											// directory
	private AbstractType keyValidator = UTF8Type.instance;
	private static final ByteBuffer grpUser = UTF8Type.instance
			.fromString("_grp");
	private static final ByteBuffer usrUser = UTF8Type.instance
			.fromString("_usr");

	private static XMLDBSecurityManager instance = new XMLDBSecurityManager();

	private ConcurrentHashMap<String, User> userCache = new ConcurrentHashMap<String, User>();
	private Timer timer = null;
	// persistence
	private ColumnFamilyStoreProxy store = new ColumnFamilyStoreProxy(
			SysConfig.SYSCATALOG, SysConfig.SYSCATALOG_USER);

	public static XMLDBSecurityManager getInstance() {
		return instance;
	}

	private XMLDBSecurityManager() {

	}

	public void init() {
		if (XMLDBStore.instance().isMutatable()) {
			timer = new Timer();
			timer.schedule(new RefreshMetaTask(), 10000, 1000);
		}
	}
	public User fetchUser(User user) {
		// read the cache to check if the user exists
		return fetchUser(user.getName());
	}

	public User fetchUser(String name) {
		User ret = userCache.get(name);
		if (ret == null) {
			Row row = store.get(keyValidator.fromString(name), null, null,
					XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL);
			if (row.cf == null && !name.equals(ADMINISTRATOR.getName())) {
				return null;
			} else if (name.equals(ADMINISTRATOR.getName())) {
				ret = ADMINISTRATOR;
			} else {
				ret = new User(name);
				UserSerializer.read(ret, row);
				//ret.read(row);
			}
			userCache.putIfAbsent(ret.getName(), ret);
		}

		return ret;
	}

	public boolean checkUser(User user) {
		boolean found = false;
		if (XMLDBStore.instance().isMutatable()) {
			found = userCache.containsKey(user.getName());
		}
		if (found)
			return found;
		else {
			Row row = store.get(usrUser, null,
					UTF8Type.instance.fromString(user.getName()),
					XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL);
			if (row.cf != null)
				return true;
			else
				return false;
		}
	}

	public boolean createUser(User user) {
		if (XMLDBStore.instance().isMutatable() == false)
			return false;
		userCache.put(user.getName(), user);
		RowMutation rm = new RowMutation(store.keyspace,
				keyValidator.fromString(user.getName()));
		//user.write(rm, store.cfName);
		UserSerializer.write(user, rm, store.cfName);
		return store.mutate(rm, XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL);
	}

	public void grpMod(User user, Vector<String> grps) {
		if (XMLDBStore.instance().isMutatable() == false)
			return;
		user.setGroups((String[]) grps.toArray());
		RowMutation rm = new RowMutation(store.keyspace,
				keyValidator.fromString(user.getName()));
		//user.write(rm, store.cfName);
		UserSerializer.write(user, rm, store.cfName);
		store.mutate(rm, XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL);
	}

	public boolean createGroup(String group) {
		if (XMLDBStore.instance().isMutatable() == false)
			return false;
		try {
			store.insert(grpUser, null, group,
					ByteBufferUtil.EMPTY_BYTE_BUFFER,
					XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean superUser(User user) {
		if (user.getName().equals(ADMINISTRATOR.getName()) || user.hasDbaRole())
			return true;
		return false;
	}
	
	public void resync() {
		userCache.clear();
	}
	
	public void onBecomeMaster() {
		resync();
		timer.cancel();
		timer = null;
	}
	
	public void onBecomeSlave() {
		timer = new Timer();
		timer.schedule(new RefreshMetaTask(), 10000, 1000);
	}
	
	private class RefreshMetaTask extends TimerTask {

		@Override
		public void run() {
			Random rand = new Random();
			Set<String> users = userCache.keySet();
			List<String> delUsers = new LinkedList<String>();
			for (String user : users) {
				if (rand.nextFloat() < 0.01f) {
					delUsers.add(user);
				}
			}

			for (String user : users)
				userCache.remove(user);
		}
	}
}

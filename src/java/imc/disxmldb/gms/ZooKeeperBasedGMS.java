/**
 * This class is used to manage the cluster membership. The whole cluster uses 
 * the zookeeper to elect the master node of this cluster. At least one node of the cluster should 
 * be configured as a master. Nodes that are configured as master mode will try to create a file that
 * contains the ip address of the master node. 
 * The znode created on zookeeper is an Ephemeral node, which means the znode will be deleted once the node lost
 * possession of it. Then all the nodes that subscribe this event will be notified by the zookeeper cluster. Those
 * nodes that are configured as master node will try to create the master file again.
 * @author xiafan xiafan68@gmail.com
 */
package imc.disxmldb.gms;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.cassandra.config.Config.XMLServerMode;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class ZooKeeperBasedGMS implements IGMSSubject, Watcher {
	public static final String MASTER_FILE_NAME = "master";
	private static Logger logger = LoggerFactory.getLogger(ZooKeeperBasedGMS.class);
	String[] serverList = null;
	String zookeeperDir = null;
	ZooKeeper zookeeper = null;
	CountDownLatch latch = null;
	int sessionTimeOut = 0;

	InetAddress localAddr = FBUtilities.getLocalAddress();
	private String masterFilePath = null;
	private List<IGMSObserver> observers = new LinkedList<IGMSObserver>();

	private static ZooKeeperBasedGMS instance = new ZooKeeperBasedGMS();

	public static ZooKeeperBasedGMS getInstance() {
		return instance;
	}

	private ZooKeeperBasedGMS() {
		if (DatabaseDescriptor.ZooKeeperEnabled()) {
			serverList = DatabaseDescriptor.getZooKeeperServerList();
			zookeeperDir = DatabaseDescriptor.getZooKeeperDir();
			sessionTimeOut = DatabaseDescriptor.getSessionTimeOut();
			masterFilePath = zookeeperDir + "/" + MASTER_FILE_NAME;
		}
	}

	public void init() {
		if (zookeeper == null && serverList != null) {
			try {
				latch = new CountDownLatch(1);
				StringBuffer serversBuf = new StringBuffer();
				for (String server : serverList) {
					serversBuf.append(server + ",");
				}
				
				zookeeper = new ZooKeeper(serversBuf.substring(0, serversBuf.length() - 1), sessionTimeOut, this);
				
				String[] paths = masterFilePath.split("/");
				String curPath = "";
				for (int i = 0; i < paths.length - 1; i++)
				{ 
					if (paths[i].length() == 0)
						continue;
					curPath += "/" + paths[i];
					Stat stat = zookeeper.exists(curPath, false);
					if (stat == null) {
						zookeeper.create(curPath, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					}
				}
				latch.await();
				latch = null;
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
		}
	}

	@Override
	public void register(IGMSObserver observer) {
		observers.add(observer);
	}

	@Override
	public void unRegister(IGMSObserver observer) {
		observers.remove(observer);
	}

	@Override
	public void GMSNotify() {
		// TODO Auto-generated method stub

	}

	@Override
	public void process(WatchedEvent event) {
		if (event.getState() == Event.KeeperState.SyncConnected
				&& latch != null) {
			latch.countDown();
		} else if (event.getType() == EventType.NodeDeleted) {
			if (event.getPath().equals(masterFilePath)) {
				/*
				 * It means previous master node lost control over the master
				 * file on the zookeeper cluster. this node should take part in
				 * the master elect process.
				 * here we only create the master file, it is determined wheter this node becomes the master node
				 * on the subsequence zookeeper event
				 */
				//createFile(masterFilePath);
				for (IGMSObserver observer : observers) {
					observer.onMasterExit();
				}
			}
		}

	}

	public void electAsMaster() {
		createFile(masterFilePath);
	}
	
	private boolean createFile(String path) {
		if (zookeeper == null)
			return false;
		Stat stat;
		try {
			stat = zookeeper.exists(path, this); //this line ensure this node will be notified any subsequent event
												 //if the master file has been created
			while (stat == null) {
				zookeeper.create(masterFilePath, localAddr.getAddress(),
						Ids.READ_ACL_UNSAFE, CreateMode.EPHEMERAL);
				stat = zookeeper.exists(path, this);
			}
			byte[] data = zookeeper.getData(masterFilePath, false, stat);
			InetAddress masterAddr = InetAddress.getByAddress(data);
			
			for (IGMSObserver observer: observers) {
				observer.onMasterNodeChange(masterAddr);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
	
		return true;
	}

}

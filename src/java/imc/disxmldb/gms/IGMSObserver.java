/**
 * @author xiafan xiafan68@gmail.com
 */
package imc.disxmldb.gms;

import java.net.InetAddress;

/**
 * This interface defines the events used to manage the cluster
 *
 */
public interface IGMSObserver {
	/**
	 * it means the master node lost the lock on the znode on the zookeeper, which means a new master
	 * node should be elected.
	 */
	public void onMasterExit();
	
	/**
	 * it means a new master node has been elected
	 * @param ip
	 * @param host
	 */
	public void onMasterNodeChange(InetAddress masterAddr);
	
	/**
	 * it means this node lost connection to the zookeeper cluster. If
	 * this node is the master node, it should stop providing service as an master as a new master may be elected.
	 */
	public void onZooKeeperLost();
}

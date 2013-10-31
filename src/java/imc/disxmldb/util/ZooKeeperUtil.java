package imc.disxmldb.util;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ZooKeeperUtil {
	public static void main(String[] args) {
		String server = args[0];
		String path = args[1];
		try {
			ZooKeeper zooKeeper = new ZooKeeper(server, 2000, null);
			Stat stat = new Stat();
			byte[] addr = zooKeeper.getData(path, false, stat);
			InetAddress iaddr = InetAddress.getByAddress(addr);
			System.out.println("the address is " + iaddr.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

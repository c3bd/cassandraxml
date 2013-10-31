package imc.disxmldb.index.btree;

import imc.disxmldb.index.Node;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.filter.IdentityXMLFilter;
import imc.disxmldb.index.filter.IXMLFilter;
import imc.disxmldb.util.CassandraSchemaProxy;
import imc.disxmldb.util.LocalCFStoreProxy;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.utils.LatencyTracker;

/**
 * This class implements the B+tree based on the super column family. The
 * posterlist is first ordered by the XML Doc ID and then by the ranges in
 * ascending order
 * 
 * @author xiafan
 * 
 */
public class CFBasedBtree implements IBtree, CFBasedBtreeMBean {
	private LatencyTracker putLatencyTracker = new LatencyTracker();
	private LatencyTracker retrievelLatencyTracker = new LatencyTracker();
	private LatencyTracker removeLatencyTracker = new LatencyTracker();

	private LocalCFStoreProxy localCFStoreProxy;
	private AbstractType keyValidator = null;

	public LocalCFStoreProxy getLocalCFStoreProxy() {
		return localCFStoreProxy;
	}

	public void setLocalCFStoreProxy(LocalCFStoreProxy localCFStoreProxy) {
		this.localCFStoreProxy = localCFStoreProxy;
	}

	public CFBasedBtree(LocalCFStoreProxy localStoreProxy) {
		this.localCFStoreProxy = localStoreProxy;
		keyValidator = localStoreProxy.getKeyValidator();

		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			mbs.registerMBean(this, new ObjectName(
					"imc.disxmldb.index:type=CFBasedBtree" + ",cfName="
							+ localStoreProxy.getCfStore().columnFamily));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void unRegister() {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			mbs.unregisterMBean(new ObjectName(
					"imc.disxmldb.index:type=CFBasedBtree" + ",cfName="
							+ localCFStoreProxy.getCfStore().columnFamily));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void put(String key, Node node) {
		long timestamp = System.currentTimeMillis() + 1;
		localCFStoreProxy.insert(
				keyValidator.fromString(key),
				Integer.toString(node.getXmlDocID()),
				Double.toString(node.getRange()[0]) + ":"
						+ Double.toString(node.getRange()[1]),
				compositeValue(node.getNodeID(), node.getLevel()), timestamp);
		putLatencyTracker.addMicro(System.currentTimeMillis() - timestamp + 1);
	}

	@Override
	public TreeMap<Integer, List<NodeUnit>> get(String key, IXMLFilter filter) {
		long startTime = System.currentTimeMillis();
		Row row = localCFStoreProxy.get(keyValidator.fromString(key), null,
				null);
		if (row.cf == null)
			return null;
		else {
			TreeMap<Integer, List<NodeUnit>> ret = new TreeMap<Integer, List<NodeUnit>>();
			for (IColumn col : row.cf.getSortedColumns()) {
				Integer xmlID = (Integer) Int32Type.instance
						.compose(col.name());
				List<NodeUnit> posterList = new ArrayList<NodeUnit>();
				for (IColumn subCol : col.getSubColumns()) {
					if (subCol.isMarkedForDelete())
						continue;
					ByteBuffer valueBuffer = subCol.value().duplicate();
					NodeUnit nodeUnit = new NodeUnit(valueBuffer.getInt(),
							byteBuffer2Range(subCol.name()),
							valueBuffer.getInt());
					posterList.add(nodeUnit);
				}
				ret.put(xmlID, posterList);
			}
			retrievelLatencyTracker.addMicro(System.currentTimeMillis()
					- startTime);
			return ret;
		}
	}

	@Override
	public TreeMap<Integer, List<NodeUnit>> getLesser(String key,
			IXMLFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TreeMap<Integer, List<NodeUnit>> getGreater(String key,
			IXMLFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TreeMap<Integer, List<NodeUnit>> get(String key) {
		return get(key, IdentityXMLFilter.instance);
	}

	@Override
	public TreeMap<Integer, List<NodeUnit>> getLesser(String key) {

		return null;
	}

	@Override
	public TreeMap<Integer, List<NodeUnit>> getGreater(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove(String key, Node node) {
		long startTime = System.currentTimeMillis();
		localCFStoreProxy.remove(
				key,
				Integer.toString(node.getXmlDocID()),
				Double.toString(node.getRange()[0]) + ":"
						+ Double.toString(node.getRange()[1]), null);
		removeLatencyTracker.addMicro(System.currentTimeMillis() - startTime);
	}

	@Override
	public void adjustCache(long cacheSize) {

	}

	@Override
	public void close() throws IOException {
		try {
			localCFStoreProxy.flush();
			unRegister();
		} catch (ExecutionException e) {
			throw new IOException(e);
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void flush() throws IOException {
		try {
			localCFStoreProxy.flush();
		} catch (ExecutionException e) {
			throw new IOException(e);
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void removeDataFiles() {
		try {
			localCFStoreProxy.getCfStore().removeAllSSTables();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ByteBuffer compositeValue(int nodeID, int level) {
		byte[] bytes = new byte[Integer.SIZE / 8 * 2];
		ByteBuffer ret = ByteBuffer.wrap(bytes);
		ret.putInt(nodeID);
		ret.putInt(level);
		ret.rewind();
		return ret;
	}

	private double[] byteBuffer2Range(ByteBuffer byteBuffer) {
		String compName = CassandraSchemaProxy.invIndexCFSubComparator
				.getString(byteBuffer);

		double lower = Double.parseDouble(compName.split(":")[0]);
		double upper = Double.parseDouble(compName.split(":")[1]);
		if (lower > upper) {
			double tmp = upper;
			upper = lower;
			lower = tmp;
		}
		double range[] = { lower, upper };
		return range;
	}

	@Override
	public double getAvgRetrievelLatency() {
		long n = retrievelLatencyTracker.getOpCount();
		long latency = retrievelLatencyTracker.getTotalLatencyMicros();
		if (n == 0) {
			return 0;
		}
		return (float) latency / (float) n;
	}

	@Override
	public double getRecentRetrievelLatency() {
		return retrievelLatencyTracker.getRecentLatencyMicros();
	}

	@Override
	public double getAvgRemoveLatency() {
		long n = removeLatencyTracker.getOpCount();
		long latency = removeLatencyTracker.getTotalLatencyMicros();
		if (n == 0) {
			return 0;
		}
		return (float) latency / (float) n;
	}

	@Override
	public double getRecentRemoveLatency() {
		return removeLatencyTracker.getRecentLatencyMicros();
	}

	@Override
	public double getAvgPutLatency() {
		long n = putLatencyTracker.getOpCount();
		long latency = putLatencyTracker.getTotalLatencyMicros();
		if (n == 0) {
			return 0;
		}
		return (float) latency / (float) n;
	}

	@Override
	public double getRecentPutLatency() {
		return putLatencyTracker.getRecentLatencyMicros();
	}

	@Override
	public long estimateKeyResultNum(String key, int replicas) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public TreeMap<Integer, List<NodeUnit>> getLesserOrEqual(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TreeMap<Integer, List<NodeUnit>> getLesserOrEqual(String key,
			IXMLFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TreeMap<Integer, List<NodeUnit>> getGreaterOrEqual(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TreeMap<Integer, List<NodeUnit>> getGreaterOrEqual(String key,
			IXMLFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean contains(String key, Node node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<NodeUnit> getParentNode(String key, Node node, int level,
			IXMLFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<NodeUnit> getChildNode(String stepName, Node node, int level,
			IXMLFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long estimateKeyResultNumB(String key, int replicas) {
		// TODO Auto-generated method stub
		return 0;
	}
}

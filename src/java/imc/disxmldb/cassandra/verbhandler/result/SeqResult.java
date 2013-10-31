package imc.disxmldb.cassandra.verbhandler.result;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.cassandra.verbhandler.XPathQueryCallback;
import imc.disxmldb.cassandra.verbhandler.XPathQueryCommand;
import imc.disxmldb.cassandra.verbhandler.XPathResult;
import imc.disxmldb.cassandra.verbhandler.XPathResult.XPathResultType;
import imc.disxmldb.config.SysConfig;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.cassandra.dht.AbstractBounds;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeqResult extends FunctionReturn {
	public static SeqResultSerializer serializer = new SeqResultSerializer();
	private static final Logger logger = LoggerFactory
			.getLogger(SeqResult.class);
	/*
	 * public int count = 0; private int cacheID = -1;
	 */
	private Pair<Integer, Integer> ret = new Pair<Integer, Integer>(-1, 0);
	private TreeMap<Integer, Pair<Integer, Integer>> counts = new TreeMap<Integer, Pair<Integer, Integer>>();
	private List<String> results = null;

	// following fields is not needed to be serialize
	private HashMap<Integer, Pair<Integer, Integer>> limits = new HashMap<Integer, Pair<Integer, Integer>>();

	public SeqResult() {
		funcType = XPathResultType.SeqStepOne;
	}

	@Override
	public void merge(int index_, FunctionReturn other_) {
		counts.put(index_, ((SeqResult) other_).ret);
	}

	@Override
	public List<String> result() {
		return results;
	}

	@Override
	public int size() {
		if (results != null)
			return results.size();
		else 
			return counts.size();
	}
	@Override
	public void setIndex(int index_) {
		counts.put(index_, ret);
	}

	public Pair<Integer, Integer> getRet() {
		return ret;
	}

	public void setRet(Pair<Integer, Integer> ret) {
		this.ret = ret;
	}

	@Override
	public ByteBuffer getDigest() {
		MessageDigest digester = FBUtilities.threadLocalMD5Digest();
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(aos);

		try {
			dos.writeInt(ret.left);
			dos.writeInt(ret.right);
			digester.digest(aos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ByteBuffer.wrap(digester.digest());
	}

	public static final String SEQ = "subsequence";
	public static final String SEQ1 = "subsequence1";
	public static final String SUBSEQSTEP2 = "subseq2";

	@Override
	public String postProcess(XPathQueryCommand cmd, byte[] cmdb,
			CollectionStore colStore, List<AbstractBounds> ranges) {
		/*
		 * the logic of this function: step 1 of subsequence: execute the xpath
		 * for the subsequence, cache the result on each node. the executors
		 * will return the cache id and count to the coordinator. step2 of
		 * subsequence: locate the nodes that contains the result falls in the
		 * range of the query. send an fetch command to all node. a limit of 0
		 * means expiring the cache on the node. step3 of subsequence: if some
		 * of the nodes containing results fails to return the result. we should
		 * send an subsequence command to the failure node, which will execute
		 * the xpath again and return the results.
		 */
		try {
			String wholeXpath = new String(cmdb, SysConfig.ENCODING).trim();
			results = new ArrayList<String>();
			int index = wholeXpath.lastIndexOf(SEQ + "(");
			if (index == -1)
				return String.format("%s is not found", SEQ);
			if (!wholeXpath.endsWith(");"))
				return String.format("close parantheses of %s is not found", SEQ);
			String argStr = wholeXpath.substring(SEQ.length()  + 1 + index,
					wholeXpath.length() - 2);
			String xpath = wholeXpath.substring(0, index);
			String[] args = argStr.split(",");
			int start = Integer.parseInt(args[0].trim());
			if (start < 0)
				start = 0;
			int limit = Integer.parseInt(args[1].trim());
			if (limit < 0)
				limit = 0;
			HashMap<Integer, XPathQueryCallback> callbacks = step2(cmd,
					colStore, ranges, start, limit);

			XPathResult ret = null;
			List<XPathQueryCallback> retryCallbacks = new LinkedList<XPathQueryCallback>();
			// we should handle node failes here
			for (Entry<Integer, XPathQueryCallback> entry : callbacks
					.entrySet()) {
				try {
					if (ret == null) {
						ret = entry.getValue().get();
						ret.index = entry.getKey();
						ret.cmdb = cmd.command;
						ret.cmd = cmd;
						ret.ranges = ranges;
						ret.colStore = colStore;
						if (judgeStep2Fails(ret)) {
							Pair<Integer, Integer> slPair = limits.get(entry
									.getKey());
							cmd.command = composeSeq1(xpath, slPair.left,
									slPair.right);
							retryCallbacks.add(colStore.ExecXPathOnNode(cmd,
									ranges.get(entry.getKey())));
						}
					} else {
						XPathResult tempRet = entry.getValue().get();
						if (judgeStep2Fails(tempRet)) {
							Pair<Integer, Integer> slPair = limits.get(entry
									.getKey());
							cmd.command = composeSeq1(xpath, slPair.left,
									slPair.right);
							retryCallbacks.add(colStore.ExecXPathOnNode(cmd,
									ranges.get(entry.getKey())));
						} else {
							tempRet.index = entry.getKey();
							ret.merge(tempRet.index, tempRet);
						}
					}
				} catch (Exception e) {
					return String.format("in get subsequence postProcess results: %s", e.getMessage());
				}
			}

			if (ret != null && ret.getFuncRet() != null) {
				ret.postProcess();
				results = ret.getFuncRet().result();
			}

			ret = null;
			for (XPathQueryCallback callback : retryCallbacks) {
				try {
					if (ret == null) {
						ret = callback.get();
					} else {
						ret.merge(0, callback.get());
						if (ret.getException() != null)
							return ret.getException();
					}
				} catch (Exception e) {
					logger.error("in get retry results: " + e.getMessage());
				}
			}
			if (ret != null && ret.getFuncRet() != null) {
				results.addAll(ret.getFuncRet().result());
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return String.format("in get subsequence postProcess results: %s", e.getMessage());
		}
		return null;
	}

	private boolean judgeStep2Fails(XPathResult result) {
		if (result.getFuncRet() == null)
			return true;
		else if (result.getFuncRet().result().size() == 0) {
			return true;
		}
		return false;
	}

	private HashMap<Integer, XPathQueryCallback> step2(XPathQueryCommand cmd,
			CollectionStore colStore, List<AbstractBounds> ranges, int start,
			int limit) throws UnsupportedEncodingException,
			UnavailableException {
		int i = -1;
		int count = 0;
		HashMap<Integer, XPathQueryCallback> callbacks = new HashMap<Integer, XPathQueryCallback>();
		for (Entry<Integer, Pair<Integer, Integer>> entry : counts.entrySet()) {
			i++;
			if (entry.getValue().right == 0)
				continue;
			int curLimit = 0;
			int curStart = -1;
			if (count <= start && start < count + entry.getValue().right) {
				curStart = start - count;
				if (count + entry.getValue().right - start >= limit) {
					// the range falls in the range of a node
					curLimit = limit;
					// we don't break now
				} else {
					// the range falls in multiple ranges
					curLimit = count + entry.getValue().right - start;
				}
				limit -= curLimit;
			} else if (start < count && count <= start + limit) {
				curStart = 0;
				if (count + entry.getValue().right <= start + limit) {
					// the query range is greater than the range of a node
					curLimit = entry.getValue().right;
				} else {
					//
					curLimit = start + limit - count + 1;
				}
				limit -= curLimit;
			} else if (start >= count + entry.getValue().right) {
				count += entry.getValue().right;
			} else {
				curLimit = 0;
			}
			cmd.command = composeStep2Cmd(entry.getValue().left, curStart, curLimit);

			if (curLimit != 0) {
				limits.put(i, new Pair<Integer, Integer>(curStart, curLimit));
				callbacks.put(
						i,
						colStore.ExecXPathOnNode(cmd,
								ranges.get(entry.getKey())));
			}
		}

		return callbacks;
	}

	private byte[] composeStep2Cmd(int cacheID, int start, int limit)
			throws UnsupportedEncodingException {
		return String.format("%s(%d,%d,%d);",SUBSEQSTEP2, cacheID, start, limit).getBytes(
				SysConfig.ENCODING);
	}

	private byte[] composeSeq1(String xpath, int start, int limit)
			throws UnsupportedEncodingException {
		return String.format("%s%s(%d,%d);", xpath, SEQ1, start, limit)
				.getBytes(SysConfig.ENCODING);
	}
}

class SeqResultSerializer extends FunctionReturnSerializer {

	@Override
	public void serialize(FunctionReturn t, DataOutputStream dos, int version)
			throws IOException {
		//dos.writeInt(t.funcType.ordinal());
		dos.writeInt(((SeqResult) t).getRet().left);
		dos.writeInt(((SeqResult) t).getRet().right);
	}

	@Override
	public SeqResult deserialize(DataInputStream dis, int version)
			throws IOException {
		SeqResult ret = new SeqResult();
		Pair<Integer, Integer> cache = new Pair<Integer, Integer>(
				dis.readInt(), dis.readInt());
		ret.setRet(cache);
		return ret;
	}
}

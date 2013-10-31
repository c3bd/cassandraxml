/*
 * author: zhangtl: OwenZhang1990@gmail.com
 */

package imc.disxmldb.index.invertindex;

import imc.disxmldb.dom.XMLNode;
import imc.disxmldb.dom.XMLNodeImpl;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.filter.IdentityXMLFilter;
import imc.disxmldb.index.filter.IXMLFilter;
import imc.disxmldb.util.CassandraSchemaProxy;
import imc.disxmldb.util.LocalCFStoreProxy;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.SuperColumn;
import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.LatencyTracker;

/**
 * This class implements the interface needed by an InvertIndex. It use 1-gram
 * and 2-gram to index the text of XML node. For English characters, sentences
 * are splitted into words. For Chinese characters, sentences are splitted into
 * characters. All punctuation are regarded as stop words and is replaced by an
 * space character. The underly store is based on sstable provided by Cassandra.
 * 
 */
public class CFInvertIndex implements IInvertIndex, CFInvertIndexMBean {
	private ICFRetrievalStrategy retrievalStrategy;
	private static final String STOPWORDS = "[\t\n\f\r/\\]";
	private LatencyTracker putLatency = new LatencyTracker();
	private LatencyTracker removeLatency = new LatencyTracker();
	private LatencyTracker retrievelLatency = new LatencyTracker();
	private LatencyTracker unitRetrievelLatency = new LatencyTracker();
	private LatencyTracker splitLatency = new LatencyTracker();

	// codes for splitting Chinese from Latin is from
	// http://stackoverflow.com/questions/1675739/to-split-only-chinese-characters-in-java
	private Set<Character.UnicodeBlock> chineseUnicodeBlocks = new HashSet<Character.UnicodeBlock>() {
		{
			add(Character.UnicodeBlock.CJK_COMPATIBILITY);
			add(Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS);
			add(Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS);
			add(Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT);
			add(Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT);
			add(Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION);
			add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
			add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
			add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B);
			add(Character.UnicodeBlock.KANGXI_RADICALS);
			add(Character.UnicodeBlock.IDEOGRAPHIC_DESCRIPTION_CHARACTERS);
		}
	};

	private LocalCFStoreProxy localCFStoreProxy;

	public CFInvertIndex(LocalCFStoreProxy localStoreProxy) {
		this.localCFStoreProxy = localStoreProxy;
		retrievalStrategy = new ParallelCFRetrieval(localStoreProxy);
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			mbs.registerMBean(this, new ObjectName(
					"imc.disxmldb.index:type=CFInvertIndex" + ",cfName="
							+ localStoreProxy.getCfStore().columnFamily));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void unRegister() {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			mbs.unregisterMBean(new ObjectName(
					"imc.disxmldb.index:type=CFInvertIndex" + ",cfName="
							+ localCFStoreProxy.getCfStore().columnFamily));
		} catch (Exception e) {
		}
	}

	@Override
	public TreeMap<Integer, List<NodeUnit>> get(String text, IXMLFilter filter) {
		long startTime = System.currentTimeMillis();
		Set<String> chineseSet = new TreeSet<String>();
		Set<String> latinSet = new TreeSet<String>();
		characterSplitting(text, chineseSet, latinSet, false);

		Set<String> set = new TreeSet<String>();
		if (!chineseSet.isEmpty())
			set.addAll(chineseSet);
		if (!latinSet.isEmpty())
			set.addAll(latinSet);

		SortedSet<ByteBuffer> xmlDocs = null;
		TreeMap<Integer, List<NodeUnit>> ret = get(set, xmlDocs, filter);
		retrievelLatency.addMicro(System.currentTimeMillis() - startTime);
		return ret;
	}

	@Override
	public TreeMap<Integer, List<NodeUnit>> get(String text) {
		long startTime = System.currentTimeMillis();
		Set<String> chineseSet = new TreeSet<String>();
		Set<String> latinSet = new TreeSet<String>();
		characterSplitting(text, chineseSet, latinSet, false);

		Set<String> set = new TreeSet<String>();
		if (!chineseSet.isEmpty())
			set.addAll(chineseSet);
		if (!latinSet.isEmpty())
			set.addAll(latinSet);

		SortedSet<ByteBuffer> xmlDocs = null;
		TreeMap<Integer, List<NodeUnit>> ret = get(set, xmlDocs);
		retrievelLatency.addMicro(System.currentTimeMillis() - startTime);
		return ret;
	}

	@Override
	public TreeMap<Integer, List<NodeUnit>> get(Set<String> set,
			SortedSet<ByteBuffer> xmlDocs, IXMLFilter filter) {
		return retrievalStrategy.retrieve(set, xmlDocs, filter);
		/*
		 * TreeMap<Integer, List<NodeUnit>> ret = new TreeMap<Integer,
		 * List<NodeUnit>>(); TreeMap<Integer, TreeSet<NodeUnit>> rtSet = null,
		 * tempMap = null;
		 * 
		 * Row row = null; //TODO: the following procedure should be
		 * parallelized to accelerate for (String word : set) { word =
		 * word.trim(); if (word.length() == 0) continue; if (xmlDocs == null) {
		 * row = localCFStoreProxy.get(UTF8Type.instance.fromString(word), null,
		 * null); } else if (xmlDocs.isEmpty()) { // no results return ret; }
		 * else { row =
		 * localCFStoreProxy.get(UTF8Type.instance.fromString(word), null,
		 * null); row = localCFStoreProxy.getSliceByNames(
		 * UTF8Type.instance.fromString(word), null, xmlDocs); }
		 * 
		 * if (row.cf == null) { // there is no posterlist that is related to
		 * word return ret; } else { SuperColumn spCol = null; int xmlDocID =
		 * -1; TreeSet<NodeUnit> posterList = null;
		 * 
		 * if (rtSet == null) { rtSet = new TreeMap<Integer,
		 * TreeSet<NodeUnit>>(); for (IColumn iCol : row.cf.getSortedColumns())
		 * { spCol = (SuperColumn) iCol; xmlDocID = Integer
		 * .parseInt(localCFStoreProxy.comparator .getString(spCol.name()));
		 * //filter first if (filter.filter(xmlDocID, null)) continue;
		 * 
		 * posterList = new TreeSet<NodeUnit>(NodeUnit.rangeFirst);
		 * rtSet.put(xmlDocID, posterList); for (IColumn iSubColumn :
		 * spCol.getSortedColumns()) { if (iSubColumn.isMarkedForDelete())
		 * continue; ByteBuffer valueBuffer = iSubColumn.value() .duplicate();
		 * NodeUnit nodeUnit = new NodeUnit( valueBuffer.getInt(),
		 * byteBuffer2Range(iSubColumn.name()), valueBuffer.getInt());
		 * posterList.add(nodeUnit); }// end of iterator over a unit of the
		 * posterlist }// end of iterator over the posterlist } else { tempMap =
		 * new TreeMap<Integer, TreeSet<NodeUnit>>(); for (IColumn iCol :
		 * row.cf.getSortedColumns()) { spCol = (SuperColumn) iCol; xmlDocID =
		 * Integer.parseInt(localCFStoreProxy.comparator
		 * .getString(spCol.name())); if (rtSet.containsKey(xmlDocID)) {
		 * posterList = rtSet.get(xmlDocID); } else { rtSet.remove(xmlDocID);
		 * continue; } TreeSet<NodeUnit> tempList = new
		 * TreeSet<NodeUnit>(NodeUnit.rangeFirst); for (IColumn iSubColumn :
		 * spCol.getSortedColumns()) { if (iSubColumn.isMarkedForDelete())
		 * continue; ByteBuffer valueBuffer = iSubColumn.value().duplicate();
		 * NodeUnit nodeUnit = new NodeUnit(valueBuffer.getInt(),
		 * byteBuffer2Range(iSubColumn.name()), valueBuffer.getInt()); if
		 * (posterList.contains(nodeUnit)) tempList.add(nodeUnit);
		 * //posterList.add(nodeUnit); }// end of iterator over a unit of the
		 * posterlist if (tempList.size() != 0) tempMap.put(xmlDocID, tempList);
		 * }// end of iterator over the posterlist rtSet = tempMap; } //end of
		 * if (rtSet == null) }// end of else of if (row.cf == null)
		 * 
		 * // generator the xmlDocs set for the next iteration xmlDocs = new
		 * TreeSet<ByteBuffer>(); for (Integer xmlDocID : rtSet.keySet()) {
		 * xmlDocs.add(Int32Type.instance.fromString(xmlDocID.toString())); } }
		 * 
		 * for (Entry<Integer, TreeSet<NodeUnit>> entry : rtSet.entrySet()) {
		 * ret.put(entry.getKey(), new LinkedList<NodeUnit>(entry.getValue()));
		 * } return ret;
		 */
	}

	@Override
	public TreeMap<Integer, List<NodeUnit>> get(Set<String> set,
			SortedSet<ByteBuffer> xmlDocs) {
		return get(set, xmlDocs, IdentityXMLFilter.instance);
	}

	private int byteBuffer2Int(ByteBuffer byteBuffer) {
		return ByteBufferUtil.toInt(byteBuffer);
	}

	private long byteBuffer2Long(ByteBuffer byteBuffer) {
		return ByteBufferUtil.toLong(byteBuffer);
	}

	public static double[] byteBuffer2Range(ByteBuffer byteBuffer) {
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
	public void put(int xmlDocID, XMLNode node) {
		put(node.getValue().toString(), xmlDocID, node.getId(),
				node.getRange(), node.getLevel());
	}

	@Override
	public void put(String text, int xmlDocID, int nodeID, double[] range,
			int level) {
		/*
		 * TODO a bug when put is invoked instantly after remove, the text will
		 * be regarded as deleted
		 */

		long putStart = System.currentTimeMillis();
		// split Chinese and Latin, build 2-gram string arrays respectively
		Set<String> chineseSet = new TreeSet<String>(); // 2-gram string arrays
		// for Chinese
		// characters
		Set<String> latinSet = new TreeSet<String>(); // 2-gram string arrays
		// for Latin words
		characterSplitting(text, chineseSet, latinSet, true);
		if (!chineseSet.isEmpty())
			put(chineseSet, xmlDocID, nodeID, range, level);
		if (!latinSet.isEmpty())
			put(latinSet, xmlDocID, nodeID, range, level);
		putLatency.addMicro(System.currentTimeMillis() - putStart);
	}

	private void put(Set<String> set, int xmlDocID, int nodeID, double[] range,
			int level) {
		for (Iterator<String> i = set.iterator(); i.hasNext();) {
			String key = i.next();
			long timestamp = System.currentTimeMillis() + 1;
			localCFStoreProxy
					.insert(key,
							Integer.toString(xmlDocID),
							Double.toString(range[0]) + ":"
									+ Double.toString(range[1]),
							compositeValue(nodeID, level), timestamp);
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

	private ByteBuffer int2ByteBuffer(int value) {
		return ByteBufferUtil.bytes(value);
	}

	private ByteBuffer string2ByteBuffer(String value) {
		return ByteBuffer.wrap(value.getBytes());
	}

	@Override
	public void remove(String text, int xmlDocID, int nodeID, double[] range) {
		long start = System.currentTimeMillis();
		Set<String> chineseSet = new TreeSet<String>();
		Set<String> latinSet = new TreeSet<String>();
		text = preprocess(text);
		characterSplitting(text, chineseSet, latinSet, true);
		remove(chineseSet, xmlDocID, nodeID, range);
		remove(latinSet, xmlDocID, nodeID, range);
		removeLatency.addMicro(System.currentTimeMillis() - start);
	}

	public void remove(Set<String> set, int xmlDocID, int nodeID, double[] range) {
		for (Iterator<String> i = set.iterator(); i.hasNext();) {
			String key = i.next();
			localCFStoreProxy
					.remove(key,
							Integer.toString(xmlDocID),
							Double.toString(range[0]) + ":"
									+ Double.toString(range[1]), null);
		}

	}

	public void characterSplitting(String text, Set<String> chineseSet,
			Set<String> latinSet, boolean getOneGram) {
		long splitStart = System.currentTimeMillis();
		StringBuffer chineseBuffer = new StringBuffer();
		StringBuffer latinBuffer = new StringBuffer();
		text = preprocess(text);
		int nc = 0; // keep the number of Chinese characters parsed;
		// we want to build a 2-gram string arrays
		boolean lastIsLatin = true;
		for (char c : text.toCharArray()) {
			if (isChinese(c)) {
				if (lastIsLatin)
					lastIsLatin = false;
				// 2-gram
				if (nc == 2) {
					nc = 1;
					chineseSet.add(chineseBuffer.toString().trim());
					chineseBuffer.delete(0, 1);
				}
				nc++;
				chineseBuffer.append(c);
				// 1-gram
				if (getOneGram)
					chineseSet.add(Character.toString(c));
			} else if (isLatin(c)) {// we need to parse out words
				// so build of a 2-gram Latin string arrays is deferred
				if (!lastIsLatin) {
					latinBuffer.append(' ');
					lastIsLatin = true;
				}
				latinBuffer.append(Character.toLowerCase(c));
			}

		}
		// push remaining characters into set
		if (chineseBuffer.length() > 0)
			chineseSet.add(chineseBuffer.toString().trim());

		// build a 2-gram Latin words string arrays
		List<String> tempLatinList = new LinkedList<String>();
		Pattern p = Pattern.compile("\\s+");
		for (String s : p.split(latinBuffer)) {
			if (!s.isEmpty())
				tempLatinList.add(s.trim());
		}

		int size = tempLatinList.size();
		for (int i = 0; i < size; i++) {
			String s1 = tempLatinList.get(i);

			// 2-gram
			if (size > 1 && i < size - 1) {
				String s2 = tempLatinList.get(i + 1);
				latinSet.add(s1 + " " + s2);
			}

			// 1-gram
			if (getOneGram || size == 1)
				latinSet.add(s1);
		}
		splitLatency.addMicro(System.currentTimeMillis() - splitStart);
	}

	/**
	 * 1. replace the punctuation and whitespace with space
	 * 
	 * @param text
	 * @return
	 */
	public String preprocess(String text) {
		String ret = text.replaceAll("\\pP", " ");
		ret = ret.replace(STOPWORDS, " ");
		return ret;
	}

	private boolean isChinese(char c) {
		return chineseUnicodeBlocks.contains(Character.UnicodeBlock.of(c));
	}

	private boolean isLatin(char c) {
		// return (c == ' ') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <=
		// 'Z');
		return Character.isLetterOrDigit(c) || Character.isWhitespace(c);
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

	public LocalCFStoreProxy getLocalCFStoreProxy() {
		return localCFStoreProxy;
	}

	public void setLocalCFStoreProxy(LocalCFStoreProxy localCFStoreProxy) {
		this.localCFStoreProxy = localCFStoreProxy;
	}

	@Override
	public void removeDataFiles() throws IOException {
		if (localCFStoreProxy != null) {
			localCFStoreProxy.getCfStore().removeAllSSTables();
		}
	}

	@Override
	public double getAvgPutLatency() {
		long n = putLatency.getOpCount();
		long latency = putLatency.getTotalLatencyMicros();
		if (n == 0) {
			return 0;
		}
		return (float) latency / (float) n;
	}

	@Override
	public double getAvgRemoveLatency() {
		long n = removeLatency.getOpCount();
		long latency = removeLatency.getTotalLatencyMicros();
		if (n == 0) {
			return 0;
		}
		return (float) latency / (float) n;
	}

	@Override
	public double getAvgRetrievelLatency() {
		long n = retrievelLatency.getOpCount();
		long latency = retrievelLatency.getTotalLatencyMicros();
		if (n == 0) {
			return 0;
		}
		return (float) latency / (float) n;
	}

	@Override
	public double getAvgUnitRetrievelLatency() {
		long n = unitRetrievelLatency.getOpCount();
		long latency = unitRetrievelLatency.getTotalLatencyMicros();
		if (n == 0) {
			return 0;
		}
		return (float) latency / (float) n;
	}

	@Override
	public double getAvgSplitLatency() {
		long n = splitLatency.getOpCount();
		long latency = splitLatency.getTotalLatencyMicros();
		if (n == 0) {
			return 0;
		}
		return (float) latency / (float) n;
	}

}
/**
 *@author:xiafan xiafan68@gmail.com
 *@version: 2011-10-27 0.1
 */
package imc.disxmldb;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import junit.framework.Assert;

import imc.disxmldb.dom.typesystem.ValueType;
import imc.disxmldb.index.Node;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.btree.ComposeKeyBtree;
import imc.disxmldb.index.btree.CacheBtree;
import imc.disxmldb.index.btree.IBtree;
import imc.disxmldb.index.btree.LazyCacheBtree;
import imc.disxmldb.index.filter.IdentityXMLFilter;
import imc.disxmldb.xpath.XPathProcessorV2;

import org.apache.cassandra.io.util.FileUtils;
import org.junit.Test;

import com.sleepycat.je.DatabaseEntry;

public class BtreeTest {
	private static final String btreeFile = "/Users/xiafan/btreeTest";

	@Test
	public void loadBtree() throws IOException {
		int count = 100;
		LazyCacheBtree btree = new LazyCacheBtree(
				"C:\\imcxml\\cassandradata\\data\\xmldb\\btree_4_STRING",
				512 * 1024, ValueType.STRING);
		TreeMap<Integer, List<NodeUnit>> ret = btree.get("20120604000001");

		DatabaseEntry entry = btree.composeKey("20120604000001", -1);
		ret = btree.get(entry, IdentityXMLFilter.instance);
		System.out.println(ret);
		Assert.assertEquals(ret.size(), 1);
		btree.close();
	}

	@Test
	public void testStringBtree() throws IOException {
		int count = 100;
		IBtree btree = new LazyCacheBtree(btreeFile + "_String", 512 * 1024,
				ValueType.STRING);
		Random rand = new Random();
		/*
		 * for (int i = 0; i < count; i++) { for (int b = 0; b < 10; b++)
		 * btree.put(i + "", new Node(rand.nextInt(), rand.nextInt(), new
		 * double[] { rand.nextDouble(), rand.nextDouble() }, 0)); }
		 */
		btree.put("1", new Node(2, 0, new double[] { 0, 100 }, 0));
		btree.put("1", new Node(2, 1, new double[] { 200, 300 }, 0));
		btree.put("2", new Node(2, 2, new double[] { 400, 500 }, 0));
		btree.put("1", new Node(5, 0, new double[] { 0, 100 }, 0));
		btree.put("2", new Node(5, 3, new double[] { 400, 500 }, 0));
		TreeMap<Integer, List<NodeUnit>> ret = btree.get("1");
		Assert.assertEquals(XPathProcessorV2.calcPosterListSize(ret), 3);
		System.out.println(btree.estimateKeyResultNum("1", 1));
		ret = btree.get("2");
		Assert.assertEquals(XPathProcessorV2.calcPosterListSize(ret), 2);

		System.out.println(ret);
		btree.close();
		File file = new File(btreeFile + "_String");
		try {
			FileUtils.deleteRecursive(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testFileSizeBtree() {
		int count = 100;
		ComposeKeyBtree btree = new ComposeKeyBtree(btreeFile + "_FileSize",
				512 * 1024, ValueType.FILESIZE);
		Random rand = new Random();
		for (int i = 0; i < count; i++) {
			for (int b = 0; b < 10; b++)
				btree.put(i + "", new Node(rand.nextInt(), rand.nextInt(),
						new double[] { rand.nextDouble(), rand.nextDouble() },
						0));
		}
		for (int i = 0; i < count; i++)
			System.out.println(btree.estimateKeyResultNum(i + "", 1));

		TreeMap<Integer, List<NodeUnit>> ret = btree.getLesser("0");
		assert ret.isEmpty();
		ret = btree.getLesserOrEqual("0");
		assert ret.isEmpty() == false;
		ret = btree.getLesser("1");

		System.out.println(ret);
		btree.close();

		// test reopen the tree
		btree = new ComposeKeyBtree(btreeFile + "_FileSize", 512 * 1024,
				ValueType.FILESIZE);
		btree.close();

		File file = new File(btreeFile + "_FileSize");
		try {
			FileUtils.deleteRecursive(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testCacheBtree() throws IOException {
		int count = 100002;
		IBtree btree = new CacheBtree(btreeFile, 512 * 1024, ValueType.STRING);

		Random rand = new Random();
		for (int i = 0; i < count; i++) {
			btree.put(3000 + "", new Node(rand.nextInt(), rand.nextInt(),
					new double[] { rand.nextDouble(), rand.nextDouble() }, 0));
		}

		System.out.println("put " + count + " nodes latency:"
				+ ((CacheBtree) btree).getAvgPutLatency());
		System.out.println("put " + count + " nodes latency:"
				+ ((CacheBtree) btree).getRecentPutLatency());
		for (int i = 0; i < 10; i++)
			btree.get("3000");
		System.out.println("get latency in microseconds:"
				+ ((CacheBtree) btree).getRecentRetrievelLatency());

		btree.close();
		File file = new File(btreeFile);
		try {
			FileUtils.deleteRecursive(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testCountPerformance() throws IOException {
		IBtree btree = new LazyCacheBtree(btreeFile + "_count", 512 * 1024,
				ValueType.STRING);
		Random rand = new Random();
		// generate data
		int count = 10;
		int keySize = 8;
		for (int i = 0; i < keySize; i++) {
			System.out.println("load key:");
			for (int b = 0; b < count; b++) {
				btree.put(i + "", new Node(rand.nextInt(), rand.nextInt(),
						new double[] { rand.nextDouble(), rand.nextDouble() },
						0));
			}
			count += 500;
		}

		for (int i = 0; i < keySize; i++) {
			long start = System.currentTimeMillis();
			System.out.println(btree.estimateKeyResultNumB(i + "", 1));
			System.out.println("invoke count cost: "
					+ (System.currentTimeMillis() - start));
		}
		
		for (int i = 0; i < keySize; i++) {
			long start = System.currentTimeMillis();
			System.out.println(btree.estimateKeyResultNum(i + "", 1));
			System.out.println("invoke estimate count cost: "
					+ (System.currentTimeMillis() - start));
		}
		
		System.gc();
		
		System.out.println("after garbage collection");
		for (int i = 0; i < keySize; i++) {
			long start = System.currentTimeMillis();
			System.out.println(btree.estimateKeyResultNumB(i + "", 1));
			System.out.println("invoke count cost: "
					+ (System.currentTimeMillis() - start));
		}
		
		for (int i = 0; i < keySize; i++) {
			long start = System.currentTimeMillis();
			System.out.println(btree.estimateKeyResultNum(i + "", 1));
			System.out.println("invoke estimate count cost: "
					+ (System.currentTimeMillis() - start));
		}
		btree.close();
		File file = new File(btreeFile + "_count");
		try {
			FileUtils.deleteRecursive(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

/**
 * @author xiafan xiafan68@gmail.com
 */
package imc.disxmldb.index.invertindex;

import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.filter.IXMLFilter;
import imc.disxmldb.util.LocalCFStoreProxy;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.SuperColumn;
import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.db.marshal.UTF8Type;

/**
 * This class implements a parallel retrieval algorithm that uses multiple
 * threads to retrieve poster lists of multiple keys in parallel.
 */
public class ParallelCFRetrieval implements ICFRetrievalStrategy {
	private LocalCFStoreProxy localCFStoreProxy = null;
	private ExecutorService retrievalService = Executors.newCachedThreadPool();

	public ParallelCFRetrieval(LocalCFStoreProxy proxy) {
		this.localCFStoreProxy = proxy;
	}

	private class RetrievalTask implements Callable {
		private String term = null;
		SortedSet<ByteBuffer> xmlDocs = null;

		public RetrievalTask(String term, SortedSet<ByteBuffer> xmlDocs) {
			this.term = term;
			this.xmlDocs = xmlDocs;
		}

		@Override
		public Object call() throws Exception {
			Row row = null;
			if (xmlDocs != null)
				row = localCFStoreProxy.getSliceByNames(
						UTF8Type.instance.fromString(term), null, xmlDocs);
			else
				row = localCFStoreProxy.get(UTF8Type.instance.fromString(term),
						null, null);
			return row;
		}

	}

	@Override
	public TreeMap<Integer, List<NodeUnit>> retrieve(Set<String> set,
			SortedSet<ByteBuffer> xmlDocs, IXMLFilter filter) {
		TreeMap<Integer, List<NodeUnit>> ret = new TreeMap<Integer, List<NodeUnit>>();
		TreeMap<Integer, TreeSet<NodeUnit>> rtSet = null, tempMap = null;

		List<Future<Row>> retrievalFuture = new LinkedList<Future<Row>>();
		List<Row> retrievalResult = new LinkedList<Row>();

		// TODO 讲所有的word都用多线程池进行查找，但是这样就无法用已检索到的结果去过滤之后的查询
		int retrievalCount = 5;
		while (!retrievalResult.isEmpty() || !retrievalFuture.isEmpty()
				|| !set.isEmpty()) {
			int count = 0;
			for (Iterator<String> termIter = set.iterator(); termIter.hasNext()
					&& count < retrievalCount;) {
				String word = termIter.next();
				termIter.remove();
				retrievalFuture.add(retrievalService.submit(new RetrievalTask(
						word, xmlDocs)));
			}

			// 冲线程池中获取执行结果
			for (Iterator<Future<Row>> iter = retrievalFuture.iterator(); iter
					.hasNext();) {
				Future<Row> future = iter.next();
				try {
					retrievalResult
							.add(future.get(1000, TimeUnit.MILLISECONDS));
					iter.remove();
				} catch (Exception ex) {

				}
			}

			int joinSize = retrievalResult.size();
			// 对查询结果执行集合交集操作
			xmlDocs = new TreeSet<ByteBuffer>();
			for (Row row : retrievalResult) {
				if (row.cf == null) {
					// there is no posterlist that is related to word
					return ret;
				} else {
					SuperColumn spCol = null;
					int xmlDocID = -1;
					TreeSet<NodeUnit> posterList = null;

					if (rtSet == null) {
						rtSet = new TreeMap<Integer, TreeSet<NodeUnit>>();
						for (IColumn iCol : row.cf.getSortedColumns()) {
							spCol = (SuperColumn) iCol;
							xmlDocID = Integer
									.parseInt(localCFStoreProxy.comparator
											.getString(spCol.name()));
							// filter first
							if (filter.filter(xmlDocID, null))
								continue;

							posterList = new TreeSet<NodeUnit>(
									NodeUnit.rangeFirst);
							rtSet.put(xmlDocID, posterList);
							for (IColumn iSubColumn : spCol.getSortedColumns()) {
								if (iSubColumn.isMarkedForDelete())
									continue;
								ByteBuffer valueBuffer = iSubColumn.value()
										.duplicate();
								NodeUnit nodeUnit = new NodeUnit(
										valueBuffer.getInt(),
										CFInvertIndex
												.byteBuffer2Range(iSubColumn
														.name()),
										valueBuffer.getInt());
								posterList.add(nodeUnit);
							}// end of iterator over a unit of the posterlist
							if (posterList.isEmpty()) {
								rtSet.remove(xmlDocID);
							} else {
								xmlDocs.add(spCol.name().duplicate());
							}
						}// end of iterator over the posterlist
					} else {
						tempMap = new TreeMap<Integer, TreeSet<NodeUnit>>();
						for (IColumn iCol : row.cf.getSortedColumns()) {
							spCol = (SuperColumn) iCol;
							xmlDocID = Integer
									.parseInt(localCFStoreProxy.comparator
											.getString(spCol.name()));
							if (rtSet.containsKey(xmlDocID)) {
								posterList = rtSet.get(xmlDocID);
							} else {
								rtSet.remove(xmlDocID);
								continue;
							}
							TreeSet<NodeUnit> tempList = new TreeSet<NodeUnit>(
									NodeUnit.rangeFirst);
							for (IColumn iSubColumn : spCol.getSortedColumns()) {
								if (iSubColumn.isMarkedForDelete())
									continue;
								ByteBuffer valueBuffer = iSubColumn.value()
										.duplicate();
								NodeUnit nodeUnit = new NodeUnit(
										valueBuffer.getInt(),
										CFInvertIndex
												.byteBuffer2Range(iSubColumn
														.name()),
										valueBuffer.getInt());
								if (posterList.contains(nodeUnit))
									tempList.add(nodeUnit);
								// posterList.add(nodeUnit);
							}// end of iterator over a unit of the posterlist
							if (tempList.size() != 0) {
								tempMap.put(xmlDocID, tempList);
								xmlDocs.add(spCol.name().duplicate());
							} else {
								tempMap.remove(xmlDocID);
							}
						}// end of iterator over the posterlist
						rtSet = tempMap;
					} // end of if (rtSet == null)
				}// end of else of if (row.cf == null)
			}// end of retrievalResult loop
			retrievalResult.clear();

			int notFetchedCount = retrievalFuture.size();
			// 冲线程池中获取执行结果
			for (Iterator<Future<Row>> iter = retrievalFuture.iterator(); iter
					.hasNext();) {
				Future<Row> future = iter.next();
				try {
					retrievalResult
							.add(future.get(1000, TimeUnit.MILLISECONDS));
					iter.remove();
				} catch (Exception ex) {

				}
			}

			retrievalCount = ((notFetchedCount - retrievalFuture.size()) / joinSize)
					* retrievalFuture.size() + 1;
		}

		for (Entry<Integer, TreeSet<NodeUnit>> entry : rtSet.entrySet()) {
			ret.put(entry.getKey(), new LinkedList<NodeUnit>(entry.getValue()));
		}
		return ret;
	}

}

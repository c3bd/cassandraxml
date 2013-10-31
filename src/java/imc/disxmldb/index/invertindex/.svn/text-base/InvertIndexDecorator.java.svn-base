package imc.disxmldb.index.invertindex;

import imc.disxmldb.index.btree.IndexQueryResult;
import imc.disxmldb.index.filter.IXMLFilter;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.cassandra.utils.Pair;

public class InvertIndexDecorator {
	private List<Pair<Integer, IInvertIndex>> indexList = null;
	
	public InvertIndexDecorator() 
	{
		indexList = new LinkedList<Pair<Integer, IInvertIndex>>();
	}
	
	public InvertIndexDecorator(List<Pair<Integer, IInvertIndex>> indexList) {
		this.indexList = indexList;
	}
	
	public void addInvertIndex(Integer colID, IInvertIndex index) {
		indexList.add(new Pair<Integer, IInvertIndex>(colID, index));
	}
	
	public IndexQueryResult get(String text) {
		IndexQueryResult result = new IndexQueryResult();
		for (Pair<Integer, IInvertIndex> entry : indexList) {
			result.addResult(entry.left, entry.right.get(text));
		}
		return result;
	}
	
	public IndexQueryResult get(Set<String> set,
			SortedSet<ByteBuffer> xmlDocs, IXMLFilter filter) {
		IndexQueryResult result = new IndexQueryResult();
		for (Pair<Integer, IInvertIndex> entry : indexList) {
			result.addResult(entry.left, entry.right.get(set, xmlDocs, filter));
		}
		return result;
	}
	/**
	 * 
	 * @param set
	 * @param xmlDocs
	 * 			if xmlDocs is not null, it will be used to filter out the posterlist with the set
	 * @return
	 */
	public IndexQueryResult get(Set<String> set, SortedSet<ByteBuffer> xmlDocs) {
		IndexQueryResult result = new IndexQueryResult();
		for (Pair<Integer, IInvertIndex> entry : indexList) {
			result.addResult(entry.left, entry.right.get(set, xmlDocs));
		}
		return result;
	}
	
	public IndexQueryResult get(String text, IXMLFilter filter) {
		IndexQueryResult result = new IndexQueryResult();
		for (Pair<Integer, IInvertIndex> entry : indexList) {
			result.addResult(entry.left, entry.right.get(text, filter));
		}
		return result;
	}
}

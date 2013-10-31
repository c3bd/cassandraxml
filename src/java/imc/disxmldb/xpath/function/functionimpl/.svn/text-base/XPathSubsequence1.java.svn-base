package imc.disxmldb.xpath.function.functionimpl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.xmldb.api.base.XMLDBException;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.XMLDBStore;
import imc.disxmldb.cassandra.verbhandler.XPathResult;
import imc.disxmldb.cassandra.verbhandler.XPathResult.XPathResultType;
import imc.disxmldb.cassandra.verbhandler.result.TextResult;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.btree.IndexQueryResult;
import imc.disxmldb.xpath.ExecContext;
import imc.disxmldb.xpath.XPathProcessorV2;
import imc.disxmldb.xpath.XPathSequence;
import imc.disxmldb.xpath.function.IXPathFunction;

public class XPathSubsequence1 implements IXPathFunction {

	@Override
	public void invoke(ExecContext context) {

		ExecContext limitContext = context.args.pop();
		ExecContext startContext = context.args.peek();
		int start = Integer.parseInt((String) startContext.getLastStep()
				.getStepValue());
		int limit = Integer.parseInt((String) limitContext.getLastStep()
				.getStepValue());
		// maybe we should execute the tempContext recursive
		XPathProcessorV2.backwardForwardExecSteps(context.parentContext);
		XPathSequence lastStep = context.parentContext.getLastStep();
		IndexQueryResult result = null;
		if (lastStep != null && lastStep.isValueSeted())
			result = (IndexQueryResult) lastStep.getStepValue();

		List<String> texts = XPathSubsequence1
				.subsequence(result, start, limit);

		TextResult ret = new TextResult();
		ret.texts = texts;
		XPathResult xpRet = new XPathResult(XPathResultType.FunctionReturn);
		xpRet.setFuncRet(ret);
		context.addResult(xpRet);
	}

	public static List<String> subsequence(IndexQueryResult result, int start,
			int limit) {
		int count = 0;
		boolean enough = false;
		List<String> texts = new LinkedList<String>();
		if (limit <= 0)
			return texts;
		
		if (result != null) {
			for (Iterator<Integer> colIDIter = result.colIterator(); colIDIter
					.hasNext();) {
				int colID = colIDIter.next();
				TreeMap<Integer, List<NodeUnit>> posterList = result
						.getColResult(colID);
				CollectionStore colStore = null;
				try {
					colStore = XMLDBStore.instance().getCollection(colID);
				} catch (XMLDBException e) {
				}
				if (colStore == null)
					continue;

				for (Entry<Integer, List<NodeUnit>> entry : posterList
						.entrySet()) {
					int XMLDocID = entry.getKey();
					List<NodeUnit> ln = entry.getValue();
					if (count < (start + limit)) {
						if ((count + ln.size()) < start) {
							count = count + ln.size();
						} else {
							for (NodeUnit nodeID : ln) {
								if (count < start) {
									count++;
								} else if (count < (start + limit)) {
									texts.add(colStore.retrieveLocal(XMLDocID,
											nodeID.getNodeID()));
									count++;
								} else {
									enough = true;
									break;
								}
							}
						}
					}
				}
				if (enough)
					break;
			}
		}
		return texts;
	}
}

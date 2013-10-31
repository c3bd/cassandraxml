package imc.disxmldb.xpath.function.functionimpl;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import imc.disxmldb.cassandra.verbhandler.XPathResult;
import imc.disxmldb.cassandra.verbhandler.XPathResult.XPathResultType;
import imc.disxmldb.cassandra.verbhandler.result.CountResult;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.btree.IndexQueryResult;
import imc.disxmldb.xpath.ExecContext;
import imc.disxmldb.xpath.XPathProcessorV2;
import imc.disxmldb.xpath.XPathSequence;
import imc.disxmldb.xpath.function.IXPathFunction;

/**
 * implementation of count function in XPath that will be executed locally. The
 * results will be aggregated on the coordinator nodes.
 */
public class XPathCount implements IXPathFunction {

	@Override
	public void invoke(ExecContext context) {
		IndexQueryResult result = null;

		if (context.args.empty()) {
			XPathProcessorV2.backwardForwardExecSteps(context.parentContext);
			XPathSequence lastStep = context.parentContext.getLastStep();
			if (lastStep != null && lastStep.isValueSeted())
				result = (IndexQueryResult) lastStep.getStepValue();
		} else {
			ExecContext tempContext = (ExecContext) context.args.pop();

			// maybe we should execute the tempContext recursive
			XPathProcessorV2.backwardForwardExecSteps(tempContext);
			XPathSequence lastStep = tempContext.getLastStep();
			if (lastStep != null && lastStep.isValueSeted())
				result = (IndexQueryResult) lastStep.getStepValue();
		}

		int count = 0;

		if (result == null || result.size() == 0) {
			//return 0
		} else {
			for (Iterator<Integer> colIter = result.colIterator(); colIter
					.hasNext();) {
				TreeMap<Integer, List<NodeUnit>> docNodes = result
						.getColResult(colIter.next());
				for (List<NodeUnit> entry : docNodes.values()) {
					count += entry.size();
				}
			}
		}

		CountResult countRet = new CountResult();
		countRet.count = count;
		XPathResult xpRet = new XPathResult(XPathResultType.FunctionReturn);
		xpRet.setFuncRet(countRet);
		context.addResult(xpRet);
	}
}

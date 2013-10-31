package imc.disxmldb.xpath.function.functionimpl;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.xmldb.api.base.XMLDBException;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.XMLDBStore;
import imc.disxmldb.cassandra.verbhandler.XPathResult;
import imc.disxmldb.cassandra.verbhandler.XPathResult.XPathResultType;
import imc.disxmldb.cassandra.verbhandler.result.MinResult;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.btree.IndexQueryResult;
import imc.disxmldb.xpath.ExecContext;
import imc.disxmldb.xpath.XPathProcessorV2;
import imc.disxmldb.xpath.XPathSequence;
import imc.disxmldb.xpath.function.IXPathFunction;

/**
 * implementation of min function in XPath that will be executed locally. The
 * results will be aggregated on the coordinator nodes.
 */
public class XPathMin implements IXPathFunction {

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

		double min = Double.MAX_VALUE;
		if (result == null || result.size() == 0) {
		} else {
			try {
				for (Iterator<Integer> colIter = result.colIterator(); colIter
						.hasNext();) {
					int colID = colIter.next();
					CollectionStore tempStore = null;
					try {
						tempStore = XMLDBStore.instance().getCollection(colID);
					} catch (XMLDBException e) {
						continue;
					}
					if (tempStore == null)
						continue;
					TreeMap<Integer, List<NodeUnit>> docNodes = result
							.getColResult(colID);
					for (Iterator<Integer> iterator = docNodes.keySet()
							.iterator(); iterator.hasNext();) {
						Integer xmlDocID = iterator.next();
						List<NodeUnit> nodes = docNodes.get(xmlDocID);
						for (NodeUnit node : nodes) {
							double temp = Double.parseDouble(tempStore
									.textLocal(xmlDocID.intValue(),
											node.getNodeID()));
							min = min < temp ? min : temp;
						}
					}
				}
			} catch (Exception ex) {
				context.setError(context.curToken, ex.toString());
			}
		}
		MinResult ret = new MinResult();
		ret.min = min;
		XPathResult xpRet = new XPathResult(XPathResultType.FunctionReturn);
		xpRet.setFuncRet(ret);
		context.addResult(xpRet);

	}
}

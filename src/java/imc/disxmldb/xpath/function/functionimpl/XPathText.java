package imc.disxmldb.xpath.function.functionimpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

/**
 * implementation of text function in XPath that will be executed locally.
 */
public class XPathText implements IXPathFunction {

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
			XPathSequence lastStep = tempContext.parentContext.getLastStep();
			if (lastStep != null && lastStep.isValueSeted())
				result = (IndexQueryResult) lastStep.getStepValue();
		}

		List<String> texts = new ArrayList<String>();
		if (result != null) {
			for (Iterator<Integer> colIter = result.colIterator(); colIter
					.hasNext();) {
				int colID = colIter.next();
				Map<Integer, List<NodeUnit>> docNodes = result
						.getColResult(colID);
				CollectionStore colStore = null;
				try {
					colStore = XMLDBStore.instance().getCollection(colID);
				} catch (XMLDBException e) {
				}
				if (colStore == null)
					continue;
				for (Iterator<Integer> iterator = docNodes.keySet().iterator(); iterator
						.hasNext();) {
					Integer xmlDocID = iterator.next();
					List<NodeUnit> nodes = docNodes.get(xmlDocID);
					for (NodeUnit node : nodes) {
						String text = colStore.textLocal(xmlDocID.intValue(),
								node.getNodeID());
						if (text != null)
							texts.add(text);
					}
				}
			}
		}
		TextResult ret = new TextResult();
		ret.texts = texts;
		XPathResult xpRet = new XPathResult(XPathResultType.FunctionReturn);
		xpRet.setFuncRet(ret);
		context.addResult(xpRet);
	}
}

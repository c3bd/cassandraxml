package imc.disxmldb.xpath.function.functionimpl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.xmldb.api.base.XMLDBException;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.XMLDBStore;
import imc.disxmldb.dom.typesystem.ValueType;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.btree.BtreeDecorator;
import imc.disxmldb.index.btree.IndexQueryResult;
import imc.disxmldb.index.filter.IXMLFilter;
import imc.disxmldb.index.filter.RootNodeFilter;
import imc.disxmldb.xpath.ExecContext;
import imc.disxmldb.xpath.XPathProcessorV2;
import imc.disxmldb.xpath.XPathSequence;
import imc.disxmldb.xpath.function.IXPathFunction;

/**
 * implementation of contain function in XPath that will be executed locally.
 * The results will be aggregated on the coordinator nodes.
 */
public class XPathContainFunc implements IXPathFunction {

	/**
	 * the first args is TreeMap, the second arg is the text string. this
	 * function returns the nodes in the first argument that contain the text
	 * 
	 * @param args
	 * @param context
	 * 
	 */
	@Override
	public void invoke(ExecContext context) {
		assert context.args.size() == 2;

		ExecContext rhsContext = context.args.pop();
		ExecContext lhsContext = context.args.pop();
		String text = rhsContext.getLastStep().getStepName();

		// XPathProcessorV2.execSteps(lhsContext);
		XPathSequence parentSeq = null;
		if (lhsContext.isStepEmpty()) {
			context.setNoResults();
			return;
		} else {
			parentSeq = lhsContext.pollLastStep();
		}
		IndexQueryResult posterList = context.colStore.getInvertIndex(
				context.recursive).get(text, context.filter);

		IndexQueryResult ret = contain(context, parentSeq, posterList);
		if (ret.size() == 0)
			context.setNoResults();
		else {
			parentSeq.setStep(ret);
			lhsContext.addSequence(parentSeq);
			XPathProcessorV2.execSteps(lhsContext);
			if (lhsContext.noResults())
				context.setNoResults();
			else
				context.addSequence(lhsContext.getFirstStep());
		}
	}

	public static IndexQueryResult contain(ExecContext context,
			XPathSequence parentSeq, IndexQueryResult posterList) {
		return XPathProcessorV2.EVJoin(parentSeq, posterList, context);
	}
}

package imc.disxmldb.xpath.function.functionimpl;

import imc.disxmldb.dom.typesystem.ValueType;
import imc.disxmldb.index.btree.IndexQueryResult;
import imc.disxmldb.xpath.ExecContext;
import imc.disxmldb.xpath.XPathProcessorV2;
import imc.disxmldb.xpath.XPathSequence;
import imc.disxmldb.xpath.function.IXPathFunction;
/**
 * implementation of less than function of String type in XPath that will be executed locally.
 */
public class XPathElementsLTString implements IXPathFunction {

	@Override
	public void invoke(ExecContext context) {
		ExecContext rhs = context.args.pop();
		ExecContext lhs = context.args.pop();
		XPathSequence parentSeq = lhs.pollLastStep();
		
		IndexQueryResult rhsNodes = context.colStore
				.getBtreeIndex(ValueType.STRING, context.recursive).getLesser(
						(String) rhs.getLastStep().getStepValue(), context.filter);
		IndexQueryResult joinRet = XPathProcessorV2.EVJoin(parentSeq, rhsNodes, context);
		if (joinRet.size() == 0)
			context.setNoResults();
		else {
			parentSeq.setStep(joinRet);
			context.addSequence(parentSeq);
		}
	}

}

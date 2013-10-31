package imc.disxmldb.xpath.function.functionimpl;

import java.nio.ByteBuffer;

import imc.disxmldb.dom.typesystem.TypeResolver;
import imc.disxmldb.dom.typesystem.ValueType;
import imc.disxmldb.index.btree.IndexQueryResult;
import imc.disxmldb.xpath.ExecContext;
import imc.disxmldb.xpath.XPathProcessorV2;
import imc.disxmldb.xpath.XPathSequence;
import imc.disxmldb.xpath.function.IXPathFunction;

public class XPathContainsFileSize implements IXPathFunction {

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

		ValueType type = TypeResolver.resolve(text);
		String preText = text;
		ByteBuffer compBuffer = ValueType.getValidator(type)
				.fromString(text);
		text = (String) ValueType.getValidator(type).compose(compBuffer);

		IndexQueryResult rhsNodes = context.colStore.getBtreeIndex(
				ValueType.FILESIZE, context.recursive).get(text,
				context.filter);
		IndexQueryResult joinRet = XPathProcessorV2.EVJoin(parentSeq, rhsNodes,
				context);
		parentSeq.setStep(null);
		
		IndexQueryResult posterList = context.colStore.getInvertIndex(
				context.recursive).get(preText, context.filter);

		IndexQueryResult ret = XPathContainFunc.contain(context, parentSeq, posterList);
		joinRet.merge(ret);
		
		if (joinRet.size() == 0)
			context.setNoResults();
		else {
			parentSeq.setStep(joinRet);
			lhsContext.addSequence(parentSeq);
			XPathProcessorV2.execSteps(lhsContext);
			if (lhsContext.noResults())
				context.setNoResults();
			else
				context.addSequence(lhsContext.getFirstStep());
		}
	}

}

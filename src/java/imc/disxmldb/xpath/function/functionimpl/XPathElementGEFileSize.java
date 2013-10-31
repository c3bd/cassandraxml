package imc.disxmldb.xpath.function.functionimpl;

import java.nio.ByteBuffer;

import imc.disxmldb.dom.typesystem.TypeResolver;
import imc.disxmldb.dom.typesystem.ValueType;
import imc.disxmldb.index.btree.IndexQueryResult;
import imc.disxmldb.xpath.ExecContext;
import imc.disxmldb.xpath.XPathProcessorV2;
import imc.disxmldb.xpath.XPathSequence;
import imc.disxmldb.xpath.function.IXPathFunction;

public class XPathElementGEFileSize implements IXPathFunction {

	@Override
	public void invoke(ExecContext context) {
		ExecContext rhs = context.args.pop();
		ExecContext lhs = context.args.pop();
		XPathSequence parentSeq = lhs.pollLastStep();

		String compStr = (String) rhs.getLastStep().getStepValue();
		ValueType type = TypeResolver.resolve(compStr);
		ByteBuffer compBuffer = ValueType.getValidator(type)
				.fromString(compStr);
		compStr = (String) ValueType.getValidator(type).compose(compBuffer);

		IndexQueryResult rhsNodes = context.colStore.getBtreeIndex(
				ValueType.FILESIZE, context.recursive).getGreaterOrEqual(
				compStr, context.filter);
		IndexQueryResult joinRet = XPathProcessorV2.EVJoin(parentSeq, rhsNodes,
				context);
		if (joinRet.size() == 0)
			context.setNoResults();
		else {
			parentSeq.setStep(joinRet);
			context.addSequence(parentSeq);
		}

	}

}

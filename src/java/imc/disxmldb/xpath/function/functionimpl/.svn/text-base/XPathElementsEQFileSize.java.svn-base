package imc.disxmldb.xpath.function.functionimpl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.TreeMap;

import imc.disxmldb.dom.XMLNode;
import imc.disxmldb.dom.typesystem.TypeResolver;
import imc.disxmldb.dom.typesystem.ValueType;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.btree.IndexQueryResult;
import imc.disxmldb.xpath.ExecContext;
import imc.disxmldb.xpath.XPathProcessorV2;
import imc.disxmldb.xpath.XPathSequence;
import imc.disxmldb.xpath.function.IXPathFunction;

/**
 * implementation of equal function of FileSize type in XPath that will be
 * executed locally.
 */
public class XPathElementsEQFileSize implements IXPathFunction {

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
				ValueType.FILESIZE, context.recursive).get(compStr,
				context.filter);
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

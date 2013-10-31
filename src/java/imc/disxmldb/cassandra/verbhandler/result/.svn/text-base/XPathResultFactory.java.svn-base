package imc.disxmldb.cassandra.verbhandler.result;

import imc.disxmldb.cassandra.verbhandler.XPathResult.XPathResultType;
import imc.disxmldb.util.IXPathResult;

public class XPathResultFactory {
	public static IXPathResult getResult(XPathResultType type_) {
		if (type_ == XPathResultType.Average) {
			return new AvgResult();
		} else if (type_ == XPathResultType.Max) {
			return new MaxResult();
		} else if (type_ == XPathResultType.Min) {
			return new MinResult();
		} else if (type_ == XPathResultType.Count) {
			return new CountResult();
		} else if (type_ == XPathResultType.Sum) {
			return new SumResult();
		} else if (type_ == XPathResultType.Text
				|| type_ == XPathResultType.SeqStepTwo) {
			return new TextResult();
		} else if (type_ == XPathResultType.SeqStepOne) {
			return new SeqResult();
		} else if (type_ == XPathResultType.xmlParts) {
			return new XMLDocXMLParts();
		} else if (type_ == XPathResultType.NodeIDs) {
			return new XMLDocNodeIDs();
		} else if (type_ == XPathResultType.NodeSketch) {
			return new XMLNodeForMutations();
		} else {
			return null;
			//throw new RuntimeException(type_.toString() + " is not supported");
		}
	}
}

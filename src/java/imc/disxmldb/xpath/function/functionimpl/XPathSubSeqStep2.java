package imc.disxmldb.xpath.function.functionimpl;

import java.util.List;

import imc.disxmldb.cassandra.verbhandler.XPathResult;
import imc.disxmldb.cassandra.verbhandler.XPathResult.XPathResultType;
import imc.disxmldb.cassandra.verbhandler.result.TextResult;
import imc.disxmldb.dom.cache.SubSequenceCache;
import imc.disxmldb.dom.cache.CacheManager.CacheType;
import imc.disxmldb.index.btree.IndexQueryResult;
import imc.disxmldb.xpath.ExecContext;
import imc.disxmldb.xpath.function.IXPathFunction;

public class XPathSubSeqStep2 implements IXPathFunction {

	@Override
	public void invoke(ExecContext context) {
		// cacheid, start, limit
		ExecContext limitContext = context.args.pop();
		ExecContext startContext = context.args.pop();
		ExecContext cacheIDContext = context.args.pop();
		int start = Integer.parseInt((String) startContext.getLastStep()
				.getStepValue());
		int limit = Integer.parseInt((String) limitContext.getLastStep()
				.getStepValue());
		int cacheID = Integer.parseInt((String) cacheIDContext.getLastStep()
				.getStepValue());
		
		SubSequenceCache cache = (SubSequenceCache) context.colStore.getCache(CacheType.SUBSEQ);
		IndexQueryResult result = null;
		if (cache == null) {
			throw new RuntimeException("subsequence cache is not found");
		} else {
			result = cache.get(cacheID);			
			cache.remove(cacheID);
		}
		
		List<String> texts = XPathSubsequence1.subsequence(result, start, limit);
		TextResult ret = new TextResult();
		ret.texts = texts;
		XPathResult xpRet = new XPathResult(XPathResultType.FunctionReturn);
		xpRet.setFuncRet(ret);
		context.addResult(xpRet);
	}
}

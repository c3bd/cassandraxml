package imc.disxmldb.xpath.function.functionimpl;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.apache.cassandra.utils.Pair;

import imc.disxmldb.cassandra.verbhandler.XPathResult;
import imc.disxmldb.cassandra.verbhandler.XPathResult.XPathResultType;
import imc.disxmldb.cassandra.verbhandler.result.CountResult;
import imc.disxmldb.cassandra.verbhandler.result.SeqResult;
import imc.disxmldb.dom.cache.CacheManager.CacheType;
import imc.disxmldb.dom.cache.SubSequenceCache;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.btree.IndexQueryResult;
import imc.disxmldb.xpath.ExecContext;
import imc.disxmldb.xpath.XPathProcessorV2;
import imc.disxmldb.xpath.XPathSequence;
import imc.disxmldb.xpath.function.IXPathFunction;

/**
 * the procedure of subsequence: 1. the first invocation is used to calculate
 * the count of results on each node 2. then the coordinator node will locate
 * the nodes that contain the results and send an subsequence_i(start, limit) to
 * those nodes
 * 
 * @author xiafan
 * 
 */
public class XPathSubsequence implements IXPathFunction {

	@Override
	public void invoke(ExecContext context) {
		IndexQueryResult result = null;
		XPathProcessorV2.backwardForwardExecSteps(context.parentContext);
		XPathSequence lastStep = context.parentContext.getLastStep();
		int count = 0;
		int cacheID = -1;
		if (lastStep == null) {
			count = 0;
		} else {
			if (context.parentContext.noResults() == false)
				result = (IndexQueryResult) context.parentContext.getLastStep()
						.getStepValue();

			if (result == null || result.size() == 0) {
				context.setNoResults();
			} else {
				count = result.size();
				SubSequenceCache cache = (SubSequenceCache) context.colStore
						.getCache(CacheType.SUBSEQ);
				cacheID = cache.nextCacheID();
				cache.put(cacheID, result);
			}
			result = null;
		}

		SeqResult countRet = new SeqResult();
		Pair<Integer, Integer> cache = new Pair<Integer, Integer>(cacheID,
				count);
		countRet.setRet(cache);
		XPathResult xpRet = new XPathResult(XPathResultType.FunctionReturn);
		xpRet.setFuncRet(countRet);
		context.addResult(xpRet);
	}
}

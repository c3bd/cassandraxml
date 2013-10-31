/**
 * @author:xiafan68 {xiafan68@gmail.com}
 * @time: 2012-01-05
 */
package imc.disxmldb.xpath;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.cassandra.verbhandler.XPathResult;
import imc.disxmldb.index.filter.IXMLFilter;
import imc.disxmldb.xpath.function.FunctionDef;
import imc.disxmldb.xpath.xpathtoken.Token;
import imc.disxmldb.xpath.xpathtoken.XPathTokenParser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * The class is used to record the state information during the process of XPath
 * execution. The information will be used to control the process of XPath
 * execution.
 * 
 * @author xiafan
 * 
 */
public class ExecContext {
	public ExecContext parentContext = null;
	public CollectionStore colStore = null;

	// when the xpath is informal, the following two fields will record the
	// error message
	public String errorMsg = null;
	public int line = -1;
	public int column = -1;
	public Token curToken = null;
	public XPathTokenParser tokenParser;

	public IXMLFilter filter = null;
	// public Stack<XPathSequence> sequences = new Stack<XPathSequence>();
	private ArrayList<XPathSequence> sequences = new ArrayList<XPathSequence>();

	public boolean isLastLevelStep = false;
	public int lastLevel = -1;
	public boolean isRoot = false;

	public FunctionDef funcDef = null;
	public Stack<ExecContext> args = new Stack<ExecContext>(); // the stack
																// should only
																// contain
	private XPathResult result = null; // used to stored function return value

	private boolean shouldStop = false;

	// recursively query the child of the collection
	public boolean recursive = true;

	public ExecContext() {
	}

	public ExecContext createChildContext() {
		ExecContext ret = new ExecContext();
		ret.parentContext = getNonEmptyStepAncestor();
		ret.colStore = colStore;
		ret.tokenParser = tokenParser;
		ret.filter = filter;
		ret.recursive = recursive;
		//ret.isRoot = isRoot;
		return ret;
	}

	private ExecContext getNonEmptyStepAncestor() {
		ExecContext ret = this;
		while (ret != null && ret.isStepEmpty())
			ret = ret.parentContext;

		if (ret != null && ret.isStepEmpty())
			return null;
		else
			return ret;
	}

	public void setNoResults() {
		sequences.clear();
		result = null;
		shouldStop = true;
	}

	public boolean noResults() {
		return shouldStop;
		/*
		 * if (errorMsg != null) return true; else if (result == null &&
		 * sequences.isEmpty() && isLastLevelStep == false) return true; return
		 * false;
		 */
	}

	public void setError(int line, int column, String errorMsg) {
		this.line = line;
		this.column = column;
		this.errorMsg = errorMsg;
		shouldStop = true;
	}

	public void setError(Token curToken, String msg) {
		if (curToken == null) {
			this.line = -1;
			this.column = -1;
		} else {
			this.line = curToken.beginLine;
			this.column = curToken.beginColumn;
		}
		shouldStop = true;
		errorMsg = msg;
	}

	public boolean hasError() {
		return errorMsg != null;
	}

	public String getError() {
		return String.format("line:%d; column: %d; message: %s", line, column,
				errorMsg);
	}

	public boolean isRootContext() {
		return parentContext == null;
	}

	public void addResult(XPathResult result) {
		this.shouldStop = false;
		this.result = result;
	}

	/**
	 * if the result of the ExecContext should be joined with the parent step,
	 * it should be stored in the sequences as an XPathSequence.
	 * 
	 * @return
	 */
	public Object getResult() {
		if (result != null)
			return result;
		if (sequences.isEmpty() == false)
			return sequences.get(sequences.size() - 1);
		return null;
	}

	public boolean isLastLevelStep() {
		return isLastLevelStep;
	}

	public void setLastLevelStep(boolean isLastLevelStep) {
		this.isLastLevelStep = isLastLevelStep;
	}

	public void setLastLevel(int level) {
		if (isRoot && level != 1)
			isRoot = false;
		lastLevel = level;
	}

	public int getLastLevel() {
		isLastLevelStep = false;
		return lastLevel;
	}

	public void addSequence(String step, int kind) {
		if (lastLevel == -1)
			lastLevel = XPathSequence.ASCIENT_DESCIENT_LEVEL;
		addSequence(step, kind, lastLevel);
	}

	/**
	 * this function will atomaticly merge the level with the lastlevel, it is
	 * useful when merge the return of a child context i.e
	 * /step1/step2[./test//contains(./step3, "test")]
	 * 
	 * @param step
	 * @param level
	 */
	public void addSequence(String step, int kind, int level) {
		int finalLevel = XPathProcessorV2.mergeLevel(lastLevel, level);
		XPathSequence sequence = new XPathSequence(step, kind, finalLevel);
		if (isRoot && finalLevel == 1) {
			sequence.setRoot(true);
		}
		isRoot = false;
		addSequence(sequence);
	}

	public void addSequence(XPathSequence sequence) {
		isLastLevelStep = false;
		lastLevel = -1;
		sequences.add(sequence);
	}

	public void addAsFirstSequence(XPathSequence sequence) {
		sequences.add(0, sequence);
	}

	public boolean isStepEmpty() {
		return sequences.isEmpty();
	}

	public XPathSequence pollLastStep() {
		XPathSequence ret = null;
		if (!sequences.isEmpty()) {
			ret = sequences.remove(sequences.size() - 1);
		}
		return ret;
	}

	public XPathSequence getLastStep() {
		if (!sequences.isEmpty()) {
			return sequences.get(sequences.size() - 1);
		} else
			return null;
	}

	public XPathSequence pollFirstStep() {
		XPathSequence ret = null;
		if (!sequences.isEmpty()) {
			ret = sequences.remove(0);
		}
		return ret;
	}

	public XPathSequence getFirstStep() {
		if (!sequences.isEmpty()) {
			return sequences.get(0);
		} else
			return null;
	}

	public FunctionDef getFuncDef() {
		return funcDef;
	}

	public void setFuncDef(FunctionDef funcDef) {
		this.funcDef = funcDef;
	}

	public boolean isResultSet() {
		return result != null;
	}

	public void setIsRoot(Boolean isRoot_) {
		isRoot = isRoot_;
	}
}

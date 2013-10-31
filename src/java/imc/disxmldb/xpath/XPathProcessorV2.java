/**
 *@author:ccy_chengcheng
 *@version: 2011-9-27 0.1
 *modify by: xiafan{xiafan68@gmail.com}
 *@author xiafan
 * @version: 2012-01-03 2.0
 */
package imc.disxmldb.xpath;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.XMLDBStore;
import imc.disxmldb.cassandra.verbhandler.XPathQueryCommand;
import imc.disxmldb.cassandra.verbhandler.XPathResult;
import imc.disxmldb.config.SysConfig;
import imc.disxmldb.dom.typesystem.TypeResolver;
import imc.disxmldb.dom.typesystem.ValueType;
import imc.disxmldb.index.ComparatorByRangeFirst;
import imc.disxmldb.index.Node;
import imc.disxmldb.index.NodeUnit;
import imc.disxmldb.index.btree.BtreeDecorator;
import imc.disxmldb.index.btree.IndexQueryResult;
import imc.disxmldb.index.filter.IXMLFilter;
import imc.disxmldb.index.filter.RootNodeFilter;
import imc.disxmldb.index.filter.XMLLocalDocFilter;
import imc.disxmldb.xpath.function.FunctionDef;
import imc.disxmldb.xpath.function.FunctionPool;
import imc.disxmldb.xpath.xpathtoken.Token;
import imc.disxmldb.xpath.xpathtoken.XPathTokenParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.utils.LatencyTracker;
import org.xmldb.api.base.XMLDBException;

/**
 * The class implements the semantic parser of XPath. It also executes the XPath
 * during the parsing. Those three core joining algorithms is also implemented
 * here.
 * 
 */
public class XPathProcessorV2 implements XPathProcessorV2MBean {
	public static LatencyTracker xpathExecLatency = new LatencyTracker();
	public static LatencyTracker eeJoinExecLatency = new LatencyTracker();
	public static LatencyTracker evJoinExecLatency = new LatencyTracker();
	public static LatencyTracker loadDataLatency = new LatencyTracker();
	static {
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName nameObj = new ObjectName(
					"imc.disxmldb.xpath:type=XPathProcessorV2");
			mbs.registerMBean(new XPathProcessorV2(), nameObj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static class XPathError {
		public static String UNKNOW_ERROR = "unknow error";
		public static String BRAKET_NOT_MATCH = "maybe the close braket is missing";
		public static String PARAN_NOT_MATCH = "maybe the close paranthesis is missing";
		public static String PARENT_NOT_TAGNAME = "this clause should be an element name";
		public static String BEFORE_ATTR_SLASH = "only \"/\" should be before the attribute step";
		public static String BRAKET_AFTER_SLASH = "\"[\" should not appear directly after \"/\"";
		public static String NO_STEP_BEFORE_PREDICATE = "there is no steps appearing before []";

		public static String NOT_IMPLEMENTED = "currently not implemented";
	}

	public static Object execXPath(XPathQueryCommand command,
			CollectionStore colStore) {
		long startTime = System.currentTimeMillis();
		try {
			IPartitioner partitioner = colStore.getCfStore().getCfStore().partitioner;
			IXMLFilter filter = new XMLLocalDocFilter(
					colStore.getColKeyValidator(), partitioner, command.range);
			ExecContext context = new ExecContext();
			// TODO create token parse may take a lot of time
			context.tokenParser = getXPathTokenParser(command.command);
			context.colStore = colStore;
			context.filter = filter;
			context.recursive = command.isRecursive();
			context.isRoot = true;
			parseXPath(context);
			if (context.hasError())
				return null;
			Object ret = context.getResult();
			if (ret instanceof XPathSequence)
				return ((XPathSequence) ret).getStepValue();
			else
				return ret;
		} finally {
			xpathExecLatency.addMicro(System.currentTimeMillis() - startTime);
		}
	}

	public static XPathTokenParser getXPathTokenParser(byte[] xpath) {
		InputStream input = new ByteArrayInputStream(xpath);
		XPathTokenParser ret = new XPathTokenParser(input, SysConfig.ENCODING);
		return ret;
	}

	public static void backwardForwardExecSteps(ExecContext context) {
		if (context.isStepEmpty()) {
			context.setNoResults();
			return;
		}
		execSteps(context);

		if (context.parentContext != null) {
			backwardForwardExecSteps(context.parentContext);
			if (context.parentContext.noResults()) {
				context.setNoResults();
				return;
			} else {
				// join the last step of the parent with the current node
				XPathSequence pSeq = context.parentContext.pollLastStep();
				XPathSequence cSeq = context.pollFirstStep();
				List<IndexQueryResult> joinRet = null;

				if (cSeq.isAttributeStep()) {
					joinRet = EAJoin(pSeq, cSeq, context);
				} else {
					int level = -1;
					if (context.parentContext.isLastLevelStep()) {
						level = context.parentContext.getLastLevel();
					}
					level = mergeLevel(level, cSeq.getLevel());
					cSeq.setLevel(level);

					joinRet = EEJoin(pSeq, cSeq, level, context);
				}
				if (joinRet == null || joinRet.size() == 0) {
					context.setNoResults();
					context.parentContext.setNoResults();
				} else {
					pSeq.setStep(joinRet.get(0));
					cSeq.setStep(joinRet.get(1));
					context.parentContext.addAsFirstSequence(pSeq);
					context.addAsFirstSequence(cSeq);
				}
			}
		}
	}

	/**
	 * execute all the joins of the steps in a backward manner
	 * 
	 * @param context
	 */
	public static void execSteps(ExecContext context) {
		// TODO 修改执行流程，改为从后向前扫描
		// 如果找到了一个被过滤过的节点，那么从该节点开始向前执行join操作。执行完毕之后从该节点开始向后执行join操作。
		// 如果没有找到这样的一个节点，那么正常的从后向前执行
		if (context.noResults())
			return;
		XPathSequence parent = null, child = null;
		List<IndexQueryResult> joinRet = null;
		XPathSequence curStep = null;
		Stack<XPathSequence> stepStack = new Stack<XPathSequence>();

		// 将xpath从后向前扫描，知道遇到一个已经过滤过的节点
		while (!context.isStepEmpty()) {
			curStep = context.pollLastStep();
			if (curStep.isValueSeted())
				break;
			else {
				stepStack.push(curStep);
			}
		}

		// 如果curStep==null,那么说明xpah step为空
		if (curStep == null) {
			return;
		} else {
			child = curStep;
			//TODO 这里需要进一步考虑一下
			//!!!还有一种情况是没有一个过滤过的节点,所有节点都入栈了，这时child等于栈顶。
			//还有一种情况是原先就只有一个节点，而它有没有进栈
			if (!stepStack.isEmpty() && stepStack.peek() == curStep) {
				stepStack.pop();
			}
		}
		
		//对xpath按照从后向前的方式执行
		while (!context.isStepEmpty()) {
			curStep = context.pollLastStep();
			if (child == null) {
				child = curStep;
				continue;
			} else {
				parent = curStep;
				if (parent.isAttributeStep()) {
					context.setError(context.curToken.beginLine,
							context.curToken.beginColumn,
							XPathError.PARENT_NOT_TAGNAME);
					return;
				}
			}

			if (child.isAttributeStep() && child.getLevel() != 1) {
				context.setError(context.curToken, XPathError.BEFORE_ATTR_SLASH);
				return;
			}
			joinRet = EEJoin(parent, child, child.getLevel(), context);
			child.setIsAttr(ValueType.NODETAG);
			if (joinRet == null || joinRet.size() == 0) {
				context.setNoResults();
				return;
			}
			//child.setStepName(parent.getStepName());
			child.setLevel(parent.getLevel() + child.getLevel());
			child.setStep(joinRet.get(1));
		} // end of execute the joins of the steps


		//开始向后执行join操作，这时不需要设置子step的level
		parent = child;
		while (!stepStack.isEmpty()) {
			child = stepStack.pop();
			if (child.isAttributeStep() && child.getLevel() != 1) {
				context.setError(context.curToken, XPathError.BEFORE_ATTR_SLASH);
				return;
			}
			joinRet = EEJoin(parent, child, child.getLevel(), context);

			if (joinRet == null || joinRet.size() == 0) {
				context.setNoResults();
				return;
			}
			child.setStepName(parent.getStepName());
			child.setLevel(parent.getLevel() + child.getLevel());
			child.setStep(joinRet.get(1));
			parent = child;
		}

		if (child != null) {
			Object step = child.getStepValue();
			if (child.isValueSeted() == false) {
				IXMLFilter filter = context.filter;
				if (child.isRoot())
					filter = new RootNodeFilter(context.filter);
				step = context.colStore.getBtreeIndex(child.getSequenceType(),
						context.recursive).get((String) step, filter);
				child.setStep((IndexQueryResult) step);
			}
			context.addSequence(child);
		} else {
			if (!context.noResults())
				context.setNoResults();
		}
	}

	@Deprecated
	private static boolean parseComparisonFunction(ExecContext context) {
		ExecContext eqFunc = context.createChildContext();
		eqFunc.funcDef = new FunctionDef(context.curToken.image.toUpperCase());
		eqFunc.funcDef.addArgType(ValueType.ELEMENTS);
		ExecContext rhs = eqFunc.createChildContext();
		context.curToken = context.tokenParser.getNextToken();
		if (context.curToken.kind == XPathTokenParser.EOF) {
			context.setError(context.curToken, XPathError.UNKNOW_ERROR);
			return false;
		} else {
			rhs.addSequence(context.curToken.image, context.curToken.kind);
			ValueType rhsType = ValueType.UNKNOW;
			if (context.curToken.kind == XPathTokenParser.StringLiteral) {
				rhsType = TypeResolver.resolve((String) rhs.getLastStep()
						.getStepName());
				if (ValueType.isFileSizeType(rhsType))
					rhsType = ValueType.FILESIZE;
			} else if (context.curToken.kind == XPathTokenParser.IntegerLiteral) {
				// rhsType = ValueType.INT;
				rhsType = ValueType.STRING;
			} else if (context.curToken.kind == XPathTokenParser.DecimalLiteral) {
				rhsType = ValueType.DOUBLE;
			} else if (context.curToken.kind == XPathTokenParser.DoubleLiteral) {
				rhsType = ValueType.DOUBLE;
			}

			eqFunc.funcDef.addArgType(rhsType);
			eqFunc.args.push(context);
			eqFunc.args.push(rhs);
			FunctionDef funcDef = FunctionPool.getFuncImpl(eqFunc.funcDef);
			if (funcDef == null || funcDef.getFuncImpl() == null) {
				context.setError(context.curToken, eqFunc.funcDef.signature()
						+ " is not implemented");
				return false;
			} else {
				funcDef.getFuncImpl().invoke(eqFunc);
				mergeFunction(context, eqFunc);
			}
		}
		return true;
	}
	@Deprecated
	private static boolean commonCase(ExecContext context) {
		boolean ret = true;
		Token curToken = context.curToken;
		switch (curToken.kind) {
		case XPathTokenParser.DOUBLESLASH:
			context.setLastLevel(XPathSequence.ASCIENT_DESCIENT_LEVEL);
			break;
		case XPathTokenParser.CURSLASH:
		case XPathTokenParser.DIVIDE:
			context.setLastLevel(1);
			break;

		case XPathTokenParser.QName:
		case XPathTokenParser.NCName:
		case XPathTokenParser.AttrName:
			context.addSequence(curToken.image, curToken.kind);
			break;
		case XPathTokenParser.FuncNameStart:
			ExecContext funcChildContext = context.createChildContext();
			funcChildContext.setFuncDef(new FunctionDef(curToken.image
					.substring(0, curToken.image.length() - 1)));
			;
			parseFunction(funcChildContext);
			mergeFunction(context, funcChildContext);
			break;
		case XPathTokenParser.OPENBRAKET:
			ExecContext predChildContext = context.createChildContext();
			parsePredicate(predChildContext);
			mergePredicate(context, predChildContext);
			break;
		case XPathTokenParser.LE:
		case XPathTokenParser.LT:
		case XPathTokenParser.GE:
		case XPathTokenParser.GT:
		case XPathTokenParser.EQ:
			ret = parseComparisonFunction(context);
			break;
		default:
			ret = false;
			break;
		}

		return ret;
	}

	/**
	 * the enter point of the xpath parse process
	 * 
	 * @param context
	 */	@Deprecated
	private static void parseXPath(ExecContext context) {
		context.curToken = context.tokenParser.getNextToken();
		while (context.curToken.kind != XPathTokenParser.EOF) {
			if (!commonCase(context)) {
				context.setError(context.curToken, XPathError.UNKNOW_ERROR);
				break;
			} else {
				if (context.noResults())
					return;
				context.curToken = context.tokenParser.getNextToken();
			}
		}

		if (context.noResults()) {
			return;
		}
		execSteps(context);
	}

	/**
	 * parse the xpath between [ and ]
	 * 
	 * @param context
	 */	@Deprecated
	public static void parsePredicate(ExecContext context) {
		Token curToken = (context.curToken = context.tokenParser.getNextToken());
		boolean shouldStop = false;

		while (curToken.kind != XPathTokenParser.EOF) {
			if (!commonCase(context)) {
				switch (curToken.kind) {
				case XPathTokenParser.CLOSEBRAKET:
					shouldStop = true;
					break;
				default:
					// TODO this is an error
					if (!context.hasError())
						context.setError(curToken.beginLine,
								curToken.beginColumn,
								XPathError.BRAKET_NOT_MATCH);
					shouldStop = true;
					break;
				}
			}
			if (context.noResults())
				return;

			if (shouldStop)
				break;
			else {
				curToken = (context.curToken = context.tokenParser
						.getNextToken());
			}
		}


		if (shouldStop && !context.hasError()) {
			execSteps(context);
		} else if (!shouldStop) {
			context.setError(curToken.beginLine, curToken.beginColumn,
					XPathError.BRAKET_NOT_MATCH);
		}
	}

	/**
	 * parse the xpath between ( and ),then execute the function
	 * 
	 * @param context
	 */	@Deprecated
	public static void parseFunction(ExecContext context) {
		ExecContext curContext = context.createChildContext();
		Token curToken = (curContext.curToken = context.tokenParser
				.getNextToken());
		boolean shouldStop = false;

		ValueType curType = null;
		while (curToken.kind != XPathTokenParser.EOF) {
			if (!commonCase(curContext)) {
				switch (curToken.kind) {
				case XPathTokenParser.CLOSEPARAN:
					// no parameters
					if (curType != null) {
						context.args.add(curContext);
						context.funcDef.addArgType(curType);
					}
					shouldStop = true;
					break;
				case XPathTokenParser.COMMA:
					context.args.add(curContext);
					context.funcDef.addArgType(curType);
					curContext = context.createChildContext();
					break;
				case XPathTokenParser.IntegerLiteral:
					curType = ValueType.INT;
					curContext.addSequence(curToken.image, curToken.kind);
					break;
				case XPathTokenParser.StringLiteral:
					curContext.addSequence(curToken.image, curToken.kind);
					curType = TypeResolver.resolve(curContext.getLastStep()
							.getStepName());
					break;
				case XPathTokenParser.DoubleLiteral:
					curType = ValueType.DOUBLE;
					curContext.addSequence(curToken.image, curToken.kind);
					break;
				default:
					// TODO this is an error
					break;
				}
			} else {
				curType = ValueType.ELEMENTS;
			}

			if (context.noResults())
				return;
			if (shouldStop)
				break;
			else {
				curToken = (curContext.curToken = curContext.tokenParser
						.getNextToken());
			}
		}

		// TODO execute the stack
		if (shouldStop) {
			FunctionDef funcDef = FunctionPool.getFuncImpl(context.funcDef);
			if (funcDef == null) {
				context.setError(context.curToken, context.funcDef.signature()
						+ " " + XPathError.NOT_IMPLEMENTED);
			} else {
				context.funcDef = funcDef;
				funcDef.getFuncImpl().invoke(context);
			}
		} else {
			context.setError(curToken.beginLine, curToken.beginColumn,
					XPathError.PARAN_NOT_MATCH);
		}
	}

	/**
	 * child is a function appearing in the parent
	 * 
	 * @param parent
	 * @param child
	 */
	public static void mergeFunction(ExecContext parent, ExecContext child) {
		if (child.hasError()) {
			parent.setError(child.line, child.column, child.errorMsg);
			return;
		} else if (child.noResults()) {
			parent.setNoResults();
			return;
		} else {
			if (child.getFuncDef().isAtomResult()) {
				parent.addResult((XPathResult) child.getResult());
			} else {
				int level = -1;
				if (parent.isLastLevelStep) {
					level = parent.getLastLevel();
				}
				level = mergeLevel(level, child.getFirstStep().getLevel());
				child.getFirstStep().setLevel(level);

				XPathSequence childStep = (XPathSequence) child.getResult();
				if (parent.isStepEmpty()) {
					childStep.setLevel(level);
					parent.addSequence(childStep);
				} else {
					XPathSequence temp = parent.pollLastStep();
					List<IndexQueryResult> ret = EEJoin(temp, childStep, level,
							parent);
					if (ret == null || ret.size() == 0) {
						parent.setNoResults();
						return;
					} else {
						temp.setStep(ret.get(0));
						parent.addSequence(temp);
					}
				}
			}
		}
	}

	public static int mergeLevel(int pLevel, int cLevel) {
		return pLevel > cLevel ? pLevel : cLevel;
	}

	/**
	 * execute the predicate
	 * 
	 * @param parent
	 * @param child
	 */
	public static void mergePredicate(ExecContext parent, ExecContext child) {
		if (child.noResults()) {
			// no results or an error occurs
			if (child.hasError())
				parent.setError(child.line, child.column, child.errorMsg);
			else {
				parent.setNoResults();
			}
		} else {
			// this is an error
			if (parent.isLastLevelStep) {
				parent.setError(parent.curToken, XPathError.BRAKET_AFTER_SLASH);
				return;
			}

			if (parent.isStepEmpty()) {
				parent.setError(parent.curToken,
						XPathError.NO_STEP_BEFORE_PREDICATE);
				return;
			}

			Object childRet = child.getResult();
			if (childRet instanceof XPathSequence) {
				XPathSequence parentStep = parent.pollLastStep();
				List<IndexQueryResult> ret = EEJoin(parentStep,
						((XPathSequence) childRet),
						((XPathSequence) childRet).getLevel(), parent);
				if (ret == null || ret.size() == 0) {
					parent.setNoResults();
					child.setNoResults();
					return;
				}
				parentStep.setStep(ret.get(0));
				parent.addSequence(parentStep);
			} else {
				parent.setError(parent.curToken, XPathError.NOT_IMPLEMENTED);
				return;
			}
		}
	}

	/**
	 * 
	 * @param parent
	 *            of type IndexQueryResult or String
	 * @param child
	 *            of type IndexQueryResult or String
	 * @param depth
	 * @param context
	 * @return
	 */
	public static List<IndexQueryResult> EEJoin(XPathSequence parent,
			XPathSequence child, int depth, ExecContext context) {
		List<IndexQueryResult> result = new ArrayList<IndexQueryResult>();
		IndexQueryResult parentIResult = null, childIResult = null;
		long startTime = System.currentTimeMillis();
		IXMLFilter parentFilter = context.filter; //这个filter只能用于过滤父亲节点
		if (parent.isRoot())
			parentFilter = new RootNodeFilter(parentFilter);

		IndexQueryResult parentResult = new IndexQueryResult();
		IndexQueryResult childResult = new IndexQueryResult();
		result.add(parentResult);
		result.add(childResult);

		if (!parent.isValueSeted() && child.isValueSeted()) {
			childIResult = (IndexQueryResult) child.getStepValue();
			// 父亲节点未被过滤过，而子节点已经通过条件过滤过
			parentIResult = new IndexQueryResult();
			// we can choose a better access method
			String eleStr = parent.getStepName();
			for (Iterator<Integer> colIter = childIResult.colIterator(); colIter
					.hasNext();) {
				Integer pColID = colIter.next();
				TreeMap<Integer, List<NodeUnit>> valuePosterList = childIResult
						.getColResult(pColID);
				int posterListSize = calcPosterListSize(valuePosterList);

				BtreeDecorator btree = getBtree(pColID,
						parent.getSequenceType());
				if (btree == null)
					continue;

				int elementSize = 0;

				elementSize = btree.estimateKeyResultNum(eleStr, 1);

				// currently set the scale factor to be 0.5
				if (posterListSize < elementSize * 0.5) {
					TreeMap<Integer, List<NodeUnit>> pResult = EEJoinBySearchParent(
							parent, btree, valuePosterList, depth, parentFilter);
					if (calcPosterListSize(pResult) != 0) {
						childResult.addResult(pColID, valuePosterList);
						parentResult.addResult(pColID, pResult);
					}
					colIter.remove();
				} else {
					parentIResult.addResult(pColID, btree.get(eleStr, parentFilter));
				}
			}

			if (childIResult.size() == 0) {
				return result;
			}
		} else if (parent.isValueSeted() && !child.isValueSeted()) {
			// 父节点已经过滤过，而子节点未被过滤过
			parentIResult = (IndexQueryResult) parent.getStepValue();
			childIResult = new IndexQueryResult();
			// we can choose a better access method
			String eleStr = child.getStepName();
			for (Iterator<Integer> colIter = parentIResult.colIterator(); colIter
					.hasNext();) {
				Integer pColID = colIter.next();
				TreeMap<Integer, List<NodeUnit>> valuePosterList = parentIResult
						.getColResult(pColID);
				int posterListSize = calcPosterListSize(valuePosterList);

				BtreeDecorator btree = getBtree(pColID, child.getSequenceType());
				if (btree == null)
					continue;

				int elementSize = 0;

				elementSize = btree.estimateKeyResultNum(eleStr, 1);

				// currently set the scale factor to be 0.5
				if (posterListSize < elementSize * 0.5) {
					TreeMap<Integer, List<NodeUnit>> cResult = EEJoinBySearchChild(
							valuePosterList, btree, child, depth, parentFilter);
					if (calcPosterListSize(cResult) != 0) {
						childResult.addResult(pColID, cResult);
						parentResult.addResult(pColID, valuePosterList);
					}
					colIter.remove();
				} else {
					childIResult.addResult(pColID, btree.get(eleStr, context.filter));
				}
			}

			if (parentIResult.size() == 0) {
				return result;
			}
		} else if (parent.isValueSeted() && child.isValueSeted()) {
			parentIResult = (IndexQueryResult) parent.getStepValue();
			childIResult = (IndexQueryResult) child.getStepValue();
		} else {
			childIResult = context.colStore.getBtreeIndex(
					child.getSequenceType(), context.recursive).get(
					child.getStepName(), context.filter);
			parentIResult = context.colStore.getBtreeIndex(
					parent.getSequenceType(), context.recursive).get(
					parent.getStepName(), parentFilter);
		}


		loadDataLatency.addMicro(System.currentTimeMillis() - startTime);
		startTime = System.currentTimeMillis();

		try {
			for (Iterator<Integer> colIter = parentIResult.colIterator(); colIter
					.hasNext();) {
				Integer pColID = colIter.next();

				if (childIResult.contains(pColID)) {
					// foreach matched collection, invoke the EEJoin for them
					List<TreeMap<Integer, List<NodeUnit>>> ret = EEJoin(
							parentIResult.getColResult(pColID),
							childIResult.getColResult(pColID), depth, parentFilter);
					if (ret != null) {
						parentResult.addResult(pColID, ret.get(0));
						childResult.addResult(pColID, ret.get(1));
					}
				}
			}

			if (parentResult.size() == 0 || childResult.size() == 0) {
				result.clear();
			}
			return result;
		} finally {
			eeJoinExecLatency.addMicro(System.currentTimeMillis() - startTime);
		}
	}

	private static TreeMap<Integer, List<NodeUnit>> EEJoinBySearchParent(
			XPathSequence parent, BtreeDecorator btree,
			TreeMap<Integer, List<NodeUnit>> valuePosterList, int depth,
			IXMLFilter filter) {
		TreeMap<Integer, List<NodeUnit>> pResult = new TreeMap<Integer, List<NodeUnit>>();
		for (Iterator<Entry<Integer, List<NodeUnit>>> posterIter = valuePosterList
				.entrySet().iterator(); posterIter.hasNext();) {
			Entry<Integer, List<NodeUnit>> posterEntry = posterIter.next();

			for (Iterator<NodeUnit> iter = posterEntry.getValue().iterator(); iter
					.hasNext();) {
				NodeUnit unit = iter.next();
				Node node = new Node(posterEntry.getKey(), unit.getNodeID(),
						unit.getRange(), unit.getLevel());
				List<NodeUnit> pRet = btree.getParentNode(parent.getStepName(),
						node, depth, filter);
				if (pRet == null) {
					posterIter.remove();
					break;
				} else if (pRet.size() != 0) {
					addNodesToPoster(pResult, posterEntry.getKey(), pRet);
				} else {
					iter.remove();
				}
			}
		}
		return pResult;
	}

	private static TreeMap<Integer, List<NodeUnit>> EEJoinBySearchChild(
			TreeMap<Integer, List<NodeUnit>> parent, BtreeDecorator btree,
			XPathSequence child, int depth, IXMLFilter filter) {
		TreeMap<Integer, List<NodeUnit>> cResult = new TreeMap<Integer, List<NodeUnit>>();
		for (Iterator<Entry<Integer, List<NodeUnit>>> posterIter = parent
				.entrySet().iterator(); posterIter.hasNext();) {
			Entry<Integer, List<NodeUnit>> posterEntry = posterIter.next();

			for (Iterator<NodeUnit> iter = posterEntry.getValue().iterator(); iter
					.hasNext();) {
				NodeUnit unit = iter.next();
				Node node = new Node(posterEntry.getKey(), unit.getNodeID(),
						unit.getRange(), unit.getLevel());
				List<NodeUnit> cRet = btree.getChildNode(child.getStepName(),
						node, depth, filter);
				if (cRet == null) {
					posterIter.remove();
				} else if (cRet.size() != 0) {
					addNodesToPoster(cResult, posterEntry.getKey(), cRet);
				} else {
					iter.remove();
				}
			}
		}
		return cResult;
	}

	private static BtreeDecorator getBtree(int colID, ValueType valueType) {
		try {
			CollectionStore colStore = XMLDBStore.instance().getCollection(
					colID);
			if (colStore == null)
				return null;
			return colStore.getBtreeIndex(valueType, false);

		} catch (XMLDBException e) {
			return null;
		}
	}

	/**
	 * 
	 * @param element1
	 *            the parent element
	 * @param element2
	 *            the child element
	 * @param depth
	 *            the depth between the parent and child
	 * @return
	 */
	public static List<TreeMap<Integer, List<NodeUnit>>> EEJoin(
			TreeMap<Integer, List<NodeUnit>> parents,
			TreeMap<Integer, List<NodeUnit>> childs, int depth,
			IXMLFilter filter) {

		List<TreeMap<Integer, List<NodeUnit>>> result = new ArrayList<TreeMap<Integer, List<NodeUnit>>>();
		if (parents == null | childs == null | parents.size() == 0
				| childs.size() == 0) {
			return null;
		}

		TreeMap<Integer, List<NodeUnit>> parentNodes = new TreeMap<Integer, List<NodeUnit>>();
		TreeMap<Integer, List<NodeUnit>> childNodes = new TreeMap<Integer, List<NodeUnit>>();
		List<NodeUnit> parent = new ArrayList<NodeUnit>();
		List<NodeUnit> child = new ArrayList<NodeUnit>();

		for (Iterator<Integer> i = parents.keySet().iterator(); i.hasNext();) {
			Integer temp = i.next();
			if (childs.containsKey(temp) && !filter.filter(temp, null)) {
				List<NodeUnit> ln1 = parents.get(temp);
				List<NodeUnit> ln2 = childs.get(temp);
				double orderMin1 = ln1.get(0).getRange()[0];
				double orderMax2 = ln2.get(ln2.size() - 1).getRange()[0];
				if (orderMin1 <= orderMax2) {
					for (int j = 0; j < ln1.size(); j++) {
						NodeUnit node1 = ln1.get(j);

						if (ln2.size() > 0
								&& node1.getRange()[1] > ln2.get(0).getRange()[0]) {
							for (int k = 0; k < ln2.size(); k++) {
								NodeUnit node2 = ln2.get(k);
								if (node1.judgeChild(node2, depth)) {
									if (!parent.contains(ln1.get(j))) {
										parent.add(ln1.get(j));
									}
									child.add(ln2.get(k));
									/*
									 * modified by xiafan add the following
									 * codes to make the execution able to
									 * handle the // operator set the child
									 * level to the level of its parent
									 */
									// ln2.get(k).setLevel(ln1.get(j).getLevel());
									ln2.remove(k);
									k--;
								}

							}
						}

					}
				}
				if (child.size() != 0 && parent.size() != 0) {
					parentNodes.put(temp, new ArrayList<NodeUnit>(parent));
					childNodes.put(temp, new ArrayList<NodeUnit>(child));
					parent.clear();
					child.clear();
				}
			}
		}

		if (parentNodes.size() == childNodes.size() && parentNodes.size() != 0) {
			result.add(parentNodes);
			result.add(childNodes);
		}
		if (result.size() > 0)
			return result;
		else
			return null;

	}

	/**
	 * 
	 * @param element1
	 *            of type IndexQueryResult or String
	 * @param element2
	 *            of type IndexQueryResult or String
	 * @param depth
	 * @param context
	 * @return
	 */
	public static List<IndexQueryResult> EAJoin(XPathSequence element,
			XPathSequence attribute, ExecContext context) {
		return EEJoin(element, attribute, 1, context);
	}

	public static List<TreeMap<Integer, List<NodeUnit>>> EAJoin(
			TreeMap<Integer, List<NodeUnit>> elements,
			TreeMap<Integer, List<NodeUnit>> attributes, IXMLFilter filter) {
		return EEJoin(elements, attributes, 1, filter);
	}

	/**
	 * 
	 * @param element1
	 *            of type IndexQueryResult or String
	 * @param element2
	 *            of type IndexQueryResult or String
	 * @param depth
	 * @param context
	 * @return
	 */
	public static IndexQueryResult EVJoin(XPathSequence element, Object value,
			ExecContext context) {
		IndexQueryResult result = new IndexQueryResult();
		IndexQueryResult elements = null, values = null;
		long startTime = System.currentTimeMillis();
		IXMLFilter filter = context.filter;
		if (element.isRoot())
			filter = new RootNodeFilter(filter);

		if (value instanceof String) {
			ValueType type = TypeResolver.resolve((String) value);
			if (ValueType.isFileSizeType(type)) {
				ByteBuffer buffer = ValueType.getValidator(type).fromString(
						(String) value);
				value = ValueType.getValidator(type).compose(buffer);
			}
			values = context.colStore.getBtreeIndex(type, context.recursive)
					.get((String) value, filter);
		} else {
			values = (IndexQueryResult) value;
		}

		if (element.isValueSeted() == false) {
			elements = new IndexQueryResult();
			// we can choose a better access method
			String eleStr = element.getStepName();
			for (Iterator<Integer> colIter = values.colIterator(); colIter
					.hasNext();) {
				Integer pColID = colIter.next();
				TreeMap<Integer, List<NodeUnit>> valuePosterList = values
						.getColResult(pColID);
				int posterListSize = calcPosterListSize(valuePosterList);

				CollectionStore colStore = null;
				try {
					colStore = XMLDBStore.instance().getCollection(pColID);
				} catch (XMLDBException e) {
					continue;
				}

				int elementSize = 0;

				elementSize = colStore.getBtreeIndex(element.getSequenceType(),
						false).estimateKeyResultNum(eleStr, 1);

				// currently set the scale factor to be 0.5
				if (posterListSize < elementSize * 0.5) {
					EVJoinByIndex(colStore.getBtreeIndex(
							element.getSequenceType(), false), element,
							valuePosterList, filter);
					result.addResult(pColID, valuePosterList);
					colIter.remove();
				} else {
					// load the data
					elements.addResult(
							pColID,
							colStore.getBtreeIndex(element.getSequenceType(),
									false).get(element.getStepName(), filter));
				}
			}
			if (values.size() == 0) {
				evJoinExecLatency.addMicro(System.currentTimeMillis()
						- startTime);
				return result;
			}

			if (element.isRoot())
				elements.filterOutRoot();
		} else {
			elements = (IndexQueryResult) element.getStepValue();
		}

		// normal execution
		for (Iterator<Integer> colIter = elements.colIterator(); colIter
				.hasNext();) {
			Integer pColID = colIter.next();

			if (values.contains(pColID)) {
				// foreach matched collection, invoke the EEJoin for them
				TreeMap<Integer, List<NodeUnit>> ret = EVJoin(
						elements.getColResult(pColID),
						values.getColResult(pColID), context);
				if (ret != null && ret.size() != 0) {
					result.addResult(pColID, ret);
				}
			}
		}
		evJoinExecLatency.addMicro(System.currentTimeMillis() - startTime);
		return result;
	}

	/**
	 * this function will filter out those node that is not joined in the value
	 * posterList
	 * 
	 * @param btree
	 * @param value
	 * @param context
	 */
	public static void EVJoinByIndex(BtreeDecorator btree,
			XPathSequence eleSeq, TreeMap<Integer, List<NodeUnit>> value,
			IXMLFilter filter) {
		for (Iterator<Entry<Integer, List<NodeUnit>>> valueIter = value
				.entrySet().iterator(); valueIter.hasNext();) {
			Entry<Integer, List<NodeUnit>> entry = valueIter.next();
			if (!filter.filter(entry.getKey(), null)) {
				for (Iterator<NodeUnit> iter = entry.getValue().iterator(); iter
						.hasNext();) {
					NodeUnit nodeUnit = iter.next();
					if (eleSeq.isRoot() && nodeUnit.getLevel() != 1) {
						iter.remove();
						continue;
					}

					Node node = new Node(entry.getKey(), nodeUnit.getNodeID(),
							nodeUnit.getRange(), nodeUnit.getLevel());

					if (!btree.contains(eleSeq.getStepName(), node)) {
						iter.remove();
					}
				}
			} else {
				valueIter.remove();
			}
		}
	}

	public static int calcPosterListSize(
			TreeMap<Integer, List<NodeUnit>> posterList) {
		int count = 0;
		for (List<NodeUnit> list : posterList.values()) {
			count += list.size();
		}
		return count;
	}

	public static void addNodesToPoster(
			TreeMap<Integer, List<NodeUnit>> poster, int XMLDocID,
			List<NodeUnit> nodeUnits) {
		for (NodeUnit nodeUnit : nodeUnits) {
			addNodeToPoster(poster, XMLDocID, nodeUnit);
		}
	}

	public static void addNodeToPoster(TreeMap<Integer, List<NodeUnit>> poster,
			int XMLDocID, NodeUnit nodeUnit) {
		if (poster.get(XMLDocID) == null) {
			List<NodeUnit> list = new ArrayList<NodeUnit>();
			list.add(nodeUnit);
			poster.put(XMLDocID, list);
		} else {
			List<NodeUnit> ln = poster.get(XMLDocID);
			int index = Collections.binarySearch(ln, nodeUnit,
					ComparatorByRangeFirst.instance);
			if (index < 0)
				ln.add(Math.abs(index) - 1, nodeUnit);
		}
	}

	public static TreeMap<Integer, List<NodeUnit>> EVJoin(
			TreeMap<Integer, List<NodeUnit>> elements,
			TreeMap<Integer, List<NodeUnit>> values, ExecContext context) {
		long startTime = System.currentTimeMillis();
		try {
			TreeMap<Integer, List<NodeUnit>> result = new TreeMap<Integer, List<NodeUnit>>();

			if (elements == null | elements == null | values.size() == 0
					| values.size() == 0) {
				return result;
			}

			List<NodeUnit> results = new ArrayList<NodeUnit>();

			TreeMap<Integer, List<NodeUnit>> outMap = elements, innerMap = values;

			// swap the map
			if (elements.size() > values.size()) {
				TreeMap<Integer, List<NodeUnit>> temp = outMap;
				outMap = innerMap;
				innerMap = temp;
			}

			for (Iterator<Entry<Integer, List<NodeUnit>>> outMapIter = outMap
					.entrySet().iterator(); outMapIter.hasNext();) {
				Entry<Integer, List<NodeUnit>> outEntry = outMapIter.next();
				List<NodeUnit> outList = outEntry.getValue();
				if (innerMap.containsKey(outEntry.getKey())
						&& !context.filter.filter(outEntry.getKey(), null)) {
					List<NodeUnit> innerList = innerMap.get(outEntry.getKey());
					if (outList.size() > innerList.size()) {
						List<NodeUnit> temp = outList;
						outList = innerList;
						innerList = temp;
					}

					Iterator<NodeUnit> outListIter = outList.iterator(), innerListIter = innerList
							.iterator();
					NodeUnit outNode = null, innerNode = null;
					while (outListIter.hasNext()) {
						outNode = outListIter.next();

						/*
						 * TODO or just use binary search???
						 */
						while (innerNode != null || innerListIter.hasNext()) {
							if (innerNode == null && innerListIter.hasNext())
								innerNode = innerListIter.next();
							else if (innerNode == null)
								break;
							// the list should contains only one equal node
							if (outNode.equals(innerNode)) {
								results.add(outNode);
								innerNode = null;
								break;
							} else if (NodeUnit.rangeFirst.compare(outNode,
									innerNode) < 0) {
								break;
							} else {
								innerNode = null;
							}
						}
					}
				}// end of handle an list
				if (results.size() > 0) {
					result.put(outEntry.getKey(), new ArrayList<NodeUnit>(
							results));
					results.clear();
				}
			}

			return result;
		} finally {
			evJoinExecLatency.addMicro(System.currentTimeMillis() - startTime);
		}
	}

	@Override
	public double getAvgEEJoinLatency() {
		long n = eeJoinExecLatency.getOpCount();
		long latency = eeJoinExecLatency.getTotalLatencyMicros();
		if (n == 0)
			return 0;
		return (float) latency / (float) n;
	}

	@Override
	public double getAvgEVJoinLatency() {
		long n = evJoinExecLatency.getOpCount();
		long latency = evJoinExecLatency.getTotalLatencyMicros();
		if (n == 0)
			return 0;
		return (float) latency / (float) n;
	}

	@Override
	public double getAvgXPathExecLatency() {
		long n = xpathExecLatency.getOpCount();
		long latency = xpathExecLatency.getTotalLatencyMicros();
		if (n == 0)
			return 0;
		return (float) latency / (float) n;
	}

	@Override
	public double getAvgLoadLatency() {
		long n = loadDataLatency.getOpCount();
		long latency = loadDataLatency.getTotalLatencyMicros();
		if (n == 0)
			return 0;
		return (float) latency / (float) n;
	}

}

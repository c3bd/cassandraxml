/**
 * @author xiafan
 */
package imc.disxmldb.xpath.function;

import imc.disxmldb.cassandra.verbhandler.result.SeqResult;
import imc.disxmldb.dom.typesystem.ValueType;
import imc.disxmldb.xpath.function.functionimpl.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * manage the mappings between the function definition and the function
 * implementation
 */
public class FunctionPool {
	private static HashMap<FunctionDef, FunctionDef> functions = new HashMap<FunctionDef, FunctionDef>();

	static {
		// compare function series
		// ge function series
		List<ValueType> argTypes = new LinkedList<ValueType>();
		argTypes.add(ValueType.ELEMENTS);
		argTypes.add(ValueType.STRING);
		FunctionDef funcDef = new FunctionDef(">", argTypes, ValueType.ELEMENTS);
		functions.put(funcDef, funcDef);

		funcDef = new FunctionDef(">");
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.addArgType(ValueType.INT);
		funcDef.setFuncImpl(new XPathElementsGTInt());
		functions.put(funcDef, funcDef);

		funcDef = new FunctionDef(">");
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.addArgType(ValueType.FILESIZE);
		funcDef.setFuncImpl(new XPathElementsGTFileSize());
		functions.put(funcDef, funcDef);

		// ge function series
		argTypes = new LinkedList<ValueType>();
		argTypes.add(ValueType.ELEMENTS);
		argTypes.add(ValueType.STRING);
		funcDef = new FunctionDef(">=", argTypes, ValueType.ELEMENTS);
		functions.put(funcDef, funcDef);

		funcDef = new FunctionDef(">=");
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.addArgType(ValueType.INT);
		funcDef.setFuncImpl(new XPathElementsGTInt());
		functions.put(funcDef, funcDef);

		funcDef = new FunctionDef(">=");
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.addArgType(ValueType.FILESIZE);
		funcDef.setFuncImpl(new XPathElementsGEFileSize());
		functions.put(funcDef, funcDef);

		// lt function series
		funcDef = new FunctionDef("<");
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.addArgType(ValueType.STRING);
		funcDef.setFuncImpl(new XPathElementsLTString());
		functions.put(funcDef, funcDef);

		funcDef = new FunctionDef("<");
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.addArgType(ValueType.INT);
		funcDef.setFuncImpl(new XPathElementsLTInt());
		functions.put(funcDef, funcDef);

		funcDef = new FunctionDef("<");
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.addArgType(ValueType.FILESIZE);
		funcDef.setFuncImpl(new XPathElementsLTFileSize());
		functions.put(funcDef, funcDef);

		// le functions series
		funcDef = new FunctionDef("<=");
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.addArgType(ValueType.STRING);
		funcDef.setFuncImpl(new XPathElementsLTString());
		functions.put(funcDef, funcDef);

		funcDef = new FunctionDef("<=");
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.addArgType(ValueType.INT);
		funcDef.setFuncImpl(new XPathElementsLTInt());
		functions.put(funcDef, funcDef);

		funcDef = new FunctionDef("<=");
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.addArgType(ValueType.FILESIZE);
		funcDef.setFuncImpl(new XPathElementsLEFileSize());
		functions.put(funcDef, funcDef);

		// EQ function series
		// argTypes = new LinkedList<ValueType>();
		// argTypes.add(ValueType.ELEMENTS);
		// argTypes.add(ValueType.STRING);
		funcDef = new FunctionDef("=");
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.addArgType(ValueType.STRING);
		funcDef.setFuncImpl(new XPathElementsEQString());
		functions.put(funcDef, funcDef);

		funcDef = new FunctionDef("=");
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.addArgType(ValueType.INT);
		funcDef.setFuncImpl(new XPathElementsEQInt());
		functions.put(funcDef, funcDef);

		funcDef = new FunctionDef("=");
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.addArgType(ValueType.FILESIZE);
		funcDef.setFuncImpl(new XPathElementsEQFileSize());
		functions.put(funcDef, funcDef);

		// contains function
		funcDef = new FunctionDef("contains");
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.addArgType(ValueType.STRING);
		funcDef.setFuncImpl(new XPathContainFunc());
		functions.put(funcDef, funcDef);

		// contains function
		funcDef = new FunctionDef("contains");
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.addArgType(ValueType.FILESIZE);
		funcDef.setFuncImpl(new XPathContainsFileSize());
		functions.put(funcDef, funcDef);

		// text function
		funcDef = new FunctionDef("text");
		funcDef.setIsAtomResult(true);
		funcDef.setFuncImpl(new XPathText());
		functions.put(funcDef, funcDef);

		funcDef = new FunctionDef("text");
		funcDef.setIsAtomResult(true);
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.setFuncImpl(new XPathText());
		functions.put(funcDef, funcDef);

		// count function
		funcDef = new FunctionDef("count");
		funcDef.setIsAtomResult(true);
		funcDef.setFuncImpl(new XPathCount());
		functions.put(funcDef, funcDef);

		funcDef = new FunctionDef("count");
		funcDef.setIsAtomResult(true);
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.setFuncImpl(new XPathCount());
		functions.put(funcDef, funcDef);

		// min function
		funcDef = new FunctionDef("min");
		funcDef.setIsAtomResult(true);
		funcDef.setFuncImpl(new XPathMin());
		functions.put(funcDef, funcDef);

		funcDef = new FunctionDef("min");
		funcDef.setIsAtomResult(true);
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.setFuncImpl(new XPathMin());
		functions.put(funcDef, funcDef);

		// max function
		funcDef = new FunctionDef("max");
		funcDef.setIsAtomResult(true);
		funcDef.setFuncImpl(new XPathMax());
		functions.put(funcDef, funcDef);

		funcDef = new FunctionDef("max");
		funcDef.setIsAtomResult(true);
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.setFuncImpl(new XPathMax());
		functions.put(funcDef, funcDef);

		// sum function
		funcDef = new FunctionDef("sum");
		funcDef.setIsAtomResult(true);
		funcDef.setFuncImpl(new XPathSum());
		functions.put(funcDef, funcDef);

		funcDef = new FunctionDef("sum");
		funcDef.setIsAtomResult(true);
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.setFuncImpl(new XPathSum());
		functions.put(funcDef, funcDef);

		// average function
		funcDef = new FunctionDef("avg");
		funcDef.setIsAtomResult(true);
		funcDef.setFuncImpl(new XPathAverage());
		functions.put(funcDef, funcDef);

		funcDef = new FunctionDef("avg");
		funcDef.setIsAtomResult(true);
		funcDef.addArgType(ValueType.ELEMENTS);
		funcDef.setFuncImpl(new XPathAverage());
		functions.put(funcDef, funcDef);

		// subsequence
		funcDef = new FunctionDef(SeqResult.SEQ);
		funcDef.setIsAtomResult(true);
		funcDef.addArgType(ValueType.INT);
		funcDef.addArgType(ValueType.INT);
		funcDef.setFuncImpl(new XPathSubsequence());
		functions.put(funcDef, funcDef);

		funcDef = new FunctionDef(SeqResult.SEQ1);
		funcDef.setIsAtomResult(true);
		funcDef.addArgType(ValueType.INT);
		funcDef.addArgType(ValueType.INT);
		funcDef.setFuncImpl(new XPathSubsequence1());
		functions.put(funcDef, funcDef);

		funcDef = new FunctionDef(SeqResult.SUBSEQSTEP2);
		funcDef.setIsAtomResult(true);
		funcDef.addArgType(ValueType.INT);
		funcDef.addArgType(ValueType.INT);
		funcDef.addArgType(ValueType.INT);
		funcDef.setFuncImpl(new XPathSubSeqStep2());
		functions.put(funcDef, funcDef);

	}

	public static FunctionDef getFuncImpl(FunctionDef funcDef) {
		return functions.get(funcDef);
	}

}

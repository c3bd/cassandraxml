package imc.disxmldb.xpath.function;


import imc.disxmldb.dom.typesystem.ValueType;

import java.util.List;

/**
 * This class encapsulate the concept of an function in XPath
 */
public class FunctionDef {
	
	private static String delimiter = "_";
	private String funcName = null;
	private String argTypeSig = "";
	private ValueType retType;
	private boolean isAtomResult = false;
	
	private String signature = null;
	
	private IXPathFunction funcImpl = null;

	public FunctionDef(String funcName) {
		this.funcName = funcName;
	}
	
	public FunctionDef(String funcName, List<ValueType> argTypes, ValueType retType) {
		this.funcName = funcName;
		for (ValueType type : argTypes) {
			argTypeSig += delimiter + type;
		}
		this.retType = retType;
	}
	
	public void addArgType(ValueType type) {
		signature = null;
		argTypeSig += delimiter + type;
	}
	
	public void setRetType(ValueType type) {
		signature = null;
		retType = type;
	}
	
	public String signature() {
		if (signature == null) {
			signature = funcName + delimiter + argTypeSig + delimiter + retType;
		}
		return signature;
	}
	
	public void setFuncImpl(IXPathFunction funcImpl) {
		this.funcImpl = funcImpl;
	}
	
	public IXPathFunction getFuncImpl() {
		return funcImpl;
	}
	
	@Override
	public int hashCode() {
		return signature().hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		return signature().equals(((FunctionDef)other).signature());
	}
	/**
	 * 
	 * @return
	 * 	true if the return of the function should not be regarded as child element nodes that
	 * 			should be further joined with the parent nodes
	 */
	public boolean isAtomResult() {
		return isAtomResult;
	}
	
	public void setIsAtomResult(boolean isAtomResult) {
		this.isAtomResult = isAtomResult;
	}
}

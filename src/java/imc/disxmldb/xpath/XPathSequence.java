/**
 * @author xiafan xiafan68@gmail.com
 */
package imc.disxmldb.xpath;

import imc.disxmldb.dom.typesystem.ValueType;
import imc.disxmldb.index.btree.IndexQueryResult;
import imc.disxmldb.xpath.xpathparser.XPathParser;

/**
 * represent a sequence in the steps stack of ExecContext. It may be a string
 * represent the XPath node or text value. This step may also be the execution
 * result after the current execution.
 * 
 */
public class XPathSequence {
	public static int ASCIENT_DESCIENT_LEVEL = 1000;
	private int level = -1; // the level difference between the parent and the
							// current node
	private ValueType valueType = ValueType.NODETAG;
	private String step = null; // maybe token or TreeMap
	private IndexQueryResult value = null;
	private boolean isRoot = false;

	public XPathSequence(String step_, int type_, int level_) {
		this.level = level_;
		this.step = step_;
		if (type_ == XPathParser.AttrName) {
			step = step_.substring(1, step_.length());
			valueType = ValueType.ATTRTAG;
		} else if (type_ == XPathParser.StringLiteral) {
			step = step_.substring(1, step_.length() - 1);
		}
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		if (level >= ASCIENT_DESCIENT_LEVEL)
			this.level = ASCIENT_DESCIENT_LEVEL;
		else
			this.level = level;
	}

	public Object getStepValue() {
		if (value != null)
			return value;
		return step;
	}

	/**
	 * return false if the value is seted
	 * 
	 * @return
	 */
	public boolean isValueSeted() {
		return value != null;
	}

	public void setStep(IndexQueryResult value_) {
		value = value_;
	}

	/*
	 * public void setStep(String step_) { this.step = step_; }
	 */
	public boolean isAttributeStep() {
		return valueType == ValueType.ATTRTAG;
	}

	public ValueType getSequenceType() {
		return valueType;
	}

	/**
	 * when this is a attribute step but has been joined with its parent node,
	 * it should be set to be an non attribute step, because the level of this
	 * node has been changed to be level of its parent. It is actually an
	 * element step now;
	 * 
	 * @param isAttr
	 */
	public void setIsAttr(ValueType isAttr) {
		this.valueType = isAttr;
	}

	public void setStepName(String stepName) {
		step = stepName;
	}

	public String getStepName() {
		return step;
	}

	public boolean isRoot() {
		return isRoot;
	}

	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

}

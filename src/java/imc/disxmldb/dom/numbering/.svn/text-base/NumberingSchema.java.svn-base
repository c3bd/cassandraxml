package imc.disxmldb.dom.numbering;

/**
 * This class implements the encoding using double as the encoding.
 * 
 */
public class NumberingSchema implements INumberingSchema {
	double[] bounds = new double[2];
	double step;
	double curCode;

	public NumberingSchema(double[] bounds, double step) {
		this.bounds[0] = bounds[0];
		this.bounds[1] = bounds[1];
		this.step = step;
		curCode = bounds[0] + step;
	}

	/**
	 * this function is usually invoked by updating the store with xupdate
	 * @param bounds
	 * @param step
	 * @param nodeCount
	 */
	public NumberingSchema(double[] bounds, double step, int nodeCount) {
		this.bounds[0] = bounds[0];
		this.bounds[1] = bounds[1];
		if (nodeCount * step * 4.3 < (bounds[1] - bounds[0])) {
			this.step = step;
			curCode = bounds[0] + step;
		} else {
			this.step = (bounds[1] - bounds[0]) / ((nodeCount+1) * 4.2);
			curCode = bounds[0] + this.step;
		}
	}
	
	@Override
	public double startElement() {
		try {
			return curCode;
		} finally {
			curCode += step;
		}
	}

	@Override
	public double endElement() {
		try {
			return curCode;
		} finally {
			curCode += step;
		}
	}

	@Override
	public double startAttribute() {
		try {
			return curCode;
		} finally {
			curCode += step;
		}
	}

	@Override
	public double endAttribute() {
		try {
			return curCode;
		} finally {
			curCode += step;
		}
	}

	@Override
	public boolean isOverflow() {
		if (curCode > bounds[1])
			return true;
		return false;
	}
	
	public double getStep() {
		return step;
	}
}

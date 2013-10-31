/**
 *@author:xiafan xiafan68@gmail.com
 *@version: 2011-10-3 0.1
 */
package imc.disxmldb.index;

import imc.disxmldb.xpath.XPathSequence;

import java.util.Arrays;

/**
 * This class defines the unit of a poster list to unify the poster list
 * representation of B+tree and inverted Index
 */
public class NodeUnit {
	public static final ComparatorByRangeFirst rangeFirst = new ComparatorByRangeFirst();

	private int nodeID;
	private double[] range = new double[2];
	private int level;

	public NodeUnit(int nodeID, double[] range, int level) {
		this.nodeID = nodeID;
		this.range = range;
		this.level = level;
	}

	public NodeUnit(Node node) {
		this.nodeID = node.getNodeID();
		this.range = node.getRange();
		this.level = node.getLevel();
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getNodeID() {
		return nodeID;
	}

	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	public double[] getRange() {
		return range;
	}

	public void setRange(double[] range) {
		this.range = range;
	}

	public boolean judgeChild(NodeUnit child, int depth) {
		if (range[0] < child.getRange()[0]
				&& child.getRange()[0] <= range[1]
				&& ((child.getLevel() == level + depth) || (depth >= XPathSequence.ASCIENT_DESCIENT_LEVEL && child
						.getLevel() > level)))
						return true;
		else 
			return false;
	}
	
	@Override
	public String toString() {
		return "NodesUnit [nodeID=" + nodeID + ", range="
				+ Arrays.toString(range) + " level " + level + "]";
	}

	public boolean equals(NodeUnit node) {
		if ((this.range[0] == node.getRange()[0] && this.range[1] == node
				.getRange()[1]) | this.nodeID == node.getNodeID()) {
			return true;
		} else {
			return false;
		}

	}
	
	

}

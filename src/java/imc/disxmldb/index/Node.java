/**
 * @author chengchengyu
 */

package imc.disxmldb.index;

/**
 * The class defines the smallest unit of the poster list stored in B+tree
 * 
 */
public class Node {
	private int xmlDocID = -1;
	private int nodeID = -1;
	private double[] range = new double[2];
	private int level = 0;

	public Node() {

	}

	public Node(int xmlDocID, int nodeID, double[] range, int level) {
		this.xmlDocID = xmlDocID;
		this.nodeID = nodeID;
		this.range = range;
		this.level = level;
	}

	public Node(Node node) {
		this.xmlDocID = node.xmlDocID;
		this.nodeID = node.nodeID;
		this.range = node.range;
		this.level = node.level;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getXmlDocID() {
		return xmlDocID;
	}

	public void setXmlDocID(int xmlDocID) {
		this.xmlDocID = xmlDocID;
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
		this.range[0] = range[0];
		this.range[1] = range[1];
	}

}
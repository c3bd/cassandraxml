package imc.disxmldb.dom.schema;

import java.util.HashMap;

public class SchemasTree {
	private static class TreeNode {
		public String tagName = null;
		public double[] ranges;
		public HashMap<String, TreeNode> statMap = new HashMap<String, TreeNode>();
		public int attrCount = 0;
		public int elemCount = 0;
		
		public TreeNode(String tagName) {
			this.tagName = tagName;
		}
		
	}
	private HashMap<String, String> nodeMaps = new HashMap<String, String>();
	
	public SchemasTree() {
		
	}
	
	public void update(XMLSchema schema) {
		
	}
	
	private void decreEntry() {
		
	}
	
}

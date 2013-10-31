package imc.disxmldb.dom.formatter;

import java.io.IOException;

import imc.disxmldb.dom.AttributeNode;
import imc.disxmldb.dom.AttributeNodeImpl;
import imc.disxmldb.dom.ElementNode;
import imc.disxmldb.dom.ElementNodeImpl;
import imc.disxmldb.dom.XMLNode;
import imc.disxmldb.util.StringOutputStream;

import org.junit.Test;

public class IndentedXMLNodeFormatterTest {

	@Test
	public void test() throws IOException {
		ElementNode eleNode = new ElementNodeImpl("root", 0, 0, null);
		AttributeNode attrNode = new AttributeNodeImpl("attr", 1, 1, (XMLNode)eleNode);
		eleNode.addAttribute((XMLNode)attrNode);
		
		ElementNode child1 = new ElementNodeImpl("child1", 0, 0, (XMLNode)eleNode);
		((ElementNodeImpl)child1).setValue("value");
		eleNode.addChild((XMLNode)child1);
		IndentedXMLNodeFormatter formatter = new IndentedXMLNodeFormatter(null);
		StringOutputStream sos = new StringOutputStream();
		formatter.format((XMLNode) eleNode, sos);
		System.out.println(sos.toString());
	}
	
}

package imc.disxmldb.dom;

import imc.disxmldb.dom.typesystem.ValueType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

public class XMLNodeForMutationTest {
	@Test
	public void serializeTest() throws IOException {
		XMLNodeForMutation node = new XMLNodeForMutation(XMLNode.ELEMENTNODE);
		node.tagName = "testNode";
		node.id = 1;
		node.level = 1;
		node.range[0] = 0.0;
		node.range[1] = 10000.0;
		node.value = ByteBuffer.wrap("test".getBytes());
		node.valueType = ValueType.STRING;
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		XMLNodeForMutation.serialize(node, dos, 1);
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		DataInputStream dis = new DataInputStream(bis);
		System.out.println(String.format("original node:%s", node.toString()));
		node = XMLNodeForMutation.deserialize(dis, 0);
		System.out.println(String.format("deserialized node:%s",node.toString()));
	}
}

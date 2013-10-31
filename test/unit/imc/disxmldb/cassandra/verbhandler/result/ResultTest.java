package imc.disxmldb.cassandra.verbhandler.result;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import junit.framework.Assert;

import imc.disxmldb.cassandra.verbhandler.XPathResult;
import imc.disxmldb.cassandra.verbhandler.XPathResult.XPathResultType;

import org.junit.Test;

public class ResultTest {

	/**
	 * 测试所有的result类型是否能够正常的进行序列化和反序列化
	 * 
	 * @throws IOException
	 */
	@Test
	public void resultTest() throws IOException {
		XPathResult result = new XPathResult(XPathResultType.Average);
		AvgResult funcRet = new AvgResult();
		funcRet.count = 1;
		funcRet.sum = 100;
		result.setFuncRet(funcRet);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		result.serialize(result, dos, 1);
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		DataInputStream dis = new DataInputStream(bis);
		result = result.deserialize(dis, 0);
		System.out.println(result.result());

		result = new XPathResult(XPathResultType.Sum);
		SumResult sumRet = new SumResult();
		sumRet.sum = 100;
		result.setFuncRet(sumRet);

		bos = new ByteArrayOutputStream();
		dos = new DataOutputStream(bos);
		result.serialize(result, dos, 1);
		bis = new ByteArrayInputStream(bos.toByteArray());
		dis = new DataInputStream(bis);
		result = result.deserialize(dis, 0);
		System.out.println(result.result());
		
		result = new XPathResult(XPathResultType.Count);
		CountResult countRet = new CountResult();
		countRet.count = 100;
		result.setFuncRet(sumRet);

		bos = new ByteArrayOutputStream();
		dos = new DataOutputStream(bos);
		result.serialize(result, dos, 1);
		bis = new ByteArrayInputStream(bos.toByteArray());
		dis = new DataInputStream(bis);
		result = result.deserialize(dis, 0);
		System.out.println(result.result());
		
		XMLDocXMLParts parts = new XMLDocXMLParts();
		parts.addXMLParts(0, 0, "parts");
		parts.addXMLParts(1, 1, "1 1");
		result = new XPathResult(XPathResultType.xmlParts);
		result.setResult(parts);
		
		bos = new ByteArrayOutputStream();
		dos = new DataOutputStream(bos);
		result.serialize(result, dos, 1);
		bis = new ByteArrayInputStream(bos.toByteArray());
		dis = new DataInputStream(bis);
		result = result.deserialize(dis, 0);
		Assert.assertEquals(2, result.size());
		System.out.println(result.result());
	}
}

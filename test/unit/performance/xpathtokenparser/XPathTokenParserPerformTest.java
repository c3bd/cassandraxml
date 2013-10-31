package performance.xpathtokenparser;

import imc.disxmldb.xpath.xpathtoken.XPathTokenParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


import org.junit.Test;


public class XPathTokenParserPerformTest {
	private static int testCount = 10000;

	@Test
	public void test() {
		InputStream input = new ByteArrayInputStream("test".getBytes());

		long start = System.currentTimeMillis();

		for (int i = 0; i < testCount; i++) {
			XPathTokenParser parser = new XPathTokenParser(input);
		}
		
		long end = System.currentTimeMillis();
		System.out.println("new " + testCount + " times each costs: " + (end -start)/(testCount * 1.0) + "ms");
	}
}

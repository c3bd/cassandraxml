package imc.disxmldb.util;

import junit.framework.Assert;

import org.junit.Test;

public class XMLUtilitiesTest {
	@Test
	public void test() {
		String origin = "sdfasd<sdfsd>sdfsd&sdf'sdfsd\"sdfd&amp;";
		String transform = XMLUtilities.encodeString(origin);
		System.out.println("string encoded: " + transform);
		String decoded = XMLUtilities.decodeString(transform);
		System.out.println("String decoded: " + decoded);
		Assert.assertEquals(origin, decoded);
	}
}

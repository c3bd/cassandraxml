package imc.disxmldb.util;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

public class StringOutputStreamTest {
	@Test
	public void test() throws IOException {
		StringOutputStream sos = new StringOutputStream();
		sos.writeUTF("test");
		System.out.println(sos.toString());
		Assert.assertEquals("test", sos.toString());
	}
}

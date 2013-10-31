package imc.disxmldb.dom.numbering;

import org.junit.Assert;
import org.junit.Test;

public class NumberingSchemaTest {
	/**
	 * test the function of NumberingSchema
	 */
	@Test
	public void onlyElementNodetest() {
		NumberingSchema numbering = new NumberingSchema(new double[] { 0.0,
				1000.0 }, 50.0);
		/**
		 * only one element
		 */
		Assert.assertEquals((int) 0.0, (int) numbering.startElement());
		Assert.assertEquals((int) 50.0, (int) numbering.endElement());

		/**
		 * contains one child
		 */
		numbering = new NumberingSchema(new double[] { 0.0, 1000.0 }, 50.0);
		Assert.assertEquals((int) 0.0, (int) numbering.startElement());
		Assert.assertEquals((int) 50.0, (int) numbering.startElement());
		Assert.assertEquals((int) 100.0, (int) numbering.endElement());
		Assert.assertEquals((int) 150.0, (int) numbering.endElement());

		/**
		 * contains multiple child
		 */
		numbering = new NumberingSchema(new double[] { 0.0, 1000.0 }, 50.0);
		Assert.assertEquals((int) 0.0, (int) numbering.startElement());
		double target = 0.0;
		for (int i = 0; i < 3; i++) {
			target += 50.0;
			Assert.assertEquals((int) target, (int) numbering.startElement());
			target += 50.0;
			Assert.assertEquals((int) target, (int) numbering.endElement());
		}
		target += 50.0;
		Assert.assertEquals((int) target, (int) numbering.endElement());
	}

	@Test
	public void onlyAttributeNodeTest() {
		/**
		 * contains only one attribute
		 */
		NumberingSchema numbering = new NumberingSchema(new double[] { 0.0,
				1000.0 }, 50.0);
		Assert.assertEquals((int) 0.0, (int) numbering.startElement());
		Assert.assertEquals((int) 50.0, (int) numbering.startAttribute());
		Assert.assertEquals((int) 100.0, (int) numbering.endAttribute());
		Assert.assertEquals((int) 150.0, (int) numbering.endElement());

		/**
		 * contains two attributes
		 */
		numbering = new NumberingSchema(new double[] { 0.0, 1000.0 }, 50.0);
		Assert.assertEquals((int) 0.0, (int) numbering.startElement());
		Assert.assertEquals((int) 50.0, (int) numbering.startAttribute());
		Assert.assertEquals((int) 100.0, (int) numbering.endAttribute());
		Assert.assertEquals((int) 150.0, (int) numbering.startAttribute());
		Assert.assertEquals((int) 200.0, (int) numbering.endAttribute());
		Assert.assertEquals((int) 250.0, (int) numbering.endElement());
	}

	/**
	 * when numberingschema is used by append of xupdate query, the boundary is posed, which will be used to
	 * caculate the step together with the nodecount
	 */
	@Test
	public void numberingForAppendTest() {
		int nodeCount = 100;
		NumberingSchema numbering = new NumberingSchema(new double[] { 111111110.0, 111111160.0 }, 50.0, nodeCount);
		System.out.println("step: " + numbering.getStep());
		for (int i = 0; i < nodeCount; i++) {
			double start =  numbering.startElement();
			double end = numbering.endElement();
			System.out.println("start element: " + start);
			System.out.println("end element: " + end);
			Assert.assertTrue(start < end);
		}
		
		System.out.println(numbering.isOverflow());
		Assert.assertTrue(!numbering.isOverflow());
	}
}

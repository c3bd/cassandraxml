/**
*@author:xiafan xiafan68@gmail.com
*@version: 2011-9-27 0.1
*/
package imc.disxmldb.dom.numbering;

/**
 * defines the interface used to generate range codes for an xml node
 *
 */
public interface INumberingSchema {
	/**
	 * get the start encoding of an element node
	 * @return
	 */
	public double startElement();
	/**
	 * get the end encoding of an element node
	 * @return
	 */
	public double endElement();
	/**
	 * get the start encoding of an attribute node
	 * @return
	 */
	public double startAttribute();
	/**
	 * get the end encoding of an attribute node
	 * @return
	 */
	public double endAttribute();
	
	/**
	 * judge whether the encoding is drained
	 * @return
	 */
	public boolean isOverflow();
}

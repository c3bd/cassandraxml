/**
 * @author xiafan
 */
package imc.disxmldb.index.filter;

import imc.disxmldb.index.Node;
import imc.disxmldb.index.NodeUnit;

/**
 * the interface used to filter out the poster list returned by the query of
 * indices.
 */
public interface IXMLFilter {
	/**
	 * judge whether the given doc or node should be filtered out, if the
	 * nodeunit is null, only filter on the document level
	 * 
	 * @param xmlDocID
	 *            the id of a xml document
	 * @param nodeUnit
	 *            the nodeunit which can be null
	 * @return true if the doc or node should be filtered out
	 */
	public boolean filter(int xmlDocID, NodeUnit nodeUnit);
	public boolean filter(Node node);
}

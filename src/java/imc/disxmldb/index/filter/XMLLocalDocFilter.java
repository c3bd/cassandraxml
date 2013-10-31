/**
 * @author xiafan
 */
package imc.disxmldb.index.filter;

import imc.disxmldb.index.Node;
import imc.disxmldb.index.NodeUnit;

import java.nio.ByteBuffer;

import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.dht.AbstractBounds;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.Token;

/**
 * When replication is not setted to one, The indices will index all the data
 * belongs to multiple replication. It is necessary to filter out those
 * unnecessary tuples. filter out the doc that is not belongs to the current
 * queries. It can improve the performance of the XPath execution
 * 
 */
public class XMLLocalDocFilter implements IXMLFilter {
	private AbstractType validator;
	private IPartitioner partitioner;
	private AbstractBounds bounds;

	public XMLLocalDocFilter(AbstractType validator, IPartitioner partitioner,
			AbstractBounds bounds) {
		this.validator = validator;
		this.partitioner = partitioner;
		this.bounds = bounds;
	}

	@Override
	public boolean filter(int xmlDocID, NodeUnit nodeUnit) {
		ByteBuffer key = validator.fromString(Integer.toString(xmlDocID));
		Token token = partitioner.getToken(key);
		if (bounds.contains(token))
			return false;
		return true;
	}

	@Override
	public boolean filter(Node node) {
		ByteBuffer key = validator.fromString(Integer.toString(node.getXmlDocID()));
		Token token = partitioner.getToken(key);
		if (bounds.contains(token))
			return false;
		return true;
	}

}

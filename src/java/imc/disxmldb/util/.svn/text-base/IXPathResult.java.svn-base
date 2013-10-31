package imc.disxmldb.util;

import java.util.List;

import org.apache.cassandra.dht.AbstractBounds;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.cassandra.verbhandler.XPathQueryCommand;
import imc.disxmldb.cassandra.verbhandler.XPathResult.XPathResultType;

public interface IXPathResult<DataType> extends IDigestAble, ISerializable<DataType> {
	/**
	 * 将当前结果与其它节点返回的结果进行合并
	 * @param index_ other_在返回结果中的索引
	 * @param other_ 另一个节点返回的结果
	 */
	public void merge(int index_, DataType other_) ;
	/**
	 * 在返回结果之前进行调用，对数据进行预处理，也可以用于实现chained responsibility模式
	 */
	public void postProcess();
	
	/**
	 * 
	 * @param cmd
	 * @param cmdb
	 * @param colStore
	 * @param ranges
	 * @return
	 * 	null means no exception is raised
	 */
	public String postProcess(XPathQueryCommand cmd, byte[] cmdb,
			CollectionStore colStore, List<AbstractBounds> ranges);
	
	/**
	 *如果可以，以文本的形式返回结果
	 * @return
	 */
	public List<String> result();
	
	public void setIndex(int index_);
	/**
	 * 返回具体的结果类型
	 * @return
	 */
	public XPathResultType getType();
	/**
	 * 返回包含的结果数
	 * @return
	 */
	public	int size();
}

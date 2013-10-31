/**
*@author:xiafan xiafan68@gmail.com
*@version: 2011-10-26 0.1
*/
package imc.disxmldb.index.invertindex;
/**
 * Interface needed by the JMX framework to provide the run state of the
 * monitored object
 */
public interface CFInvertIndexMBean {
	public double getAvgPutLatency();
	public double getAvgRemoveLatency();
	/**
	 * return the latency for a get(text) operation
	 * @return
	 */
	public double getAvgRetrievelLatency();
	/**
	 * return the latency for a get(text) operation, where the text has been shingled
	 * @return
	 */
	public double getAvgUnitRetrievelLatency();
	
	public double getAvgSplitLatency();
}

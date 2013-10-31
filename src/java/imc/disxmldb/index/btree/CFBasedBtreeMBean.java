package imc.disxmldb.index.btree;
/**
 * Interface needed by the JMX framework to provide the run state of the
 * monitored object
 */
public interface CFBasedBtreeMBean {
	public double getAvgRetrievelLatency();

	public double getRecentRetrievelLatency();

	public double getAvgRemoveLatency();

	public double getRecentRemoveLatency();

	public double getAvgPutLatency();

	public double getRecentPutLatency();
}

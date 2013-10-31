package imc.disxmldb;

/**
 * the interface needed by JMX to display the runtime state of the monitored class
 */
public interface CollectionStoreMBean {
	public double getAvgRetrieveLocalLatency();
	public double getAvgAssembleXMLLatency();
	public double getAvgRtLocalLatency();
	public double getAvgRetrieveRemoteLatency();
	
	public double getAvgXupdateLatency();
	long getTotalXupdateLatency();
}

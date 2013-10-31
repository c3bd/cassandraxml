package org.apache.cassandra.db;

public interface TableMBean {
	public long getReadIdxColLatency();
	public double getAvgReadIdxColLatency();
	
	public long getApplyLatency();
	public double getAvgApplyLatency();
	public long[] getApplyTimeSlice();
	public double getAvgUpdateIdxLatency();
	public double getAvgCfsApplyLatency();
	
	public long[] getApplyTime();
	
}

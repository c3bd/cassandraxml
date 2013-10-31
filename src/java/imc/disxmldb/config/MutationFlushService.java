package imc.disxmldb.config;

import org.apache.cassandra.db.RowMutation;
import org.apache.cassandra.thrift.ConsistencyLevel;

/**
 * define the interface used to flush the MetaData mutation
 * @author xiafan
 *
 */
public interface MutationFlushService{
	/**
	 * the MetaData mutation is submitted in the form of RowMutation.
	 * @param rm
	 */
	public boolean flush(RowMutation rm, ConsistencyLevel level);

	public void shutdown();
}

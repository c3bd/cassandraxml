package imc.disxmldb.config;

/**
 * This class is the factory class that creates the MutationFlushService which is used
 * to flush the meta data to the underlying store.
 *
 */
public class DataFlushServiceFactory {
	public static DataFlushServiceFactory instance = new DataFlushServiceFactory();
	
	public MutationFlushService getMetaDataFlushService() {
		return new InstantMetaDataFlushService();
	}
}

package imc.disxmldb.config;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.cassandra.db.RowMutation;
import org.apache.cassandra.service.StorageProxy;
import org.apache.cassandra.thrift.ConsistencyLevel;

/**
 * a synchronized manner to execute the flush operation, which means the invoker must wait for this
 * operation to finish
 * @author xiafan
 * 
 */
public class InstantMetaDataFlushService implements
		MutationFlushService {

	private ExecutorService service = Executors.newSingleThreadExecutor();

	@Override
	public boolean flush(RowMutation rm, ConsistencyLevel level) {
		final RowMutation frm = rm;
		//create a callable object that invokes that mutation operation
		Callable call = new Callable() {
			@Override
			public Object call() {
				try {
					StorageProxy.mutate(Arrays.asList(frm),
							XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL);
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				return true;
			}
		};

		//execute the operation and wait for it to complete
		Future<Boolean> result = service.submit(call);
		for (int i = 0; i < SysConfig.FAILURE_RETRY; i++) {
			try {
				boolean ret = result.get();
				return ret;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}

	@Override
	public void shutdown() {
		service.shutdown();
	}

}

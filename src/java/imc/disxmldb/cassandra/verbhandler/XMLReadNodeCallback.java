/**
 *@author:xiafan xiafan68@gmail.com
 *@version: 2011-10-9 0.1
 */
package imc.disxmldb.cassandra.verbhandler;

import imc.disxmldb.config.SysConfig;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.net.IAsyncCallback;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.service.DigestMismatchException;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.cassandra.utils.SimpleCondition;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLReadNodeCallback implements IAsyncCallback {
	private static Logger logger = LoggerFactory.getLogger(XMLReadNodeCallback.class);
	protected final SimpleCondition condition = new SimpleCondition();
	private final long startTime;
	protected final int blockfor;
	public final List<InetAddress> endpoints;
	private final XMLReadNodeCommand command;
	private final AtomicInteger received = new AtomicInteger(0);
	private XMLNodeDigestResolver resolver;

	public XMLReadNodeCallback(XMLNodeDigestResolver resolver, ConsistencyLevel consistencyLevel,
			XMLReadNodeCommand command, List<InetAddress> endpoints) {
		this.blockfor = determineBlockFor(consistencyLevel,
				SysConfig.DEFAULT_KEYSPACE);
		this.endpoints = preferredEndpoints(endpoints);
		this.startTime = System.currentTimeMillis();
		this.command = command;
		this.resolver = resolver;
	}

	public String get() throws TimeoutException, IOException, DigestMismatchException {
		 long timeout = DatabaseDescriptor.getRpcTimeout() - (System.currentTimeMillis() - startTime);
	        boolean success;
	        try
	        {
	            success = condition.await(timeout, TimeUnit.MILLISECONDS);
	        }
	        catch (InterruptedException ex)
	        {
	            throw new AssertionError(ex);
	        }

	        if (!success)
	        {
	            StringBuilder sb = new StringBuilder("");
	            for (Message message : resolver.getMessages())
	                sb.append(message.getFrom()).append(", ");
	            throw new TimeoutException("Operation timed out - received only " + received.get() + " responses from " + sb.toString() + " .");
	        }

	        return blockfor == 1 ? resolver.getData() : resolver.resolve();
	}
	
	@Override
	public boolean isLatencyForSnitch() {
		return true;
	}

	@Override
	public void response(Message msg) {
		resolver.preprocess(msg);
		int n = received.incrementAndGet();
		if (n >= blockfor && resolver.isDataPresent())
			condition.signalAll();
	}

	public void response(XMLReadNodeResponse result) {
		resolver.injectPreProcessed(result);
		int n = received.incrementAndGet();
		if (n >= blockfor && resolver.isDataPresent()) {
			condition.signalAll();
			//TODO do repair as ReadCallback
		}
	}
	
	public int determineBlockFor(ConsistencyLevel consistencyLevel, String table) {
		switch (consistencyLevel) {
		case ONE:
		case ANY:
			return 1;
		case TWO:
			return 2;
		case THREE:
			return 3;
		case QUORUM:
			return (Table.open(table).getReplicationStrategy()
					.getReplicationFactor() / 2) + 1;
		case ALL:
			return Table.open(table).getReplicationStrategy()
					.getReplicationFactor();
		default:
			throw new UnsupportedOperationException(
					"invalid consistency level: " + consistencyLevel);
		}
	}
	
    private List<InetAddress> preferredEndpoints(List<InetAddress> endpoints)
    {
        return endpoints.subList(0, Math.min(endpoints.size(), blockfor)); // min so as to not throw exception until assureSufficient is called
    }
    
    public void assureSufficientLiveNodes() throws UnavailableException
    {
        if (endpoints.size() < blockfor)
        {
            logger.debug("Live nodes {} do not satisfy ConsistencyLevel ({} required)",
                         StringUtils.join(endpoints, ", "), blockfor);
            throw new UnavailableException();
        }
    }
}

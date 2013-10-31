package imc.disxmldb.util;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.service.StorageService;

public abstract class DroppableRunnable implements Runnable {
	private final long constructionTime = System.currentTimeMillis();
	private final StorageService.Verb verb;

	public DroppableRunnable(StorageService.Verb verb) {
		this.verb = verb;
	}

	public final void run() {
		if (System.currentTimeMillis() > constructionTime
				+ DatabaseDescriptor.getRpcTimeout()) {
			MessagingService.instance().incrementDroppedMessages(verb);
			return;
		}

		try {
			runMayThrow();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	abstract protected void runMayThrow() throws Exception;
}

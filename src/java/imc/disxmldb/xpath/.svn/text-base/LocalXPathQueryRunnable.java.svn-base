package imc.disxmldb.xpath;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.XMLDBStore;
import imc.disxmldb.cassandra.verbhandler.XPathQueryCallback;
import imc.disxmldb.cassandra.verbhandler.XPathQueryCommand;
import imc.disxmldb.cassandra.verbhandler.XPathQueryResponse;
import imc.disxmldb.cassandra.verbhandler.XPathResult;
import imc.disxmldb.cassandra.verbhandler.XPathResult.XPathResultType;
import imc.disxmldb.util.DroppableRunnable;

import java.io.IOException;

import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.base.XMLDBException;

public class LocalXPathQueryRunnable extends DroppableRunnable {
	public static final Logger logger = LoggerFactory
			.getLogger(LocalXPathQueryRunnable.class);
	private final XPathQueryCommand command;
	private final XPathQueryCallback handler;
	private final long start = System.currentTimeMillis();

	LocalXPathQueryRunnable(XPathQueryCommand command,
			XPathQueryCallback handler) {
		super(StorageService.Verb.XML_READ_NODE);
		this.command = command;
		this.handler = handler;
	}

	protected void runMayThrow() throws IOException, XMLDBException {
		if (logger.isDebugEnabled())
			logger.debug("LocalReadRunnable reading " + command);
		// TODO 需要检查这里在改成nodeonly之后会不会有问题
		CollectionStore colStore = XMLDBStore.instance().getCollection(
				command.colID);
		XPathQueryResponse response = new XPathQueryResponse(
				command.isDigest());
		try {
			XPathResult result = colStore.XPathLocal(command);
			if (command.isDigest()) {
				response.digest = result.getDigest();
			} else {
				response.result = result;
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			response.result = new XPathResult(XPathResultType.Null);
		}
		MessagingService.instance().addLatency(
				FBUtilities.getBroadcastAddress(),
				System.currentTimeMillis() - start);
		handler.response(response);
	}
}
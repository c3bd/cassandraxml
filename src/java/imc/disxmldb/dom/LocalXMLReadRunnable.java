package imc.disxmldb.dom;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.XMLDBStore;
import imc.disxmldb.cassandra.verbhandler.XMLReadNodeCallback;
import imc.disxmldb.cassandra.verbhandler.XMLReadNodeCommand;
import imc.disxmldb.cassandra.verbhandler.XMLReadNodeResponse;
import imc.disxmldb.cassandra.verbhandler.XMLReadNodeVerbHandler;
import imc.disxmldb.util.DroppableRunnable;

import java.io.IOException;

import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.base.XMLDBException;

public class LocalXMLReadRunnable extends DroppableRunnable {
	public static final Logger logger = LoggerFactory
			.getLogger(LocalXMLReadRunnable.class);
	
	private final XMLReadNodeCommand command;
	private final XMLReadNodeCallback handler;
	private final long start = System.currentTimeMillis();

	public LocalXMLReadRunnable(XMLReadNodeCommand command,
			XMLReadNodeCallback handler) {
		super(StorageService.Verb.XML_READ_NODE);
		this.command = command;
		this.handler = handler;
	}

	protected void runMayThrow() throws IOException, XMLDBException {
		if (logger.isDebugEnabled())
			logger.debug("LocalReadRunnable reading " + command);

		CollectionStore colStore = XMLDBStore.instance().getCollection(
				command.getColID());
		String xmlContent = colStore.retrieveLocal(command.getXmlID(),
				command.getNodeID());
		XMLReadNodeResponse result = XMLReadNodeVerbHandler.getResponse(
				command, xmlContent);
		MessagingService.instance().addLatency(
				FBUtilities.getBroadcastAddress(),
				System.currentTimeMillis() - start);
		handler.response(result);
	}
}
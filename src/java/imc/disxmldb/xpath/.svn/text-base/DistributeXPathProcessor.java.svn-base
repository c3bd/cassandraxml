package imc.disxmldb.xpath;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.cassandra.verbhandler.XPathQueryCallback;
import imc.disxmldb.cassandra.verbhandler.XPathQueryCommand;
import imc.disxmldb.cassandra.verbhandler.XPathQueryResolver;
import imc.disxmldb.cassandra.verbhandler.XPathResult;
import imc.disxmldb.config.SysConfig;
import imc.disxmldb.xpath.xpathcompile.XPathCompiler;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.List;

import org.apache.cassandra.concurrent.Stage;
import org.apache.cassandra.concurrent.StageManager;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.dht.AbstractBounds;
import org.apache.cassandra.dht.Bounds;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.net.CachingMessageProducer;
import org.apache.cassandra.net.MessageProducer;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.service.StorageProxy;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistributeXPathProcessor {
	public static final Logger logger = LoggerFactory
			.getLogger(DistributeXPathProcessor.class);
	private CollectionStore colStore = null;
	
	public DistributeXPathProcessor(CollectionStore colStore_) {
		colStore = colStore_;
	}
	
	/**
	 * poll all data servers to execute the command
	 * 
	 * @param command
	 * @return
	 * @throws UnavailableException
	 * @throws UnsupportedEncodingException
	 * @throws imc.disxmldb.xpath.xpathcompile.ParseException
	 */
	public XPathResult XPathInternal(byte[] cmd, byte flag_)
			throws UnavailableException, UnsupportedEncodingException,
			imc.disxmldb.xpath.xpathcompile.ParseException {
		String xpath = new String(cmd, SysConfig.ENCODING);
		if (!xpath.endsWith(";"))
			xpath += ";";

		XPathCompiler compiler = new XPathCompiler(new ByteArrayInputStream(
				xpath.getBytes(SysConfig.ENCODING)), SysConfig.ENCODING);
		compiler.compile();

		cmd = xpath.getBytes(SysConfig.ENCODING);
		XPathQueryCommand command = new XPathQueryCommand(colStore.getMetaData()
				.getColID(), cmd, flag_);
		// cmd.

		assert !(command.isDigest());
		logger.debug("Command/ConsistencyLevel is {}/{}", command,
				SysConfig.XMLREAD_CONSISTENCY_LEVEL);
		IPartitioner p = DatabaseDescriptor.getPartitioner();
		AbstractBounds bounds = new Bounds(
				p.getToken(ByteBufferUtil.EMPTY_BYTE_BUFFER),
				p.getToken(ByteBufferUtil.EMPTY_BYTE_BUFFER));
		// sending xpath query to all nodes
		List<AbstractBounds> ranges = StorageProxy.getRestrictedRanges(bounds);
		XPathQueryCallback[] callbacks = new XPathQueryCallback[ranges.size()];
		for (int i = 0; i < ranges.size(); i++) {
			AbstractBounds range = ranges.get(i);
			command = command.copy();
			command.range = range;
			callbacks[i] = ExecXPathOnNode(command, range);
		}

		XPathResult ret = null;

		int i = 0;
		for (XPathQueryCallback callback : callbacks) {
			try {
				if (ret == null) {
					ret = callback.get();
					ret.index = i;
					ret.cmdb = cmd;
					ret.cmd = command;
					ret.ranges = ranges;
					ret.colStore = colStore;
				} else {
					XPathResult tempRet = callback.get();
					tempRet.index = i;
					ret.merge(i, tempRet);
					if (ret.getException() != null)
						break;
				}
			} catch (Exception e) {

			}
			i++;
		}

		ret.postProcess();
		return ret;
	}

	public XPathQueryCallback ExecXPathOnNode(XPathQueryCommand cmd_,
			AbstractBounds range_) throws UnavailableException {
		cmd_.range = range_;
		List<InetAddress> endpoints = StorageService.instance
				.getNaturalEndpoints(SysConfig.DEFAULT_KEYSPACE, range_.right);
		DatabaseDescriptor.getEndpointSnitch().sortByProximity(
				FBUtilities.getBroadcastAddress(), endpoints);

		XPathQueryResolver resolver = new XPathQueryResolver();
		XPathQueryCallback callback = new XPathQueryCallback(resolver,
				SysConfig.XMLREAD_CONSISTENCY_LEVEL, cmd_, endpoints);
		callback.assureSufficientLiveNodes();
		assert !callback.endpoints.isEmpty();

		// The data-request message is sent to dataPoint, the node that
		// will actually get the data for us
		InetAddress dataPoint = callback.endpoints.get(0);
		if (dataPoint.equals(FBUtilities.getBroadcastAddress())) {
			logger.debug("reading data locally");
			XPathQueryCommand temp = cmd_.copy();
			StageManager.getStage(Stage.XML_QUERY).execute(
					new LocalXPathQueryRunnable(temp, callback));
		} else {
			logger.debug("reading data from {}", dataPoint);
			MessagingService.instance().sendRR(cmd_, dataPoint, callback);
		}

		if (callback.endpoints.size() == 1)
			return callback;

		// send the other endpoints a digest request
		XPathQueryCommand digestCommand = cmd_.copy();
		digestCommand.range = range_;
		digestCommand.flag = XPathQueryCommand.installFlag(digestCommand.flag,
				XPathQueryCommand.XPATH_DIGEST);
		MessageProducer producer = null;
		for (InetAddress digestPoint : callback.endpoints.subList(1,
				callback.endpoints.size())) {
			if (digestPoint.equals(FBUtilities.getBroadcastAddress())) {
				logger.debug("reading digest locally");
				StageManager.getStage(Stage.XML_READ_NODE).execute(
						new LocalXPathQueryRunnable(digestCommand, callback));
			} else {
				logger.debug("reading digest from {}", digestPoint);
				// (We lazy-construct the digest Message object since it
				// may not be necessary if we
				// are doing a local digest read, or no digest reads at
				// all.)
				if (producer == null)
					producer = new CachingMessageProducer(digestCommand);
				MessagingService.instance().sendRR(producer, digestPoint,
						callback);
			}
		}

		return callback;
	}
}

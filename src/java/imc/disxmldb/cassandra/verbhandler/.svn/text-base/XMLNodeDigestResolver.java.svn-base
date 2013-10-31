/**
 *@author:xiafan xiafan68@gmail.com
 *@version: 2011-10-9 0.1
 */
package imc.disxmldb.cassandra.verbhandler;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.config.SysConfig;
import java.io.DataInputStream;
import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.io.util.FastByteArrayInputStream;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.service.DigestMismatchException;
import org.apache.cassandra.service.IResponseResolver;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.commons.lang.ArrayUtils;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLNodeDigestResolver implements IResponseResolver<String> {
	private static Logger logger = LoggerFactory
			.getLogger(XMLNodeDigestResolver.class);
	private static final Message FAKE_MESSAGE = new Message(
			FBUtilities.getBroadcastAddress(),
			StorageService.Verb.INTERNAL_RESPONSE, ArrayUtils.EMPTY_BYTE_ARRAY,
			-1);

	private final int colID;
	private final int xmlDocID;
	private final int nodeID;
	private String data = null;

	private ConcurrentMap<Message, XMLReadNodeResponse> replies = new NonBlockingHashMap<Message, XMLReadNodeResponse>();

	public XMLNodeDigestResolver(int colID, int xmlDocID, int nodeID) {
		this.colID = colID;
		this.xmlDocID = xmlDocID;
		this.nodeID = nodeID;
	}

	@Override
	public String resolve() throws DigestMismatchException, IOException {
		if (logger.isDebugEnabled())
			logger.debug("resolving " + replies.size() + " responses");

		long startTime = System.currentTimeMillis();
		ByteBuffer keyBuf = CollectionStore.COLKEYVALIDATOR.fromString(colID
				+ ":" + xmlDocID);
		DecoratedKey key = DatabaseDescriptor.getPartitioner().decorateKey(
				keyBuf);
		// validate digests against each other; throw immediately on mismatch.
		// also extract the data reply, if any.
		ByteBuffer digest = null;
		for (Map.Entry<Message, XMLReadNodeResponse> entry : replies.entrySet()) {
			XMLReadNodeResponse response = entry.getValue();
			if (response.getDigest() != null) {
				if (digest == null) {
					digest = response.getDigest();
				} else {
					ByteBuffer digest2 = response.getDigest();
					if (!digest.equals(digest2)) {
						throw new DigestMismatchException(key, digest, digest2);
					}
				}
			} else {
				data = response.getXmlContent();
			}
		}

		// Compare digest (only one, since we threw earlier if there were
		// different replies)
		// with the data response. If there is a mismatch then throw an
		// exception so that read repair can happen.
		//
		// It's important to note that we do not consider the possibility of
		// multiple data responses --
		// that can only happen when we're doing the repair post-mismatch, and
		// will be handled by RowRepairResolver.
		if (digest != null) {
			ByteBuffer digest2 = ByteBuffer.wrap(FBUtilities
					.threadLocalMD5Digest().digest(
							data.getBytes(SysConfig.ENCODING)));
			if (!digest.equals(digest2))
				throw new DigestMismatchException(key, digest, digest2);
			if (logger.isDebugEnabled())
				logger.debug("digests verified");
		}

		if (logger.isDebugEnabled())
			logger.debug("resolve: " + (System.currentTimeMillis() - startTime)
					+ " ms.");
		return data;
	}

	@Override
	public boolean isDataPresent() {
		for (XMLReadNodeResponse result : replies.values()) {
			if (result.getXmlContent() != null)
				return true;
		}
		return false;
	}

	@Override
	public String getData() throws IOException {
		for (Map.Entry<Message, XMLReadNodeResponse> entry : replies.entrySet()) {
			if (entry.getValue().getXmlContent() != null)
				return entry.getValue().getXmlContent();
		}
		
		throw new AssertionError(
				"getData should not be invoked when no data is present");
	}

	public void injectPreProcessed(XMLReadNodeResponse response) {
		replies.put(FAKE_MESSAGE, response);
	}

	@Override
	public void preprocess(Message message) {
		byte[] body = message.getMessageBody();
		FastByteArrayInputStream bufIn = new FastByteArrayInputStream(body);
		try {
			XMLReadNodeResponse response = XMLReadNodeResponse.serializer()
					.deserialize(new DataInputStream(bufIn),
							message.getVersion());
			replies.put(message, response);
		} catch (IOException e) {
			throw new IOError(e);
		}

	}

	@Override
	public Iterable<Message> getMessages() {
		return replies.keySet();
	}

	@Override
	public int getMaxLiveColumns() {
		// TODO unsupported
		return 0;
	}

}

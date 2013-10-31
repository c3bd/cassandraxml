package imc.disxmldb.cassandra.verbhandler;


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
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.commons.lang.ArrayUtils;
import org.cliffc.high_scale_lib.NonBlockingHashMap;


public class XPathQueryResolver implements IResponseResolver<XPathResult>{
	
	private static final Message FAKE_MESSAGE = new Message(
			FBUtilities.getBroadcastAddress(),
			StorageService.Verb.INTERNAL_RESPONSE, ArrayUtils.EMPTY_BYTE_ARRAY,
			-1);
	private ConcurrentMap<Message, XPathQueryResponse> replies = new NonBlockingHashMap<Message, XPathQueryResponse>();

	XPathResult data = null;
	
	//TODO: we maybe should set key to be the xpath command
	private DecoratedKey key = DatabaseDescriptor.getPartitioner().decorateKey(
			ByteBufferUtil.EMPTY_BYTE_BUFFER);
	
	@Override
	public XPathResult resolve() throws DigestMismatchException, IOException {

		long startTime = System.currentTimeMillis();
		ByteBuffer digest = null;
		for (Map.Entry<Message, XPathQueryResponse> entry : replies.entrySet()) {
			XPathQueryResponse response = entry.getValue();
			if (response.isDigest) {
				if (digest == null) {
					digest = response.digest;
				} else {
					ByteBuffer digest2 = response.digest;
					if (!digest.equals(digest2)) {
						throw new DigestMismatchException(key, digest, digest2);
					}
				}
			} else {
				data = response.result;
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
			ByteBuffer digest2 = data.getDigest();
			if (!digest.equals(digest2))
				throw new DigestMismatchException(key, digest, digest2);
		}
		return data;
	}

	@Override
	public boolean isDataPresent() {
		if (data != null)
			return true;
		for (XPathQueryResponse response : replies.values()) {
			if (!response.isDigest)
				return true;
		}
		return false;
	}

	@Override
	public XPathResult getData() throws IOException {
		if (data != null)
			return data;
		for (XPathQueryResponse response : replies.values()) {
			if (!response.isDigest)
				return  response.result;
		}
		throw new AssertionError(
		"getData should not be invoked when no data is present");
	}

	@Override
	public void preprocess(Message message) {
		FastByteArrayInputStream fis = new FastByteArrayInputStream(message.getMessageBody());
		DataInputStream dis = new DataInputStream(fis);
		try {
			XPathQueryResponse response = XPathQueryResponse.serializer.deserialize(dis, message.getVersion());
			replies.put(message, response);
		} catch (IOException e) {
			e.printStackTrace();
			//throw new IOError(e);
		} 
		
	}
	
	public void injectPreProcessed(XPathQueryResponse response) {
		replies.put(FAKE_MESSAGE, response);
	}

	@Override
	public Iterable<Message> getMessages() {
	return replies.keySet();
	}

	@Override
	public int getMaxLiveColumns() {
		// TODO Auto-generated method stub
		return 0;
	}

}

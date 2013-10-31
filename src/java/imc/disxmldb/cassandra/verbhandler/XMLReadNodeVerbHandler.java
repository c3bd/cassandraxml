/**
 *@author:xiafan xiafan68@gmail.com
 *@version: 2011-10-8 0.1
 */
package imc.disxmldb.cassandra.verbhandler;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.XMLDBStore;
import imc.disxmldb.config.SysConfig;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ReadCommand;
import org.apache.cassandra.db.ReadResponse;
import org.apache.cassandra.db.ReadVerbHandler;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.io.util.DataOutputBuffer;
import org.apache.cassandra.io.util.FastByteArrayInputStream;
import org.apache.cassandra.net.IVerbHandler;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.base.XMLDBException;

public class XMLReadNodeVerbHandler implements IVerbHandler {

	private static Logger logger_ = LoggerFactory
			.getLogger(XMLReadNodeVerbHandler.class);

	// re-use output buffers between requests
	private static ThreadLocal<DataOutputBuffer> threadLocalOut = new ThreadLocal<DataOutputBuffer>() {
		@Override
		protected DataOutputBuffer initialValue() {
			return new DataOutputBuffer();
		}
	};

	public void doVerb(Message message, String id) {
		if (StorageService.instance.isBootstrapMode()) {
			throw new RuntimeException(
					"Cannot service reads while bootstrapping!");
		}

		try {
			FastByteArrayInputStream in = new FastByteArrayInputStream(
					message.getMessageBody());
			XMLReadNodeCommand command = XMLReadNodeCommand.serializer()
					.deserialize(new DataInputStream(in), message.getVersion());
			CollectionStore colStore = XMLDBStore.instance().getCollection(
					command.getColID());
			String xmlContent = colStore.retrieveLocal(command.getXmlID(),
					command.getNodeID());
			
			
			DataOutputBuffer out = threadLocalOut.get();
			out.reset();
			XMLReadNodeResponse.serializer().serialize(getResponse(command, xmlContent), out,
					message.getVersion());
			byte[] bytes = new byte[out.getLength()];
			System.arraycopy(out.getData(), 0, bytes, 0, bytes.length);
			Message response = message.getReply(
					FBUtilities.getBroadcastAddress(), bytes,
					message.getVersion());

			if (logger_.isDebugEnabled())
				logger_.debug(String.format(
						"read node for collection:%d, xmlDoc:%d, nodeID:%d",
						command.getColID(), command.getXmlID(),
						command.getNodeID()));
			MessagingService.instance().sendReply(response, id,
					message.getFrom());
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} catch (XMLDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static XMLReadNodeResponse getResponse(XMLReadNodeCommand command, String xmlContent) {
		if (command.getIsDigest() == XMLReadNodeCommand.DIGEST) {
			 MessageDigest digest = FBUtilities.threadLocalMD5Digest();
			 try {
				digest.update(xmlContent.getBytes(SysConfig.ENCODING));
			} catch (UnsupportedEncodingException e) {
				digest.update(new byte[0]);
			}
			 ByteBuffer digestBuffer = ByteBuffer.wrap(digest.digest());
			if (logger_.isDebugEnabled())
				logger_.debug("digest is "
						+ ByteBufferUtil.bytesToHex(digestBuffer.duplicate()));
			
			return new XMLReadNodeResponse(digestBuffer);
		} else {
			return new XMLReadNodeResponse(xmlContent);
		}
	}

}

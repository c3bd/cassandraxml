/**
*@author:xiafan xiafan68@gmail.com
*@version: 2011-10-6 0.1
*/
package imc.disxmldb.cassandra.verbhandler;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.XMLDBStore;
import imc.disxmldb.cassandra.verbhandler.XPathResult.XPathResultType;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.cassandra.io.util.DataOutputBuffer;
import org.apache.cassandra.io.util.FastByteArrayInputStream;
import org.apache.cassandra.net.IVerbHandler;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.Pair;
import org.xmldb.api.base.XMLDBException;

public class XPathQueryVerbHandler implements IVerbHandler{

	// re-use output buffers between requests
	private static ThreadLocal<DataOutputBuffer> threadLocalOut = new ThreadLocal<DataOutputBuffer>() {
		@Override
		protected DataOutputBuffer initialValue() {
			return new DataOutputBuffer();
		}
	};
	
	@Override
	public void doVerb(Message message, String id) {
		FastByteArrayInputStream in = new FastByteArrayInputStream(
				message.getMessageBody());
		boolean succ = false;
		XPathQueryCommand command = null;
		try {
			command = XPathQueryCommand.serializer().deserialize(new DataInputStream(in), message.getVersion());
			CollectionStore colStore = XMLDBStore.instance().getCollection(command.colID);
			XPathResult result  = colStore.XPathLocal(command);
	
			XPathQueryResponse response = new XPathQueryResponse(command.isDigest());
			if (command.isDigest()) {
				response.digest = result.getDigest();
			} else {
				response.result = result;
			}
			DataOutputBuffer out = threadLocalOut.get();
			out.reset();
			XPathQueryResponse.serializer.serialize(response, out, message.getVersion());
			byte[] bytes = new byte[out.getLength()];
			System.arraycopy(out.getData(), 0, bytes, 0, bytes.length);
			Message reply =  message.getReply(FBUtilities.getBroadcastAddress(), bytes, message.getVersion());
			
			MessagingService.instance().sendReply(reply, id, message.getFrom());
			succ = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (succ == false) {
				if (command == null)
					return;
				
				XPathQueryResponse response = new XPathQueryResponse(
						command.isDigest());
				XPathResult result = new XPathResult(XPathResultType.Null);
				response.result = result;
				DataOutputBuffer out = threadLocalOut.get();
				out.reset();
				try {
					XPathQueryResponse.serializer.serialize(response, out,
							message.getVersion());
					byte[] bytes = new byte[out.getLength()];
					System.arraycopy(out.getData(), 0, bytes, 0, bytes.length);
					Message reply = message.getReply(FBUtilities.getBroadcastAddress(),
							bytes, message.getVersion());

					MessagingService.instance().sendReply(reply, id, message.getFrom());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}

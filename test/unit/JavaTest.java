import imc.disxmldb.cassandra.verbhandler.XPathQueryResponse;

import java.util.concurrent.ConcurrentMap;

import org.apache.cassandra.net.Message;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.commons.lang.ArrayUtils;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.junit.Test;

public class JavaTest {
	@Test
	public void FakeMessageTest() {
		Message FAKE_MESSAGE1 = new Message(FBUtilities.getBroadcastAddress(),
				StorageService.Verb.INTERNAL_RESPONSE,
				ArrayUtils.EMPTY_BYTE_ARRAY, -1);
		Message FAKE_MESSAGE2 = new Message(FBUtilities.getBroadcastAddress(),
				StorageService.Verb.INTERNAL_RESPONSE,
				ArrayUtils.EMPTY_BYTE_ARRAY, -2);
		
		 ConcurrentMap<Message, Integer> replies = new NonBlockingHashMap<Message, Integer>();
		 replies.put(FAKE_MESSAGE1, new Integer(1));

		 if (replies.get(FAKE_MESSAGE2) == null) {
			 System.out.println("fake message 1 and 2 is not equal");
		 }
	}
}

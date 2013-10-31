/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.cassandra.db;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.XMLDBStore;
import imc.disxmldb.dom.XMLNodeImpl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.base.XMLDBException;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.db.RowMutation.XUpdateCommand;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.net.IVerbHandler;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.hadoop.io.SequenceFile.Metadata;

public class RowMutationVerbHandler implements IVerbHandler {
	private static Logger logger_ = LoggerFactory
			.getLogger(RowMutationVerbHandler.class);

	public void doVerb(Message message, String id) {
		RowMutation rm = null;
		try {
			rm = RowMutation.fromBytes(message.getMessageBody(),
					message.getVersion());
			if (logger_.isDebugEnabled())
				logger_.debug("Applying " + rm);

			// Check if there were any forwarding headers in this message
			byte[] forwardBytes = message.getHeader(RowMutation.FORWARD_HEADER);
			if (forwardBytes != null)
				forwardToLocalNodes(message, forwardBytes);

			int modifyCount = 0;
			if (rm.getCommand() == XUpdateCommand.update) {
				CollectionStore colStore = XMLDBStore.instance().getCollection(
						rm.getColID());
				//rm = colStore.modifyUpdateMutation(rm);
			} else if (rm.getCommand() == XUpdateCommand.delete) {
				CollectionStore colStore = XMLDBStore.instance().getCollection(
						rm.getColID());
				// delete all the child nodes
				CFMetaData meta = colStore.getCfStore().getCfStore().metadata;
				int xmlID = Integer.parseInt(meta.getKeyValidator().getString(
						rm.key()));
				for (ColumnFamily cf : rm.getColumnFamilies()) {
					for (IColumn col : cf.getSortedColumns()) {
						int nodeID = Integer.parseInt(meta.getComparatorFor(
								null).getString(col.name()));
						if (colStore.deleteNodeLocal(xmlID, nodeID,
								col.getMarkedForDeleteAt()) > 0)
							modifyCount++;
					}
				}
			}
			rm.apply();

			/*
			 * if (rm.getColID() != -1) { CollectionStore.storeXMLNodeLocal(rm);
			 * }
			 */
			WriteResponse response = new WriteResponse(rm.getTable(), rm.key(),
					true);
			response.modifyCount = modifyCount;
			Message responseMessage = WriteResponse.makeWriteResponseMessage(
					message, response);
			if (logger_.isDebugEnabled())
				logger_.debug(rm + " applied.  Sending response to " + id + "@"
						+ message.getFrom());
			MessagingService.instance().sendReply(responseMessage, id,
					message.getFrom());

		} catch (IOException e) {
			logger_.error("Error in row mutation", e);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (XMLDBException e) {
			WriteResponse response = new WriteResponse(rm.getTable(), rm.key(),
					false);
			response.exMessage = e.getMessage();
			Message responseMessage = null;
			try {
				responseMessage = WriteResponse.makeWriteResponseMessage(
						message, response);
				MessagingService.instance().sendReply(responseMessage, id,
						message.getFrom());
			} catch (IOException e1) {
				// nothing to do
				logger_.error("Error in row mutation", e);
			}
		}
	}

	private void forwardToLocalNodes(Message message, byte[] forwardBytes)
			throws UnknownHostException {
		// remove fwds from message to avoid infinite loop
		message.removeHeader(RowMutation.FORWARD_HEADER);

		int bytesPerInetAddress = FBUtilities.getBroadcastAddress()
				.getAddress().length;
		assert forwardBytes.length >= bytesPerInetAddress;
		assert forwardBytes.length % bytesPerInetAddress == 0;

		int offset = 0;
		byte[] addressBytes = new byte[bytesPerInetAddress];

		// Send a message to each of the addresses on our Forward List
		while (offset < forwardBytes.length) {
			System.arraycopy(forwardBytes, offset, addressBytes, 0,
					bytesPerInetAddress);
			InetAddress address = InetAddress.getByAddress(addressBytes);

			if (logger_.isDebugEnabled())
				logger_.debug("Forwarding message to " + address);

			// Send the original message to the address specified by the
			// FORWARD_HINT
			// Let the response go back to the coordinator
			MessagingService.instance().sendOneWay(message, address);

			offset += bytesPerInetAddress;
		}
	}
}

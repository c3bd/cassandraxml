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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.io.util.FastByteArrayOutputStream;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;


/*
 * This message is sent back the row mutation verb handler 
 * and basically specifies if the write succeeded or not for a particular 
 * key in a table
 */
public class WriteResponse 
{
    private static WriteResponseSerializer serializer_ = new WriteResponseSerializer();

    public static WriteResponseSerializer serializer()
    {
        return serializer_;
    }

    public static Message makeWriteResponseMessage(Message original, WriteResponse writeResponseMessage) throws IOException
    {
    	FastByteArrayOutputStream bos = new FastByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream( bos );
        WriteResponse.serializer().serialize(writeResponseMessage, dos, original.getVersion());
        return original.getReply(FBUtilities.getBroadcastAddress(), bos.toByteArray(), original.getVersion());
    }

	private final String table_;
	private final ByteBuffer key_;
	private final boolean status_;
	public int modifyCount = 0;
	public String exMessage = null;
	
	public WriteResponse(String table, ByteBuffer key, boolean bVal) {
		table_ = table;
		key_ = key;
		status_ = bVal;
	}

	public String table()
	{
		return table_;
	}

	public ByteBuffer key()
	{
		return key_;
	}

	public boolean isSuccess()
	{
		return status_;
	}

    public static class WriteResponseSerializer implements ICompactSerializer<WriteResponse>
    {
        public void serialize(WriteResponse wm, DataOutputStream dos, int version) throws IOException
        {
            dos.writeUTF(wm.table());
            ByteBufferUtil.writeWithShortLength(wm.key(), dos);
            dos.writeBoolean(wm.isSuccess());
            if (!wm.isSuccess())
            	dos.writeUTF(wm.exMessage);
            dos.writeInt(wm.modifyCount);
        }

        public WriteResponse deserialize(DataInputStream dis, int version) throws IOException
        {
            String table = dis.readUTF();
            ByteBuffer key = ByteBufferUtil.readWithShortLength(dis);
            boolean status = dis.readBoolean();
            WriteResponse ret = new WriteResponse(table, key, status);
            if (!status)
            	ret.exMessage = dis.readUTF();
            ret.modifyCount = dis.readInt();
            return ret ;
        }
    }
}

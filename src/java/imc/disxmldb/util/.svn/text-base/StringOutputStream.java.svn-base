package imc.disxmldb.util;

import imc.disxmldb.config.SysConfig;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class StringOutputStream implements DataOutput{
	private DataOutput dop = null; 
	private StringBuilder output = new StringBuilder();
	
	public StringOutputStream() {
		//output = new ByteArrayOutputStream();
		//dop = new DataOutputStream(output);
	}
	
	public StringOutputStream(DataOutput dop) {
		this.dop = dop;
	}

	@Override
	public void writeUTF(String str) throws IOException{
		output.append(str);
		//dop.writeUTF(str);
	}

	@Override
	public String toString() {
		return output.toString();
	}
	
	/**
	 * return the size of the buffer or zero if the stream is provided
	 * @return
	 */
	public long getSize() {
		if (output != null)
			return output.length();
		else
			return 0;
	}
	
	@Override
	public void write(int arg0) throws IOException {
		//dop.write(arg0);
	}

	@Override
	public void write(byte[] arg0) throws IOException {
		//dop.write(arg0);
	}

	@Override
	public void write(byte[] arg0, int arg1, int arg2) throws IOException {
		//dop.write(arg0, arg1, arg2);
	}

	@Override
	public void writeBoolean(boolean arg0) throws IOException {
		//dop.writeBoolean(arg0);
	}

	@Override
	public void writeByte(int arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeBytes(String arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeChar(int arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeChars(String arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeDouble(double arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeFloat(float arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeInt(int arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeLong(long arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeShort(int arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}
}

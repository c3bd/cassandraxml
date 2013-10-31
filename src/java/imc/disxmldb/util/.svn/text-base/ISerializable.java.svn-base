package imc.disxmldb.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface ISerializable<Data> {

	public void serialize(Data t, DataOutputStream dos, int version)
			throws IOException ;


	public Data deserialize(DataInputStream dis, int version)
			throws IOException ;
}

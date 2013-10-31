package imc.disxmldb.cassandra.verbhandler.result;

import imc.disxmldb.cassandra.verbhandler.XPathResult.XPathResultType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.cassandra.io.ICompactSerializer;
/**
 * This is the facet class that contains the serialization and deserialization of all the 
 * funciton result class
 */
public class FunctionReturnSerializer implements
		ICompactSerializer<FunctionReturn> {
	public static Map<XPathResultType, FunctionReturnSerializer> serializers = new HashMap<XPathResultType, FunctionReturnSerializer>();
	static {
		serializers.put(XPathResultType.Average, new AvgResultSerializer());
		serializers.put(XPathResultType.Sum, new SumResultSerializer());
		serializers.put(XPathResultType.Count, new CountResultSerializer());
		serializers.put(XPathResultType.Max, new MaxResultSerializer());
		serializers.put(XPathResultType.Min, new MinResultSerializer());
		serializers.put(XPathResultType.Text, new TextResultSerializer());
		serializers.put(XPathResultType.SeqStepOne, new SeqResultSerializer());
	}
	
	@Override
	public void serialize(FunctionReturn t, DataOutputStream dos, int version)
			throws IOException {
		serializers.get(t.funcType).serialize(t, dos, version);
	}

	@Override
	public FunctionReturn deserialize(DataInputStream dis, int version)
			throws IOException {
		return serializers.get(FunctionReturn.funcTypes[dis.readInt()]).deserialize(dis, version);
	}

}

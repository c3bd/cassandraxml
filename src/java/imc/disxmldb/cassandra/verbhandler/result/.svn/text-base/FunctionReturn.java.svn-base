package imc.disxmldb.cassandra.verbhandler.result;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.cassandra.verbhandler.XPathQueryCommand;
import imc.disxmldb.cassandra.verbhandler.XPathResult.XPathResultType;
import imc.disxmldb.util.IXPathResult;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.cassandra.dht.AbstractBounds;

/**
 * This is the abstract class that defines the interface needs by an class of
 * the function result. As an xpath function may be executed on many nodes,
 * interfaces need to be defined so that the results can be merged at the
 * coordinator.
 */
public abstract class FunctionReturn implements IXPathResult<FunctionReturn> {
	/*
	 * public enum FunctionType { Count, Average, Max, Min, Text, Sum,
	 * SeqStepOne, SeqStepTwo };
	 */

	public static XPathResultType[] funcTypes = XPathResultType.values();
	public static final FunctionReturnSerializer serializer = new FunctionReturnSerializer();

	protected XPathResultType funcType = XPathResultType.Text;

	public abstract void merge(int index_, FunctionReturn other_);

	public abstract List<String> result();

	public abstract ByteBuffer getDigest();

	@Override
	public void setIndex(int index_) {

	}

	@Override
	public String postProcess(XPathQueryCommand cmd, byte[] cmdb,
			CollectionStore colStore, List<AbstractBounds> ranges) {
		return null;
	}

	@Override
	public void postProcess() {

	}

	@Override
	public void serialize(FunctionReturn t, DataOutputStream dos, int version)
			throws IOException {
		serializer.serialize(t, dos, version);
	}

	@Override
	public FunctionReturn deserialize(DataInputStream dis, int version)
			throws IOException {
		return serializer.serializers.get(funcType).deserialize(dis, version);
		//return serializer.deserialize(dis, version);
	}

	@Override
	public XPathResultType getType() {
		return funcType;
	}

	@Override
	public int size() {
		return 1;
	}
}

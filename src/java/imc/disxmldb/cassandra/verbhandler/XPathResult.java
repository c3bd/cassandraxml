package imc.disxmldb.cassandra.verbhandler;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.cassandra.verbhandler.XPathResult.XPathResultType;
import imc.disxmldb.cassandra.verbhandler.result.FunctionReturn;
import imc.disxmldb.cassandra.verbhandler.result.XPathResultFactory;
import imc.disxmldb.util.IXPathResult;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.cassandra.dht.AbstractBounds;
import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.utils.ByteBufferUtil;

public class XPathResult implements IXPathResult<XPathResult> {
	public static final XPathResultSerializer serializer = new XPathResultSerializer();

	public enum XPathResultType {
		XPathResult, NodeIDs, xmlParts, FunctionReturn, Exception, Null, Count, Average, Max, Min, Text, Sum, SeqStepOne, SeqStepTwo,NodeSketch
	};

	public static XPathResultType[] types = XPathResultType.values();

	public XPathResultType type = XPathResultType.NodeIDs;

	private IXPathResult result = null;

	private String exception = null; // 用于记录异常信息

	// currently the following fields is only used in seq function
	public int index = -1;
	public byte[] cmdb = null;
	public XPathQueryCommand cmd = null;
	public List<AbstractBounds> ranges = null;

	public CollectionStore colStore;

	public XPathResult(XPathResultType type) {
		this.type = type;
		result = XPathResultFactory.getResult(type);
	}

	public XPathResult(String exception) {
		type = XPathResultType.Exception;
		this.exception = exception;
	}

	public FunctionReturn getFuncRet() {
		return (FunctionReturn) result;
	}

	public void setFuncRet(FunctionReturn funcRet) {
		if (funcRet != null)
			type = funcRet.getType();
		this.result = funcRet;
	}

	@Override
	public ByteBuffer getDigest() {
		if (result != null)
			return result.getDigest();
		else
			return ByteBufferUtil.EMPTY_BYTE_BUFFER;
	}

	@Override
	public void merge(int index_, XPathResult result_) {
		if (result_ == null || result_.type == XPathResultType.Null)
			return;
		if (result_.type == XPathResultType.Exception) {
			setException(result_.getException());
			return;
		}

		if (type == XPathResultType.Null) {
			type = result_.type;
			result = result_.result;
			index = result_.index;
			setException(result_.getException());
		} else if (type == XPathResultType.Exception) {
			return;
		} else {
			result.merge(index_,result_.result);
		}
	}

	@Override
	public void postProcess() {
		if (result != null) {
			result.setIndex(index);
			setException(result.postProcess(cmd, cmdb, colStore, ranges));
		}
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		if (exception != null) {
			type = XPathResultType.Exception;
			this.exception = exception;
		}
	}

	public IXPathResult getResult() {
		return result;
	}

	public void setResult(IXPathResult result) {
		this.result = result;
	}

	@Override
	public void serialize(XPathResult t, DataOutputStream dos, int version)
			throws IOException {
		serializer.serialize(t, dos, version);
	}

	@Override
	public XPathResult deserialize(DataInputStream dis, int version)
			throws IOException {
		return serializer.deserialize(dis, version);
	}

	@Override
	public String postProcess(XPathQueryCommand cmd, byte[] cmdb,
			CollectionStore colStore, List<AbstractBounds> ranges) {
		if (result != null)
			return result.postProcess(cmd, cmdb, colStore, ranges);
		else
			return exception;
	}

	@Override
	public List<String> result() {
		if (exception != null) {
			return Arrays.asList(exception);
		}
		if (result == null)
			return new LinkedList<String>();
		return result.result();
	}

	@Override
	public void setIndex(int index_) {
	}

	@Override
	public XPathResultType getType() {
		return XPathResultType.XPathResult;
	}

	@Override
	public int size() {
		return result == null ? 0 : result.size();
	}
}

class XPathResultSerializer implements ICompactSerializer<XPathResult> {
	@Override
	public void serialize(XPathResult t, DataOutputStream dos, int version)
			throws IOException {
		dos.writeInt(t.type.ordinal());

		if (t.type != XPathResultType.Null
				&& t.type != XPathResultType.Exception) {
			t.getResult().serialize(t.getResult(), dos, version);
		} else {
			if (t.getException() != null)
				dos.writeUTF(t.getException());
		}
	}

	@Override
	public XPathResult deserialize(DataInputStream dis, int version)
			throws IOException {
		XPathResultType type = XPathResult.types[dis.readInt()];
		XPathResult result = new XPathResult(type);

		if (type == XPathResultType.Exception) {
			result.setException(dis.readUTF());
		} else if (type != XPathResultType.Null) {
			result.setResult((IXPathResult) result.getResult().deserialize(dis, version));
		}

		return result;
	}

}

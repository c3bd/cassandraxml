/**
 *@author:xiafan xiafan68@gmail.com
 *@version: 2011-10-3 0.1
 */
package imc.disxmldb.util;

import imc.disxmldb.config.DataFlushServiceFactory;
import imc.disxmldb.config.MutationFlushService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.db.ReadCommand;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.RowMutation;
import org.apache.cassandra.db.SliceByNamesReadCommand;
import org.apache.cassandra.db.SliceFromReadCommand;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.MarshalException;
import org.apache.cassandra.service.StorageProxy;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.cassandra.utils.ByteBufferUtil;

/**
 * encapsulate the interface of the underlying Cassandra interface to provide a 
 * simple and neat interface.
 */
public class ColumnFamilyStoreProxy {
	public static int TTL = 60 * 60 * 24 * 365;

	public String keyspace;
	public String cfName;
	private AbstractType spComparator = null;
	private AbstractType subComparator = null;
	private MutationFlushService flushService = DataFlushServiceFactory.instance
			.getMetaDataFlushService();

	public ColumnFamilyStoreProxy(String keyspace, String cfName,
			MutationFlushService flushService) {
		this.keyspace = keyspace;
		this.cfName = cfName;
		this.flushService = flushService;
		CFMetaData cfMeta = Schema.instance.getCFMetaData(keyspace, cfName);
		spComparator = cfMeta.getComparatorFor(null);
		subComparator = cfMeta
				.getComparatorFor(ByteBufferUtil.EMPTY_BYTE_BUFFER);
	}

	public ColumnFamilyStoreProxy(String keyspace, String cfName) {
		this.keyspace = keyspace;
		this.cfName = cfName;
		CFMetaData cfMeta = Schema.instance.getCFMetaData(keyspace, cfName);
		spComparator = cfMeta.getComparatorFor(null);
		subComparator = cfMeta
				.getComparatorFor(ByteBufferUtil.EMPTY_BYTE_BUFFER);
	}

	public boolean insert(ByteBuffer key, ByteBuffer spColName,
			ByteBuffer colName, ByteBuffer value,
			ConsistencyLevel consistency_level) {
		assert (colName != null && value != null);

		RowMutation rm = new RowMutation(keyspace, key);

		rm.add(new QueryPath(cfName, spColName, colName), value,
				System.currentTimeMillis());

		return flushService.flush(rm, consistency_level);
	}

	public RowMutation batch_insert(ByteBuffer key, ByteBuffer spColName,
			ByteBuffer colName, ByteBuffer value) {
		assert (colName != null && value != null);

		RowMutation rm = new RowMutation(keyspace, key);
		rm.add(new QueryPath(cfName, spColName, colName), value,
				System.currentTimeMillis());
		return rm;
	}

	public RowMutation incre_insert(ByteBuffer key, ByteBuffer spColName,
			ByteBuffer colName, ByteBuffer value, RowMutation rm) {
		assert (colName != null && value != null);
		if (rm == null)
			rm = new RowMutation(keyspace, key);
		rm.add(new QueryPath(cfName, spColName, colName), value,
				System.currentTimeMillis());
		return rm;
	}

	public boolean mutate(RowMutation rm, ConsistencyLevel consistency_level) {
		return flushService.flush(rm, consistency_level);
	}

	public void batch_mutate(List<RowMutation> mutations,
			ConsistencyLevel consistency_level) throws UnavailableException,
			TimeoutException {
		StorageProxy.mutate(mutations, consistency_level);
	}

	/**
	 * 
	 * @param key
	 * 
	 * @param spColName
	 *            supercolumn name
	 * @param colName
	 *            column name
	 * @param value
	 *            the value of the correspond column
	 * @param consistency_level
	 * @throws InvalidRequestException
	 * @throws UnavailableException
	 * @throws TimedOutException
	 */
	public boolean insert(ByteBuffer key, String spColName, String colName,
			ByteBuffer value, ConsistencyLevel consistency_level)
			throws InvalidRequestException, UnavailableException,
			TimeoutException {
		assert (colName != null && value != null);

		RowMutation rm = new RowMutation(keyspace, key);
		try {
			ByteBuffer spColBuffer = null;
			ByteBuffer colBuffer = null;
			if (spColName != null) {
				spColBuffer = spComparator.fromString(spColName);
				if (subComparator != null)
					colBuffer = subComparator.fromString(colName);
			} else {
				colBuffer = spComparator.fromString(colName);
			}

			return insert(key, spColBuffer, colBuffer, value, consistency_level);
		} catch (MarshalException e) {
			throw new InvalidRequestException(e.getMessage());
		}
	}

	public boolean delete(ByteBuffer key, String spColName, String colName,
			ConsistencyLevel consistency_level) {
		assert (colName != null);
		RowMutation rm;
		ByteBuffer spColBuffer = null;
		ByteBuffer colBuffer = null;
		if (spColName != null) {
			spColBuffer = spComparator.fromString(spColName);
			if (subComparator != null)
				colBuffer = subComparator.fromString(colName);
		} else {
			colBuffer = spComparator.fromString(colName);
		}
		rm = incre_delete(key, spColBuffer, colBuffer, null);
		return flushService.flush(rm, consistency_level);
	}

	/**
	 * delete a column. if colName is null, the entire super column will be
	 * deleted
	 * 
	 * @param key
	 * @param spColName
	 * @param colName
	 * @param rm
	 * @return
	 */
	public RowMutation incre_delete(ByteBuffer key, ByteBuffer spColName,
			ByteBuffer colName, RowMutation rm) {
		if (spColName == null)
			return rm;

		if (rm == null)
			rm = new RowMutation(keyspace, key);
		rm.delete(new QueryPath(cfName, spColName, colName),
				System.currentTimeMillis());
		return rm;
	}

	public RowMutation incre_delete(ByteBuffer key, String spColName,
			String colName, RowMutation rm) {
		if (rm == null)
			rm = new RowMutation(keyspace, key);
		ByteBuffer spBuffer = null, colBuffer = null;

		if (spComparator != null)
			spBuffer = spComparator.fromString(spColName);
		if (colName != null && subComparator != null)
			colBuffer = subComparator.fromString(colName);
		return incre_delete(key, spBuffer, colBuffer, rm);
	}
	
	/**
	 * 
	 * @param key
	 * @param spName
	 *            the name of the super column
	 * @param colName
	 *            the name of the sub column, if null, a super column will be
	 *            selected
	 * @return
	 */
	public Row get(ByteBuffer key, ByteBuffer spName, ByteBuffer colName,
			ConsistencyLevel consistency_level) {
		if (colName == null && spName != null) {
			SortedSet<ByteBuffer> colNames = new TreeSet<ByteBuffer>();
			colNames.add(spName);
			return getSliceByNames(key, null, colNames, consistency_level);
		} else if (colName == null && spName == null) {
			return getSliceByRange(key, spName,
					ByteBufferUtil.EMPTY_BYTE_BUFFER,
					ByteBufferUtil.EMPTY_BYTE_BUFFER, false, consistency_level);
		} else {
			SortedSet<ByteBuffer> colNames = new TreeSet<ByteBuffer>();
			colNames.add(colName);
			return getSliceByNames(key, spName, colNames, consistency_level);
		}
	}

	/**
	 * choose subcolumn columns if both spName and colNames are not null,
	 * otherwise, standard columns or super columns are choose according to the
	 * SortedSet
	 * 
	 * @param key
	 * @param spName
	 *            the the name of the super column
	 * @param colNames
	 * @return
	 */
	public Row getSliceByNames(ByteBuffer key, ByteBuffer spName,
			SortedSet<ByteBuffer> colNames, ConsistencyLevel consistency_level) {
		QueryPath path = new QueryPath(cfName, spName, null);
		ReadCommand command = new SliceByNamesReadCommand(keyspace, key, path,
				colNames);
		try {
			List<Row> rows = StorageProxy.read(Arrays.asList(command),
					consistency_level);
			if (rows.size() > 0)
				return rows.get(0);
			else
				return null;
		} catch (Exception e) {
			return null;
		}

	}

	public Row getSliceByRange(ByteBuffer key, ByteBuffer spName,
			ByteBuffer lower, ByteBuffer upper, boolean reversed,
			ConsistencyLevel consistency_level) {
		QueryPath path = new QueryPath(cfName, spName);

		ReadCommand command = new SliceFromReadCommand(keyspace, key, path,
				lower, upper, reversed, Integer.MAX_VALUE);

		List<Row> rows = null;
		try {
			rows = StorageProxy.read(Arrays.asList(command), consistency_level);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		if (rows.size() > 0)
			return rows.get(0);
		return null;

	}

	public List<Row> getSuperColumn(ByteBuffer key, String spName,
			String colName, ConsistencyLevel consistency_level) {

		QueryPath path = new QueryPath(cfName, colName == null ? null
				: spComparator.fromString(spName));
		List<ByteBuffer> nameAsList = Arrays
				.asList(colName == null ? spComparator.fromString(spName)
						: subComparator.fromString(colName));
		ReadCommand command = new SliceByNamesReadCommand(keyspace, key, path,
				nameAsList);
		try {
			return StorageProxy.read(Arrays.asList(command), consistency_level);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public List<Row> getSuperColumn(ByteBuffer key, ByteBuffer spName,
			ByteBuffer colName, ConsistencyLevel consistency_level) {

		QueryPath path = new QueryPath(cfName, colName == null ? null : spName);
		List<ByteBuffer> nameAsList = Arrays.asList(colName == null ? spName
				: colName);
		ReadCommand command = new SliceByNamesReadCommand(keyspace, key, path,
				nameAsList);
		try {
			return StorageProxy.read(Arrays.asList(command), consistency_level);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public List<Row> getStandardColumn(ByteBuffer key, List<String> colNames,
			ConsistencyLevel consistency_level) {
		QueryPath path = new QueryPath(cfName, null);
		List<ByteBuffer> nameAsList = new ArrayList<ByteBuffer>();
		for (String colName : colNames) {
			nameAsList.add(ByteBufferUtil.bytes(colName));
		}

		ReadCommand command = new SliceByNamesReadCommand(keyspace, key, path,
				nameAsList);
		try {
			return StorageProxy.read(Arrays.asList(command), consistency_level);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidRequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public List<Row> getSuperColumnSlice(ByteBuffer key, List<String> spCol,
			List<String> subCols, ConsistencyLevel consistency_level) {
		return null;
	}
}

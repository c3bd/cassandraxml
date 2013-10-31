/**
 *@author:xiafan xiafan68@gmail.com
 *@version: 2011-10-6 0.1
 */
package imc.disxmldb.util;

import java.nio.ByteBuffer;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.db.Column;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.SuperColumn;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.utils.ByteBufferUtil;
/**
 * encapsulate the Cassandra API to manipulate the local data.
 * @author xiafan
 *
 */
public class LocalCFStoreProxy {
	private ColumnFamilyStore cfStore = null;
	private Table table = null;

	public AbstractType keyValidator = null;
	public AbstractType comparator = null;
	public AbstractType subComparator = null;
	/*
	 * private LocalPartitioner partitioner = new LocalPartitioner(
	 * UTF8Type.instance);
	 */

	public LocalCFStoreProxy(Table table, ColumnFamilyStore cfStore) {
		this.cfStore = cfStore;
		this.table = table;
		this.keyValidator = cfStore.metadata.getKeyValidator();
		this.comparator = cfStore.metadata.getComparatorFor(null);
		this.subComparator = cfStore.metadata.getComparatorFor(ByteBufferUtil.EMPTY_BYTE_BUFFER);
	}

	public LocalCFStoreProxy(String keyspace, String cfName) {
		table = Table.open(keyspace);
		cfStore = table.getColumnFamilyStore(cfName);
	}

	public void insert(String key, String spName, String colName,
			ByteBuffer value, long timestamp) {
		if (spName != null) {
			insert(keyValidator.fromString(key),
					comparator.fromString(spName),
					subComparator.fromString(
							colName), value, timestamp);
		} else {
			insert(keyValidator.fromString(key), null, cfStore.metadata
					.getComparatorFor(null).fromString(colName), value,
					timestamp);
		}
	}

	/**
	 * insert to column into the columnfamily, which can be either standard or
	 * super
	 * 
	 * @param key
	 * @param spName
	 * @param colName
	 * @param value
	 * @param timestamp
	 */
	public void insert(ByteBuffer key, String spName, String colName,
			ByteBuffer value, long timestamp) {
		if (spName != null) {
			insert(key,
					comparator.fromString(spName),
					subComparator.fromString(
							colName), value, timestamp);
		} else {
			insert(key, null, comparator
					.fromString(colName), value, timestamp);
		}
	}

	public void insert(ByteBuffer key, ByteBuffer spName, ByteBuffer colName,
			ByteBuffer value, long timestamp) {
		DecoratedKey dKey = cfStore.partitioner.decorateKey(key);
		ColumnFamily cf = ColumnFamily.create(cfStore.metadata);
		Column col = new Column(colName, value, timestamp);
		if (spName != null) {
			cf.addColumn(spName, col);
		} else {
			cf.addColumn(col);
		}
		cfStore.apply(dKey, cf);
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
	public Row get(ByteBuffer key, ByteBuffer spName, ByteBuffer colName) {
		if (colName == null && spName != null) {
			SortedSet<ByteBuffer> colNames = new TreeSet<ByteBuffer>();
			colNames.add(spName);
			return getSliceByNames(key, null, colNames);
		} else if (colName == null && spName == null) {
			return getSliceByRange(key, spName,
					ByteBufferUtil.EMPTY_BYTE_BUFFER,
					ByteBufferUtil.EMPTY_BYTE_BUFFER);
		} else {
			SortedSet<ByteBuffer> colNames = new TreeSet<ByteBuffer>();
			colNames.add(colName);
			return getSliceByNames(key, spName, colNames);
		}
	}

	/**
	 * choose subcolumn columns if both spName and colNames are not null, otherwise,
	 * standard columns or super columns are choose according to the SortedSet
	 * 
	 * @param key
	 * @param spName
	 *            the the name of the super column
	 * @param colNames
	 * @return
	 */
	public Row getSliceByNames(ByteBuffer key, ByteBuffer spName,
			SortedSet<ByteBuffer> colNames) {
		DecoratedKey<?> dk = cfStore.partitioner.decorateKey(key);
		QueryPath queryPath = new QueryPath(cfStore.getColumnFamilyName(),
				spName);
		ColumnFamily cf = cfStore.getColumnFamily(QueryFilter.getNamesFilter(
				dk, queryPath, colNames));
		Row row = new Row(cfStore.partitioner.decorateKey(key), cf);
		return row;
	}

	public Row getSliceByRange(ByteBuffer key, ByteBuffer spName,
			ByteBuffer lower, ByteBuffer upper) {
		DecoratedKey<?> dk = cfStore.partitioner.decorateKey(key);
		QueryPath queryPath = new QueryPath(cfStore.getColumnFamilyName(),
				spName);

		ColumnFamily cf = cfStore.getColumnFamily(QueryFilter.getSliceFilter(
				dk, queryPath, lower, upper, false, 10000000));
		Row row = new Row(cfStore.partitioner.decorateKey(key), cf);

		return row;

	}

	public void remove(String key, String spName, String colName,
			ByteBuffer value) {
		DecoratedKey dKey = cfStore.partitioner.decorateKey(keyValidator.fromString(key));
		CFMetaData metaData = cfStore.metadata;
		ColumnFamily cf = ColumnFamily.create(cfStore.metadata);
		
		int localDeleteTime = (int) (System.currentTimeMillis() / 1000);
		long timestamp = System.currentTimeMillis();
		if (spName == null && colName == null) {
			cf.delete(localDeleteTime, timestamp);
		} else if (colName == null) {
			SuperColumn sc = new SuperColumn(comparator.fromString(spName),
					comparator);
			sc.delete(localDeleteTime, timestamp);
			cf.addColumn(sc);
		} else {
			QueryPath path = new QueryPath(metaData.cfName, comparator.fromString(spName), subComparator.fromString(colName));
			cf.addTombstone(path, localDeleteTime, timestamp);
		}
		
		cfStore.apply(dKey, cf);
	}

	public AbstractType getKeyValidator() {
		return cfStore.metadata.getKeyValidator();
	}

	public void flush() throws ExecutionException, InterruptedException {
		cfStore.forceBlockingFlush();
	}

	public ColumnFamilyStore getCfStore() {
		return cfStore;
	}

	public void setCfStore(ColumnFamilyStore cfStore) {
		this.cfStore = cfStore;
	}

}

/**
 *@author:xiafan xiafan68@gmail.com
 *@version: 2011-10-7 0.1
 */
package imc.disxmldb.util;

import imc.disxmldb.CollectionStore;
import imc.disxmldb.config.CollectionMetaData;
import imc.disxmldb.config.SysConfig;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.cassandra.auth.Permission;
import org.apache.cassandra.concurrent.Stage;
import org.apache.cassandra.concurrent.StageManager;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.ColumnFamilyType;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.db.marshal.DoubleType;
import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.db.migration.AddColumnFamily;
import org.apache.cassandra.db.migration.DropColumnFamily;
import org.apache.cassandra.db.migration.Migration;
import org.apache.cassandra.dht.LocalPartitioner;
import org.apache.cassandra.service.StorageProxy;
import org.apache.cassandra.thrift.CfDef;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.SchemaDisagreementException;
import org.apache.cassandra.thrift.ThriftValidation;
import org.apache.thrift.TException;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;

/**
 * This class is used to create all the meta data related to Cassandra. The XML
 * document data of a collection is stored in a column family. The column family
 * is also based on column family. The schema of these column family is created
 * by this class.
 * 
 */
public class CassandraSchemaProxy {
	public static final AbstractType invIndexCFSubComparator = getInvertIndexSubComparator();

	public static CFMetaData createCollectionCFMetaData(String cfName) {
		CFMetaData cfMeta = new CFMetaData(SysConfig.DEFAULT_KEYSPACE, cfName,
				ColumnFamilyType.Super, Int32Type.instance, UTF8Type.instance);
		cfMeta.keyCacheSize(SysConfig.COLCF_KEYCACHE)
				.keyCacheSavePeriod(SysConfig.COLCF_KEY_CACHE_SAVE_PERIOD)
				.keyValidator(CollectionStore.getColKeyValidator())
				.rowCacheSize(SysConfig.COLCF_ROWCACHE)
				.rowCacheSavePeriod(SysConfig.COLCF_ROW_CACHE_SVAE_PERIOD)
				.maxCompactionThreshold(SysConfig.COLCF_MAXCOMPACTIONTHRESHOLD)
				.minCompactionThreshold(SysConfig.COLCF_MINCOMPACTIONTHRESHOLD);

		return cfMeta;
	}

	public static CFMetaData createCollectionCFMetaData(
			CollectionMetaData metaData) {
		CFMetaData cfMeta = new CFMetaData(SysConfig.DEFAULT_KEYSPACE,
				metaData.getCFName(), ColumnFamilyType.Super,
				Int32Type.instance, UTF8Type.instance);
		cfMeta.keyCacheSize(SysConfig.COLCF_KEYCACHE)
				.keyCacheSavePeriod(SysConfig.COLCF_KEY_CACHE_SAVE_PERIOD)
				.keyValidator(CollectionStore.getColKeyValidator())
				.rowCacheSize(SysConfig.COLCF_ROWCACHE)
				.rowCacheSavePeriod(SysConfig.COLCF_ROW_CACHE_SVAE_PERIOD)
				.maxCompactionThreshold(SysConfig.COLCF_MAXCOMPACTIONTHRESHOLD)
				.minCompactionThreshold(SysConfig.COLCF_MINCOMPACTIONTHRESHOLD);
		return cfMeta;
	}

	public static ColumnFamilyStore createInvertIndexCFMetaData(
			String keyspace, String idxName) {
		CFMetaData indexedCfMetadata = new CFMetaData(keyspace, idxName,
				ColumnFamilyType.Super, getInvertIndexSuperComparator(),
				getInvertIndexSubComparator())
				.keyValidator(UTF8Type.instance)
				.rowCacheSize(1000)
				.readRepairChance(0.0)
				.gcGraceSeconds(SysConfig.INVINDEX_GCGRACESECONDS)
				.minCompactionThreshold(
						SysConfig.INVINDEX_MINCOMPACTIONTHRESHOLD)
				.maxCompactionThreshold(
						SysConfig.INVINDEX_MAXCOMPACTIONTHRESHOLD);
		ColumnFamilyStore indexedCfs = ColumnFamilyStore
				.createColumnFamilyStore(Table.open(keyspace),
						indexedCfMetadata.cfName, new LocalPartitioner(
								UTF8Type.instance), indexedCfMetadata);
		return indexedCfs;
	}

	public static ColumnFamilyStore createInvertIndexCFMetaData(Table table,
			String idxName) {
		CFMetaData indexedCfMetadata = new CFMetaData(table.name, idxName,
				ColumnFamilyType.Super, getInvertIndexSuperComparator(),
				getInvertIndexSubComparator())
				.keyValidator(UTF8Type.instance)
				.rowCacheSize(1000)
				.readRepairChance(0.0)
				.gcGraceSeconds(SysConfig.INVINDEX_GCGRACESECONDS)
				.minCompactionThreshold(
						SysConfig.INVINDEX_MINCOMPACTIONTHRESHOLD)
				.maxCompactionThreshold(
						SysConfig.INVINDEX_MAXCOMPACTIONTHRESHOLD);
		ColumnFamilyStore indexedCfs = ColumnFamilyStore
				.createColumnFamilyStore(table, indexedCfMetadata.cfName,
						new LocalPartitioner(UTF8Type.instance),
						indexedCfMetadata);
		return indexedCfs;
	}

	public static ColumnFamilyStore createBtreeIndexCFMetaData(Table table,
			String idxName) {
		CFMetaData indexedCfMetadata = new CFMetaData(table.name, idxName,
				ColumnFamilyType.Super, getInvertIndexSuperComparator(),
				getInvertIndexSubComparator())
				.keyValidator(UTF8Type.instance)
				.rowCacheSize(1000)
				.readRepairChance(0.0)
				.gcGraceSeconds(SysConfig.INVINDEX_GCGRACESECONDS)
				.minCompactionThreshold(
						SysConfig.INVINDEX_MINCOMPACTIONTHRESHOLD)
				.maxCompactionThreshold(
						SysConfig.INVINDEX_MAXCOMPACTIONTHRESHOLD);
		ColumnFamilyStore indexedCfs = ColumnFamilyStore
				.createColumnFamilyStore(table, indexedCfMetadata.cfName,
						new LocalPartitioner(UTF8Type.instance),
						indexedCfMetadata);
		return indexedCfs;
	}

	public static AbstractType getInvertIndexSuperComparator() {
		return Int32Type.instance;
	}

	public static AbstractType getInvertIndexSubComparator() {
		List<AbstractType> types = new LinkedList<AbstractType>();
		types.add(DoubleType.instance);
		types.add(DoubleType.instance);
		try {
			CompositeType ret = CompositeType.getInstance(types);
			return ret;
		} catch (ConfigurationException e) {
			assert true : "this should never happen";
			return null;
		}
	}

	/**
	 * create a new column family.when the function returns, it is not confirmed
	 * that all nodes have reach a consistent state, but it will tries to wait
	 * for at most 2000ms
	 * 
	 * @param metaData
	 * @return
	 * @throws InvalidRequestException
	 * @throws SchemaDisagreementException
	 */
	public static synchronized String system_add_column_family(
			CFMetaData metaData) throws InvalidRequestException,
			SchemaDisagreementException {
		// this is necessary;
		validateSchemaAgreement();
		try {
			applyMigrationOnStage(new AddColumnFamily(metaData));

			boolean consistent = false;
			for (int i = 0; i < SysConfig.FAILURE_RETRY && !consistent; i++) {
				try {
					validateSchemaAgreement();
					consistent = true;
				} catch (SchemaDisagreementException ex) {
					try {
						Thread.sleep(SysConfig.RETRY_INTERVAL_INMS);
					} catch (InterruptedException e) {
						// nothing todo
					}
				}
			}

			return Schema.instance.getVersion().toString();
		} catch (ConfigurationException e) {
			InvalidRequestException ex = new InvalidRequestException(
					e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (IOException e) {
			InvalidRequestException ex = new InvalidRequestException(
					e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}

	public static synchronized String system_drop_column_family(
			String keyspace, String column_family)
			throws InvalidRequestException, SchemaDisagreementException,
			TException {
		validateSchemaAgreement();

		try {
			applyMigrationOnStage(new DropColumnFamily(keyspace, column_family));
			boolean consistent = false;
			for (int i = 0; i < SysConfig.FAILURE_RETRY && !consistent; i++) {
				try {
					validateSchemaAgreement();
					consistent = true;
				} catch (SchemaDisagreementException ex) {
					try {
						Thread.sleep(SysConfig.RETRY_INTERVAL_INMS);
					} catch (InterruptedException e) {
						// nothing todo
					}
				}
			}
			return Schema.instance.getVersion().toString();
		} catch (ConfigurationException e) {
			InvalidRequestException ex = new InvalidRequestException(
					e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (IOException e) {
			InvalidRequestException ex = new InvalidRequestException(
					e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}

	// helper method to apply migration on the migration stage. typical
	// migration failures will throw an
	// InvalidRequestException. atypical failures will throw a RuntimeException.
	private static void applyMigrationOnStage(final Migration m) {
		Future f = StageManager.getStage(Stage.MIGRATION).submit(
				new Callable() {
					public Object call() throws Exception {
						m.apply();
						m.announce();
						return null;
					}
				});
		try {
			f.get();
		} catch (InterruptedException e) {
			throw new AssertionError(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private static void validateSchemaAgreement()
			throws SchemaDisagreementException {
		// unreachable hosts don't count towards disagreement
		Map<String, List<String>> versions = Maps.filterKeys(
				StorageProxy.describeSchemaVersions(),
				Predicates.not(Predicates.equalTo(StorageProxy.UNREACHABLE)));
		if (versions.size() > 1)
			throw new SchemaDisagreementException();
	}
}

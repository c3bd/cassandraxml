/**
 *@author:xiafan xiafan68@gmail.com
 *@version: 2011-10-3 0.1
 */
package imc.disxmldb.config;

import java.nio.charset.CharacterCodingException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.cassandra.utils.ByteBufferUtil;

import imc.disxmldb.util.ColumnFamilyStoreSerializable;
import imc.disxmldb.util.ColumnFamilyStoreProxy;

/**
 * this metadata is stored in a standard column family
 * 
 * @author Administrator
 * 
 */
public class XMLSchemaMetaData implements ColumnFamilyStoreSerializable {
	// public static final String BASEINFO_NAME = "name";
	public static final String BASEINFO_CREATEDATE = "createdate";
	public static final String BASEINFO_MODIFYDATE = "modifydate";
	public static final String BASEINFO_SCHEMA = "schema";

	private String schemaName = null;
	private String schema;
	private long createDate = -1;
	private long modifyDate = -1;

	private boolean isDirty = true;

	public XMLSchemaMetaData(String schemaName) {
		this.schemaName = schemaName;
	}

	/**
	 * used to create a new schema
	 * 
	 * @param schemaName
	 * @param schema
	 * @param createDate
	 * @param modifyDate
	 */
	public XMLSchemaMetaData(String schemaName, String schema, long createDate,
			long modifyDate) {
		this.schemaName = schemaName;
		this.schema = schema;
		this.createDate = createDate;
		this.modifyDate = modifyDate;
	}

	public boolean isDirty() {
		return isDirty;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName, boolean flush) {
		this.schemaName = schemaName;
		setDirty(true, flush);
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema, boolean flush) {
		this.schema = schema;
		setDirty(true, flush);
	}

	public long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(long createDate, boolean flush) {
		this.createDate = createDate;
		setDirty(true, flush);
	}

	public long getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(long modifyDate, boolean flush) {
		this.modifyDate = modifyDate;
		setDirty(true, flush);
	}

	public void setDirty(boolean isDirty, boolean flush) {
		if (isDirty == false) {
			this.isDirty = isDirty;
		} else if (isDirty == true && this.isDirty == false) {
			this.isDirty = isDirty;
			if (flush)
				XMLDBCatalogManager.instance().flushDirtySchemaMetaData(this);
		}
	}

	@Override
	public void serialize(ColumnFamilyStoreProxy cfStore) {
		setDirty(false, false);
		try {
			cfStore.insert(ByteBufferUtil.bytes(schemaName), null,
					BASEINFO_SCHEMA, ByteBufferUtil.bytes(schema),
					XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL);

			cfStore.insert(ByteBufferUtil.bytes(schemaName), null,
					BASEINFO_CREATEDATE, ByteBufferUtil.bytes(createDate),
					XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL);

			cfStore.insert(ByteBufferUtil.bytes(schemaName), null,
					BASEINFO_MODIFYDATE, ByteBufferUtil.bytes(modifyDate),
					XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL);
		} catch (InvalidRequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void deserialize(ColumnFamilyStoreProxy cfStore) {
		setDirty(false, false);
		String[] cols = new String[] { BASEINFO_CREATEDATE,
				BASEINFO_MODIFYDATE, BASEINFO_SCHEMA };
		List<Row> rows = cfStore.getStandardColumn(
				ByteBufferUtil.bytes(schemaName), Arrays.asList(cols),
				XMLDBCatalogManager.CATALOG_CONSISTENCYLEVEL);

		if (rows == null || rows.size() == 0) {
			return;
		} else {
			try {
				for (Row row : rows) {
					if (row.cf == null)
						continue;
					IColumn col = row.cf.getColumn(ByteBufferUtil
							.bytes(BASEINFO_SCHEMA));
					schema = ByteBufferUtil.string(col.value());

					col = row.cf.getColumn(ByteBufferUtil
							.bytes(BASEINFO_CREATEDATE));
					createDate = ByteBufferUtil.toLong(col.value());

					col = row.cf.getColumn(ByteBufferUtil
							.bytes(BASEINFO_MODIFYDATE));
					modifyDate = ByteBufferUtil.toLong(col.value());
				}
			} catch (CharacterCodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

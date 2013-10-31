/**
 *@author:xiafan xiafan68@gmail.com
 *@version: 2011-10-3 0.1
 */
package imc.disxmldb.config;
import java.util.ArrayList;
import java.util.List;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.locator.SimpleStrategy;
import org.apache.cassandra.thrift.ConsistencyLevel;

/**
 * this class defines all the parameters used to configure the system.
 * @author xiafan
 *
 */
public class SysConfig {

	public final static int MATCH_EXACT = 0;

	public final static int MATCH_REGEXP = 1;

	public final static int MATCH_WILDCARDS = 2;

	public final static String ROOT_COLLECTION_NAME = "db";

	public final static String ROOT_COLLECTION = "/" + ROOT_COLLECTION_NAME;

	public final static String SYSTEM_COLLECTION_NAME = "system";

	public final static String SYSTEM_COLLECTION = ROOT_COLLECTION + "/"
			+ SYSTEM_COLLECTION_NAME;

	public final static String TEMP_COLLECTION_NAME = "temp";

	public final static String TEMP_COLLECTION = SYSTEM_COLLECTION + "/"
			+ TEMP_COLLECTION_NAME;

	// TODO : move elsewhere
	public static final String CONFIGURATION_ELEMENT_NAME = "xupdate";

	// TODO : move elsewhere
	public final static String XUPDATE_FRAGMENTATION_FACTOR_ATTRIBUTE = "allowed-fragmentation";

	// TODO : move elsewhere
	public final static String PROPERTY_XUPDATE_FRAGMENTATION_FACTOR = "xupdate.fragmentation";

	// TODO : move elsewhere
	public final static String XUPDATE_CONSISTENCY_CHECKS_ATTRIBUTE = "enable-consistency-checks";

	// TODO : move elsewhere
	public final static String PROPERTY_XUPDATE_CONSISTENCY_CHECKS = "xupdate.consistency-checks";

	public final static String CONFIG_COLLECTION = SYSTEM_COLLECTION
			+ "/config";
	public final static String COLLECTION_CONFIG_FILENAME = "collection.xconf";

	public static final String ENCODING = "UTF-8";
	public static final String STOREXMLMUTATION = "sx";
	public static final String STORE_MUTATION_COLID = "colID";

	// config of keyspace for data and root collection
	public static int startSystemColID = 7;
	public static final String DEFAULT_KEYSPACE = "xmldb";
	public static final Class DEFAULT_KEYSPACE_PARTITIONER = SimpleStrategy.class;
	//public static final int DATA_KEYSPACE_REPLICAS = 1;
	public static final int ROOT_COLLECTION_ID = 0;
	public static final CollectionMetaData rootColMeta = new CollectionMetaData(
			ROOT_COLLECTION_ID, ROOT_COLLECTION_NAME, null);
	public static final CFMetaData rootColCf = CFMetaData
			.newXMLCollectionMetaData(rootColMeta.getCFName(), startSystemColID++,
					"meta data for root collection");

	// column family definition for XML system catalog
	public static final String SYSCATALOG = "xmldb_sys_catalog";
	public static final String SYSCATALOG_COLLECTION = "colcatalog";
	public static final String SYSCATALOG_XML = "xmlcatalog";
	public static final String SYSCATALOG_SCHEMA = "schemaCatalog";
	public static final String SYSCATALOG_USER = "user";
	

	public static final CFMetaData CollectionCf = CFMetaData
			.newXMLSystemMetadata(SYSCATALOG_COLLECTION, startSystemColID++,
					"for collection", Int32Type.instance, UTF8Type.instance,
					UTF8Type.instance);
	public static final CFMetaData XMLSchemaCf = CFMetaData
			.newXMLSystemMetadata(SYSCATALOG_SCHEMA, startSystemColID++,
					"the catalog of xml schema", UTF8Type.instance,
					UTF8Type.instance, null);

	public static final AbstractType xmlCatalogKeyValidator = getXMLCatalogKeyValidator();
	public static final CFMetaData XMLCf = CFMetaData.newXMLSystemMetadata(
			SYSCATALOG_XML, startSystemColID++, "catalog of the xml doc",
			xmlCatalogKeyValidator, UTF8Type.instance, UTF8Type.instance);
	public static final CFMetaData UserCf = CFMetaData.newXMLSystemMetadata(
			SYSCATALOG_USER, startSystemColID++, "catalog of user profile",
			UTF8Type.instance, UTF8Type.instance, null);

	public static final ConsistencyLevel XMLSTORE_CONSISTENCY_LEVEL = ConsistencyLevel.ALL;
	public static final ConsistencyLevel XMLREAD_CONSISTENCY_LEVEL = ConsistencyLevel.ONE;

	// public static final int BTREE_CACHE_SIZE = 128 * 1024;
	// configuration for
	public static final double COLCF_KEYCACHE = 0.99;
	public static final double COLCF_ROWCACHE = 10000;
	public static final int COLCF_ROW_CACHE_SVAE_PERIOD = 0;
	public static final int COLCF_KEY_CACHE_SAVE_PERIOD = 3600;
	public static final int COLCF_MINCOMPACTIONTHRESHOLD = 32;
	public static final int COLCF_MAXCOMPACTIONTHRESHOLD = 256;

	// configuration for invert index
	public static final double INVINDEXCF_KEYCACHE = 0.99;
	public static final double INVINDEXCF_ROWCACHE = 100000;
	public static final int INVINDEXCF_ROW_CACHE_SVAE_PERIOD = 0;
	public static final int INVINDEXCF_KEY_CACHE_SAVE_PERIOD = 3600;
	public static final double INVINDEXCF_READ_REPAIR_CHANCE = 0.0; // disable
																	// read
																	// repair
	public static final int INVINDEX_GCGRACESECONDS = 100;
	public static final int INVINDEX_MINCOMPACTIONTHRESHOLD = 32;
	public static final int INVINDEX_MAXCOMPACTIONTHRESHOLD = 256;

	// configuration for sax validation
	public static final String SAX_NAMESPACES = "http://xml.org/sax/features/namespaces";
	public static final String SAX_NAMESPACES_PREFIX = "http://xml.org/sax/features/namespace-prefixes";
	public static final String SAX_VALIDATION = "http://xml.org/sax/features/validation";
	public static final String APACHE_VALIDATION = "http://apache.org/xml/features/validation/schema";
	public static final String APACHE_VALIDATION_DYNAMIC = "http://apache.org/xml/features/validation/dynamic";

	// split xml doc
	public static final int SPLIT_THRESHOLD = 10000; // 10000 nodes per xml part
	public static final double DEFAULT_RANGE_SIZE = 50.0;

	public static final int XML_TRYLOCK_TIME = 10000; // 10s
	public static final int FAILURE_RETRY = 10; // the retry number when
												// something fails
	public static final long RETRY_INTERVAL_INMS = 200;
	
	public static AbstractType getXMLCatalogKeyValidator() {
		List<AbstractType> colKey_type = new ArrayList<AbstractType>();
		colKey_type.add(Int32Type.instance);
		colKey_type.add(Int32Type.instance);
		CompositeType ret = null;
		try {
			ret = CompositeType.getInstance(colKey_type);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
}

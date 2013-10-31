package imc.disxmldb.config;

import junit.framework.Assert;

import org.apache.cassandra.db.ColumnFamilyStore;
import org.junit.Test;

public class ParseColIDFromCFNameTest {
	@Test
	public void test() {
		String cfName = CollectionMetaData.COLDATA_CFNAME_PREFIX + "12";
		Assert.assertEquals(12, ColumnFamilyStore.getColID(cfName));
		
		cfName = CollectionMetaData.COLDATA_CFNAME_PREFIX + "12" + "_inv";
		Assert.assertEquals(12, ColumnFamilyStore.getColID(cfName));
	}
}

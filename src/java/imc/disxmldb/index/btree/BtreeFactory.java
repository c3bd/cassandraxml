package imc.disxmldb.index.btree;

import imc.disxmldb.dom.typesystem.ValueType;

public class BtreeFactory {
	public static IBtree createBtree(String dbpath, int cacheSize, ValueType valueType) {
		return new LazyCacheBtree(dbpath, cacheSize, valueType);
	}
}

/**
*@author:xiafan xiafan68@gmail.com
*@version: 2011-10-3 0.1
*/
package imc.disxmldb.util;

public interface ColumnFamilyStoreSerializable {
	public void serialize(ColumnFamilyStoreProxy cfStore);
	public void deserialize(ColumnFamilyStoreProxy cfStore);
}

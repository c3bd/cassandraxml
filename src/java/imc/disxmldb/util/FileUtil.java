/**
*@author:xiafan xiafan68@gmail.com
*@version: 2011-11-17 0.1
*/
package imc.disxmldb.util;

import java.io.File;

public class FileUtil {
		
	
	/**
	 * remove a file or directory
	 * @param path
	 * @return
	 */
	public static boolean removeFile(String path) {
		File file = new File(path);
		return removeFile(file);
	}
	
	/**
	 * remove the file or directory recursively
	 * @param file
	 * @return
	 */
	public static boolean removeFile(File file) {
		if (file.isDirectory()) {
			boolean ret = true;
			for (File child : file.listFiles()) {
				if ((ret = removeFile(child)) == false) {
					break;
				}
			}
			return ret & file.delete();
		} else {
			return file.delete();
		}
	}
}

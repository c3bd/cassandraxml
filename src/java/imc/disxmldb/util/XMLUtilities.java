package imc.disxmldb.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class XMLUtilities {

	/*public static BiMap<String, String> escapeMap = HashBiMap.create(); 
	static {
		escapeMap.put("&", "&amp;");
		escapeMap.put("<", "&lt;");
		escapeMap.put(">", "&gt;");
	}*/
	
	/**
	 * escape characters like <, &, >
	 * @param text
	 * @return
	 */
	public static String encodeString(String text) {
		String ret = null;
		ret = text.replaceAll("&", "&amp;");
		ret = ret.replaceAll("<", "&lt;");
		ret = ret.replaceAll(">", "&gt;");
		ret = ret.replaceAll("'", "&apos;");
		ret = ret.replaceAll("\"", "&quot;");
		return ret;
	}
	
	/**
	 * decode the string to <, >, </
	 * @param text
	 * @return
	 */
	public static String decodeString(String text) {
		String ret = null;
		ret = text.replaceAll("&lt;", "<");
		ret = ret.replaceAll("&gt;", ">");
		ret = ret.replaceAll("&apos;", "'");
		ret = ret.replaceAll("&quot;", "\"");
		ret = ret.replaceAll("&amp;", "&");
		return ret;
	}

}

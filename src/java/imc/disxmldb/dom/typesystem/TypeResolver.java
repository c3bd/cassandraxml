package imc.disxmldb.dom.typesystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class resolve the type of data in the text form.Currently it only
 * recognize the file size type
 * 
 */
public class TypeResolver {
	private static Map<Pattern, ValueType> typeMappings = new HashMap<Pattern, ValueType>();
	static {
		typeMappings.put(Pattern.compile("\\d*(\\.\\d*)?[ ]*[kK]"),
				ValueType.KILOSIZE);
		typeMappings.put(Pattern.compile("\\d*(\\.\\d+)?[ ]*([mM]|MB)"),
				ValueType.MEGASIZE);
		typeMappings.put(Pattern.compile("\\d*(\\.\\d*)?[ ]*[gG]"),
				ValueType.GIGASIZE);
		/*
		 * typeMappings .put(Pattern.compile("[\+-]*\\d*(\\.\\d*)"),
		 * ValueType.GIGASIZE);
		 */
	}

	/*
	 * private static Pattern kiloPattern = Pattern.compile("\\d*\\.\\d*[kK]");
	 * private static Pattern megaPattern = Pattern.compile("\\d*\\.\\d*[mM]");
	 * private static Pattern gegaPattern = Pattern.compile("\\d*\\.\\d*[gG]");
	 */

	public static ValueType resolve(String text) {
		ValueType ret = ValueType.STRING;
		for (Entry<Pattern, ValueType> entry : typeMappings.entrySet()) {
			Matcher matcher = entry.getKey().matcher(text);
			if (matcher.matches()) {
				ret = entry.getValue();
				break;
			}
		}
		return ret;
	}
}

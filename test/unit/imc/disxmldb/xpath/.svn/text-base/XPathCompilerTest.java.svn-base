package imc.disxmldb.xpath;

import imc.disxmldb.config.SysConfig;
import imc.disxmldb.xpath.xpathcompile.ParseException;
import imc.disxmldb.xpath.xpathcompile.XPathCompiler;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class XPathCompilerTest {
	public String[] legalQueries = {
			"/ProteinDatabase/ProteinEntry[./@id='A56894'];",
			"/ProteinEntry[./@id='A56894'];",
			"/ProteinDatabase/ProteinEntry[./@id='A56894']/reference//title;",
			"/ProteinDatabase/ProteinEntry[./header/uid='A56894'];",
			"/ProteinDatabase/ProteinEntry[.//uid='A56894'];",
			"/ProteinDatabase/ProteinEntry[./header/created_date='05-Jan-1996'];",
			"/ProteinDatabase/ProteinEntry[./header/uid='A56894'][./header/created_date='05-Jan-1996'];",
			"/ProteinDatabase/ProteinEntry[./header/uid='A56894'][./header/created_date='05-Jan-1997'];",
			"/ProteinDatabase/ProteinEntry[./header/uid='A56894'][./header/created_date='05-Jan-1996']/reference//title;",
			"/Database/ProteinEntry[./reference[./refinfo[./@refid='A94455'][./contains(./citation, 'unpublished results, 1966, cited by Margoliash')]]]/@id;",
			"/Database/ProteinEntry[./contains(.//title, 'cytochrome c gene: two classes of processed')]/@id;",
			"/Database/ProteinEntry[./contains(./reference/refinfo[./@refid='A94455']/citation, 'unpublished results, 1966, cited by Margoliash')]/@id;",
			"/table/T[./O_ORDERDATE='s']/O_TOTALPRICE/max();",
			"/table/T[./O_ORDERDATE='s_']/O_TOTALPRICE/min();",
			"/table/T[./O_ORDERDATE=';ss']/O_TOTALPRICE/avg();",
			"/table/T[./O_ORDERDATE='ss;']/O_TOTALPRICE/sum();",
			"/table/T[./O_ORDERDATE=';']/O_TOTALPRICE/count();",
			"/table/T[./O_ORDERKEY=7]/O_COMMENT/text();",
			"/table/T[./contains(./O_COMMENT,'pinto beans')]/O_COMMENT;",
			"/table/T[./contains(./O_COMMENT, '易碎物品')]/O_COMMENT;",
			"/table/T[./contains(./O_COMMENT, '易碎物品made in China')]/O_COMMENT;",
			"/table/T[./contains(./O_COMMENT, 'pinto beans')]/O_COMMENT/count();",
			"/table/T[./contains(./O_COMMENT, 'pinto beans')]/O_COMMENT/subsequence(0,1);",
			"/table/T[./contains(./O_COMMENT, 'pinto beans')]/O_COMMENT/subsequence(0,5);",
			"/table/T[./contains(./O_COMMENT, 'pinto beans')]/O_COMMENT/subsequence(4,1);" };

	public String[] illegalQueries = { "测试", "/test///", "/[./test]",
			"/test[./test)", "/test[./test(/test[./exe]]", "]","/test][./test]" };

	@Test
	public void compilerTest() throws IOException {

		for (String query : legalQueries) {
			XPathCompiler compiler = new XPathCompiler(
					new ByteArrayInputStream(query.getBytes(SysConfig.ENCODING)),
					SysConfig.ENCODING);
			try {
				System.out.println(String.format("compile %s", query));
				compiler.compile();
			} catch (ParseException e) {
				e.printStackTrace();
				Assert.assertTrue(false);
			}
		}

		for (String query : illegalQueries) {
			XPathCompiler compiler = new XPathCompiler(
					new ByteArrayInputStream(query.getBytes(SysConfig.ENCODING)),
					SysConfig.ENCODING);
			try {
				System.out.println(String.format("compile %s", query));
				compiler.compile();
				Assert.assertTrue(false);
			} catch (ParseException e) {
				System.out.println(e.getMessage());
				Assert.assertTrue(true);
			}
		}

	}

}

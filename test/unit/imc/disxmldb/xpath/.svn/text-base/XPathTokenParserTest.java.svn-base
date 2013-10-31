package imc.disxmldb.xpath;

import imc.disxmldb.xpath.xpathtoken.Token;
import imc.disxmldb.xpath.xpathtoken.XPathTokenParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import junit.framework.Assert;

import org.junit.Test;

public class XPathTokenParserTest {

	public static String[] legalIdentifiers = { "__", "_identifier", "_12identifier", "i12dentifier",
			"identifier12","T","汉语" };
	public static String[] illegalIdentifiers = { "12identifier", "+identifier" };

	@Test
	public void IdentifierTest() throws IOException {
		PipedOutputStream output = new PipedOutputStream();
		PipedInputStream input = new PipedInputStream();
		input.connect(output);
		XPathTokenParser tokenParser = new XPathTokenParser(input);
		for (String query : legalIdentifiers) {
			System.out.println("input: " + query);
			tokenParser = new XPathTokenParser(new ByteArrayInputStream(
					query.getBytes()));
			Token token = tokenParser.getNextToken();
			while (token.kind != XPathTokenParser.EOF) {
				Assert.assertEquals(token.kind, XPathTokenParser.NCName);
				System.out.println(token.image + " type:" + token.kind);
				token = tokenParser.getNextToken();
			}
		}

		for (String query : illegalIdentifiers) {
			System.out.println("input: " + query);
			tokenParser = new XPathTokenParser(new ByteArrayInputStream(
					query.getBytes()));
			Token token = tokenParser.getNextToken();
			while (token.kind != XPathTokenParser.EOF) {
				System.out.println(token.image + " type:" + token.kind);
				token = tokenParser.getNextToken();
			}
		}
	}

	public static String[] functions = {"contains(", "_contains(", "_12contains(", "i12contains(",
		"contains12("};

	@Test
	public void XPathFunctionTest() throws IOException {
		PipedOutputStream output = new PipedOutputStream();
		PipedInputStream input = new PipedInputStream();
		input.connect(output);
		XPathTokenParser tokenParser = new XPathTokenParser(input);
		for (String query : functions) {
			System.out.println("input: " + query);
			tokenParser = new XPathTokenParser(new ByteArrayInputStream(
					query.getBytes()));
			Token token = tokenParser.getNextToken();
			while (token.kind != XPathTokenParser.EOF) {
				Assert.assertEquals(token.kind, XPathTokenParser.FuncNameStart);
				System.out.println(token.image + " type:" + token.kind);
				token = tokenParser.getNextToken();
			}
		}
	}
	
	public static String[] integerLex = {
		"1234567890", "+1234567890", "-1234567890"
	};

	public static String[] doubleLex = {
		"11.1", "0.11", ".011","+11.1", "-11.1", "+.01", "-.01"
	};
	
	@Test
	public void XPathNumbericTest() throws IOException {
		PipedOutputStream output = new PipedOutputStream();
		PipedInputStream input = new PipedInputStream();
		input.connect(output);
		XPathTokenParser tokenParser = new XPathTokenParser(input);
		for (String query : integerLex) {
			System.out.println("input: " + query);
			tokenParser = new XPathTokenParser(new ByteArrayInputStream(
					query.getBytes()));
			Token token = tokenParser.getNextToken();
			while (token.kind != XPathTokenParser.EOF) {
				Assert.assertEquals(token.kind, XPathTokenParser.IntegerLiteral);
				System.out.println(token.image + " type:" + token.kind);
				token = tokenParser.getNextToken();
			}
		}
		
		for (String query : doubleLex) {
			System.out.println("input: " + query);
			tokenParser = new XPathTokenParser(new ByteArrayInputStream(
					query.getBytes()));
			Token token = tokenParser.getNextToken();
			while (token.kind != XPathTokenParser.EOF) {
				Assert.assertEquals(token.kind, XPathTokenParser.DecimalLiteral);
				System.out.println(token.image + " type:" + token.kind);
				token = tokenParser.getNextToken();
			}
		}
	}
	
	
	public static final String[] stringQueries = {
		"'normal string'",
		"'escaped quote '''",
		"\"normal string\"",
		"\"normal string\"\"\"",
		"'/normal/string'",
		"'./normal//string'",
		"'function()'",
		"'1112'",
		"'11.1'",
		"'中文支持测试？'",
		"'en中文支持测试？'",
		"'en中文支持测试？en'",
	};
	@Test
	public void XPathStringTest() throws IOException {
		PipedOutputStream output = new PipedOutputStream();
		PipedInputStream input = new PipedInputStream();
		input.connect(output);
		XPathTokenParser tokenParser = new XPathTokenParser(input);
		for (String query : stringQueries) {
			System.out.println("input: " + query);
			tokenParser = new XPathTokenParser(new ByteArrayInputStream(
					query.getBytes()));
			Token token = tokenParser.getNextToken();
			while (token.kind != XPathTokenParser.EOF) {
				Assert.assertEquals(token.kind, XPathTokenParser.StringLiteral);
				System.out.println(token.image + " type:" + token.kind);
				token = tokenParser.getNextToken();
			}
		}
	}
	
	public static String[] queries = {

			"/ProteinDatabase[/accinfo/accession='AE0077']",
			"/ProteinDatabase/ProteinEntry[organism/variety='strain Marburg']/reference/accinfo/xrefs",
			"/ProteinDatabase/ProteinEntry[reference//year='1988']/organism",
			"/ProteinDatabase/ProteinEntry[reference/accinfo/note]",
			"/ProteinDatabase/ProteinEntry[reference/refinfo/year='1988']/reference/accinfo[status='preliminary']/xrefs",
			"//ProteinDataBase/ProteinEntry[./contains(./User_Comment, 'test asdfsdf')]",
			"/basicfeature/test/text()",
			"/basicfeature/hello[.//contains(.//test/User_Comment, \"test _ /haha \"\"\")]",
			"/basicfeature/hello[.//contains(./@Id, '中文支持测试？。，。sf')]", "/-asdf" };

	@Test
	public void test() throws IOException {
		PipedOutputStream output = new PipedOutputStream();
		PipedInputStream input = new PipedInputStream();
		input.connect(output);
		XPathTokenParser tokenParser = new XPathTokenParser(input);
		for (String query : queries) {
			// output.write(query.getBytes(Charset.forName("UTF8")));
			System.out.println(query);
			tokenParser = new XPathTokenParser(new ByteArrayInputStream(
					query.getBytes()));
			Token token = tokenParser.getNextToken();
			while (token.kind != XPathTokenParser.EOF) {
				System.out.println(token.image);
				token = tokenParser.getNextToken();
			}
		}
	}
}

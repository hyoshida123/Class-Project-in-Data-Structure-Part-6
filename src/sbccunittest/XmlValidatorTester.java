package sbccunittest;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.apache.commons.io.*;
import org.junit.*;

import xmlvalidator.*;

/* 11/19/15 */
public class XmlValidatorTester {

	// The following tests will be done on a locally created String rather than the contents of a
	// downloaded file
	// they have proprietary names but are not case-sensitive
	public ArrayList<String> localStrings = new ArrayList<String>(Arrays.asList("Valid File", "Big Valid File",
			"Unclosed Tag at End", "Orphan Closing Tag", "Attribute Not Quoted", "Unclosed Tag"));

	// The following tests will be done on a downloaded file, not a locally created String
	public ArrayList<String> fileStrings = new ArrayList<String>();

	BasicXmlValidator validator;

	BasicStringStack stack;

	HashMap<String, String> testStrings;

	Random randomGenerator = new Random();

	ArrayList<String> possibleAttributes = new ArrayList<String>(
			Arrays.asList("Version", "default", "pattern", "value", "color", "property", "name", "outfile"));

	ArrayList<String> possibleValues = new ArrayList<String>(
			Arrays.asList("1.0", "dark", "dd/mm/yyyy", "503", "#FFFFFF", "primaryID", "tagName", "file.dat"));

	String standardXMLDeclarationTag = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	int nestLevel = 0; // used to track proper indentation

	int overallNesting = 0; // used to give the tag labels a root-> child

	int lineNumber = 0;

	HashMap<String, String> errorTags = new HashMap<String, String>();

	HashMap<String, String> errorParameters = new HashMap<String, String>();

	HashMap<String, String> errorLines = new HashMap<String, String>();

	String unclosedEndTagParentLine;

	public static int totalScore = 0;

	public static int extraCredit = 0;


	@BeforeClass
	public static void beforeTesting() {
		totalScore = 0;
		extraCredit = 0;
	}


	@AfterClass
	public static void afterTesting() {
		System.out.println("Estimated score (assuming no late penalties, etc.) = " + totalScore);
		System.out.println("Estimated extra credit (assuming on time submission) = " + extraCredit);
	}


	@Before
	public void setUp() throws Exception {
		stack = new BasicStringStack();
		validator = new BasicXmlValidator();

		// Load or Construct Strings for testing
		prepareTestingStrings();
	}


	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void testPush() {
		int numberOfStringsToTest = randomGenerator.nextInt(3) + 1;
		String[] strings = new String[numberOfStringsToTest];
		String tString;
		for (int i = 1; i <= numberOfStringsToTest; i++) {
			tString = getRandomString();
			strings[i - 1] = tString;
			stack.push(tString);
		}

		for (int i = 1; i <= numberOfStringsToTest; i++) {
			assertEquals(strings[i - 1], stack.peek(numberOfStringsToTest - i));
		}
		totalScore += 2;
	}


	@Test
	public void testPop() {
		int i;
		int numberOfStringsToTest = randomGenerator.nextInt(3) + 1;
		String[] strings = new String[numberOfStringsToTest];
		String tString;
		for (i = 1; i <= numberOfStringsToTest; i++) {
			tString = getRandomString();
			strings[i - 1] = tString;
			stack.push(tString);
		}

		for (i = numberOfStringsToTest; i > 1; i--) {
			assertEquals(strings[i - 1], stack.pop());
		}
		stack.pop();
		assertEquals(null, stack.pop());
		totalScore += 3;
	}


	@Test
	public void testExercise() {
		int i;
		int numberOfStringsToTest = randomGenerator.nextInt(3) + 3;
		String[] strings = new String[numberOfStringsToTest];
		String tString;
		for (i = 1; i <= numberOfStringsToTest; i++) {
			tString = getRandomString();
			strings[i - 1] = tString;
			stack.push(tString);
		}
		assertEquals(stack.peek(0), strings[numberOfStringsToTest - 1]);
		assertEquals(numberOfStringsToTest, stack.getCount());
		assertEquals(strings[numberOfStringsToTest - 3], stack.peek(2));
		assertEquals(stack.pop(), strings[numberOfStringsToTest - 1]);
		assertEquals(numberOfStringsToTest - 1, stack.getCount());

		for (i = 1; i < numberOfStringsToTest; i++) {
			assertEquals(strings[numberOfStringsToTest - i - 1], stack.pop());
		}
		assertEquals(0, stack.getCount());

		stack.pop();
		stack.pop();
		assertEquals(null, stack.pop());
		totalScore += 5;
	}


	@Test
	public void testValidFile() {
		// The BasicXmlValidator has to be able to find the most basic
		// tag mismatch in order to get credit for valid files.
		int numInvalidTests = (int) (10 * Math.random()) + 1;
		for (int i = 0; i < numInvalidTests; i++) {
			String[] actual = new BasicXmlValidator().validate("<x><y></x>");
			assertEquals("Tag mismatch", actual[0]);
		}

		// Now test a valid doc
		String xmlDocument = testStrings.get("valid file");
		String[] result = validator.validate(xmlDocument);
		assertNull(result);
		totalScore += 10;
	}


	@Test
	public void testBigValidFile() throws IOException {
		// The BasicXmlValidator has to be able to find the most basic
		// tag mismatch in order to get credit for valid files.
		int numInvalidTests = (int) (10 * Math.random()) + 1;
		for (int i = 0; i < numInvalidTests; i++) {
			String[] actual = new BasicXmlValidator().validate("<x><y></x>");
			assertEquals("Tag mismatch", actual[0]);
		}

		// Now test a valid doc
		String xmlDocument = testStrings.get("big valid file");
		String[] result = validator.validate(xmlDocument);
		assertNull(result);
		totalScore += 5;
	}


	@Test
	public void testOrphanClosingTag() throws IOException {
		String xmlDocument = testStrings.get("orphan closing tag");
		String[] result = validator.validate(xmlDocument);
		assertEquals("Orphan closing tag", result[0]);
		assertEquals(errorTags.get("orphan closing tag"), result[1]);
		assertEquals(errorLines.get("orphan closing tag"), result[2]);
		totalScore += 5;
	}


	@Test
	public void testUnclosedTag() throws IOException {
		String xmlDocument = testStrings.get("unclosed tag");
		String[] result = validator.validate(xmlDocument);
		assertEquals("Tag mismatch", result[0]);
		assertEquals(errorTags.get("unclosed tag"), result[1]);
		assertEquals(errorLines.get("unclosed tag"), result[2]);
		assertEquals(errorParameters.get("unclosed tag"), result[3]);
		assertEquals(unclosedEndTagParentLine, result[4]);
		totalScore += 10;
	}


	@Test
	public void testUnclosedTagAtEnd() throws IOException {
		String xmlDocument = testStrings.get("unclosed tag at end");
		String[] result = validator.validate(xmlDocument);
		assertEquals("Unclosed tag at end", result[0]);
		assertEquals(errorTags.get("unclosed tag at end"), result[1]);
		assertEquals(errorLines.get("unclosed tag at end"), result[2]);
		totalScore += 10;
	}


	@Test
	public void testAttributeNotQuoted() throws IOException {
		String xmlDocument = testStrings.get("attribute not quoted");
		String[] result = validator.validate(xmlDocument);
		assertEquals("Attribute not quoted", result[0]);
		assertEquals(errorTags.get("attribute not quoted"), result[1]);
		assertEquals(errorLines.get("attribute not quoted"), result[2]);
		assertEquals(errorParameters.get("attribute not quoted"), result[3]);
		assertEquals(errorLines.get("attribute not quoted"), result[4]);
		extraCredit += 3;
	}


	public void prepareTestingStrings() throws IOException {
		testStrings = new HashMap<String, String>();
		for (String l : localStrings) {
			testStrings.put(l.toLowerCase(), getLocalString(l));
		}

		for (String l : fileStrings) {
			testStrings.put(l.toLowerCase(), getFileString(l));
		}
	}


	public String getFileString(String testName) throws IOException {
		if (testName.equalsIgnoreCase("unclosed tag")) {
			return FileUtils.readFileToString(new File("TestFile1.xml"));
		}
		if (testName.equalsIgnoreCase("unclosed tag at end")) {
			return FileUtils.readFileToString(new File("TestFile2.xml"));
		}
		if (testName.equalsIgnoreCase("valid file")) {
			return FileUtils.readFileToString(new File("TestFile3.xml"));
		}
		if (testName.equalsIgnoreCase("big valid file")) {
			return FileUtils.readFileToString(new File("TestFile4.xml"));
		}
		if (testName.equalsIgnoreCase("attribute not quoted")) {
			return FileUtils.readFileToString(new File("TestFile5.xml"));
		}
		if (testName.equalsIgnoreCase("orphan closing tag")) {
			return FileUtils.readFileToString(new File("TestFile6.xml"));
		}
		return "";
	}


	public String getLocalString(String testName) {
		if (testName.equalsIgnoreCase("unclosed tag")) {
			return constructXMLWithUnclosedTag(2, 2, true);
		}
		if (testName.equalsIgnoreCase("unclosed tag at end")) {
			return constructXMLWithUnclosedEndTag(2, 2, true);
		}
		if (testName.equalsIgnoreCase("valid file")) {
			return constructValidXMLString(2, 1, true);
		}
		if (testName.equalsIgnoreCase("big valid file")) {
			return constructValidXMLString(5, 2, true, true);
		}
		if (testName.equalsIgnoreCase("attribute not quoted")) {
			return constructXMLWithUnquotedAttribute(5, 2, true);
		}
		if (testName.equalsIgnoreCase("orphan closing tag")) {
			return constructXMLWithOrphanClosingTag(4, 2, true);
		}

		return "";
	}


	public String constructValidXMLString(int minimumNestingLevel, int minimumNumberOfTags, boolean includeAttributes) {
		return constructValidXMLString(minimumNestingLevel, minimumNumberOfTags, includeAttributes, false);
	}


	public String constructValidXMLString(int minimumNestingLevel, int minimumNumberOfTags, boolean includeAttributes,
			boolean includeComments) {
		StringBuilder sb = new StringBuilder();
		sb.append(standardXMLDeclarationTag);
		lineNumber = 2;
		overallNesting = minimumNestingLevel;
		sb.append("\r\n<rootTag>");

		while (minimumNumberOfTags > 0) {
			sb.append(getXMLTag(minimumNestingLevel - 1, includeAttributes, includeComments));
			minimumNumberOfTags--;
		}

		sb.append("\r\n</rootTag>");
		lineNumber++;

		return sb.toString();
	}


	public String constructXMLWithUnclosedTag(int minimumNestingLevel, int minimumNumberOfTags,
			boolean includeAttributes) {
		return constructXMLWithUnclosedTag(minimumNestingLevel, minimumNumberOfTags, includeAttributes, false);
	}


	public String constructXMLWithUnclosedTag(int minimumNestingLevel, int minimumNumberOfTags,
			boolean includeAttributes, boolean includeComments) {
		StringBuilder sb = new StringBuilder();
		sb.append(standardXMLDeclarationTag);
		lineNumber = 2;
		overallNesting = minimumNestingLevel;
		sb.append("\r\n<rootTag>");

		while (minimumNumberOfTags > 1) {
			sb.append(getXMLTag(minimumNestingLevel - 1, includeAttributes, includeComments));
			minimumNumberOfTags--;
		}
		sb.append(getUnclosedXMLTagTest("unclosed tag"));

		sb.append("\r\n</rootTag>");
		lineNumber++;

		return sb.toString();
	}


	public String constructXMLWithUnclosedEndTag(int minimumNestingLevel, int minimumNumberOfTags,
			boolean includeAttributes) {
		return constructXMLWithUnclosedEndTag(minimumNestingLevel, minimumNumberOfTags, includeAttributes, false);
	}


	public String constructXMLWithUnclosedEndTag(int minimumNestingLevel, int minimumNumberOfTags,
			boolean includeAttributes, boolean includeComments) {
		StringBuilder sb = new StringBuilder();
		sb.append(standardXMLDeclarationTag);
		lineNumber = 2;
		overallNesting = minimumNestingLevel;
		sb.append("\r\n<rootTag>");

		while (minimumNumberOfTags > 1) {
			sb.append(getXMLTag(minimumNestingLevel - 1, includeAttributes, includeComments));
			minimumNumberOfTags--;
		}
		sb.append("\r\n<unclosedEnd>Content");
		lineNumber++;
		errorTags.put("unclosed tag at end", "unclosedEnd");
		errorLines.put("unclosed tag at end", Integer.toString(lineNumber));

		return sb.toString();
	}


	public String constructXMLWithUnquotedAttribute(int minimumNestingLevel, int minimumNumberOfTags,
			boolean includeComments) {
		StringBuilder sb = new StringBuilder();
		sb.append(standardXMLDeclarationTag);
		lineNumber = 2;
		overallNesting = minimumNestingLevel;
		sb.append("\r\n<rootTag>");

		int unquotedAttributesTag = randomGenerator.nextInt(minimumNumberOfTags - 1) + 1;

		while (minimumNumberOfTags > 0) {
			sb.append(getXMLTag(minimumNestingLevel - 1, true, includeComments,
					minimumNumberOfTags != unquotedAttributesTag));
			minimumNumberOfTags--;
		}

		sb.append("\r\n</rootTag>");
		lineNumber++;

		return sb.toString();
	}


	public String constructXMLWithOrphanClosingTag(int minimumNestingLevel, int minimumNumberOfTags,
			boolean includeAttributes) {
		StringBuilder sb = new StringBuilder();
		sb.append(standardXMLDeclarationTag);
		lineNumber = 2;
		overallNesting = minimumNestingLevel;
		sb.append("\r\n<rootTag>");

		while (minimumNumberOfTags > 1) {
			sb.append(getXMLTag(minimumNestingLevel - 1, includeAttributes, false));
			minimumNumberOfTags--;
		}

		sb.append("\r\n</rootTag>");
		lineNumber++;

		errorTags.put("orphan closing tag", "level" + Integer.toString(minimumNestingLevel - 1));
		String extraCloser = "\r\n</" + errorTags.get("orphan closing tag") + ">";
		lineNumber++;
		sb.append(extraCloser);
		errorLines.put("orphan closing tag", Integer.toString(lineNumber));

		return sb.toString();
	}


	public String getXMLTag(int childNestingLevel, boolean includeAttributes) {
		return getXMLTag(childNestingLevel, includeAttributes, false);
	}


	public String getXMLTag(int childNestingLevel, boolean includeAttributes, boolean includeComments) {
		return getXMLTag(childNestingLevel, includeAttributes, includeComments, true);
	}


	public String getXMLTag(int childNestingLevel, boolean includeAttributes, boolean includeComments,
			boolean quoteAttributes) {
		StringBuilder sb = new StringBuilder();
		String tagName = "level" + Integer.toString(overallNesting - childNestingLevel);
		sb.append("\r\n");
		lineNumber++;
		for (int i = 0; i <= nestLevel; i++) {
			sb.append("    ");
		}
		if (includeComments) {
			sb.append("\r\n");
			for (int i = 0; i <= nestLevel; i++) {
				sb.append("    ");
			}
			sb.append("<!--  This is a comment -->\r\n");
			lineNumber += 2;
			for (int i = 0; i <= nestLevel; i++) {
				sb.append("    ");
			}
		}
		sb.append("<").append(tagName).append(" ");
		if (includeAttributes) {
			sb.append(getRandomAttribute(quoteAttributes));
			if (!quoteAttributes) {
				errorTags.put("attribute not quoted", tagName);
				errorLines.put("attribute not quoted", Integer.toString(lineNumber));
				quoteAttributes = true;
			}
		}
		sb.append(">");
		if (childNestingLevel > 0) {
			nestLevel++;
			int children = randomGenerator.nextInt(3) + 1;
			for (int c = 0; c < children; c++) {
				sb.append(getXMLTag(childNestingLevel - 1, includeAttributes));
			}
			nestLevel--;
			sb.append("\r\n");
			lineNumber++;
			for (int i = 0; i <= nestLevel; i++) {
				sb.append("    ");
			}
		} else {
			sb.append("Tag Content");
		}
		sb.append("</").append(tagName).append(">");

		return sb.toString();
	}


	public String getUnclosedXMLTagTest(String test) {
		StringBuilder sb = new StringBuilder();

		errorLines.put(test, Integer.toString((lineNumber += 2)));
		errorTags.put(test, "unclosed");
		errorParameters.put(test, "parentTag");
		unclosedEndTagParentLine = Integer.toString(lineNumber += 2);

		sb.append("\r\n<parentTag>").append("\r\n<unclosed>Content\r\n").append("\r\n</parentTag>");
		lineNumber += 2;

		return sb.toString();
	}


	public String getRandomAttribute() {
		return getRandomAttribute(true);
	}


	public String getRandomAttribute(boolean quoted) {
		StringBuilder sb = new StringBuilder();
		int index = randomGenerator.nextInt(possibleAttributes.size());
		String attributeName = possibleAttributes.get(index);
		index = randomGenerator.nextInt(possibleValues.size());
		String value = possibleValues.get(index);
		sb.append(attributeName).append("=");
		if (quoted) {
			sb.append("\"");
		}
		sb.append(value);
		if (quoted) {
			sb.append("\"");
		} else {
			errorParameters.put("attribute not quoted", attributeName);
		}

		return sb.toString();
	}


	public String getRandomString() {
		return getRandomString(4);
	}


	public String getRandomString(int stringLength) {
		return getRandomString(stringLength, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ", "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
	}


	public String getRandomString(int stringLength, String possibleCharacters) {
		return getRandomString(stringLength, possibleCharacters, possibleCharacters);
	}


	public String getRandomString(int stringLength, String possibleCharacters, String startingCharacters) {
		StringBuilder sb = new StringBuilder(stringLength);
		sb.append(startingCharacters.charAt(randomGenerator.nextInt(startingCharacters.length())));
		for (int i = 1; i < stringLength; i++) {
			sb.append(possibleCharacters.charAt(randomGenerator.nextInt(possibleCharacters.length())));
		}
		return sb.toString();
	}
}
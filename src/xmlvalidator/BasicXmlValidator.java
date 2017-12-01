package xmlvalidator;

import java.util.regex.*;

public class BasicXmlValidator implements XmlValidator {

	private BasicStringStack stack = new BasicStringStack();
	private BasicStringStack numLine = new BasicStringStack();

	public String extractTagName(String tag) {
		// Get rid of < & > and split by " ".
		tag = tag.substring(1, tag.length() - 1).split(" ")[0];
		if (tag.charAt(0) != '/')
			return tag;
		return tag.substring(1);
	}

	public String extractAttribute(String tag) {
		if (!tag.contains(" ")) {
			return null;
		} else {
			tag = tag.substring(1, tag.length() - 1).split(" ")[1];
			if (tag.charAt(tag.length() - 1) == '/') {
				return tag.substring(0, tag.length() - 1);
			}
			return tag;
		}
		// Expected String should look like attributeName="..." if it's properly
		// quoted.
	}

	public int getLineNumber(String in, int position) {
		String s = in.substring(0, position);
		String[] lines = s.split("\n", -1);
		return lines.length;
	}

	@Override
	public String[] validate(String xmlDocument) {
		String[] string = null;
		String tagName = null;
		String attributeName = null;
		String numberLine = null;
		boolean flag = true;
		final Pattern p = Pattern.compile("<[^>?!]+>");
		// Get a matcher to process the XML string.
		Matcher m = p.matcher(xmlDocument);
		// Find all of the matches for regex in text.
		while (m.find()) {
			// Check whether it is a self-closing tag.
			if (m.group().charAt(m.group().length() - 1) != '/') {
				// Check whether it is a closing tag.
				if (m.group().charAt(1) != '/') {
					if ((extractAttribute(m.group()) != null) && (extractAttribute(m.group()).contains("="))
							&& (!extractAttribute(m.group()).contains("\""))) {
						tagName = extractTagName(m.group());
						attributeName = extractAttribute(m.group()).split("=")[0];
						numberLine = Integer.toString(getLineNumber(xmlDocument, m.start()));
						flag = false;
					}
					// Store tag names on a stack.
					stack.push(extractTagName(m.group()));
					// Store line numbers on a stack.
					numLine.push(Integer.toString(getLineNumber(xmlDocument, m.start())));
				} else {
					// Check whether the stack is empty.
					if (stack.getCount() == 0) {
						string = new String[3];
						string[0] = "Orphan closing tag";
						string[1] = extractTagName(m.group());
						string[2] = Integer.toString(getLineNumber(xmlDocument, m.start()));
						return string;
					}
					if (flag) {
						// Check whether a closing tag name matches the tag name
						// on the top of the stack.
						if (stack.peek(0).equals(extractTagName(m.group()))) {
							stack.pop();
							numLine.pop();

						}
						// Check whether a closing tag name does not match
						// the tag name on the top of the stack.
						else {
							string = new String[5];
							string[0] = "Tag mismatch";
							string[1] = stack.peek(0);
							string[2] = numLine.peek(0);
							string[3] = extractTagName(m.group());
							string[4] = Integer.toString(getLineNumber(xmlDocument, m.start()));
							return string;
						}
					} else {
						string = new String[5];
						string[0] = "Attribute not quoted";
						string[1] = tagName;
						string[2] = numberLine;
						string[3] = attributeName;
						string[4] = numberLine;
						return string;
					}

				}
			}
		}
		// Check whether the stack is not empty when the end of the
		// xmlDocument is reached.
		if (stack.getCount() != 0) {
			string = new String[3];
			string[0] = "Unclosed tag at end";
			string[1] = stack.peek(0);
			string[2] = numLine.peek(0);
			return string;
		}
		return null;
	}
}

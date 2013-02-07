package org.openpano.wikieater.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mstandio
 */
public class StripUtils {

	public static final String RESULT_CSS_TAG = "[CSS]";

	public String stripPageContent(String pageContent) {
		return getPageHeader(pageContent) + getPageBody(pageContent) + "</html>";
	}

	String getPageHeader(String pageContent) {
		String patternTagOpen = "<[^>/]+/?>";
		String patternTagClose = "</[^>]+>";
		Pattern pattern = Pattern.compile(patternTagOpen + "|" + patternTagClose, Pattern.CASE_INSENSITIVE);
		StringBuffer stringBuffer = new StringBuffer();
		Matcher matcher = pattern.matcher(pageContent);
		while (matcher.find()) {
			String group = matcher.group().toLowerCase();
			if (group.matches(patternTagOpen)) {
				if (group.startsWith("<!doctype") || group.startsWith("<html") || group.startsWith("<head")
						|| group.startsWith("<meta")) {
					stringBuffer.append(group);
				}
			} else {
				if (group.startsWith("</head")) {
					stringBuffer.append("<link rel=\"stylesheet\" href=\"" + RESULT_CSS_TAG + "\"/>");
					stringBuffer.append(group);
					break;
				}
			}
		}
		return stringBuffer.toString();
	}

	String getPageBody(String pageContent) {
		pageContent = extractBodyFromPageContent(pageContent);
		pageContent = removeScriptsFromPagecontent(pageContent);
		pageContent = removeCommentsFromPagecontent(pageContent);
		pageContent = removeElementsFromPageContent(pageContent, ElementType.div, "right-navigation");
		pageContent = removeElementsFromPageContent(pageContent, ElementType.div, "mw-panel");
		pageContent = removeElementsFromPageContent(pageContent, ElementType.div, "portal");
		pageContent = removeElementsFromPageContent(pageContent, ElementType.div, "footer");
		pageContent = removeElementsFromPageContent(pageContent, ElementType.div, "mw-head");
		pageContent = removeElementsFromPageContent(pageContent, ElementType.table, "whos_here");
		pageContent = removeElementsFromPageContent(pageContent, ElementType.span, "editsection");
		pageContent = cleanupPageContent(pageContent);
		return pageContent;
	}

	String extractBodyFromPageContent(String pageContent) {
		String patternTagOpen = "<body[^>]*>";
		String patternTagClose = "</body>";
		Pattern pattern = Pattern.compile(patternTagOpen + "|" + patternTagClose, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(pageContent);
		StringBuilder stringBuilder = new StringBuilder();
		int start = 0;
		while (matcher.find()) {
			String group = matcher.group();
			if (group.matches(patternTagOpen)) {
				start = matcher.start();
			} else {
				stringBuilder.append(pageContent.substring(start, matcher.end()));
			}
		}
		return stringBuilder.toString();
	}

	String removeScriptsFromPagecontent(String pageContent) {
		String patternTagOpen = "<script[^>]*/?>";
		String patternTagClose = "</script>";
		Pattern pattern = Pattern.compile(patternTagOpen + "|" + patternTagClose, Pattern.CASE_INSENSITIVE);
		StringBuffer stringBuffer = new StringBuffer();
		Matcher matcher = pattern.matcher(pageContent);
		int start = 0;
		while (matcher.find()) {
			String group = matcher.group();
			if (group.matches(patternTagOpen)) {
				stringBuffer.append(pageContent.substring(start, matcher.start()));
			} else {
				start = matcher.end();
			}
		}
		stringBuffer.append(pageContent.substring(start, pageContent.length()));
		return stringBuffer.toString();
	}

	String removeCommentsFromPagecontent(String pageContent) {
		String patternCommentOpen = "<!--";
		String patternCommentClose = "-->";
		Pattern pattern = Pattern.compile(patternCommentOpen + "|" + patternCommentClose, Pattern.CASE_INSENSITIVE);
		StringBuffer stringBuffer = new StringBuffer();
		Matcher matcher = pattern.matcher(pageContent);
		int start = 0;
		while (matcher.find()) {
			String group = matcher.group();
			if (group.matches(patternCommentOpen)) {
				stringBuffer.append(pageContent.substring(start, matcher.start()));
			} else {
				start = matcher.end();
			}
		}
		stringBuffer.append(pageContent.substring(start, pageContent.length()));
		return stringBuffer.toString();
	}

	String extractDivFromPageContent(String pageContent, String divName) {
		String patternTagOpen = "<div[^>/]*>";
		String patternTagClose = "</div>";
		Pattern pattern = Pattern.compile(patternTagOpen + "|" + patternTagClose, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(pageContent);
		boolean found = false;
		int tagCounter = 0;
		int start = 0;
		int end = pageContent.length();
		while (matcher.find()) {
			String group = matcher.group();
			if (!found && (group.contains("id=\"" + divName + "\"") || group.contains("class=\"" + divName + "\""))) {
				found = true;
				tagCounter++;
				start = matcher.start();
			} else if (found) {
				if (group.matches(patternTagOpen)) {
					tagCounter++;
				} else if (group.matches(patternTagClose)) {
					tagCounter--;
					if (tagCounter == 0) {
						end = matcher.end();
						break;
					}
				}
			}
		}
		return pageContent.substring(start, end);
	}

	enum ElementType {
		body, div, span, table
	}

	String removeElementsFromPageContent(String pageContent, ElementType elementType, String elementName) {
		String patternTagOpen = "<" + elementType.name() + "[^>/]*>";
		String patternTagClose = "</" + elementType.name() + ">";

		Pattern pattern = Pattern.compile(patternTagOpen + "|" + patternTagClose, Pattern.CASE_INSENSITIVE);
		StringBuffer stringBuffer = new StringBuffer();
		Matcher matcher = pattern.matcher(pageContent);
		boolean found = false;
		int tagCounter = 0;
		int start = 0;
		while (matcher.find()) {
			String group = matcher.group();
			if (!found
					&& (group.contains("id=\"" + elementName + "\"") || group.contains("class=\"" + elementName + "\""))) {
				found = true;
				stringBuffer.append(pageContent.substring(start, matcher.start()));
				tagCounter++;
			} else if (found) {
				if (group.matches(patternTagOpen)) {
					tagCounter++;
				} else if (group.matches(patternTagClose)) {
					tagCounter--;
					if (tagCounter == 0) {
						start = matcher.end();
						found = false;
					}
				}
			}
		}
		stringBuffer.append(pageContent.substring(start, pageContent.length()));
		return stringBuffer.toString();
	}

	String cleanupPageContent(String pageContent) {
		String patternTagOpen = "<[^>/]+/?>";
		String patternTagClose = "</[^>]+>";
		Pattern pattern = Pattern.compile(patternTagOpen + "|" + patternTagClose, Pattern.CASE_INSENSITIVE);
		StringBuffer stringBuffer = new StringBuffer();
		Matcher matcher = pattern.matcher(pageContent);
		String typeOpening = "";
		String typeClosing = "";
		int previousEnd = 0;
		int previousStart = 0;
		int start = 0;
		while (matcher.find()) {
			String group = matcher.group();
			if (group.matches(patternTagOpen)) {
				if (group.contains(" ")) {
					typeOpening = group.substring(1, group.indexOf(" "));
				} else {
					typeOpening = group.substring(1, group.indexOf(">"));
				}
				previousStart = matcher.start();
				previousEnd = matcher.end();
			} else {
				typeClosing = group.substring(2, group.indexOf(">"));
				if (typeOpening.equals(typeClosing) && !typeOpening.equals("script") && !typeOpening.equals("link")) {
					if (pageContent.substring(previousEnd, matcher.start()).trim().isEmpty()) {
						stringBuffer.append(pageContent.substring(start, previousStart));
						start = matcher.end();
					}
				}
			}
		}
		stringBuffer.append(pageContent.substring(start, pageContent.length()));
		return stringBuffer.toString();
	}
}

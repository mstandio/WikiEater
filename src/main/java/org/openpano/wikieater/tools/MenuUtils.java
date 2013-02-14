package org.openpano.wikieater.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openpano.wikieater.data.PageData;

/**
 * @author mstandio
 */
public class MenuUtils extends ResourceUtils {

	public PageData getMenuPageData(File menuFile, List<PageData> pageDataList) throws IOException {
		String menuFileName = "wikieater-menu.html";
		String menuTemplate = getResourceContent("/html/" + menuFileName);
		String menuBody = makeMenuBody(menuFile, pageDataList);
		menuTemplate = menuTemplate.replace("[MENU_BODY]", menuBody);
		return new PageData(null, menuFileName, menuTemplate);
	}

	String makeMenuBody(File menuFile, List<PageData> pageDataList) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(menuFile),
					Charset.forName(FileUtils.ENC)));
			String prevLine = null;
			String currLine = bufferedReader.readLine();
			String nextLine = bufferedReader.readLine();
			while (currLine != null) {
				stringBuilder.append(digestLineUrlTags(digestLines(prevLine, currLine, nextLine), pageDataList));
				prevLine = currLine;
				currLine = nextLine;
				nextLine = bufferedReader.readLine();
			}
		} finally {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}
		return stringBuilder.toString();
	}

	String digestLineUrlTags(String line, List<PageData> pageDataList) {
		Pattern pattern = Pattern.compile("\\[http(s)?[^\\]]+\\]");
		Matcher matcher = pattern.matcher(line);
		StringBuffer stringBuffer = new StringBuffer();
		int end = 0;
		while (matcher.find()) {
			matcher.appendReplacement(stringBuffer, digestUrlTag(matcher.group(), pageDataList));
			end = matcher.end();
		}
		stringBuffer.append(line.substring(end, line.length()));
		return stringBuffer.toString();
	}

	String digestUrlTag(String urlTag, List<PageData> pageDataList) {
		urlTag = urlTag.substring(1, urlTag.length() - 1);
		urlTag = urlTag.replaceAll("\\s+", " ");
		String urlVal = null;
		String urlDesc = null;
		if (urlTag.contains(" ")) {
			urlVal = urlTag.substring(0, urlTag.indexOf(" "));
			urlDesc = urlTag.substring(urlTag.indexOf(" ") + 1, urlTag.length());
		} else {
			urlVal = urlTag;
			urlDesc = "[?]";
		}
		String href = null;
		for (PageData pageData : pageDataList) {
			if (urlVal.contains(pageData.getPageUrl())) {
				href = pageData.getHtmlFileName();
				break;
			}
		}
		if (href != null) {
			return "<a href=\"" + href + "\" target=\"content\">" + urlDesc + "</a>";
		}
		return urlVal;
	}

	String digestLines(String prevLine, String currLine, String nextLine) {
		if (currLine.trim().isEmpty()) {
			return "<br/>";
		} else if (currLine.startsWith("=")) {
			return digestTitle(currLine);
		} else if (currLine.startsWith("*")) {
			return digestList(prevLine, currLine, nextLine);
		} else {
			return currLine;
		}
	}

	String digestTitle(String line) {
		int titleLevel = getTitleLevel(line);
		if (titleLevel > 0) {
			line = line.replaceAll("=", "");
			return "<h" + titleLevel + ">" + line + "</h" + titleLevel + ">";
		} else {
			return line;
		}
	}

	int getTitleLevel(String line) {
		if (line != null) {
			for (int i = 0; i < line.length(); i++) {
				if (line.charAt(i) != '=') {
					return i;
				}
			}
		}
		return 0;
	}

	String digestList(String prevLine, String currLine, String nextLine) {
		int prevDepthLevel = getListLevel(prevLine);
		int currDepthLevel = getListLevel(currLine);
		int nextDepthLevel = getListLevel(nextLine);
		StringBuilder stringBuilder = new StringBuilder();
		if (prevDepthLevel < currDepthLevel) {
			for (int i = 0; i < (currDepthLevel - prevDepthLevel); i++) {
				stringBuilder.append("<ul><li>");
			}
		} else if (prevDepthLevel == currDepthLevel) {
			stringBuilder.append("</li><li>");
		}
		stringBuilder.append(currLine.replaceAll("^[\\*]+", ""));
		if (nextDepthLevel < currDepthLevel) {
			for (int i = 0; i < (currDepthLevel - nextDepthLevel); i++) {
				stringBuilder.append("</li></ul>");
			}
		}
		return stringBuilder.toString();
	}

	int getListLevel(String line) {
		if (line != null) {
			line = line + " ";
			for (int i = 0; i < line.length(); i++) {
				if (line.charAt(i) != '*') {
					return i;
				}
			}
		}
		return 0;
	}
}

package org.openpano.wikieater.tools;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openpano.wikieater.data.CssData;
import org.openpano.wikieater.data.PageData;

/**
 * @author mstandio
 */
public class CssUtils extends MediaUtils {

	public Set<String> harvestCssUrls(List<PageData> pageDataList) {
		Set<String> cssUrls = new HashSet<String>();
		for (PageData pageData : pageDataList) {
			cssUrls.addAll(makeMediaUrls(extractCssLinks(pageData.getPageContent()), pageData.getPageUrl()));
		}
		return cssUrls;
	}

	public String extractEmbededCss(String pageContent) {
		String patternTagOpen = "<style[^>]*>";
		String patternTagClose = "</style>";
		Pattern pattern = Pattern.compile(patternTagOpen + "|" + patternTagClose, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(pageContent);
		StringBuilder stringBuilder = new StringBuilder();
		int start = 0;
		while (matcher.find()) {
			String group = matcher.group();
			if (group.matches(patternTagOpen)) {
				start = matcher.end();
			} else {
				stringBuilder.append(pageContent.substring(start, matcher.start()));
			}
		}
		return stringBuilder.toString();
	}

	public Set<CssData> extractCssData(String cssContent) {
		String patternStyleOpen = "\\{";
		String patternStyleClose = "\\}";
		Pattern pattern = Pattern.compile(patternStyleOpen + "|" + patternStyleClose);
		Matcher matcher = pattern.matcher(cssContent);
		Set<CssData> cssDataSet = new HashSet<CssData>();
		String selector = null;
		String body = null;
		int start = 0;
		while (matcher.find()) {
			String group = matcher.group();
			if (group.matches(patternStyleOpen)) {
				selector = cssContent.substring(start, matcher.start()).trim();
			} else {
				body = cssContent.substring(start, matcher.start());
				cssDataSet.add(new CssData(selector, body));
			}
			start = matcher.end();
		}
		return cssDataSet;
	}

	Set<String> extractCssLinks(String pageContent) {
		Set<String> extractedCssLinks = new HashSet<String>();
		Pattern pattern = Pattern.compile("<link[^>]*rel=\"stylesheet\"[^>]*/>", Pattern.CASE_INSENSITIVE);
		Pattern patternHref = Pattern.compile("(?<=href=\")([^\"]+)");
		Matcher matcher = pattern.matcher(pageContent);
		while (matcher.find()) {
			Matcher matcherHref = patternHref.matcher(matcher.group());
			if (matcherHref.find()) {
				extractedCssLinks.add(matcherHref.group().trim());
			}
		}
		return extractedCssLinks;
	}
}

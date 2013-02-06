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
				CssData cssData = new CssData(selector, body);
				analyseCssData(cssData);
				cssDataSet.add(cssData);
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

	void analyseCssData(CssData cssData) {
		String selector = cssData.getSelector().replaceAll(",", "");
		String patternStyleCls = "\\.[\\w_-]+";
		String patternStyleIds = "#[\\w_-]+";
		cssData.isUniversal = selector.matches("((\\s|^)\\w+(\\s|$))|(a(\\.|:))");
		Pattern pattern = Pattern.compile(patternStyleCls + "|" + patternStyleIds);
		Matcher matcher = pattern.matcher(selector);
		while (matcher.find()) {
			String group = matcher.group();
			if (group.matches(patternStyleCls)) {
				cssData.cls.add(group.substring(1));
			} else {
				cssData.ids.add(group.substring(1));
			}
		}
	}

	public Set<CssData> findOccuringCss(Set<CssData> cssDataSet, String pageContent) {
		Set<CssData> cssDataSetResult = new HashSet<CssData>();
		String patternSelectorCls = "class=\"[\\w\\s_-]+\"";
		String patternSelectorIds = "id=\"[\\w\\s_-]+\"";
		Pattern pattern = Pattern.compile(patternSelectorCls + "|" + patternSelectorIds, Pattern.CASE_INSENSITIVE);
		for (CssData cssData : cssDataSet) {
			Matcher matcher = pattern.matcher(pageContent);
			m: while (matcher.find()) {
				String group = matcher.group();
				if (group.matches(patternSelectorCls)) {
					for (String selectorCls : cssData.cls) {
						if (group.contains(selectorCls)) {
							cssDataSetResult.add(cssData);
							break m;
						}
					}
				} else {
					for (String selectorIds : cssData.ids) {
						if (group.contains(selectorIds)) {
							cssDataSetResult.add(cssData);
							break m;
						}
					}
				}
			}
		}
		for (CssData cssData : cssDataSet) {
			if (cssData.isUniversal) {
				cssDataSetResult.add(cssData);
			}
		}
		return cssDataSetResult;
	}
}

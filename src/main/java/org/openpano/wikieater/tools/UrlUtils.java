package org.openpano.wikieater.tools;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openpano.wikieater.data.PageData;

/**
 * @author mstandio
 */
public class UrlUtils {

	public void replacePageUrls(List<PageData> pageDataList) {
		HrefReplacer hrefPageReplacer = new HrefPageReplacer(pageDataList);
		for (PageData pageData : pageDataList) {
			pageData.setPageContent(replacePageContentHrefs(pageData.getPageContent(), hrefPageReplacer));
		}
	}

	String replacePageContentHrefs(String pageContent, HrefReplacer hrefReplacer) {
		Pattern pattern = Pattern.compile("(?<=href=\")([^\"]+)|(?<=src=\")([^\"]+)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(pageContent);
		StringBuffer stringBuffer = new StringBuffer();
		int end = 0;
		while (matcher.find()) {
			matcher.appendReplacement(stringBuffer, hrefReplacer.getReplacement(matcher.group()));
			end = matcher.end();
		}
		stringBuffer.append(pageContent.substring(end, pageContent.length()));
		return stringBuffer.toString();
	}

	String findHrefPageReplacement(String href, List<PageData> pageDataList) {
		for (PageData pageData : pageDataList) {
			if (pageUrlMatchesHref(pageData.getPageUrl(), href)) {
				return translateHtmlFileNameToHref(pageData.getHtmlFileName(), href);
			}
		}
		return href;
	}

	boolean pageUrlMatchesHref(String pageUrl, String href) {
		pageUrl = pageUrl.replaceAll("(.+)(?=/[^/]+$)", "");
		return href.contains(pageUrl);
	}

	String translateHtmlFileNameToHref(String htmlFileName, String href) {
		if (href.contains("#")) {
			htmlFileName += href.substring(href.indexOf("#"));
		}
		return htmlFileName;
	}

	interface HrefReplacer {
		public String getReplacement(String href);
	}

	class HrefPageReplacer implements HrefReplacer {

		private final List<PageData> pageDataList;

		public HrefPageReplacer(List<PageData> pageDataList) {
			this.pageDataList = pageDataList;
		}

		@Override
		public String getReplacement(String href) {
			return findHrefPageReplacement(href, pageDataList);
		}
	}

	class HrefMediaReplacer implements HrefReplacer {

		@Override
		public String getReplacement(String href) {
			throw new IllegalStateException();
		}
	}
}

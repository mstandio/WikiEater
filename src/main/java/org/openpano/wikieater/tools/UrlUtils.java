package org.openpano.wikieater.tools;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openpano.wikieater.data.ImageData;
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

	public void replaceImageUrls(List<PageData> pageDataList, Set<ImageData> imageDataSet, String outputDirectory) {
		HrefReplacer hrefImageReplacer = new HrefImageReplacer(imageDataSet, outputDirectory);
		for (PageData pageData : pageDataList) {
			pageData.setPageContent(replacePageContentHrefs(pageData.getPageContent(), hrefImageReplacer));
		}
	}

	String replacePageContentHrefs(String pageContent, HrefReplacer hrefReplacer) {
		Pattern pattern = Pattern.compile("(?<=href=\")([^\"]+)|(?<=src=\")([^\"]+)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(pageContent);
		StringBuffer stringBuffer = new StringBuffer();
		int end = 0;
		while (matcher.find()) {
			matcher.appendReplacement(stringBuffer, hrefReplacer.getReplacement(matcher.group().trim()));
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

	String findSrcImageReplacement(String src, Set<ImageData> imageDataSet, String outputDirectory) {
		for (ImageData imageData : imageDataSet) {
			if (src.contains(imageData.getImageName())) {
				return outputDirectory + "/" + imageData.getImageName();
			}
		}
		return src;
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

	class HrefImageReplacer implements HrefReplacer {

		private final Set<ImageData> imageDataSet;
		private final String outputDirectory;

		public HrefImageReplacer(Set<ImageData> imageDataSet, String outputDirectory) {
			this.imageDataSet = imageDataSet;
			this.outputDirectory = outputDirectory;
		}

		@Override
		public String getReplacement(String src) {
			return findSrcImageReplacement(src, imageDataSet, outputDirectory);
		}
	}
}

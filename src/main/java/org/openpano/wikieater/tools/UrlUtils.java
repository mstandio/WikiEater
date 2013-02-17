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
public class UrlUtils extends MediaUtils {

	public void replacePageUrls(List<PageData> pageDataList) {
		HrefReplacer hrefPageReplacer = new HrefPageReplacer(pageDataList);
		for (PageData pageData : pageDataList) {
			pageData.setPageContent(replacePageContentHrefs(pageData, hrefPageReplacer));
		}
	}

	public void replaceImageUrls(List<PageData> pageDataList, Set<ImageData> imageDataSet, String outputDirectory) {
		HrefReplacer hrefImageReplacer = new HrefImageReplacer(imageDataSet, outputDirectory);
		for (PageData pageData : pageDataList) {
			pageData.setPageContent(replacePageContentHrefs(pageData, hrefImageReplacer));
		}
	}

	public void replaceCssUrl(List<PageData> pageDataList, String cssFileName, String outputdirecotry) {
		HrefReplacer hrefImageReplacer = new HrefCssResultReplacer(outputdirecotry + "/" + cssFileName);
		for (PageData pageData : pageDataList) {
			pageData.setPageContent(replacePageContentHrefs(pageData, hrefImageReplacer));
		}
	}

	String replacePageContentHrefs(PageData pageData, HrefReplacer hrefReplacer) {
		Pattern pattern = Pattern.compile("(?<=href=\")([^\"]+)|(?<=src=\")([^\"]+)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(pageData.getPageContent());
		StringBuffer stringBuffer = new StringBuffer();
		int end = 0;
		while (matcher.find()) {
			matcher.appendReplacement(stringBuffer,
					hrefReplacer.getReplacement(matcher.group().trim(), pageData.getPageUrl()));
			end = matcher.end();
		}
		stringBuffer.append(pageData.getPageContent().substring(end, pageData.getPageContent().length()));
		return stringBuffer.toString();
	}

	String findHrefPageReplacement(String href, String pageUrl, List<PageData> pageDataList) {
		for (PageData pageData : pageDataList) {
			if (pageUrlMatchesHref(pageData.getPageUrl(), href)) {
				return translateHtmlFileNameToHref(pageData.getHtmlFileName(), href);
			}
		}
		if (href.matches(".+\\.css$|.+\\.jpg$|.+\\.jpeg$|.+\\.gif|.+\\.png|.+\\.bmp")) {
			return href;
		} else {
			return makeMediaUrl(href, pageUrl);
		}
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

	String findCssResultReplacement(String href, String cssResultUrl) {
		if (href.equals(StripUtils.RESULT_CSS_TAG)) {
			return cssResultUrl;
		} else {
			return href;
		}
	}

	interface HrefReplacer {
		public String getReplacement(String href, String pageUrl);
	}

	class HrefPageReplacer implements HrefReplacer {

		private final List<PageData> pageDataList;

		public HrefPageReplacer(List<PageData> pageDataList) {
			this.pageDataList = pageDataList;
		}

		@Override
		public String getReplacement(String href, String pageUrl) {
			return findHrefPageReplacement(href, pageUrl, pageDataList);
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
		public String getReplacement(String src, String pageUrl) {
			return findSrcImageReplacement(src, imageDataSet, outputDirectory);
		}
	}

	class HrefCssResultReplacer implements HrefReplacer {

		private final String cssResultUrl;

		public HrefCssResultReplacer(String cssResultUrl) {
			this.cssResultUrl = cssResultUrl;
		}

		@Override
		public String getReplacement(String href, String pageUrl) {
			return findCssResultReplacement(href, cssResultUrl);
		}
	}
}

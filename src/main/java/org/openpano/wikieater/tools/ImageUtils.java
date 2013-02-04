package org.openpano.wikieater.tools;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openpano.wikieater.data.PageData;

/**
 * @author mstandio
 */
public class ImageUtils extends MediaUtils {

	public Set<String> harvestImageUrls(List<PageData> pageDataList) {
		Set<String> imageUrls = new HashSet<String>();
		for (PageData pageData : pageDataList) {
			imageUrls.addAll(makeMediaUrls(extractImageLinks(pageData.getPageContent()), pageData.getPageUrl()));
		}
		return imageUrls;
	}

	Set<String> extractImageLinks(String pageContent) {
		Set<String> imageLinks = new HashSet<String>();
		Pattern pattern = Pattern.compile("(?<=src=\")([^\"]+)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(pageContent);
		while (matcher.find()) {
			String group = matcher.group().trim();
			if (group.matches(".+\\.gif$") || group.matches(".+\\.png$") || group.matches(".+\\.jpg$")
					|| group.matches(".+\\.jpeg$") || group.matches(".+\\.bmp$")) {
				imageLinks.add(group);
			}
		}
		return imageLinks;
	}
}

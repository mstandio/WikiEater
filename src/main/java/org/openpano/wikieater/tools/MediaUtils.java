package org.openpano.wikieater.tools;

import java.util.HashSet;
import java.util.Set;

/**
 * @author mstandio
 */
public abstract class MediaUtils {

	Set<String> makeMediaUrls(Set<String> mediaLinks, String pageUrl) {
		Set<String> mediaUrls = new HashSet<String>();
		for (String mediaLink : mediaLinks) {
			mediaUrls.add(makeMediaUrl(mediaLink, pageUrl));
		}
		return mediaUrls;
	}

	String makeMediaUrl(String mediaLink, String pageUrl) {
		mediaLink = mediaLink.replaceAll("&amp;", "&");
		if (mediaLink.matches("http(s)?://.+") || mediaLink.startsWith("#") || mediaLink.startsWith("[")) {
			return mediaLink;
		} else if (mediaLink.startsWith("//")) {
			return mediaLink.replace("//", "http://");
		} else {
			String disassembledUrl[] = pageUrl.split("/");
			return disassembledUrl[0] + disassembledUrl[1] + "//" + disassembledUrl[2] + mediaLink;
		}
	}
}

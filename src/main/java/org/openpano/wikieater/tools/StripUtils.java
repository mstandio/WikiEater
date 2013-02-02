package org.openpano.wikieater.tools;

/**
 * @author mstandio
 */
public class StripUtils {

	public String stripPageContent(String pageContent) {
		return pageContent;
	}

	String stripDocType(String urlContent) {
		return urlContent.substring(urlContent.indexOf(">") + 1, urlContent.length());
	}

	String stripNavigation(String urlContent) {
		return null;
	}

	String stripFooter(String urlContent) {
		return null;
	}
}

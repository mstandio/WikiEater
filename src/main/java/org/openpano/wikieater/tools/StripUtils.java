package org.openpano.wikieater.tools;

/**
 * @author MSt
 */
public class StripUtils {

	public String stripDocType(String urlContent) {
		return urlContent.substring(urlContent.indexOf(">") + 1, urlContent.length());
	}

	public String stripNavigation(String urlContent) {
		return null;
	}

	public String stripFooter(String urlContent) {
		return null;
	}
}

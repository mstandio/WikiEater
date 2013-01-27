package org.openpano.wikieater;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openpano.wikieater.tools.FileUtils;
import org.openpano.wikieater.tools.StripUtils;

/**
 * @author mstandio
 */
public class WikiEater {

	private final FileUtils fileUtils = new FileUtils();
	private final StripUtils stripUtils = new StripUtils();

	void processLinks() throws IOException {
		final File linksFile = new File("./files/links.txt");
		final File cacheFolder = new File("./files/cache");
		List<String> links = fileUtils.readLinks(linksFile);
		for (String link : links) {
			String strippedContent = stripUrlContent(fileUtils.getUrlContent(link, cacheFolder));
			File htmlFile = new File("./files/output/" + fileUtils.makeHtmlFileName(link) + ".html");
			fileUtils.saveAsHtmlFile(htmlFile, strippedContent);
		}
	}

	String stripUrlContent(String urlContent) {
		// return stripUtils.stripDocType(urlContent);
		return urlContent;
	}

	public static void main(String[] args) {
		WikiEater wikiEater = new WikiEater();
		try {
			wikiEater.processLinks();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

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

	private static final String directoryCache = "./files/cache";
	private static final String directoryOutput = "./files/output";

	private final FileUtils fileUtils = new FileUtils();
	private final StripUtils stripUtils = new StripUtils();

	void processLinks() throws IOException {
		final File linksFile = new File("./files/links.txt");
		final File cacheFolder = new File(directoryCache);
		List<String> links = fileUtils.readLinks(linksFile);
		for (String link : links) {
			String strippedContent = stripUrlContent(fileUtils.getUrlContent(link, cacheFolder));
			File htmlFile = new File(directoryOutput + "/" + fileUtils.makeHtmlFileName(link) + ".html");
			fileUtils.saveAsHtmlFile(htmlFile, strippedContent);
		}
	}

	String stripUrlContent(String urlContent) {
		// return stripUtils.stripDocType(urlContent);
		return urlContent;
	}

	public static void main(String[] args) {

		new File(WikiEater.directoryCache).mkdir();
		new File(WikiEater.directoryOutput).mkdir();

		WikiEater wikiEater = new WikiEater();
		try {
			wikiEater.processLinks();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

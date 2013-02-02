package org.openpano.wikieater;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openpano.wikieater.data.PageData;
import org.openpano.wikieater.tools.FileUtils;
import org.openpano.wikieater.tools.StripUtils;
import org.openpano.wikieater.tools.UrlUtils;

/**
 * @author mstandio
 */
public class WikiEater {

	private final String directoryCache = "./files/cache";
	private final String directoryOutput = "./files/output";

	private final FileUtils fileUtils = new FileUtils();
	private final StripUtils stripUtils = new StripUtils();
	private final UrlUtils urlUtils = new UrlUtils();

	void processLinks() throws IOException {
		final File linksFile = new File("./files/links.txt");
		final File cacheFolder = new File(directoryCache);
		List<String> links = fileUtils.readLinks(linksFile);
		List<PageData> pagesData = new ArrayList<PageData>();

		for (String link : links) {
			String strippedPageContent = stripUtils.stripPageContent(fileUtils.getUrlContent(link, cacheFolder));
			pagesData.add(new PageData(link, fileUtils.makeHtmlFileName(link), strippedPageContent));
		}

		urlUtils.replacePageUrls(pagesData);

		for (PageData pageData : pagesData) {
			File htmlFile = new File(directoryOutput + "/" + pageData.getHtmlFileName());
			fileUtils.saveAsHtmlFile(htmlFile, pageData.getPageContent());
		}
	}	

	public static void main(String[] args) {

		WikiEater wikiEater = new WikiEater();

		new File(wikiEater.directoryCache).mkdir();
		new File(wikiEater.directoryOutput).mkdir();

		try {
			wikiEater.processLinks();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

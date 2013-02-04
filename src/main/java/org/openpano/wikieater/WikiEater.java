package org.openpano.wikieater;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openpano.wikieater.data.CssData;
import org.openpano.wikieater.data.ImageData;
import org.openpano.wikieater.data.PageData;
import org.openpano.wikieater.tools.CssUtils;
import org.openpano.wikieater.tools.FileUtils;
import org.openpano.wikieater.tools.FileUtils.FileType;
import org.openpano.wikieater.tools.ImageUtils;
import org.openpano.wikieater.tools.StripUtils;
import org.openpano.wikieater.tools.UrlUtils;

/**
 * @author mstandio
 */
public class WikiEater {

	private final String directoryCache = "./files/cache";
	private final String directoryCacheResources = "./files/cache/resources";
	private final String directoryCacheResourcesCss = "./files/cache/resources/css";
	private final String directoryCacheResourcesImages = "./files/cache/resources/images";
	private final String directoryOutput = "./files/output";
	private final String directoryOutputResources = "./files/output/Resources";
	private final String directoryOutputResourcesCss = "./files/output/Resources/css";
	private final String directoryOutputResourcesImages = "./files/output/Resources/images";

	private final FileUtils fileUtils = new FileUtils();
	private final CssUtils cssUtils = new CssUtils();
	private final ImageUtils imageUtils = new ImageUtils();
	private final UrlUtils urlUtils = new UrlUtils();
	private final StripUtils stripUtils = new StripUtils();

	void processUrls() throws IOException {

		// read pages

		final File urlsFile = new File("./files/links.txt");
		Set<String> pageUrls = fileUtils.readUrls(urlsFile);
		final File cacheFolder = new File(directoryCache);
		List<PageData> pageDataList = new ArrayList<PageData>();
		for (String pageUrl : pageUrls) {
			pageDataList.add(new PageData(pageUrl, fileUtils.makeHtmlFileName(pageUrl), fileUtils.getUrlContent(
					pageUrl, cacheFolder, FileType.HTML)));
		}

		// read css

		Set<String> cssUrls = cssUtils.harvestCssUrls(pageDataList);
		final File cacheFolderCss = new File(directoryCacheResourcesCss);
		Set<CssData> cssDataSet = new HashSet<CssData>();
		for (String cssUrl : cssUrls) {
			cssDataSet.addAll(cssUtils.extractCssData(fileUtils.getUrlContent(cssUrl, cacheFolderCss, FileType.CSS)));
		}
		for (PageData pageData : pageDataList) {
			cssDataSet.addAll(cssUtils.extractCssData(cssUtils.extractEmbededCss(pageData.getPageContent())));
		}

		// read images

		Set<String> imageUrls = imageUtils.harvestImageUrls(pageDataList);
		final File cacheFolderImages = new File(directoryCacheResourcesImages);
		Set<ImageData> ImageDataSet = new HashSet<ImageData>();
		for (String imageUrl : imageUrls) {
			ImageDataSet.add(new ImageData(fileUtils.makeImageFileName(imageUrl), fileUtils.getUrlFile(imageUrl,
					cacheFolderImages, FileType.IMAGE)));
		}

		// rework pages

		for (PageData pageData : pageDataList) {
			pageData.setPageContent(stripUtils.stripPageContent(pageData.getPageContent()));
		}
		urlUtils.replacePageUrls(pageDataList);
		urlUtils.replaceImageUrls(pageDataList, ImageDataSet, directoryOutputResourcesImages);

		// save data

		for (PageData pageData : pageDataList) {			
			fileUtils.saveAsHtmlFile(pageData, directoryOutput);
		}
		for (ImageData imageData : ImageDataSet) {
			fileUtils.copyFile(imageData.getImageFile(), directoryOutputResourcesImages);
		}
	}

	public static void main(String[] args) {

		WikiEater wikiEater = new WikiEater();

		new File(wikiEater.directoryCache).mkdir();
		new File(wikiEater.directoryCacheResources).mkdir();
		new File(wikiEater.directoryCacheResourcesCss).mkdir();
		new File(wikiEater.directoryCacheResourcesImages).mkdir();
		new File(wikiEater.directoryOutput).mkdir();
		new File(wikiEater.directoryOutputResources).mkdir();
		new File(wikiEater.directoryOutputResourcesCss).mkdir();
		new File(wikiEater.directoryOutputResourcesImages).mkdir();

		try {
			wikiEater.processUrls();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mstandio
 */
public class WikiEater {

	private static final Logger logger = LoggerFactory.getLogger(WikiEater.class);

	private final String directoryCache = "./files/cache";
	private final String directoryCacheResources = "./files/cache/resources";
	private final String directoryCacheResourcesCss = "./files/cache/resources/css";
	private final String directoryCacheResourcesImages = "./files/cache/resources/images";
	private final String directoryOutput = "./files/output";
	private final String directoryOutputResources = "./files/output/resources";
	private final String directoryOutputResourcesCss = "./files/output/resources/css";
	private final String directoryOutputResourcesImages = "./files/output/resources/images";
	
	private final String pathResourcesImages = "resources/images";
	private final String pathResourcesCss = "resources/css";

	private final FileUtils fileUtils = new FileUtils();
	private final CssUtils cssUtils = new CssUtils();
	private final ImageUtils imageUtils = new ImageUtils();
	private final UrlUtils urlUtils = new UrlUtils();
	private final StripUtils stripUtils = new StripUtils();

	void processUrls() throws IOException {

		logger.info("Reading pages...");

		final File urlsFile = new File("./files/links.txt");
		final Set<String> pageUrls = fileUtils.readUrls(urlsFile);
		final File cacheFolder = new File(directoryCache);
		final List<PageData> pageDataList = new ArrayList<PageData>();
		for (String pageUrl : pageUrls) {
			pageDataList.add(new PageData(pageUrl, fileUtils.makeHtmlFileName(pageUrl), fileUtils.getUrlContent(
					pageUrl, cacheFolder, FileType.HTML)));
		}

		logger.info("Reading css...");

		final Set<String> cssUrls = cssUtils.harvestCssUrls(pageDataList);
		final File cacheFolderCss = new File(directoryCacheResourcesCss);
		final Set<CssData> cssDataSet = new HashSet<CssData>();
		for (String cssUrl : cssUrls) {
			cssDataSet.addAll(cssUtils.extractCssData(fileUtils.getUrlContent(cssUrl, cacheFolderCss, FileType.CSS)));
		}
		for (PageData pageData : pageDataList) {
			cssDataSet.addAll(cssUtils.extractCssData(cssUtils.extractEmbededCss(pageData.getPageContent())));
		}

		logger.info("Stripping pages...");

		for (PageData pageData : pageDataList) {
			pageData.setPageContent(stripUtils.stripPageContent(pageData.getPageContent()));
		}

		logger.info("Reworking css..");

		final Set<CssData> cssDataSetResult = new HashSet<CssData>();
		final File cssResultFile = new File(directoryOutputResourcesCss, "style.css");
		for (PageData pageData : pageDataList) {
			cssDataSetResult.addAll(cssUtils.findOccuringCss(cssDataSet, pageData.getPageContent()));
		}
		fileUtils.saveCssDataIntoFile(cssDataSetResult, cssResultFile);

		logger.info("Reading images...");

		final Set<String> imageUrls = imageUtils.harvestImageUrls(pageDataList);
		final File cacheFolderImages = new File(directoryCacheResourcesImages);
		final Set<ImageData> ImageDataSet = new HashSet<ImageData>();
		for (String imageUrl : imageUrls) {
			ImageDataSet.add(new ImageData(fileUtils.makeImageFileName(imageUrl), fileUtils.getUrlFile(imageUrl,
					cacheFolderImages, FileType.IMAGE)));
		}

		logger.info("Reworking pages..");

		urlUtils.replacePageUrls(pageDataList);
		urlUtils.replaceImageUrls(pageDataList, ImageDataSet, pathResourcesImages);
		urlUtils.replaceCssUrl(pageDataList, cssResultFile.getName(), pathResourcesCss);

		logger.info("Saving data..");

		for (PageData pageData : pageDataList) {
			fileUtils.saveAsHtmlFile(pageData, directoryOutput);
		}
		for (ImageData imageData : ImageDataSet) {
			fileUtils.copyFile(imageData.getImageFile(), directoryOutputResourcesImages);
		}

		logger.info("Done!");
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

package org.openpano.wikieater;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Logger;

import org.openpano.wikieater.data.CliData;
import org.openpano.wikieater.data.CssData;
import org.openpano.wikieater.data.ImageData;
import org.openpano.wikieater.data.PageData;
import org.openpano.wikieater.tools.CliUtils;
import org.openpano.wikieater.tools.CssUtils;
import org.openpano.wikieater.tools.FileUtils;
import org.openpano.wikieater.tools.FileUtils.FileType;
import org.openpano.wikieater.tools.ImageUtils;
import org.openpano.wikieater.tools.IndexUtils;
import org.openpano.wikieater.tools.MenuUtils;
import org.openpano.wikieater.tools.StripUtils;
import org.openpano.wikieater.tools.UrlUtils;
import org.openpano.wikieater.util.LoggerFormatter;

/**
 * @author mstandio
 */
public class WikiEater {

	private static final Logger logger = Logger.getLogger(WikiEater.class.getName());

	static {
		Formatter formatter = new LoggerFormatter();
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(formatter);
		logger.addHandler(consoleHandler);
		logger.setUseParentHandlers(false);
	}

	private final CliData cliData;

	private final File directoryCache;
	private final String directoryCacheResources;
	private final String directoryCacheResourcesCss;
	private final String directoryCacheResourcesImages;
	private final File directoryOutput;
	private final String directoryOutputResources;
	private final String directoryOutputResourcesCss;
	private final String directoryOutputResourcesImages;

	private final String pathResourcesImages = "images";
	private final String pathResourcesCss = "css";

	private final FileUtils fileUtils = new FileUtils();
	private final CssUtils cssUtils = new CssUtils();
	private final ImageUtils imageUtils = new ImageUtils();
	private final UrlUtils urlUtils = new UrlUtils();
	private final StripUtils stripUtils = new StripUtils();
	private final MenuUtils menuUtils = new MenuUtils();
	private final IndexUtils indexUtils = new IndexUtils();

	public WikiEater(CliData cliData) {
		this.cliData = cliData;

		if (cliData.cacheDir == null) {
			cliData.cacheDir = "./cache";
			new File(cliData.cacheDir).mkdir();
		}

		if (cliData.outputDir == null) {
			cliData.outputDir = "./output";
			new File(cliData.outputDir).mkdir();
		}

		if (cliData.menuFile == null) {
			cliData.outputDir = "./menu.txt";
		}
		directoryCache = new File(cliData.cacheDir);
		if (!directoryCache.exists() || !directoryCache.isDirectory()) {
			throw new IllegalArgumentException("Directory is not valid: " + directoryCache);
		}

		directoryCacheResources = directoryCache + "/resources";
		directoryCacheResourcesCss = directoryCache + "/resources/css";
		directoryCacheResourcesImages = directoryCache + "/resources/images";

		directoryOutput = new File(cliData.outputDir);
		if (!directoryOutput.exists() || !directoryOutput.isDirectory()) {
			throw new IllegalArgumentException("Directory is not valid: " + directoryOutput);
		}

		directoryOutputResources = directoryOutput + "/resources";
		directoryOutputResourcesCss = directoryOutput + "/resources/css";
		directoryOutputResourcesImages = directoryOutput + "/resources/images";

		new File(directoryCacheResources).mkdir();
		new File(directoryCacheResourcesCss).mkdir();
		new File(directoryCacheResourcesImages).mkdir();

		new File(directoryOutputResources).mkdir();
		new File(directoryOutputResourcesCss).mkdir();
		new File(directoryOutputResourcesImages).mkdir();
	}

	public void processMenuFile() throws IOException {

		if (cliData.cleanCache) {
			logger.info("Cleaning cache...");
			fileUtils.cleanDirectory(directoryCache);
		}

		logger.info("Reading pages...");

		final File menuFile = new File(cliData.menuFile);
		final Set<String> pageUrls = fileUtils.readUrls(menuFile);
		final File cacheFolder = directoryCache;
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

		logger.info("Processing css..");

		final Set<CssData> cssDataSetResult = new HashSet<CssData>();
		final File cssResultFile = new File(directoryOutputResourcesCss, "style.css");
		for (PageData pageData : pageDataList) {
			cssDataSetResult.addAll(cssUtils.findOccuringCss(cssDataSet, pageData.getPageContent()));
		}

		logger.info("Reading images...");

		final Set<String> imageUrls = imageUtils.harvestImageUrls(pageDataList);
		final File cacheFolderImages = new File(directoryCacheResourcesImages);
		final Set<ImageData> ImageDataSet = new HashSet<ImageData>();
		for (String imageUrl : imageUrls) {
			ImageDataSet.add(new ImageData(fileUtils.makeImageFileName(imageUrl), fileUtils.getUrlFile(imageUrl,
					cacheFolderImages, FileType.IMAGE)));
		}

		logger.info("Assembling pages..");

		urlUtils.replacePageUrls(pageDataList);
		urlUtils.replaceImageUrls(pageDataList, ImageDataSet, pathResourcesImages);
		urlUtils.replaceCssUrl(pageDataList, cssResultFile.getName(), pathResourcesCss);

		fileUtils.cleanDirectory(directoryOutput);

		logger.info("Saving data..");

		for (PageData pageData : pageDataList) {
			fileUtils.saveAsHtmlFile(pageData, directoryOutputResources);
		}
		for (ImageData imageData : ImageDataSet) {
			fileUtils.copyFile(imageData.getImageFile(), directoryOutputResourcesImages);
		}
		fileUtils.saveCssDataIntoFile(cssDataSetResult, cssResultFile);
		fileUtils.saveAsHtmlFile(menuUtils.getMenuPageData(menuFile, pageDataList), directoryOutputResources);
		fileUtils.saveAsHtmlFile(indexUtils.getIndexPageData(menuFile, pageDataList), directoryOutput.getName());

		logger.info("Done!");
	}

	public static void main(String[] args) {

		CliUtils cliUtils = new CliUtils();
		WikiEater wikiEater = new WikiEater(cliUtils.parseArguments(args));

		try {
			wikiEater.processMenuFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

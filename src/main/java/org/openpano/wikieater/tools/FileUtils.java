package org.openpano.wikieater.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mstandio
 */
public class FileUtils {

	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

	private static final String ENC = "UTF-8";

	public String getUrlContent(String url, File cacheFolder) throws IOException {
		url = url.trim();
		String cacheFileName = makeCacheFileName(url);
		List<String> cachedUrls = listCachedUrls(cacheFolder);
		if (cachedUrls.contains(url)) {
			File[] cacheFiles = cacheFolder.listFiles();
			for (File cacheFile : cacheFiles) {
				if (cacheFile.getName().equals(cacheFileName)) {
					return getCacheRemainingLines(cacheFile);
				}
			}
			throw new IOException("Could not find: " + cacheFileName);
		} else {
			logger.info("Url '{}' was not found in cache, downloading...", url);
			String urlContent = readFromUrl(url);
			File cacheFile = new File(cacheFolder, cacheFileName);
			saveToCache(cacheFile, url, urlContent);
			return urlContent;
		}
	}

	public List<String> readLinks(File linksFile) throws IOException {
		List<String> links = new ArrayList<String>();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(linksFile),
					Charset.forName(ENC)));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty() && !line.startsWith("#")) {
					links.add(removeNamedAnchorFromUrl(line));
				}
			}
		} finally {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}
		logger.info("Found {} links in file '{}'", links.size(), linksFile.getName());
		return links;
	}

	List<String> listCachedUrls(File cacheFolder) throws IOException {
		final List<String> firstLines = new ArrayList<String>();
		File[] cacheFiles = cacheFolder.listFiles();
		for (File cacheFile : cacheFiles) {
			firstLines.add(getCacheFirstLine(cacheFile));
		}
		return firstLines;
	}

	String getCacheFirstLine(File file) throws IOException {
		String firstLine = null;
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName(ENC)));
			firstLine = bufferedReader.readLine();
		} finally {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}
		return firstLine;
	}

	String getCacheRemainingLines(File file) throws IOException {
		final StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName(ENC)));

			bufferedReader.readLine(); // discard first line
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
		} finally {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}
		return stringBuilder.toString();
	}

	String readFromUrl(String url) throws IOException {
		final StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
		} finally {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}
		return stringBuilder.toString();
	}

	String makeCacheFileName(String url) {
		String fileName = url.toLowerCase();
		fileName = removeNamedAnchorFromUrl(fileName);
		fileName = fileName.replaceAll("_", "-");
		if (url.contains("/")) {
			fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
		}
		fileName = fileName.replaceAll(":", "_");
		return fileName;
	}

	String removeNamedAnchorFromUrl(String url) {
		if (url.contains("#")) {
			url = url.substring(0, url.indexOf("#"));
		}
		return url;
	}

	void saveToCache(File cacheFile, String url, String urlContent) throws IOException {
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile), ENC));
			bufferedWriter.write(url);
			bufferedWriter.write("\n");
			bufferedWriter.write(urlContent);
		} finally {
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}
		}
	}

	public String makeHtmlFileName(String url) {
		return makeCacheFileName(url) + ".html";
	}

	public void saveAsHtmlFile(File htmlFile, String fileContent) throws IOException {
		try {
			// too slow for now
			// Source xmlInput = new StreamSource(new
			// StringReader(fileContent));
			// StringWriter stringWriter = new StringWriter();
			// StreamResult xmlOutput = new StreamResult(stringWriter);
			// TransformerFactory transformerFactory =
			// TransformerFactory.newInstance();
			// transformerFactory.setAttribute("indent-number", 2);
			// Transformer transformer = transformerFactory.newTransformer();
			// transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			// transformer.transform(xmlInput, xmlOutput);
			// fileContent = xmlOutput.getWriter().toString();
		} catch (Exception e) {
			logger.info("Could not format file '{}', cause: {}", htmlFile.getName(), e.getMessage());
		}

		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(htmlFile), ENC));
			bufferedWriter.write(fileContent);
			logger.info("Saved file '{}'", htmlFile.getName());
		} finally {
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}
		}
	}
}

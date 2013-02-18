package org.openpano.wikieater.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.openpano.wikieater.data.CssData;
import org.openpano.wikieater.data.PageData;
import org.openpano.wikieater.util.LoggerFormatter;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author mstandio
 */
public class FileUtils {

	public static final String ENC = "UTF-8";

	public enum FileType {
		HTML, CSS, IMAGE
	}

	private static final Logger logger = Logger.getLogger(FileUtils.class.getName());

	static {
		Formatter formatter = new LoggerFormatter();
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(formatter);
		logger.addHandler(consoleHandler);
		logger.setUseParentHandlers(false);
	}

	public String getUrlContent(String url, File cacheFolder, FileType fileType) throws IOException {
		url = url.trim();
		String cacheFileName = null;
		if (FileType.HTML.equals(fileType)) {
			cacheFileName = makeHtmlCacheFileName(url);
		} else if (FileType.CSS.equals(fileType)) {
			cacheFileName = makeCssCacheFileName(url);
		} else {
			throw new RuntimeException("Unsupported fileType: " + fileType);
		}
		if (cacheFileName == null) {
			return "";
		}
		Set<String> cachedUrls = listCachedUrls(cacheFolder);
		if (cachedUrls.contains(url)) {
			File[] cacheFiles = cacheFolder.listFiles();
			for (File cacheFile : cacheFiles) {
				if (cacheFile.isFile() && cacheFile.getName().equals(cacheFileName)) {
					return getCacheRemainingLines(cacheFile);
				}
			}
			throw new IOException("Could not find: " + cacheFileName);
		} else {
			logger.info("Downloading: " + url);
			String urlContent = readFromUrl(url);
			if (FileType.CSS.equals(fileType)) {
				urlContent = cleanCssContent(urlContent);
			}
			File cacheFile = new File(cacheFolder, cacheFileName);
			saveToCache(cacheFile, url, urlContent);
			return urlContent;
		}
	}

	public File getUrlFile(String url, File cacheFolder, FileType fileType) throws IOException {
		url = url.trim();
		String cacheFileName = null;
		if (FileType.IMAGE.equals(fileType)) {
			cacheFileName = makeImageCacheFileName(url);
		} else {
			throw new RuntimeException("Unsupported fileType: " + fileType);
		}
		File cacheFile = new File(cacheFolder, cacheFileName);
		if (cacheFile.exists()) {
			return cacheFile;
		}
		logger.info("Downloading: " + url);
		URL website = new URL(url);
		FileOutputStream fileOutputStream = null;
		try {
			ReadableByteChannel readableByteChannel = Channels.newChannel(website.openStream());
			fileOutputStream = new FileOutputStream(cacheFile);
			fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, 1 << 24);
			fileOutputStream.close();
		} finally {
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}
		}
		return cacheFile;
	}

	public Set<String> readUrls(File menuFile) throws IOException {
		Set<String> urls = new HashSet<String>();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(menuFile),
					Charset.forName(ENC)));
			Pattern pattern = Pattern.compile("http(s)?://[^\\s\\]]+");
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					urls.add(removeNamedAnchorFromUrl(matcher.group()));
				}
			}
		} finally {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}
		logger.info("Found " + urls.size() + " urls in file " + menuFile.getName());
		return urls;
	}

	Set<String> listCachedUrls(File cacheFolder) throws IOException {
		final Set<String> firstLines = new HashSet<String>();
		File[] cacheFiles = cacheFolder.listFiles();
		for (File cacheFile : cacheFiles) {
			if (cacheFile.isFile()) {
				firstLines.add(getCacheFirstLine(cacheFile));
			}
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
				stringBuilder.append("\n");
			}
		} finally {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}
		return stringBuilder.toString();
	}

	String makeHtmlCacheFileName(String url) {
		String fileName = url.toLowerCase();
		fileName = removeNamedAnchorFromUrl(fileName);
		fileName = fileName.replaceAll("_", "-");
		fileName = fileName.replaceAll("\\s+", "-");
		fileName = fileName.replaceAll(":", "_");
		fileName = fileName.replaceAll(";", "_");
		fileName = fileName.replaceAll("[\\*\\?\\|\"/\\\\]+", "");
		fileName += ".html";
		if (url.contains("/")) {
			fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
		}
		return fileName;
	}

	String removeNamedAnchorFromUrl(String url) {
		if (url.contains("#")) {
			url = url.substring(0, url.indexOf("#"));
		}
		return url;
	}

	String makeCssCacheFileName(String url) {
		url = url.replaceAll("=", ".");
		Pattern pattern = Pattern.compile("[^\\*\\?\\|\"/\\\\:;\\s]+\\.css", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {
			return matcher.group();
		} else {
			return null;
		}
	}

	String cleanCssContent(String cssContent) {
		String patternCommentOpen = "/\\*";
		String patternCommentClose = "\\*/";
		Pattern pattern = Pattern.compile(patternCommentOpen + "|" + patternCommentClose);
		Matcher matcher = pattern.matcher(cssContent);
		StringBuilder stringBuilder = new StringBuilder();
		int start = 0;
		while (matcher.find()) {
			String group = matcher.group();
			if (group.matches(patternCommentOpen)) {
				stringBuilder.append(cssContent.substring(start, matcher.start()));
			} else {
				start = matcher.end();
			}
		}
		stringBuilder.append(cssContent.substring(start, cssContent.length()));
		cssContent = stringBuilder.toString();
		cssContent = cssContent.replaceAll("\\s+", " ");
		cssContent = cssContent.replaceAll("}\\s+", "}\n");
		return cssContent;
	}

	String makeImageCacheFileName(String url) {
		String fileName = url.trim();
		fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
		return fileName;
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
		return makeHtmlCacheFileName(url);
	}

	public String makeImageFileName(String url) {
		return makeImageCacheFileName(url);
	}

	public void saveAsHtmlFile(PageData pageData, String outputDirecotry) throws IOException {
		String pageContent = pageData.getPageContent();
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			documentBuilder.setEntityResolver(new EntityResolver() {

				@Override
				public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
					return new InputSource(new ByteArrayInputStream("".getBytes()));
				}
			});
			Document document = documentBuilder.parse(new ByteArrayInputStream(pageContent.getBytes(ENC)));

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute("indent-number", 4);
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "html");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			StringWriter stringWriter = new StringWriter();
			StreamResult streamResult = new StreamResult(stringWriter);
			DOMSource domSource = new DOMSource(document);
			transformer.transform(domSource, streamResult);
			pageContent = stringWriter.toString();

		} catch (Exception e) {
			logger.info("Could not format file '" + pageData.getHtmlFileName() + "', cause: " + e.getMessage());
			pageContent = pageData.getPageContent();
		}

		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(new File(
					outputDirecotry), pageData.getHtmlFileName())), ENC));
			bufferedWriter.write(pageContent);
			logger.info("Saved file " + pageData.getHtmlFileName());
		} finally {
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}
		}
	}

	public void copyFile(File file, String targetDirectory) throws IOException {
		InputStream inStream = null;
		OutputStream outStream = null;
		try {
			inStream = new FileInputStream(file);
			outStream = new FileOutputStream(new File(new File(targetDirectory), file.getName()));
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inStream.read(buffer)) > 0) {
				outStream.write(buffer, 0, length);
			}
		} finally {
			if (outStream != null) {
				outStream.close();
			}
			if (inStream != null) {
				inStream.close();
			}
		}
	}

	public void saveCssDataIntoFile(Set<CssData> cssDataSet, File cssFile) throws IOException {
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cssFile), ENC));
			for (CssData cssData : cssDataSet) {
				bufferedWriter.write(cssData.getSelector() + " {" + cssData.getBody() + "}\n");
			}
		} finally {
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}
		}
	}

	public void cleanDirectory(File directory) {
		if (directory.isDirectory() && directory.exists()) {
			for (File file : directory.listFiles()) {
				if (file.isDirectory()
						&& (file.getName().equals("resources") || file.getName().equals("images") || file.getName()
								.equals("css"))) {
					cleanDirectory(file);
				} else if (file.getName().toLowerCase()
						.matches(".+\\.html$|.+\\.css$|.+\\.jpg$|.+\\.jpeg$|.+\\.gif|.+\\.png|.+\\.bmp")) {
					file.delete();
				}
			}
		}
	}
}

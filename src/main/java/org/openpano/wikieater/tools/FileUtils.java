package org.openpano.wikieater.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openpano.wikieater.data.CssData;
import org.openpano.wikieater.data.PageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mstandio
 */
public class FileUtils {

	public enum FileType {
		HTML, CSS, IMAGE
	}

	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

	private static final String ENC = "UTF-8";

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
			logger.info("Downloading: {}", url);
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
		logger.info("Downloading: '{}'", url);
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

	public Set<String> readUrls(File urlsFile) throws IOException {
		Set<String> urls = new HashSet<String>();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(urlsFile),
					Charset.forName(ENC)));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty() && !line.startsWith("#")) {
					urls.add(removeNamedAnchorFromUrl(line));
				}
			}
		} finally {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}
		logger.info("Found {} urls in file '{}'", urls.size(), urlsFile.getName());
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

	String makeCssCacheFileName(String url) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(url.getBytes());
			byte[] digest = md.digest();
			StringBuffer stringBuffer = new StringBuffer();
			for (byte b : digest) {
				stringBuffer.append(Integer.toHexString((int) (b & 0xff)));
			}
			return stringBuffer.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
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
		return makeHtmlCacheFileName(url) + ".html";
	}

	public String makeImageFileName(String url) {
		return makeImageCacheFileName(url);
	}

	public void saveAsHtmlFile(PageData pageData, String outputDirecotry) throws IOException {
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
			logger.info("Could not format file '{}', cause: {}", pageData.getHtmlFileName(), e.getMessage());
		}

		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(new File(
					outputDirecotry), pageData.getHtmlFileName())), ENC));
			bufferedWriter.write(pageData.getPageContent());
			logger.info("Saved file '{}'", pageData.getHtmlFileName());
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
}

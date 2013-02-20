package org.openpano.wikieater.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openpano.wikieater.tools.FileUtils.FileType;

/**
 * @author mstandio
 */
public class FileUtilsTest {

	FileUtils fileUtils;
	File cacheFolder;
	File file1;
	File file2;

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		fileUtils = new FileUtils();

		cacheFolder = new File(this.getClass().getResource("/cache").getFile());
		assertTrue(cacheFolder.exists());

		file1 = new File(this.getClass().getResource("/cache/file1").getFile());
		file2 = new File(this.getClass().getResource("/cache/file2").getFile());
		assertTrue(file1.exists());
		assertTrue(file2.exists());
	}

	@Test
	public void getCacheFirstLineTest() throws Exception {
		assertEquals("file1 first line", fileUtils.getCacheFirstLine(file1));
	}

	@Test
	public void getCacheRemainingLines() throws Exception {
		String remainingLines = fileUtils.getCacheRemainingLines(file1);
		assertFalse(remainingLines.matches(".*first.*"));
		assertTrue(remainingLines.matches(".*second.*"));
		assertTrue(remainingLines.matches(".*third.*"));
	}

	@Test
	public void listCachedUrlsTest() throws Exception {
		Set<String> cachedUrls = fileUtils.listCachedUrls(cacheFolder);
		assertEquals(2, cachedUrls.size());
		assertTrue(cachedUrls.contains("file1 first line"));
		assertTrue(cachedUrls.contains("file2 first line"));
	}

	@Test
	public void makeHtmlCacheFileNameTest() throws Exception {
		assertEquals("module_image-gallery.html",
				fileUtils.makeHtmlCacheFileName("http://panozona.com/wiki/Module:Image_Gallery#group"));
	}

	@Test
	public void makeCssCacheFileNameTest() throws Exception {
		assertEquals("Print.css", fileUtils.makeCssCacheFileName("/w/index.php?title=MediaWiki:Print.css&amp;"));
		assertEquals("combined.min.css", fileUtils.makeCssCacheFileName("/css/combined.min.css?69"));
		assertEquals("gen.css", fileUtils.makeCssCacheFileName("action=raw&amp;maxage=18000&amp;gen=css"));
		assertEquals(null, fileUtils.makeCssCacheFileName(""));
	}

	@Test
	public void cleanCssContentTest() throws Exception {
		String cssContent = "A/*B*/C/*D*/E";

		String expectedResult = "ACE";

		assertEquals(expectedResult, fileUtils.cleanCssContent(cssContent));
	}

	@Test
	public void makeImageCacheFileNameTest() throws Exception {
		assertEquals("image.png", fileUtils.makeImageCacheFileName("http://panozona.com/wiki/file/image.png"));
	}

	@Test
	public void removeNamedAnchorFromUrlTest() throws Exception {
		assertEquals("http://panozona.com/wiki/Module:ImageGallery",
				fileUtils.removeNamedAnchorFromUrl("http://panozona.com/wiki/Module:ImageGallery#group"));
	}

	@Test
	public void saveToCacheTest() throws Exception {
		final String sampleUrl = "http://file3";
		final String sampleContent = "sampleContent";

		File tmpFile3 = testFolder.newFile("file3");

		fileUtils.saveToCache(tmpFile3, sampleUrl, sampleContent);

		assertEquals(sampleUrl, fileUtils.getCacheFirstLine(tmpFile3));
		assertEquals(sampleContent, fileUtils.getCacheRemainingLines(tmpFile3));
	}

	@Test
	public void getUrlContentFromCacheTest() throws Exception {
		final String sampleUrl = "http://file4";
		final String sampleContent = "sampleContent";

		File tmpFile4 = testFolder.newFile("file4.html");
		fileUtils.saveToCache(tmpFile4, sampleUrl, sampleContent);

		assertEquals(sampleContent, fileUtils.getUrlContent(sampleUrl, testFolder.getRoot(), FileType.HTML));
	}

	@Test
	public void getUrlContentFromUrlTest() throws Exception {
		final String sampleUrl = "http://file5";
		final String sampleContent = "sampleContent";

		FileUtilsMock fileUtilsMock = new FileUtilsMock(sampleContent);

		assertEquals(sampleContent, fileUtilsMock.getUrlContent(sampleUrl, testFolder.getRoot(), FileType.HTML));
	}

	private class FileUtilsMock extends FileUtils {

		final String urlResult;

		FileUtilsMock(String urlResult) {
			this.urlResult = urlResult;
		}

		@Override
		String readFromUrl(String url) throws IOException {
			return urlResult;
		}
	}

	@Test
	public void readUrlsTest() throws IOException {
		File fileMenu = new File(this.getClass().getResource("/menu.txt").getFile());
		assertTrue(fileMenu.exists());

		Set<String> urls = fileUtils.readUrls(fileMenu);

		assertEquals(2, urls.size());
		assertTrue(urls.contains("http://link1"));
		assertTrue(urls.contains("http://link2"));
	}

	@Test
	public void makeHtmlFileNameTest() throws Exception {
		assertEquals("module_imagegallery.html",
				fileUtils.makeHtmlCacheFileName("http://panozona.com/wiki/Module:ImageGallery#group"));
	}
}

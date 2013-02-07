package org.openpano.wikieater.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openpano.wikieater.data.ImageData;
import org.openpano.wikieater.data.PageData;
import org.openpano.wikieater.tools.UrlUtils.HrefReplacer;

/**
 * @author mstandio
 */
public class UrlUtilsTest {

	UrlUtils urlUtils;
	List<PageData> pageDataList;
	Set<ImageData> imageDataSet;
	PageData pageData;

	@Before
	public void setUp() throws Exception {
		urlUtils = new UrlUtils();
		pageDataList = new ArrayList<PageData>();
		imageDataSet = new HashSet<ImageData>();
	}

	@Test
	public void replacePageContentHrefsTest() throws Exception {
		pageData = new PageData("", "", "XY<a href=\"X\">XY<img src=\"Y\">XY");
		
		HrefReplacerMock hrefReplacerMock = new HrefReplacerMock();
		String result = urlUtils.replacePageContentHrefs(pageData, hrefReplacerMock);

		assertEquals(2, hrefReplacerMock.requestedReplacements.size());
		assertEquals("X", hrefReplacerMock.requestedReplacements.get(0));
		assertEquals("Y", hrefReplacerMock.requestedReplacements.get(1));
		assertEquals("XY<a href=\"XX\">XY<img src=\"YY\">XY", result);
	}

	@Test
	public void replacePageContentHrefsNoMatchesTest() throws Exception {
		pageData = new PageData("","","XYXY");

		HrefReplacerMock hrefReplacerMock = new HrefReplacerMock();
		String result = urlUtils.replacePageContentHrefs(pageData, hrefReplacerMock);

		assertEquals(0, hrefReplacerMock.requestedReplacements.size());
		assertEquals(pageData.getPageContent(), result);
	}

	class HrefReplacerMock implements HrefReplacer {

		final List<String> requestedReplacements = new ArrayList<String>();

		@Override
		public String getReplacement(String href) {
			requestedReplacements.add(href);
			return href + href;
		}
	}

	@Test
	public void findHrefPageReplacementTest() throws Exception {
		pageDataList.add(new PageData("http://x/a", "a.html", ""));
		pageDataList.add(new PageData("http://x/b", "b.html", ""));

		assertEquals("a.html", urlUtils.findHrefPageReplacement("/a", pageDataList));
		assertEquals("/c", urlUtils.findHrefPageReplacement("/c", pageDataList));
	}

	@Test
	public void pageUrlMatchesHrefTest() throws Exception {
		final String pageUrl = "http://a/b/c/d:e";

		assertTrue(urlUtils.pageUrlMatchesHref(pageUrl, "/c/d:e"));
		assertTrue(urlUtils.pageUrlMatchesHref(pageUrl, "/c/d:e#f"));
		assertTrue(urlUtils.pageUrlMatchesHref(pageUrl, "http://a/b/c/d:e"));
		assertTrue(urlUtils.pageUrlMatchesHref(pageUrl, "http://a/b/c/d:e#f"));

		assertFalse(urlUtils.pageUrlMatchesHref(pageUrl, "/a/b/c/d:h"));
		assertFalse(urlUtils.pageUrlMatchesHref(pageUrl, "http://a/b/c/d:h"));
	}

	@Test
	public void translateHtmlFileNameToHrefTest() throws Exception {
		assertEquals("page.html", urlUtils.translateHtmlFileNameToHref("page.html", "a:b"));
		assertEquals("page.html#c", urlUtils.translateHtmlFileNameToHref("page.html", "a:b#c"));
	}

	@Test
	public void replacePageUrlsTest() throws Exception {
		pageDataList.add(new PageData("http://x/y/z/a", "a.html", "<a href=\"/z/b\">"));
		pageDataList.add(new PageData("http://x/y/z/b", "b.html", "<a href=\"/z/a#c\">"));

		urlUtils.replacePageUrls(pageDataList);

		assertEquals("<a href=\"b.html\">", pageDataList.get(0).getPageContent());
		assertEquals("<a href=\"a.html#c\">", pageDataList.get(1).getPageContent());
	}

	@Test
	public void findSrcImageReplacementTest() throws Exception {
		imageDataSet.add(new ImageData("a.jpg", null));
		imageDataSet.add(new ImageData("b.gif", null));

		assertEquals("out/a.jpg", urlUtils.findSrcImageReplacement("/a.jpg", imageDataSet, "out"));
		assertEquals("out/b.gif", urlUtils.findSrcImageReplacement("/b.gif", imageDataSet, "out"));
	}

	@Test
	public void replaceImageUrlsTest() throws Exception {
		pageDataList.add(new PageData("http://x/y/z", "", "<img src=\"/a.jpg\">"));
		pageDataList.add(new PageData("http://x/y/z", "", "<img src=\"/b.gif\">"));

		imageDataSet.add(new ImageData("a.jpg", null));
		imageDataSet.add(new ImageData("b.gif", null));

		urlUtils.replaceImageUrls(pageDataList, imageDataSet, "out");

		assertEquals("<img src=\"out/a.jpg\">", pageDataList.get(0).getPageContent());
		assertEquals("<img src=\"out/b.gif\">", pageDataList.get(1).getPageContent());
	}
}

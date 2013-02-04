package org.openpano.wikieater.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openpano.wikieater.data.PageData;
import org.openpano.wikieater.tools.UrlUtils.HrefReplacer;

/**
 * @author mstandio
 */
public class UrlUtilsTest {

	UrlUtils urlUtils;
	List<PageData> pageDataList;

	@Before
	public void setUp() throws Exception {
		urlUtils = new UrlUtils();
		pageDataList = new ArrayList<PageData>();
	}

	@Test
	public void replacePageContentHrefsTest() throws Exception {
		String pageContent = "XY<a href=\"X\">XY<img src=\"Y\">XY";

		HrefReplacerMock hrefReplacerMock = new HrefReplacerMock();
		String result = urlUtils.replacePageContentHrefs(pageContent, hrefReplacerMock);

		assertEquals(2, hrefReplacerMock.requestedReplacements.size());
		assertEquals("X", hrefReplacerMock.requestedReplacements.get(0));
		assertEquals("Y", hrefReplacerMock.requestedReplacements.get(1));
		assertEquals("XY<a href=\"XX\">XY<img src=\"YY\">XY", result);
	}

	@Test
	public void replacePageContentHrefsNoMatchesTest() throws Exception {
		String pageContent = "XYXY";

		HrefReplacerMock hrefReplacerMock = new HrefReplacerMock();
		String result = urlUtils.replacePageContentHrefs(pageContent, hrefReplacerMock);

		assertEquals(0, hrefReplacerMock.requestedReplacements.size());
		assertEquals(pageContent, result);
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
	public void findHrefReplacementTest() throws Exception {
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
}

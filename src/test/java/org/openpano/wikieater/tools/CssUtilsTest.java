package org.openpano.wikieater.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openpano.wikieater.data.CssData;
import org.openpano.wikieater.data.PageData;

/**
 * @author mstandio
 */
public class CssUtilsTest {

	CssUtils cssUtils;
	List<PageData> pageDataList;

	@Before
	public void setUp() throws Exception {
		cssUtils = new CssUtils();
		pageDataList = new ArrayList<PageData>();
	}

	@Test
	public void makeCssUrlTest() throws Exception {
		assertEquals("http://site.com/w/file.css", cssUtils.makeCssUrl("/w/file.css", "http://site.com/wiki/some/page"));
	}

	@Test
	public void makeCssUrlsTest() throws Exception {
		String pageUrl = "http://site.com/wiki/some/page";
		Set<String> cssLinks = new HashSet<String>();
		cssLinks.add("/w/a");
		cssLinks.add("/w/b");
		Set<String> cssUrls = cssUtils.makeCssUrls(cssLinks, pageUrl);

		assertEquals(2, cssUrls.size());
		assertTrue(cssUrls.contains("http://site.com/w/a"));
		assertTrue(cssUrls.contains("http://site.com/w/b"));
	}

	@Test
	public void extractCssLinksTest() throws Exception {
		String pageContent = "<link rel=\"stylesheet\" href=\"/A\"/>" + "<link rel=\"stylesheet\" href=\"/B\"/>";
		Set<String> cssLinks = cssUtils.extractCssLinks(pageContent);

		assertEquals(2, cssLinks.size());
		assertTrue(cssLinks.contains("/A"));
		assertTrue(cssLinks.contains("/B"));
	}

	@Test
	public void harvestCssUrlsTest() throws Exception {
		pageDataList.add(new PageData("http://site.com/wiki/a", "", "<link rel=\"stylesheet\" href=\"/A\"/>"));
		pageDataList.add(new PageData("http://site.com/wiki/b", "", "<link rel=\"stylesheet\" href=\"/B\"/>"));

		Set<String> cssUrls = cssUtils.harvestCssUrls(pageDataList);

		assertEquals(2, cssUrls.size());
		assertTrue(cssUrls.contains("http://site.com/A"));
		assertTrue(cssUrls.contains("http://site.com/B"));
	}

	@Test
	public void extractEmbededCssTest() throws Exception {
		String pageContent = "A<style>B</style>C<style>D</style>E";

		assertEquals("BD", cssUtils.extractEmbededCss(pageContent));
	}

	@Test
	public void extractCssDataTest() throws Exception {
		String cssContent = "A{B}C{D}A{E}";

		Set<CssData> cssDataSet = cssUtils.extractCssData(cssContent);
		assertEquals(2, cssDataSet.size());

		boolean foundA = false;
		boolean foundC = false;

		for (CssData cssData : cssDataSet) {
			if (cssData.getSelector().equals("A")) {
				foundA = true;
				assertEquals("B", cssData.getBody());
			} else if (cssData.getSelector().equals("C")) {
				foundC = true;
				assertEquals("D", cssData.getBody());
			}
		}

		assertTrue(foundA);
		assertTrue(foundC);
	}
}

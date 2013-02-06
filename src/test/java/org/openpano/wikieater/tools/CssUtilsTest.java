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
import org.openpano.wikieater.data.CssData;
import org.openpano.wikieater.data.PageData;

/**
 * @author mstandio
 */
public class CssUtilsTest {

	CssUtils cssUtils;
	List<PageData> pageDataList;
	CssData cssData;

	@Before
	public void setUp() throws Exception {
		cssUtils = new CssUtils();
		pageDataList = new ArrayList<PageData>();
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

	@Test
	public void analyseCssDataTest() throws Exception {
		cssData = new CssData("", "");
		cssUtils.analyseCssData(cssData);

		assertEquals(0, cssData.ids.size());
		assertEquals(0, cssData.cls.size());

		cssData = new CssData("#s_ids", "");
		cssUtils.analyseCssData(cssData);

		assertEquals(1, cssData.ids.size());
		assertEquals(0, cssData.cls.size());
		assertTrue(cssData.ids.contains("s_ids"));
		assertFalse(cssData.isUniversal);

		cssData = new CssData(".s-cls", "");
		cssUtils.analyseCssData(cssData);

		assertEquals(0, cssData.ids.size());
		assertEquals(1, cssData.cls.size());
		assertTrue(cssData.cls.contains("s-cls"));
		assertFalse(cssData.isUniversal);

		cssData = new CssData("li", "");
		cssUtils.analyseCssData(cssData);
		assertTrue(cssData.isUniversal);
	}

	@Test
	public void findOccuringCssTest() throws Exception {
		Set<CssData> cssDataSetResult = null;
		Set<CssData> cssDataSet = new HashSet<CssData>();

		cssDataSet.clear();
		cssData = new CssData(".scls", "");
		cssUtils.analyseCssData(cssData);
		cssDataSet.add(cssData);
		cssData = new CssData(".ath", "");
		cssUtils.analyseCssData(cssData);
		cssDataSet.add(cssData);
		
		cssDataSetResult = cssUtils.findOccuringCss(cssDataSet, "class=\"scls\"");

		assertEquals(1, cssDataSetResult.size());

		cssDataSet.clear();
		cssData = new CssData("#scls", "");
		cssUtils.analyseCssData(cssData);
		cssDataSet.add(cssData);
		cssData = new CssData("#ath", "");
		cssUtils.analyseCssData(cssData);
		cssDataSet.add(cssData);
		cssDataSetResult = cssUtils.findOccuringCss(cssDataSet, "id=\"scls\"");
		
		assertEquals(1, cssDataSetResult.size());
		
		cssDataSet.clear();
		cssData = new CssData(".scls", "");
		cssUtils.analyseCssData(cssData);
		cssDataSet.add(cssData);
		cssData = new CssData("#ath", "");
		cssUtils.analyseCssData(cssData);
		cssDataSet.add(cssData);
		cssDataSetResult = cssUtils.findOccuringCss(cssDataSet, "class=\"scls\" id=\"ath\"");
		
		assertEquals(2, cssDataSetResult.size());
		
		cssDataSet.clear();
		cssData = new CssData(".scls", "");
		cssUtils.analyseCssData(cssData);
		cssDataSet.add(cssData);
		cssData = new CssData("#scls", "");
		cssUtils.analyseCssData(cssData);
		cssDataSet.add(cssData);
		cssDataSetResult = cssUtils.findOccuringCss(cssDataSet, "class=\"scls\"");
		
		assertEquals(1, cssDataSetResult.size());
	}
}

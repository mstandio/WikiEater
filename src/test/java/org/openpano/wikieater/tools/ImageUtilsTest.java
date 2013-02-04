package org.openpano.wikieater.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openpano.wikieater.data.PageData;

/**
 * @author mstandio
 */
public class ImageUtilsTest {

	ImageUtils imageUtils;
	List<PageData> pageDataList;

	@Before
	public void setUp() throws Exception {
		imageUtils = new ImageUtils();
		pageDataList = new ArrayList<PageData>();
	}

	@Test
	public void extractImageLinksTest() throws Exception {
		String pageContent = "<img src=\"/a.bmp\">" + "<img src=\"/b.jpg\">";
		Set<String> imageLinks = imageUtils.extractImageLinks(pageContent);

		assertEquals(2, imageLinks.size());
		assertTrue(imageLinks.contains("/a.bmp"));
		assertTrue(imageLinks.contains("/b.jpg"));
	}

	@Test
	public void harvestImageUrlsTest() throws Exception {
		pageDataList.add(new PageData("http://site.com/some/page", "", "<img src=\"/a.bmp\">"));
		pageDataList.add(new PageData("http://site.com/some/page", "", "<img src=\"/b.jpg\">"));

		Set<String> imageUrls = imageUtils.harvestImageUrls(pageDataList);
		assertEquals(2, imageUrls.size());
		assertTrue(imageUrls.contains("http://site.com/a.bmp"));
		assertTrue(imageUrls.contains("http://site.com/b.jpg"));
	}
}

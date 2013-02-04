package org.openpano.wikieater.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * @author mstandio
 */
public class MediaUtilsTest {

	MediaUtilsImpl mediaUtils;

	@Before
	public void setUp() throws Exception {
		mediaUtils = new MediaUtilsImpl();
	}

	@Test
	public void makeMeidaUrlTest() throws Exception {
		assertEquals("http://site.com/w/file.css",
				mediaUtils.makeMediaUrl("/w/file.css", "http://site.com/wiki/some/page"));
		
		assertEquals("http://extsite.com/file.css",
				mediaUtils.makeMediaUrl("http://extsite.com/file.css", "http://site.com/wiki/some/page"));
	}

	@Test
	public void makeMeidiaUrlsTest() throws Exception {
		String pageUrl = "http://site.com/wiki/some/page";
		Set<String> cssLinks = new HashSet<String>();
		cssLinks.add("/w/a");
		cssLinks.add("/w/b");
		Set<String> cssUrls = mediaUtils.makeMediaUrls(cssLinks, pageUrl);

		assertEquals(2, cssUrls.size());
		assertTrue(cssUrls.contains("http://site.com/w/a"));
		assertTrue(cssUrls.contains("http://site.com/w/b"));
	}

	class MediaUtilsImpl extends MediaUtils {

	}
}

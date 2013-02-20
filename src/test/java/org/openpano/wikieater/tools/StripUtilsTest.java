package org.openpano.wikieater.tools;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openpano.wikieater.tools.StripUtils.ElementType;

/**
 * @author mstandio
 */
public class StripUtilsTest {

	StripUtils stripUtils;

	@Before
	public void setUp() throws Exception {
		stripUtils = new StripUtils();
	}

	@Test
	public void extractBodyFromPageContent() throws Exception {
		String pageContent = "A<body BC>D</body>E";

		String expectedResult = "<body BC>D</body>";

		assertEquals(expectedResult, stripUtils.extractBodyFromPageContent(pageContent));
	}

	@Test
	public void removeScriptsFromPagecontent() throws Exception {
		String pageContent = "A<script>D</script>E";

		String expectedResult = "AE";

		assertEquals(expectedResult, stripUtils.removeScriptsFromPagecontent(pageContent));
	}

	@Test
	public void removeCommentsFromPagecontent() throws Exception {
		String pageContent = "A<!--B-->C";

		String expectedResult = "AC";

		assertEquals(expectedResult, stripUtils.removeCommentsFromPagecontent(pageContent));
	}

	@Test
	public void extractDivFromPageContent() throws Exception {
		String pageContent = "<div>" + "<div id=\"a\"></div>" + "<div class=\"b\">" + "<div class=\"c\"></div>"
				+ "</div>" + "<div class=\"d\"></div>" + "</div>";

		String expectedResult = "<div class=\"b\">" + "<div class=\"c\"></div>" + "</div>";

		assertEquals(expectedResult, stripUtils.extractDivFromPageContent(pageContent, "b"));
	}

	@Test
	public void removeElementsFromPageContentTest() throws Exception {
		String pageContent = "<div>" + "<div id=\"a\"></div>" + "<div class=\"a\">" + "<div class=\"b\"></div>"
				+ "</div>" + "<div class=\"c\"></div>" + "</div>";

		String expectedResult = "<div>" + "<div class=\"c\"></div>" + "</div>";

		assertEquals(expectedResult, stripUtils.removeElementsFromPageContent(pageContent, ElementType.div, "a"));
	}

	@Test
	public void peelElementsFromPageContentTest() throws Exception {
		String pageContent = "<div>" + "<div id=\"a\"></div>" + "<div class=\"a\">" + "<div class=\"b\"></div>"
				+ "</div>" + "<div class=\"c\"></div>" + "</div>";

		String expectedResult = "<div>" + "<div class=\"b\"></div>" + "<div class=\"c\"></div>" + "</div>";

		assertEquals(expectedResult, stripUtils.peelElementsFromPageContent(pageContent, ElementType.div, "a"));
	}

	@Test
	public void cleanupPageContent() throws Exception {
		String pageContent = "A<h1></h1>B<h2>C</h2>D<h3 class=\"F\">  </h3>E<h4/><h4/>";

		String expectedResult = "AB<h2>C</h2>DE<h4/><h4/>";

		assertEquals(expectedResult, stripUtils.cleanupPageContent(pageContent));
	}

	@Test
	public void stripPageContentTest() throws Exception {

	}
}

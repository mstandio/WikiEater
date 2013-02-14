package org.openpano.wikieater.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openpano.wikieater.data.PageData;

/**
 * @author MSt
 */
public class MenuUtilsTest {
	MenuUtils menuUtils;
	List<PageData> pageDataList;

	@Before
	public void setUp() throws Exception {
		menuUtils = new MenuUtils();
		pageDataList = new ArrayList<PageData>();
	}

	@Test
	public void digestUrlTagTest() throws Exception {
		pageDataList.add(new PageData("http://a", "file.html", null));
		assertEquals("<a href=\"file.html\" target=\"content\">A</a>",
				menuUtils.digestUrlTag("[http://a A]", pageDataList));
	}
	
	@Test
	public void digestUrlTagsTest() throws Exception {
		pageDataList.add(new PageData("http://a", "file1.html", null));
		pageDataList.add(new PageData("http://b", "file2.html", null));
		
		String result = menuUtils.digestLineUrlTags("[http://a A][http://b]", pageDataList); 
		
		String expected = "<a href=\"file1.html\" target=\"content\">A</a>" +
				"<a href=\"file2.html\" target=\"content\">[?]</a>";
		
		assertEquals(expected, result);
	}

	@Test
	public void getTitleLevelTest() throws Exception {
		assertEquals(0, menuUtils.getTitleLevel(null));
		assertEquals(0, menuUtils.getTitleLevel("=="));
		assertEquals(1, menuUtils.getTitleLevel("=A"));
		assertEquals(1, menuUtils.getTitleLevel("=A="));
		assertEquals(2, menuUtils.getTitleLevel("==A=="));
	}

	@Test
	public void getListLevelTest() throws Exception {
		assertEquals(0, menuUtils.getListLevel(null));
		assertEquals(1, menuUtils.getListLevel("*"));
		assertEquals(2, menuUtils.getListLevel("**"));
		assertEquals(3, menuUtils.getListLevel("*** *"));
	}

	@Test
	public void digestListTest() throws Exception {

		// single li
		assertEquals("<ul><li>B</li></ul>", menuUtils.digestList(null, "*B", null));

		// two li same level
		assertEquals("<ul><li>A", menuUtils.digestList(null, "*A", "*B"));
		assertEquals("</li><li>B</li></ul>", menuUtils.digestList("*A", "*B", null));

		// two li level increase
		assertEquals("<ul><li>A", menuUtils.digestList(null, "*A", "**B"));
		assertEquals("<ul><li>B</li></ul></li></ul>", menuUtils.digestList("*A", "**B", null));

		// two li level decrease
		assertEquals("<ul><li><ul><li>A</li></ul>", menuUtils.digestList(null, "**A", "*B"));
		assertEquals("B</li></ul>", menuUtils.digestList("**A", "*B", null));
	}

	@Test
	public void makeMenuBodyTest() throws Exception {
		File menuFile = new File(this.getClass().getResource("/menu.txt").getFile());
		assertTrue(menuFile.exists());
		
		pageDataList.add(new PageData("http://link1", "link1.html", ""));
		pageDataList.add(new PageData("http://link2", "link2.html", ""));
		
		String result = menuUtils.makeMenuBody(menuFile, pageDataList);
		
		String expected = "<h1>T1</h1>" + 
				"<ul><li>"+ "<a href=\"link1.html\" target=\"content\">desc</a>" +  
				"<ul><li>"+ "<a href=\"link2.html\" target=\"content\">[?]</a>" +
				"</li></ul>"+
				"</li></ul>"+
				"<br/>" +
				"<h2>T2</h2>";
		
		assertEquals(expected, result);
	}
}

package org.openpano.wikieater.tools;

import static org.junit.Assert.assertEquals;

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
		assertEquals("<a href=\"file.html\">B</a>", menuUtils.digestUrlTag("[http://a B]", pageDataList));
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
}

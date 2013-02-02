package org.openpano.wikieater.data;

/**
 * @author mstandio
 */
public class PageData {

	private final String pageUrl;
	private final String htmlFileName;
	private String pageContent;

	public PageData(String pageUrl, String htmlFileName, String pageContent) {
		this.pageUrl = pageUrl;
		this.htmlFileName = htmlFileName;
		this.pageContent = pageContent;
	}

	public String getPageUrl() {
		return pageUrl;
	}

	public String getHtmlFileName() {
		return htmlFileName;
	}

	public String getPageContent() {
		return pageContent;
	}

	public void setPageContent(String pageContent) {
		this.pageContent = pageContent;
	}
}

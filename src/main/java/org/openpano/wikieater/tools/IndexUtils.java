package org.openpano.wikieater.tools;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openpano.wikieater.data.PageData;

/**
 * @author mstandio
 */
public class IndexUtils extends ResourceUtils {

	public PageData getIndexPageData(File menuFile, List<PageData> pageDataList) throws IOException {
		String indexFileName = "index.html";
		String indexTemplate = getResourceContent("/html/" + indexFileName);
		if (pageDataList.size() > 0) {
			// TODO select first in file
			indexTemplate = indexTemplate.replace("[DEFAULT]", pageDataList.get(0).getHtmlFileName());
		}
		return new PageData(null, indexFileName, indexTemplate);
	}
}

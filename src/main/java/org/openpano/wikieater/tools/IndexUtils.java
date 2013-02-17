package org.openpano.wikieater.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openpano.wikieater.data.PageData;

/**
 * @author mstandio
 */
public class IndexUtils extends ResourceUtils {

	public PageData getIndexPageData(File menuFile, List<PageData> pageDataList) throws IOException {
		String indexFileName = "index.html";
		String indexTemplate = getResourceContent("/html/" + indexFileName);
		if (pageDataList.size() > 0) {
			String firstUrl = null;
			BufferedReader bufferedReader = null;
			try {
				bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(menuFile),
						Charset.forName(FileUtils.ENC)));
				Pattern pattern = Pattern.compile("http(s)?://[^\\s\\]]+");
				String line = null;
				while ((line = bufferedReader.readLine()) != null) {
					Matcher matcher = pattern.matcher(line);
					if (matcher.find()) {
						firstUrl = matcher.group();
						break;
					}
				}
			} finally {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			}
			PageData firstPageData = null;
			for (PageData pageData : pageDataList) {
				if (firstUrl != null && firstUrl.contains(pageData.getPageUrl())) {
					firstPageData = pageData;
				}
			}
			if (firstPageData != null) {
				indexTemplate = indexTemplate.replace("[DEFAULT]", firstPageData.getHtmlFileName());
			}
		}
		return new PageData(null, indexFileName, indexTemplate);
	}
}

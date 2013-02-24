package org.openpano.wikieater.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * @author mstandio
 */
public class ResourceUtils {

	String getResourceContent(String resourcePath) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(
				resourcePath), FileUtils.ENC));
		for (int c = bufferedReader.read(); c != -1; c = bufferedReader.read()) {
			stringBuilder.append((char) c);
		}
		return stringBuilder.toString();
	}

	public void copyResource(String resourcePath, File targetDirectory) throws IOException {
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			String resourceName = resourcePath.substring(resourcePath.lastIndexOf("/"));
			inputStream = this.getClass().getResourceAsStream(resourcePath);
			outputStream = new FileOutputStream(new File(targetDirectory, resourceName));
			int read = 0;
			byte[] bytes = new byte[256];
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			inputStream.close();

		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}

			} finally {
				if (outputStream != null) {
					outputStream.flush();
					outputStream.close();
				}
			}
		}
	}
}

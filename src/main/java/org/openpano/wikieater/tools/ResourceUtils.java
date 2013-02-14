package org.openpano.wikieater.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author mstandio
 */
public class ResourceUtils {

	String getResourceContent(String resourceName) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(
				resourceName), FileUtils.ENC));
		for (int c = bufferedReader.read(); c != -1; c = bufferedReader.read()) {
			stringBuilder.append((char) c);
		}
		return stringBuilder.toString();
	}
}

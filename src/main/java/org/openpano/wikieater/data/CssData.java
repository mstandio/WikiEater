package org.openpano.wikieater.data;

import java.util.HashSet;
import java.util.Set;

/**
 * @author mstandio
 */
public class CssData {

	private final String selector;
	private final String body;

	public final Set<String> ids = new HashSet<String>();
	public final Set<String> cls = new HashSet<String>();
	public Boolean isUniversal = false;

	public CssData(String selector, String body) {
		this.selector = selector;
		this.body = body;
	}

	public String getSelector() {
		return selector;
	}

	public String getBody() {
		return body;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || !(obj instanceof CssData)) {
			return false;
		}
		return ((CssData) obj).selector.equals(selector);
	};

	@Override
	public int hashCode() {
		return selector.hashCode();
	}
}

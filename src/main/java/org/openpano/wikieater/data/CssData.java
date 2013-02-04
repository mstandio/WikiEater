package org.openpano.wikieater.data;

/**
 * @author mstandio
 */
public class CssData {

	private final String selector;
	private final String body;

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

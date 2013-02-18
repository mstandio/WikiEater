package org.openpano.wikieater.util;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author mstandio
 */
public class LoggerFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		StringBuilder builder = new StringBuilder(200);
		builder.append("[").append(record.getLevel()).append("] - ");
		builder.append(formatMessage(record));
		builder.append("\n");
		return builder.toString();
	}
}

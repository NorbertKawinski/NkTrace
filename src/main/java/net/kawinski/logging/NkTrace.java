package net.kawinski.logging;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.event.Level;
import org.slf4j.spi.LocationAwareLogger;

/**
 * Main class used for creating entry/exit message logs.
 *
 * Provides a closeable resource which can be conveniently used within try-with-resources blocks.
 * When created, it increments an indentation level for new logs.
 */
public class NkTrace implements AutoCloseable {
	/**
	 * To reduce configuration effort, NkTrace provides default formatting.
	 * If you want, you can disable this feature and configure the pattern yourself.
	 */
	public static boolean useDefaultFormatting = Boolean.parseBoolean(System.getProperty("NKTRACE_USE_DEFAULT_FORMATTING", "true"));

	private static final String DEFAULT_FQCN = NkTrace.class.getName();

	/**
	 * Name used to mark "entry" log entries.
	 * It's mainly used by log filters to apply correct message format.
	 */
	public static final String MARKER_TRACE_ENTRY_NAME = "NkTraceEntry";

	/**
	 * Name used to mark "exit" log entries.
	 * It's mainly used by log filters to apply correct message format.
	 */
	public static final String MARKER_TRACE_EXIT_NAME = "NkTraceExit";

	/**
	 * Marker object for "entry" messages.
	 * See {@link #MARKER_TRACE_ENTRY_NAME}
	 */
	public static final Marker MARKER_TRACE_ENTRY = MarkerFactory.getMarker(MARKER_TRACE_ENTRY_NAME);

	/**
	 * Marker object for "exit" messages.
	 * See {@link #MARKER_TRACE_EXIT_NAME}
	 */
	public static final Marker MARKER_TRACE_EXIT = MarkerFactory.getMarker(MARKER_TRACE_EXIT_NAME);

	private final String fqcn;
	private final LocationAwareLogger logger;
	private final Level level;
	private final boolean canLog;
	private final CallerInfo caller;
	private Object returning = null;
	private String exitMsgFormat = "";
	private Object[] exitMsgFormatArgs = {};

	/**
	 * Produces "entry" message and increments indentation for log messages.
	 *
	 * @param fqcn Fully Qualified Class Name of the logger
	 * @param logger Logger to use when producing entry/exit messages
	 * @param level Level to use when producing entry/exit messages
	 * @param entryFormat Format of the message conforming to SLF4J formatting
	 * @param entryFormatArgs Arguments to use when formatting the message
	 */
	public NkTrace(final String fqcn, final Logger logger, final Level level, final String entryFormat, final Object... entryFormatArgs) {
		this.fqcn = fqcn;
		this.logger = (LocationAwareLogger) logger;
		this.level = level;
		this.canLog = LoggingUtils.canLog(logger, level);
		if(!canLog) {
			caller = CallerInfo.UNKNOWN;
			return;
		}
		caller = CallerInfo.getCaller(fqcn);

		doEntryLog(entryFormat, entryFormatArgs);
		NkTraceIndent.increment();
	}

	/**
	 * Produces "exit" message and reduces indentation for log messages.
	 */
	@Override
	public void close() {
		if(!canLog) {
			return;
		}

		NkTraceIndent.decrement();
		doExitLog();
	}

	/**
	 * Specifies custom message to append to "exit" log.
	 *
	 * @param exitMsgFormat Format of the custom message
	 * @param exitMsgFormatArgs Arguments for the custom message
	 */
	public void setExitMsg(final String exitMsgFormat, final Object... exitMsgFormatArgs) {
		this.exitMsgFormat = exitMsgFormat;
		this.exitMsgFormatArgs = exitMsgFormatArgs.clone();
	}

	/**
	 * Convenience method to append function results to the "exit" logs.
	 *
	 * @param result function result
	 * @param <T> result type
	 * @return function result from the argument
	 */
	public <T> T returning(final T result) {
		returning = result;
		return result;
	}

	/**
	 * Convenience method with the functionality of both {@link #returning(Object)} and {@link #setExitMsg(String, Object...)}
	 *
	 * @param result function result
	 * @param exitMsgFormat Format of the custom message
	 * @param exitMsgFormatArgs Arguments for the custom message
	 * @param <T> result type
	 * @return function result from the argument
	 */
	public <T> T returning(final T result, final String exitMsgFormat, final Object... exitMsgFormatArgs) {
		returning = result;
		this.exitMsgFormat = exitMsgFormat;
		this.exitMsgFormatArgs = exitMsgFormatArgs.clone();
		return result;
	}

	private void doEntryLog(final String extraMsgFormat, final Object[] extraMsgFormatArgs) {
		String finalFormat;
		if(useDefaultFormatting) {
			finalFormat = ">> " + caller.shortClassName + "." + caller.methodName + ":" + caller.lineNumber +
					(extraMsgFormat.isEmpty() ? "" : " " + extraMsgFormat);
		} else {
			finalFormat = extraMsgFormat;
		}
		this.logger.log(MARKER_TRACE_ENTRY, fqcn, level.toInt(), finalFormat, extraMsgFormatArgs, null);
	}

	private void doExitLog() {
		final StringBuilder finalExitFormat = new StringBuilder();
		Object[] finalExitFormatArgs = exitMsgFormatArgs;

		if(useDefaultFormatting) {
			finalExitFormat.append("<< ");
			finalExitFormat.append(caller.shortClassName);
			finalExitFormat.append(".");
			finalExitFormat.append(caller.methodName);
			finalExitFormat.append(":");
			finalExitFormat.append(caller.lineNumber);
		}

		if(returning != null) {
			finalExitFormat.append(" returning({})");
			finalExitFormatArgs = LoggingUtils.prepend(exitMsgFormatArgs, returning);
		}

		if(exitMsgFormat != null && !exitMsgFormat.isEmpty()) {
			finalExitFormat.append(" ");
			finalExitFormat.append(exitMsgFormat);
		}

		logger.log(MARKER_TRACE_EXIT, fqcn, level.toInt(), finalExitFormat.toString(), finalExitFormatArgs, null);
	}

	/**
	 * Convenience method that creates NkTrace with "trace" log level
	 * @param logger logger
	 * @return NkTrace
	 */
	public static NkTrace trace(final Logger logger) {
		return new NkTrace(DEFAULT_FQCN, logger, Level.TRACE, "");
	}

	/**
	 * Convenience method that creates NkTrace with "trace" log level and extra message
	 * @param logger logger
	 * @param format message format
	 * @param formatArgs message format args
	 * @return NkTrace
	 */
	public static NkTrace trace(final Logger logger, final String format, final Object... formatArgs) {
		return new NkTrace(DEFAULT_FQCN, logger, Level.TRACE, format, formatArgs);
	}

	/**
	 * Convenience method that creates NkTrace with "debug" log level
	 * @param logger logger
	 * @return NkTrace
	 */
	public static NkTrace debug(final Logger logger) {
		return new NkTrace(DEFAULT_FQCN, logger, Level.DEBUG, "");
	}

	/**
	 * Convenience method that creates NkTrace with "debug" log level and extra message
	 * @param logger logger
	 * @param format message format
	 * @param formatArgs message format args
	 * @return NkTrace
	 */
	public static NkTrace debug(final Logger logger, final String format, final Object... formatArgs) {
		return new NkTrace(DEFAULT_FQCN, logger, Level.DEBUG, format, formatArgs);
	}

	/**
	 * Convenience method that creates NkTrace with "info" log level
	 * @param logger logger
	 * @return NkTrace
	 */
	public static NkTrace info(final Logger logger) {
		return new NkTrace(DEFAULT_FQCN, logger, Level.INFO, "");
	}

	/**
	 * Convenience method that creates NkTrace with "info" log level and extra message
	 * @param logger logger
	 * @param format message format
	 * @param formatArgs message format args
	 * @return NkTrace
	 */
	public static NkTrace info(final Logger logger, final String format, final Object... formatArgs) {
		return new NkTrace(DEFAULT_FQCN, logger, Level.INFO, format, formatArgs);
	}
}

package net.kawinski.logging;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.event.Level;
import org.slf4j.spi.LocationAwareLogger;

/**
 * Provides a closeable resource which logs entry/exit message logs.
 * When created, it increments an indentation level for new logs.
 */
public class NkTrace implements AutoCloseable {
	/**
	 * Set to false if you want to customize format of entry/exit messages
	 */
	public static boolean useDefaultFormatting = Boolean.parseBoolean(System.getProperty("NKTRACE_USE_DEFAULT_FORMATTING", "true"));

	public static final String DEFAULT_FQCN = NkTrace.class.getName();
	public static final String MARKER_TRACE_ENTRY_NAME = "NkTraceEntry";
	public static final String MARKER_TRACE_EXIT_NAME = "NkTraceExit";
	public static final Marker MARKER_TRACE_ENTRY = MarkerFactory.getMarker(MARKER_TRACE_ENTRY_NAME);
	public static final Marker MARKER_TRACE_EXIT = MarkerFactory.getMarker(MARKER_TRACE_EXIT_NAME);

	/**
	 * When using custom format, NkTrace doesn't retrieve the stacktrace.
	 * Instead the FQCN is passed and the target logger walks the stacktrace.
	 */
	private final String fqcn;
	private final Logger logger;
	private final Level level;
	private final boolean canLog;
	private final CallerInfo caller;
	private Object returning = null;
	private String exitMsg = "";
	private Object[] exitMsgArgs = {};

	public NkTrace(final String fqcn, final Logger logger, final Level level, final String format, final Object... formatArgs) {
		this.fqcn = fqcn;
		this.logger = logger;
		this.level = level;
		this.canLog = LoggingUtils.canLog(logger, level);
		if(!canLog) {
			caller = CallerInfo.UNKNOWN;
			return;
		}

		caller = CallerInfo.getCaller(fqcn);
		doEntryLog(format, formatArgs);
		NkTraceIndent.increment();
	}
	
	@Override
	public void close() {
		if(!canLog) {
			return;
		}
		NkTraceIndent.decrement();
		doExitLog();
	}

	public void setExitMsg(final String exitMsgFormat, final Object... exitMsgFormatArgs) {
		this.exitMsg = exitMsgFormat;
		this.exitMsgArgs = exitMsgFormatArgs.clone();
	}

	public <T> T returning(final T result) {
		returning = result;
		return result;
	}

	public <T> T returning(final T result, final String exitMsgFormat, final Object... exitMsgFormatArgs) {
		returning = result;
		exitMsg = exitMsgFormat;
		exitMsgArgs = exitMsgFormatArgs.clone();
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
		doLog(fqcn, logger, level, MARKER_TRACE_ENTRY, finalFormat, extraMsgFormatArgs);
	}

	private void doExitLog() {
		final StringBuilder finalExitMsg = new StringBuilder();
		Object[] finalExitMsgArgs = exitMsgArgs;

		if(useDefaultFormatting) {
			finalExitMsg.append("<< ");
			finalExitMsg.append(caller.shortClassName);
			finalExitMsg.append(".");
			finalExitMsg.append(caller.methodName);
			finalExitMsg.append(":");
			finalExitMsg.append(caller.lineNumber);
		}

		if(returning != null) {
			if(finalExitMsg.length() > 0)
				finalExitMsg.append(" ");
			finalExitMsg.append("returning({})");
			finalExitMsgArgs = LoggingUtils.prepend(exitMsgArgs, returning);
		}

		if(exitMsg != null && !exitMsg.isEmpty()) {
			if(finalExitMsg.length() > 0)
				finalExitMsg.append(" ");
			finalExitMsg.append(exitMsg);
		}

		doLog(fqcn, logger, level, MARKER_TRACE_EXIT, finalExitMsg.toString(), finalExitMsgArgs);
	}

	private static void doLog(final String fqcn, final Logger loggerArg, final Level level, final Marker marker, final String format, final Object[] formatArgs) {
		final LocationAwareLogger logger = (LocationAwareLogger)loggerArg;
		logger.log(marker, fqcn, level.toInt(), format, formatArgs, null);
	}


	public static NkTrace trace(final Logger logger) {
		return new NkTrace(DEFAULT_FQCN, logger, Level.TRACE, "");
	}

	public static NkTrace trace(final Logger logger, final String format, final Object... formatArgs) {
		return new NkTrace(DEFAULT_FQCN, logger, Level.TRACE, format, formatArgs);
	}

	public static NkTrace debug(final Logger logger) {
		return new NkTrace(DEFAULT_FQCN, logger, Level.DEBUG, "");
	}

	public static NkTrace debug(final Logger logger, final String format, final Object... formatArgs) {
		return new NkTrace(DEFAULT_FQCN, logger, Level.DEBUG, format, formatArgs);
	}

	public static NkTrace info(final Logger logger) {
		return new NkTrace(DEFAULT_FQCN, logger, Level.INFO, "");
	}

	public static NkTrace info(final Logger logger, final String format, final Object... formatArgs) {
		return new NkTrace(DEFAULT_FQCN, logger, Level.INFO, format, formatArgs);
	}
}

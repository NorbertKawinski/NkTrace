package net.kawinski.logging;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.event.Level;
import org.slf4j.spi.LocationAwareLogger;

public class NkTrace implements AutoCloseable
{
	/**
	 * Set to false if you want to customize format of entry/exit messages
	 */
	public static boolean useDefaultFormatting = Boolean.parseBoolean(System.getProperty("NKTRACE_USE_DEFAULT_FORMATTING", "true"));

	public static final String DEFAULT_FQCN = NkTrace.class.getName();
	public static final String MARKER_TRACE_ENTRY_NAME = "NkTraceEntry";
	public static final String MARKER_TRACE_EXIT_NAME = "NkTraceExit";
	public static final Marker MARKER_TRACE_ENTRY = MarkerFactory.getMarker(MARKER_TRACE_ENTRY_NAME);
	public static final Marker MARKER_TRACE_EXIT = MarkerFactory.getMarker(MARKER_TRACE_EXIT_NAME);
	// Idea: Maybe add MARKER_TRACE_ENTRY/EXIT_MESSAGE to allow different pattern (would allow fixing two-spaces issue)

	private final String fqcn;
	@SuppressWarnings("NonConstantLogger")
	private final Logger logger;
	private final Level level;
	private final boolean canLog;
	private final CallerInfo caller;
	private Object returning = null;
	private String exitMsg = "";
	private Object[] exitMsgArgs = {};

	public NkTrace(final Logger logger, final Level level, final String format, final Object... formatArgs) {
		this(DEFAULT_FQCN, logger, level, format, formatArgs);
	}

	public NkTrace(final String fqcn, final Logger logger, final Level level, final String format, final Object... formatArgs)
	{
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
	public void close()
	{
		if(!canLog)
			return;

		NkTraceIndent.decrement();
		doExitLog();
	}

	public void setExitMsg(final Object exitMsg) {
		setExitMsg("{}", exitMsg);
	}

	public void setExitMsg(final String format, final Object... formatArgs) {
		returning(returning, format, formatArgs);
	}

	public <T> T returning(final T result)
	{
		return returning(result, exitMsg, exitMsgArgs);
	}

	public <T> T returning(final T result, final String format, final Object... formatArgs)
	{
		returning = result;
		exitMsg = format;
		exitMsgArgs = formatArgs.clone();
		return result;
	}

	private static void doLog(final String fqcn, final Logger loggerArg, final Level level, final Marker marker, final String format, final Object[] formatArgs) {
		final LocationAwareLogger logger = (LocationAwareLogger)loggerArg;
		logger.log(marker, fqcn, level.toInt(), format, formatArgs, null);
	}

	private void doEntryLog(final String format, final Object[] formatArgs) {
		String finalFormat = format;
		if(useDefaultFormatting) {
			finalFormat = ">> " + caller.shortClassName + "." + caller.methodName + ":" + caller.lineNumber +
					(format.isEmpty() ? "" : " " + format);
		}
		doLog(fqcn, logger, level, MARKER_TRACE_ENTRY, finalFormat, formatArgs);
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

	public static NkTrace trace(final Logger logger) {
		return new NkTrace(logger, Level.TRACE, "");
	}

	public static NkTrace trace(final Logger logger, final String format, final Object... formatArgs) {
		return new NkTrace(logger, Level.TRACE, format, formatArgs);
	}

	public static NkTrace debug(final Logger logger) {
		return new NkTrace(logger, Level.DEBUG, "");
	}

	public static NkTrace debug(final Logger logger, final String format, final Object... formatArgs) {
		return new NkTrace(logger, Level.DEBUG, format, formatArgs);
	}

	public static NkTrace info(final Logger logger) {
		return new NkTrace(logger, Level.INFO, "");
	}

	public static NkTrace info(final Logger logger, final String format, final Object... formatArgs) {
		return new NkTrace(logger, Level.INFO, format, formatArgs);
	}
}

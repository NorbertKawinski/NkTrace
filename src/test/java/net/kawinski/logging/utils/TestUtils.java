package net.kawinski.logging.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.boolex.OnMarkerEvaluator;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.filter.EvaluatorFilter;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import net.kawinski.logging.NkTrace;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestUtils {
    private static final ByteArrayOutputStream loggerOutput = new ByteArrayOutputStream();

    private static Logger getRootLogger() {
        return (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    }

    public static org.slf4j.Logger getFreshLogger() {
        Logger logger = getRootLogger();
        loggerOutput.reset();
        logger.setLevel(Level.ALL);
        setupLogger(logger);
        return logger;
    }

    public static void setLoggerLevel(final Level level) {
        getRootLogger().setLevel(level);
    }

    public static void assertLoggerOutputEqual(final String... expected) {
        final String[] output = loggerOutput.toString().split(System.lineSeparator());
        assertThat(output.length, is(expected.length));
        for(int i = 0; i < expected.length; ++i) {
            assertThat(output[i], is(expected[i]));
        }
    }

    private static PatternLayoutEncoder createPatternEncoder(LoggerContext lc, String pattern) {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(lc);
        encoder.setPattern(pattern);
        encoder.start();
        return encoder;
    }

    private static OnMarkerEvaluator createMarkerEvaluator(LoggerContext lc, String... markers) {
        OnMarkerEvaluator evaluator = new OnMarkerEvaluator();
        evaluator.setContext(lc);
        for(String marker : markers) {
            evaluator.addMarker(marker);
        }
        evaluator.start();
        return evaluator;
    }

    private static Filter<ILoggingEvent> createMarkerFilter(LoggerContext lc, FilterReply onMatch, FilterReply onMismatch, String... markers) {
        OnMarkerEvaluator evaluator = createMarkerEvaluator(lc, markers);
        EvaluatorFilter<ILoggingEvent> filter = new EvaluatorFilter<>();
        filter.setContext(lc);
        filter.setEvaluator(evaluator);
        filter.setOnMatch(onMatch);
        filter.setOnMismatch(onMismatch);
        filter.start();
        return filter;
    }

    private static void setupAppender(LoggerContext lc, Logger logger, String pattern, Filter<ILoggingEvent> filter) {
        Encoder<ILoggingEvent> encoder = createPatternEncoder(lc, pattern);

        // Setup buffer appender.
        // This appender is needed for tests to assert the correct logging output.
        final OutputStreamAppender<ILoggingEvent> customBufferAppender = new OutputStreamAppender<>();
        customBufferAppender.addFilter(filter);
        customBufferAppender.setEncoder(encoder);
        customBufferAppender.setContext(lc);
        customBufferAppender.setOutputStream(loggerOutput);
        customBufferAppender.start();
        logger.addAppender(customBufferAppender);

        // Setup console appender.
        // It's not necessarily required for tests, but it's nice to have some console output when debugging manually
        final ConsoleAppender<ILoggingEvent> customConsoleAppender = new ConsoleAppender<>();
        customConsoleAppender.addFilter(filter);
        customConsoleAppender.setEncoder(encoder);
        customConsoleAppender.setContext(lc);
        customConsoleAppender.start();
        logger.addAppender(customConsoleAppender);
    }

    public static void setupLogger(Logger logger) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        logger.detachAndStopAllAppenders();
        logger.setLevel(Level.ALL);

        setupAppender(lc, logger, "%5level %mdc{NkTrace_Indent}>> %class{0}.%method%msg%n",
                createMarkerFilter(lc, FilterReply.NEUTRAL, FilterReply.DENY, NkTrace.MARKER_TRACE_ENTRY_NAME)
        );
        setupAppender(lc, logger, "%5level %mdc{NkTrace_Indent}%msg%n",
                createMarkerFilter(lc, FilterReply.DENY, FilterReply.NEUTRAL, NkTrace.MARKER_TRACE_ENTRY_NAME, NkTrace.MARKER_TRACE_EXIT_NAME)
        );
        setupAppender(lc, logger, "%5level %mdc{NkTrace_Indent}<< %class{0}.%method%msg%n",
                createMarkerFilter(lc, FilterReply.NEUTRAL, FilterReply.DENY, NkTrace.MARKER_TRACE_EXIT_NAME)
        );
    }
}

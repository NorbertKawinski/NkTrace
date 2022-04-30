package net.kawinski.logging.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestUtils {
    private static final String DEFAULT_LOGGER_NAME = "net.kawinski";
    private static final String CUSTOM_LOGGER_NAME = "test.customLogger";

    private static final ByteArrayOutputStream loggerOutput = new ByteArrayOutputStream();

    private static final Logger defaultLogger = (Logger) LoggerFactory.getLogger(DEFAULT_LOGGER_NAME);
    private static final Logger customLogger = (Logger) LoggerFactory.getLogger(CUSTOM_LOGGER_NAME);
    @SuppressWarnings("NonConstantLogger")
    private static org.slf4j.Logger logger = defaultLogger;

    static {
        setupLoggerForTesting();
    }

    public static org.slf4j.Logger getFreshLogger() {
        return getFreshLogger(true);
    }

    public static org.slf4j.Logger getFreshLogger(final boolean useDefaultFormatting) {
        if(useDefaultFormatting)
            logger = defaultLogger;
        else
            logger = customLogger;
        resetLogger();
        return logger;
    }

    public static void resetLogger() {
        setLoggerLevel(Level.ALL);
        loggerOutput.reset();
    }

    public static void setLoggerLevel(final Level level) {
        ((Logger) logger).setLevel(level);
    }

    private static String getLoggerOutput() {
        return loggerOutput.toString();
    }

    public static void assertLoggerOutputEqual(final String... expected) {
        final String[] output = getLoggerOutput().split(System.lineSeparator());
        assertThat(output.length, is(expected.length));
        for(int i = 0; i < expected.length; ++i) {
            assertThat(output[i], is(expected[i]));
        }
    }

    private static void setupRootLogger(final LoggerContext lc) {
        final Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.detachAndStopAllAppenders();
        rootLogger.setLevel(Level.ALL);
    }

    private static void setupDefaultLogger(final LoggerContext lc) {
        final PatternLayoutEncoder defaultLayoutEncoder = new PatternLayoutEncoder();
        defaultLayoutEncoder.setPattern("%5level %mdc{NkTrace_Indent}%msg%n");
        defaultLayoutEncoder.setContext(lc);
        defaultLayoutEncoder.start();

        final OutputStreamAppender<ILoggingEvent> defaultBufferAppender = new OutputStreamAppender<>();
        defaultBufferAppender.setEncoder(defaultLayoutEncoder);
        defaultBufferAppender.setContext(lc);
        defaultBufferAppender.setOutputStream(loggerOutput);
        defaultBufferAppender.start();

        final ConsoleAppender<ILoggingEvent> defaultConsoleAppender = new ConsoleAppender<>();
        defaultConsoleAppender.setEncoder(defaultLayoutEncoder);
        defaultConsoleAppender.setContext(lc);
        defaultConsoleAppender.start();

        defaultLogger.addAppender(defaultBufferAppender);
        defaultLogger.addAppender(defaultConsoleAppender);
    }

    private static void setupCustomLogger(final LoggerContext lc) {
        final NkPatternLayout customPatternLayout = new NkPatternLayout();
        customPatternLayout.setContext(lc);
        customPatternLayout.setEntryPattern("%5level %mdc{NkTrace_Indent}Entering %method in %class{0} %msg .\\(%file:%line\\)%n");
        customPatternLayout.setRegularPattern("%5level %mdc{NkTrace_Indent}%msg .\\(%file:%line\\)%n");
        customPatternLayout.setExitPattern("%5level %mdc{NkTrace_Indent}Exiting %method in %class{0} %msg .\\(%file:%line\\)%n");
        customPatternLayout.start();

        final LayoutWrappingEncoder<ILoggingEvent> customLayoutEncoder = new LayoutWrappingEncoder<>();
        customLayoutEncoder.setContext(lc);
        customLayoutEncoder.setLayout(customPatternLayout);
        customLayoutEncoder.start();

        final OutputStreamAppender<ILoggingEvent> customBufferAppender = new OutputStreamAppender<>();
        customBufferAppender.setEncoder(customLayoutEncoder);
        customBufferAppender.setContext(lc);
        customBufferAppender.setOutputStream(loggerOutput);
        customBufferAppender.start();

        final ConsoleAppender<ILoggingEvent> customConsoleAppender = new ConsoleAppender<>();
        customConsoleAppender.setEncoder(customLayoutEncoder);
        customConsoleAppender.setContext(lc);
        customConsoleAppender.start();

        customLogger.addAppender(customBufferAppender);
        customLogger.addAppender(customConsoleAppender);
    }

    /**
     * Configures ROOT logger for easier testing
     */
    private static void setupLoggerForTesting() {
        final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        setupRootLogger(lc);
        setupDefaultLogger(lc);
        setupCustomLogger(lc);
    }
}

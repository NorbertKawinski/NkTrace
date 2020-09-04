package net.kawinski.logging;

import net.kawinski.logging.utils.TestUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class LoggingUtilsTest {

    @Test
    public void should_allow_logging_only_if_matching_level_is_enabled() {
        final Logger logger = TestUtils.getFreshLogger();

        TestUtils.setLoggerLevel(ch.qos.logback.classic.Level.OFF);
        assertThat(LoggingUtils.canLog(logger, Level.TRACE), is(false));
        assertThat(LoggingUtils.canLog(logger, Level.DEBUG), is(false));
        assertThat(LoggingUtils.canLog(logger, Level.INFO), is(false));
        assertThat(LoggingUtils.canLog(logger, Level.WARN), is(false));
        assertThat(LoggingUtils.canLog(logger, Level.ERROR), is(false));

        TestUtils.setLoggerLevel(ch.qos.logback.classic.Level.TRACE);
        assertThat(LoggingUtils.canLog(logger, Level.TRACE), is(true));
        assertThat(LoggingUtils.canLog(logger, Level.DEBUG), is(true));
        assertThat(LoggingUtils.canLog(logger, Level.INFO), is(true));
        assertThat(LoggingUtils.canLog(logger, Level.WARN), is(true));
        assertThat(LoggingUtils.canLog(logger, Level.ERROR), is(true));

        TestUtils.setLoggerLevel(ch.qos.logback.classic.Level.DEBUG);
        assertThat(LoggingUtils.canLog(logger, Level.TRACE), is(false));
        assertThat(LoggingUtils.canLog(logger, Level.DEBUG), is(true));
        assertThat(LoggingUtils.canLog(logger, Level.INFO), is(true));
        assertThat(LoggingUtils.canLog(logger, Level.WARN), is(true));
        assertThat(LoggingUtils.canLog(logger, Level.ERROR), is(true));

        TestUtils.setLoggerLevel(ch.qos.logback.classic.Level.INFO);
        assertThat(LoggingUtils.canLog(logger, Level.TRACE), is(false));
        assertThat(LoggingUtils.canLog(logger, Level.DEBUG), is(false));
        assertThat(LoggingUtils.canLog(logger, Level.INFO), is(true));
        assertThat(LoggingUtils.canLog(logger, Level.WARN), is(true));
        assertThat(LoggingUtils.canLog(logger, Level.ERROR), is(true));

        TestUtils.setLoggerLevel(ch.qos.logback.classic.Level.WARN);
        assertThat(LoggingUtils.canLog(logger, Level.TRACE), is(false));
        assertThat(LoggingUtils.canLog(logger, Level.DEBUG), is(false));
        assertThat(LoggingUtils.canLog(logger, Level.INFO), is(false));
        assertThat(LoggingUtils.canLog(logger, Level.WARN), is(true));
        assertThat(LoggingUtils.canLog(logger, Level.ERROR), is(true));

        TestUtils.setLoggerLevel(ch.qos.logback.classic.Level.ERROR);
        assertThat(LoggingUtils.canLog(logger, Level.TRACE), is(false));
        assertThat(LoggingUtils.canLog(logger, Level.DEBUG), is(false));
        assertThat(LoggingUtils.canLog(logger, Level.INFO), is(false));
        assertThat(LoggingUtils.canLog(logger, Level.WARN), is(false));
        assertThat(LoggingUtils.canLog(logger, Level.ERROR), is(true));

        TestUtils.setLoggerLevel(ch.qos.logback.classic.Level.OFF);
        assertThat(LoggingUtils.canLog(logger, Level.TRACE), is(false));
        assertThat(LoggingUtils.canLog(logger, Level.DEBUG), is(false));
        assertThat(LoggingUtils.canLog(logger, Level.INFO), is(false));
        assertThat(LoggingUtils.canLog(logger, Level.WARN), is(false));
        assertThat(LoggingUtils.canLog(logger, Level.ERROR), is(false));

        TestUtils.setLoggerLevel(ch.qos.logback.classic.Level.ALL);
        assertThat(LoggingUtils.canLog(logger, Level.TRACE), is(true));
        assertThat(LoggingUtils.canLog(logger, Level.DEBUG), is(true));
        assertThat(LoggingUtils.canLog(logger, Level.INFO), is(true));
        assertThat(LoggingUtils.canLog(logger, Level.WARN), is(true));
        assertThat(LoggingUtils.canLog(logger, Level.ERROR), is(true));
    }
}

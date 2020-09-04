package net.kawinski.logging.utils;

import ch.qos.logback.classic.Level;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestUtilsTest {
    private Logger logger;

    @Before
    public void resetLogger() {
        logger = TestUtils.getFreshLogger();
    }

    @Test
    public void loggerOutput_should_expose_log_messages() {
        TestUtils.assertLoggerOutputEqual("");
        logger.info("Hello World");
        logger.info("Foo Bar");
        logger.info("Bazzzz Bai");
        TestUtils.assertLoggerOutputEqual(
                " INFO Hello World",
                " INFO Foo Bar",
                " INFO Bazzzz Bai");
    }

    @Test
    public void reset_should_clear_messages_logged_so_far() {
        logger.info("Hello World");
        TestUtils.assertLoggerOutputEqual(" INFO Hello World");

        logger = TestUtils.getFreshLogger();

        logger.info("Foo Bar");
        TestUtils.assertLoggerOutputEqual(" INFO Foo Bar");
    }

    @Test
    public void setLevel_should_change_logger_level() {
        logger.info("Hello World");
        TestUtils.assertLoggerOutputEqual(" INFO Hello World");

        TestUtils.setLoggerLevel(Level.ERROR);

        logger.info("This shouldn't be printed");
        TestUtils.assertLoggerOutputEqual(" INFO Hello World");
    }
}

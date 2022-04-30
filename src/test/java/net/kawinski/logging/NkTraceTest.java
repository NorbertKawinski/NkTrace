package net.kawinski.logging;

import ch.qos.logback.classic.Level;
import net.kawinski.logging.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

@SuppressWarnings("EmptyTryBlock")
public class NkTraceTest {
    private Logger logger;

    @Before
    public void resetLogger() {
        logger = TestUtils.getFreshLogger();
    }

    @Test
    public void trace_should_print_entry_and_exit_messages() {
        try(final NkTrace trace = NkTrace.info(logger)) {
        }
        TestUtils.assertLoggerOutputEqual(
                " INFO >> NkTraceTest.trace_should_print_entry_and_exit_messages",
                " INFO << NkTraceTest.trace_should_print_entry_and_exit_messages");
    }

    @Test
    public void trace_entry_should_print_formatted_message() {
        try(final NkTrace trace = NkTrace.info(logger, "Hello {}. 2+2={}", "Peter", 4)) {
        }
        TestUtils.assertLoggerOutputEqual(
                " INFO >> NkTraceTest.trace_entry_should_print_formatted_message Hello Peter. 2+2=4",
                " INFO << NkTraceTest.trace_entry_should_print_formatted_message");
    }

    @Test
    public void trace_exit_should_print_formatted_message() {
        try(final NkTrace trace = NkTrace.info(logger)) {
            trace.setExitMsg("I'm {} and I'm quitting! 2+2={}", "Peter", 4);
        }
        TestUtils.assertLoggerOutputEqual(
                " INFO >> NkTraceTest.trace_exit_should_print_formatted_message",
                " INFO << NkTraceTest.trace_exit_should_print_formatted_message I'm Peter and I'm quitting! 2+2=4");
    }

    @Test
    public void trace_exit_should_print_returned_object() {
        try(final NkTrace trace = NkTrace.info(logger)) {
            trace.returning("Something");
        }
        TestUtils.assertLoggerOutputEqual(
                " INFO >> NkTraceTest.trace_exit_should_print_returned_object",
                " INFO << NkTraceTest.trace_exit_should_print_returned_object returning(Something)");
    }

    @Test
    public void trace_exit_should_print_returned_object_and_formatted_message() {
        try(final NkTrace trace = NkTrace.info(logger)) {
            trace.returning("Something");
            trace.setExitMsg("I'm {} and I'm quitting! 2+2={}", "Peter", 4);
        }
        TestUtils.assertLoggerOutputEqual(
                " INFO >> NkTraceTest.trace_exit_should_print_returned_object_and_formatted_message",
                " INFO << NkTraceTest.trace_exit_should_print_returned_object_and_formatted_message returning(Something) I'm Peter and I'm quitting! 2+2=4");
    }

    @Test
    public void messages_inside_trace_should_be_indented() {
        try(final NkTrace trace = NkTrace.info(logger)) {
            logger.info("Indented message");
            try(final NkTrace innerTrace = NkTrace.info(logger)) {
                logger.info("Indented twice");
            }
        }
        TestUtils.assertLoggerOutputEqual(
                " INFO >> NkTraceTest.messages_inside_trace_should_be_indented",
                " INFO    Indented message",
                " INFO    >> NkTraceTest.messages_inside_trace_should_be_indented",
                " INFO       Indented twice",
                " INFO    << NkTraceTest.messages_inside_trace_should_be_indented",
                " INFO << NkTraceTest.messages_inside_trace_should_be_indented");
    }

    @Test
    public void traces_with_low_level_should_be_ignored() {
        TestUtils.setLoggerLevel(Level.INFO);
        try(final NkTrace trace = NkTrace.info(logger)) {
            logger.info("Indented message");
            try(final NkTrace innerTrace = NkTrace.debug(logger)) {
                logger.debug("Ignored message");
            }
        }
        TestUtils.assertLoggerOutputEqual(
                " INFO >> NkTraceTest.traces_with_low_level_should_be_ignored",
                " INFO    Indented message",
                " INFO << NkTraceTest.traces_with_low_level_should_be_ignored");
    }

    @Test
    public void traces_can_be_created_with_different_log_levels() {
        try(final NkTrace trace = NkTrace.trace(logger)) {
        }
        try(final NkTrace midTrace = NkTrace.debug(logger)) {
        }
        try(final NkTrace innerTrace = NkTrace.info(logger)) {
        }
        try(final NkTrace trace = NkTrace.trace(logger, "with param {}", 1)) {
        }
        try(final NkTrace midTrace = NkTrace.debug(logger, "with param {}", 2)) {
        }
        try(final NkTrace innerTrace = NkTrace.info(logger, "with param {}", 3)) {
        }
        TestUtils.assertLoggerOutputEqual(
                "TRACE >> NkTraceTest.traces_can_be_created_with_different_log_levels",
                "TRACE << NkTraceTest.traces_can_be_created_with_different_log_levels",
                "DEBUG >> NkTraceTest.traces_can_be_created_with_different_log_levels",
                "DEBUG << NkTraceTest.traces_can_be_created_with_different_log_levels",
                " INFO >> NkTraceTest.traces_can_be_created_with_different_log_levels",
                " INFO << NkTraceTest.traces_can_be_created_with_different_log_levels",
                "TRACE >> NkTraceTest.traces_can_be_created_with_different_log_levels with param 1",
                "TRACE << NkTraceTest.traces_can_be_created_with_different_log_levels",
                "DEBUG >> NkTraceTest.traces_can_be_created_with_different_log_levels with param 2",
                "DEBUG << NkTraceTest.traces_can_be_created_with_different_log_levels",
                " INFO >> NkTraceTest.traces_can_be_created_with_different_log_levels with param 3",
                " INFO << NkTraceTest.traces_can_be_created_with_different_log_levels");
    }

}

package net.kawinski.logging;

import ch.qos.logback.classic.Level;
import net.kawinski.logging.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

// This comment serves only for line-number alignment purposes
// This comment serves only for line-number alignment purposes
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
                " INFO >> NkTraceTest.trace_should_print_entry_and_exit_messages:22",
                " INFO << NkTraceTest.trace_should_print_entry_and_exit_messages:22");
    }

    @Test
    public void trace_entry_should_print_formatted_message() {
        try(final NkTrace trace = NkTrace.info(logger, "Hello {}. 2+2={}", "Peter", 4)) {
        }
        TestUtils.assertLoggerOutputEqual(
                " INFO >> NkTraceTest.trace_entry_should_print_formatted_message:31 Hello Peter. 2+2=4",
                " INFO << NkTraceTest.trace_entry_should_print_formatted_message:31");
    }

    @Test
    public void trace_exit_should_print_formatted_message() {
        try(final NkTrace trace = NkTrace.info(logger)) {
            trace.setExitMsg("I'm {} and I'm quitting! 2+2={}", "Peter", 4);
        }
        TestUtils.assertLoggerOutputEqual(
                " INFO >> NkTraceTest.trace_exit_should_print_formatted_message:40",
                " INFO << NkTraceTest.trace_exit_should_print_formatted_message:40 I'm Peter and I'm quitting! 2+2=4");
    }

    @Test
    public void trace_exit_should_print_returned_object() {
        try(final NkTrace trace = NkTrace.info(logger)) {
            trace.returning("Something");
        }
        TestUtils.assertLoggerOutputEqual(
                " INFO >> NkTraceTest.trace_exit_should_print_returned_object:50",
                " INFO << NkTraceTest.trace_exit_should_print_returned_object:50 returning(Something)");
    }

    @Test
    public void trace_exit_should_print_returned_object_and_formatted_message() {
        try(final NkTrace trace = NkTrace.info(logger)) {
            trace.returning("Something");
            trace.setExitMsg("I'm {} and I'm quitting! 2+2={}", "Peter", 4);
        }
        TestUtils.assertLoggerOutputEqual(
                " INFO >> NkTraceTest.trace_exit_should_print_returned_object_and_formatted_message:60",
                " INFO << NkTraceTest.trace_exit_should_print_returned_object_and_formatted_message:60 returning(Something) I'm Peter and I'm quitting! 2+2=4");
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
                " INFO >> NkTraceTest.messages_inside_trace_should_be_indented:71",
                " INFO    Indented message",
                " INFO    >> NkTraceTest.messages_inside_trace_should_be_indented:73",
                " INFO       Indented twice",
                " INFO    << NkTraceTest.messages_inside_trace_should_be_indented:73",
                " INFO << NkTraceTest.messages_inside_trace_should_be_indented:71");
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
                " INFO >> NkTraceTest.traces_with_low_level_should_be_ignored:89",
                " INFO    Indented message",
                " INFO << NkTraceTest.traces_with_low_level_should_be_ignored:89");
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
                "TRACE >> NkTraceTest.traces_can_be_created_with_different_log_levels:103",
                "TRACE << NkTraceTest.traces_can_be_created_with_different_log_levels:103",
                "DEBUG >> NkTraceTest.traces_can_be_created_with_different_log_levels:105",
                "DEBUG << NkTraceTest.traces_can_be_created_with_different_log_levels:105",
                " INFO >> NkTraceTest.traces_can_be_created_with_different_log_levels:107",
                " INFO << NkTraceTest.traces_can_be_created_with_different_log_levels:107",
                "TRACE >> NkTraceTest.traces_can_be_created_with_different_log_levels:109 with param 1",
                "TRACE << NkTraceTest.traces_can_be_created_with_different_log_levels:109",
                "DEBUG >> NkTraceTest.traces_can_be_created_with_different_log_levels:111 with param 2",
                "DEBUG << NkTraceTest.traces_can_be_created_with_different_log_levels:111",
                " INFO >> NkTraceTest.traces_can_be_created_with_different_log_levels:113 with param 3",
                " INFO << NkTraceTest.traces_can_be_created_with_different_log_levels:113");
    }

    @Test
    public void custom_logger_should_produce_different_format() {
        final Logger logger = TestUtils.getFreshLogger(false);
        NkTrace.useDefaultFormatting = false;
        try {
            try(final NkTrace trace = NkTrace.info(logger)) {
                logger.info("Inner message");
            }
            TestUtils.assertLoggerOutputEqual(
                    " INFO Entering custom_logger_should_produce_different_format in NkTraceTest  .(NkTraceTest.java:135)",
                    " INFO    Inner message .(NkTraceTest.java:136)",
                    " INFO Exiting custom_logger_should_produce_different_format in NkTraceTest  .(NkTraceTest.java:137)");
        } finally {
            NkTrace.useDefaultFormatting = true;
        }
    }

}

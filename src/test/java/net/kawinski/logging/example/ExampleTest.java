package net.kawinski.logging.example;

import net.kawinski.logging.NkTrace;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Well. These aren't necessarily tests,
 * but at least they help a bit as an example code to learn NkTrace functionality and usage.
 */
@SuppressWarnings({"MagicNumber", "SameParameterValue"}) // Tests are pretty much all about magic numbers
public class ExampleTest {
    final Logger logger = LoggerFactory.getLogger(ExampleTest.class);

    @Test
    public void example() {
        logger.info("Make sure, you're seeing this line in the log output.");
        logger.info("If you don't, check out the SLF4J setup & configuration.");

        example01();
        example02();
    }

    private void example01() {
        try (final NkTrace trace = NkTrace.info(logger)) {
            logger.info("Inside example01");
            example01A();
            example01B();
        }
    }

    private void example01A() {
        try (final NkTrace trace = NkTrace.debug(logger)) {
            logger.debug("Inside 01A");
        }
    }

    private void example01B() {
        try (final NkTrace trace = NkTrace.trace(logger)) {
            logger.trace("Inside 01B");
            logger.debug("Before 01C");
            example01C();
            logger.debug("After 01C");
        }
    }

    private void example01C() {
        try (final NkTrace trace = NkTrace.trace(logger)) {
            logger.trace("Inside 01C");
        }
    }

private void example02() {
    try (final NkTrace trace = NkTrace.trace(logger)) {
        logger.info("Received 1: {}", myMultiply1(3.0, 6.0));
        logger.info("Received 2: {}", myMultiply2(3.0, 6.0));
    }
}

private double myMultiply1(final double a, final double b) {
    try (final NkTrace trace = NkTrace.debug(logger, "a: {}, b: {}", a, b)) {
        logger.debug("You can log the return value into the exit trace instead of logging another line");
        final double result = a * b;
        trace.returning(result); // instead of logger.info("Returning({})", result);
        return result;
    }
}

private double myMultiply2(final double a, final double b) {
    try (final NkTrace trace = NkTrace.debug(logger, "a: {}, b: {}", a, b)) {
        logger.debug("You can shorten exit trace and return value into a single line");
        return trace.returning(a * b);
    }
}

    public static void main(final String[] args) {
        new ExampleTest().example();
    }
}

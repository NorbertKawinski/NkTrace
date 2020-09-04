package net.kawinski.logging.utils;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LayoutBase;
import net.kawinski.logging.NkTrace;

/**
 * Since Logback doesn't allow setting different layouts based on the marker, we have to do it ourselves.
 * This class holds 3 pattern layouts inside and switches on them based on the marker in the logging event.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class NkPatternLayout extends LayoutBase<ILoggingEvent> {
    private final PatternLayout entryPatternEncoder = new PatternLayout();
    private final PatternLayout regularPatternEncoder = new PatternLayout();
    private final PatternLayout exitPatternEncoder = new PatternLayout();

    public void setEntryPattern(final String pattern) {
        entryPatternEncoder.setPattern(pattern);
    }

    public void setRegularPattern(final String pattern) {
        regularPatternEncoder.setPattern(pattern);
    }

    public void setExitPattern(final String pattern) {
        exitPatternEncoder.setPattern(pattern);
    }

    @Override
    public void setContext(final Context context) {
        super.setContext(context);
        entryPatternEncoder.setContext(context);
        regularPatternEncoder.setContext(context);
        exitPatternEncoder.setContext(context);
    }

    @Override
    public void start() {
        super.start();
        entryPatternEncoder.start();
        regularPatternEncoder.start();
        exitPatternEncoder.start();
    }

    @Override
    public void stop() {
        super.stop();
        entryPatternEncoder.stop();
        regularPatternEncoder.stop();
        exitPatternEncoder.stop();
    }

    @Override
    public String doLayout(final ILoggingEvent event) {
        if(event.getMarker() == NkTrace.MARKER_TRACE_ENTRY)
            return entryPatternEncoder.doLayout(event);
        if(event.getMarker() == NkTrace.MARKER_TRACE_EXIT)
            return exitPatternEncoder.doLayout(event);
        return regularPatternEncoder.doLayout(event);
    }
}

package net.kawinski.logging;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class NkTraceIndentTest {

    @Before
    @After
    public void reset_indent_count() {
        NkTraceIndent.reset();
    }

    @Test
    public void reset_should_set_indent_count_to_0() {
        NkTraceIndent.increment();
        assertThat(NkTraceIndent.getIndentCount(), is(1));
        NkTraceIndent.reset();
        assertThat(NkTraceIndent.getIndentCount(), is(0));
    }

    @Test
    public void reset_should_clear_MDC() {
        NkTraceIndent.increment();
        assertThat(MDC.get(NkTraceIndent.MDC_INDENT_KEY), is("   "));
        NkTraceIndent.reset();
        assertThat(MDC.get(NkTraceIndent.MDC_INDENT_KEY), is(""));
    }

    @Test
    public void increment_should_increment_indent_value() {
        assertThat(NkTraceIndent.getIndentCount(), is(0));
        NkTraceIndent.increment();
        assertThat(NkTraceIndent.getIndentCount(), is(1));
    }

    @Test
    public void increment_should_update_MDC_indent() {
        assertThat(MDC.get(NkTraceIndent.MDC_INDENT_KEY), is(""));
        NkTraceIndent.increment();
        assertThat(MDC.get(NkTraceIndent.MDC_INDENT_KEY), is("   "));
    }

    @Test
    public void decrement_should_decrement_indent_value() {
        assertThat(NkTraceIndent.getIndentCount(), is(0));
        NkTraceIndent.increment();
        NkTraceIndent.decrement();
        assertThat(NkTraceIndent.getIndentCount(), is(0));
    }

    @Test
    public void decrement_should_update_MDC_indent() {
        assertThat(MDC.get(NkTraceIndent.MDC_INDENT_KEY), is(""));
        NkTraceIndent.increment();
        NkTraceIndent.decrement();
        assertThat(MDC.get(NkTraceIndent.MDC_INDENT_KEY), is(""));
    }

    @Test
    public void getIndentCount_should_return_current_value() {
        assertThat(NkTraceIndent.getIndentCount(), is(0));
        NkTraceIndent.increment();
        NkTraceIndent.increment();
        NkTraceIndent.increment();
        assertThat(NkTraceIndent.getIndentCount(), is(3));
    }

    @Test
    public void getIndent_should_return_indented_string_for_given_depth() {
        assertThat(NkTraceIndent.getIndent(0), is(""));
        assertThat(NkTraceIndent.getIndent(1), is("   "));
        assertThat(NkTraceIndent.getIndent(3), is("         "));
    }

    @Test
    public void getCurrentIndent_should_return_indented_string_for_current_depth() {
        assertThat(NkTraceIndent.getCurrentIndent(), is(""));
        NkTraceIndent.increment();
        assertThat(NkTraceIndent.getCurrentIndent(), is("   "));
        NkTraceIndent.increment();
        NkTraceIndent.increment();
        assertThat(NkTraceIndent.getCurrentIndent(), is("         "));
    }

}

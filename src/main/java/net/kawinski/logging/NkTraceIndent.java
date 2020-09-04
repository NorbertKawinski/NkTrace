package net.kawinski.logging;

import org.slf4j.MDC;

/**
 * Indents module for NkTrace.
 * It exposes 'indent' variable to logger's context.
 *
 * To utilize the indenting feature, setup the logger's pattern to something like:
 * - Logback example: %date [%thread] %5level %mdc{indent}%msg \(%file:%line\) %n
 * - WildFly (color-pattern) example: %K{level}%d{HH:mm:ss,SSS} [%t] %5p %X{indent}%s%e{} a.t(%F:%L)%n
 * - Log4j example: ???
 * - ??? example: ???
 *
 * Defaults:
 * - WildFly: %d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n
 */
public final class NkTraceIndent
{
	public static final String MDC_INDENT_KEY = "indent";

	/**
	 * 3 is a prefix length of ">> " and "<< ".
	 * This way, inner logs align vertically to the traces which looks pretty
	 */
	public static final int SPACES_PER_INDENT = 3;

	private static final ThreadLocal<Integer> threadIndent = ThreadLocal.withInitial(() -> 0);

	private NkTraceIndent() {
	}

	/**
	 * Resets indentation value back to 0.
	 *
	 * Useful when reusing threads.
	 * A reused thread might not have decremented back to 0.
	 * This issue might arise if:
	 * - Not all increment/decrement is paired with finally{} block
	 * - But even then, killing a thread could leave the indentation unbalanced as finally{} blocks won't be called
	 */
	public static void reset() {
		threadIndent.remove();
		updateMDC();
	}

	public static void increment()
	{
		threadIndent.set(threadIndent.get() + 1);
		updateMDC();
	}
	
	public static void decrement()
	{
		threadIndent.set(threadIndent.get() - 1);
		updateMDC();
	}

	private static void updateMDC() {
		MDC.put(MDC_INDENT_KEY, getCurrentIndent());
	}

	public static int getIndentCount()
	{
		return threadIndent.get();
	}

	public static String getIndent(final int indentCount)
	{
		final int spaces = indentCount * SPACES_PER_INDENT;

		final StringBuilder indentBuilder = new StringBuilder(spaces);
		for (int i = 0; i < spaces; ++i)
			indentBuilder.append(" ");
		return indentBuilder.toString();
	}

	public static String getCurrentIndent() {
		return getIndent(getIndentCount());
	}
}
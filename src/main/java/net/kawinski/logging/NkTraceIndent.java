package net.kawinski.logging;

import org.slf4j.MDC;

/**
 * Indents module for NkTrace.
 * It exposes 'indent' variable to logger's context via ThreadLocal variables and MDC.
 *
 * To utilize the indenting feature, setup the logger's pattern to something like:
 * - Logback example: %date [%thread] %5level %mdc{NkTrace_Indent}%msg \(%file:%line\) %n
 * - WildFly (color-pattern) example: %K{level}%d{HH:mm:ss,SSS} [%t] %5p %X{NkTrace_Indent}%s%e{} a.t(%F:%L)%n
 * - Log4j example: ???
 * - ??? example: ???
 *
 * Defaults:
 * - WildFly: %d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n
 */
public final class NkTraceIndent {
	public static final String MDC_INDENT_KEY = "NkTrace_Indent";

	/**
	 * 3 spaces is the same as length of "&gt;&gt; " and "&lt;&lt; " which are commonly used by NkTrace.
	 * This way, inner logs align vertically to the traces which looks pretty
	 */
	public static final String SINGLE_INDENT = "   ";

	private static String[] indentsByLevel = pregenerateIndents(16);

	private static final ThreadLocal<Integer> indentLevelByThread = ThreadLocal.withInitial(() -> 0);

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
		indentLevelByThread.remove();
		updateMDC();
	}

	public static void increment() {
		indentLevelByThread.set(indentLevelByThread.get() + 1);
		updateMDC();
	}
	
	public static void decrement() {
		indentLevelByThread.set(indentLevelByThread.get() - 1);
		updateMDC();
	}

	private static void updateMDC() {
		MDC.put(MDC_INDENT_KEY, getCurrentIndent());
	}

	public static int getIndentLevel() {
		return indentLevelByThread.get();
	}

	public static String getIndent(final int indentLevel) {
		if(indentLevel >= indentsByLevel.length) {
			indentsByLevel = pregenerateIndents(indentLevel * 2);
		}
		return indentsByLevel[indentLevel];
	}

	public static String getCurrentIndent() {
		return getIndent(getIndentLevel());
	}

	private static String[] pregenerateIndents(int depth) {
		final String[] indents = new String[depth];
		indents[0] = "";
		for(int i = 1; i < depth; ++i) {
			indents[i] = indents[i-1] + SINGLE_INDENT;
		}
		return indents;
	}
}
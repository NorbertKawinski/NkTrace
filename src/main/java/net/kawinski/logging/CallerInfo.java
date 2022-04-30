package net.kawinski.logging;

/**
 * Helper class for collecting method's caller information.
 */
public final class CallerInfo {
    public static final CallerInfo UNKNOWN = new CallerInfo("unknown", "unknown", 0);

    @SuppressWarnings("FieldNotUsedInToString") // Too much noise. Short name is good enough
    public final String fullClassName;
    public final String shortClassName;
    public final String methodName;
    public final int lineNumber;

    /**
     * Extracts caller info from StackFrame
     * @param frame stack frame from which to extract information about the caller
     */
    private CallerInfo(final StackWalker.StackFrame frame) {
        this(frame.getClassName(), frame.getMethodName(), frame.getLineNumber());
    }

    private CallerInfo(final String fullClassName, final String methodName, final int lineNumber) {
        this.fullClassName = fullClassName;
        int shortClassNameIndex = fullClassName.lastIndexOf(".");
        this.shortClassName = fullClassName.substring(shortClassNameIndex + 1);
        this.methodName = methodName;
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return shortClassName + "." + methodName + ":" + lineNumber;
    }

    /**
     * @return Immediate caller of this function
     */
    public static CallerInfo getCaller() {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        return walker.walk(frames -> frames
                .skip(2L) // Skip ourselves and our caller
                .map(CallerInfo::new)
                .findFirst()
                .orElseThrow());
    }

    /**
     * @param fqcn class name to filter for the stacktrace.
     *             All other stack entries are ignored.
     * @return The first caller after fqcn
     */
    public static CallerInfo getCaller(final String fqcn) {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        return walker.walk(frames -> frames
                .dropWhile(frame -> !frame.getClassName().equals(fqcn))
                .dropWhile(frame -> frame.getClassName().equals(fqcn))
                .map(CallerInfo::new)
                .findFirst()
                .orElse(UNKNOWN));
    }
}
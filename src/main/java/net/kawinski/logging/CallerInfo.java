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
     * Extracts caller info from stack trace element
     * @param callerSte stack element from which to extract information about the caller
     */
    private CallerInfo(final StackTraceElement callerSte)
    {
        this(callerSte.getClassName(), callerSte.getMethodName(), callerSte.getLineNumber());
    }

    private CallerInfo(final String fullClassName, final String methodName, final int lineNumber)
    {
        this.fullClassName = fullClassName;
        final String[] classNameArr = fullClassName.split("\\.");
        this.shortClassName = classNameArr[classNameArr.length - 1];
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
    public static CallerInfo getCaller()
    {
        final int callerStackTraceIndex = 3; // Empirical research shows that the caller resides at this index...
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        final StackTraceElement stack = stackTrace[callerStackTraceIndex];
        return new CallerInfo(stack);
    }

    /**
     * @param fqcn class name to filter for the stacktrace.
     *             All other stack entries are ignored.
     * @return The first caller after fqcn
     */
    public static CallerInfo getCaller(final String fqcn)
    {
        final int callerStackTraceIndex = 3; // Empirical research shows that the caller resides at this index...
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for(int i = callerStackTraceIndex; i < stackTrace.length; ++i) {
            final StackTraceElement stack = stackTrace[i];
            if(fqcn.equals(stack.getClassName())) {
                // stacktrace[i + 1] --> It theoretically could result in index out-of-bounds exceptions,
                // but in reality there's no chance thread's run() method would already be a proxy.
                // Only proxy loggers are allowed to pass their FQCN. So index out-of-bound exception would be a programmer error anyway.
                final StackTraceElement resultStack = stackTrace[i + 1];
                if(fqcn.equals(resultStack.getClassName()))
                    continue; // We're still in the same FQCN. Let's dig deeper.
                return new CallerInfo(resultStack);
            }
        }
        return UNKNOWN;
    }
}

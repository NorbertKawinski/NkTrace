package net.kawinski.logging;

import org.slf4j.Logger;
import org.slf4j.event.Level;

public final class LoggingUtils {

    private LoggingUtils() {
    }

    public static boolean canLog(final Logger logger, final Level level) {
        switch(level) {
            case ERROR: return logger.isErrorEnabled();
            case WARN: return logger.isWarnEnabled();
            case INFO: return logger.isInfoEnabled();
            case DEBUG: return logger.isDebugEnabled();
            case TRACE: return logger.isTraceEnabled();
        }
        // We'd usually throw here, but let's avoid that in logger code
        // Note that under normal circumstances this code should never be executed
        // because we already checked all cases in the switch above.
        // This protects us from the API adding new logging levels
        return false;
    }

    /**
     * Returns a new array with new element prepended
     */
    public static Object[] prepend(final Object[] array, final Object element) {
        final Object[] newArray = new Object[array.length + 1];
        newArray[0] = element;
        System.arraycopy(array, 0, newArray, 1, array.length);
        return newArray;
    }
}

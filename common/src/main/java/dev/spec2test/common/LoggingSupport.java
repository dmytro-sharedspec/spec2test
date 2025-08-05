package dev.spec2test.common;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

/**
 * Contains supporting methods for printing build log messages.
 */
public interface LoggingSupport {

    /**
     * Logs an error message to the build log.
     * @param message - the message to log
     */
    default void logError(String message) {

        logMessage(message, Diagnostic.Kind.ERROR);
    }

    /**
     * Logs a warning message to the build log.
     * @param message - the message to log
     */
    default void logWarning(String message) {

        logMessage(message, Diagnostic.Kind.WARNING);
    }

    /**
     * Logs an informational message to the build log.
     * @param message - the message to log
     */
    default void logInfo(String message) {

        logMessage(message, Diagnostic.Kind.NOTE);
    }

    /**
     * Logs a message of kind OTHER to the build log.
     * @param message - the message to log
     */
    default void logOther(String message) {

        logMessage(message, Diagnostic.Kind.OTHER);
    }

    private void logMessage(String message, Diagnostic.Kind kind) {

        String prefix = "[" + this.getClass().getSimpleName() + "] ";
        getProcessingEnv().getMessager().printMessage(kind, prefix + message);
    }

    /**
     * Override this method to provide an instance of the {@link ProcessingEnvironment} that is needed to log messages.
     * @return the processing environment
     */
    ProcessingEnvironment getProcessingEnv();
}
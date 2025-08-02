package dev.spec2test.feature2junit;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

public interface MessageSupport  {

    default void logError(String message) {

        logMessage(message, Diagnostic.Kind.ERROR);
    }

    default void logWarning(String message) {

        logMessage(message, Diagnostic.Kind.WARNING);
    }

    default void logInfo(String message) {

        logMessage(message, Diagnostic.Kind.NOTE);
    }

    default void logOther(String message) {

        logMessage(message, Diagnostic.Kind.OTHER);
    }

    private void logMessage(String message, Diagnostic.Kind kind) {

        String prefix = "[" + this.getClass().getSimpleName() + "] ";
        getProcessingEnv().getMessager().printMessage(kind, prefix + message);
    }

    ProcessingEnvironment getProcessingEnv();
}
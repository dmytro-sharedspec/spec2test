package dev.spec2test.feature2junit;

import javax.annotation.processing.AbstractProcessor;
import javax.tools.Diagnostic;

abstract class GeneratorBase extends AbstractProcessor {

    public void logError(String message) {

        logMessage(message, Diagnostic.Kind.ERROR);
    }

    public void logWarning(String message) {

        logMessage(message, Diagnostic.Kind.WARNING);
    }

    public void logInfo(String message) {

        logMessage(message, Diagnostic.Kind.NOTE);
    }

    public void logOther(String message) {

        logMessage(message, Diagnostic.Kind.OTHER);
    }

    private void logMessage(String message, Diagnostic.Kind kind) {

        String prefix = "[" + this.getClass().getSimpleName() + "] ";
        processingEnv.getMessager().printMessage(kind, prefix + message);
    }
}
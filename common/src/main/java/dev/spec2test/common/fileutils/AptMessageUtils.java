package dev.spec2test.common.fileutils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

public class AptMessageUtils {

    public static void message(String message, ProcessingEnvironment processingEnv) {

//        System.out.println("### " + message);
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.MANDATORY_WARNING, "### " + message);
    }

    public static void messageError(String message, ProcessingEnvironment processingEnv) {

//        System.out.println("### " + message);
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR, "### " + message);
    }
}

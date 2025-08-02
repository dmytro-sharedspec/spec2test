package dev.spec2test.feature2junit;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import java.io.PrintWriter;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import org.apache.commons.lang3.exception.ExceptionUtils;

@SupportedAnnotationTypes("dev.spec2test.feature2junit.Feature2JUnit")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class Feature2JUnitGenerator extends AbstractProcessor implements MessageSupport {

    private final String suffixForGeneratedClass = "Scenarios";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        int totalClassesProcessed = 0;

        logInfo("Running " + this.getClass().getSimpleName());

        for (TypeElement annotation : annotations) {

            String annotationName = annotation.getQualifiedName().toString();
            if (!annotationName.equals(Feature2JUnit.class.getName())) {
                continue;
            }

            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element annotatedElement : annotatedElements) {

                totalClassesProcessed++;

                TypeElement annotatedClass = (TypeElement) annotatedElement;

                logInfo("Processing '" + annotatedClass.getQualifiedName() + "'");

                Filer filer = processingEnv.getFiler();
                Feature2JUnit targetAnnotation = annotatedClass.getAnnotation(Feature2JUnit.class);
                String subclassFullyQualifiedName = annotatedClass.getQualifiedName() + suffixForGeneratedClass;
                TestSubclassCreator subclassGenerator = new TestSubclassCreator(processingEnv);

                PrintWriter out = null;
                try {

                    JavaFile javaFile = subclassGenerator.createTestSubclass(annotatedClass, targetAnnotation);
                    JavaFileObject subclassFile = filer.createSourceFile(subclassFullyQualifiedName);

                    out = new PrintWriter(subclassFile.openWriter());
                    javaFile.writeTo(out);
                }
                catch (Throwable t) {
                    logError("An error occurred while processing annotated element - '" + annotatedClass.getQualifiedName() + "'");
                    String rootCauseMessage = ExceptionUtils.getRootCauseMessage(t);
                    logError("Root cause message: " + rootCauseMessage);
                    String stackTrace = ExceptionUtils.getStackTrace(t);
                    logError("Stack trace: \n" + stackTrace);
                }
                finally {
                    if (out != null) {
                        out.close();
                    }
                }

                logInfo("Generated test subclass: " + subclassFullyQualifiedName);
            }
        }

        logInfo("Finished, total classes processed: " + totalClassesProcessed);

        return true;
    }

    @Override
    public ProcessingEnvironment getProcessingEnv() {
        return processingEnv;
    }
}
package dev.spec2test.feature2junit;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import dev.spec2test.common.GeneratorOptions;
import dev.spec2test.common.LoggingSupport;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.PrintWriter;
import java.util.Set;

/**
 * Annotation processor that generates JUnit test subclasses for classes annotated with {@link Feature2JUnit} annotation.
 */
@SupportedAnnotationTypes("dev.spec2test.feature2junit.Feature2JUnit")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class Feature2JUnitGenerator extends AbstractProcessor implements LoggingSupport {

    static final String defaultSuffixForGeneratedClass = "Test";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (roundEnv.processingOver() || roundEnv.errorRaised()) return false;

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

                Feature2JUnitOptions optionsAnnotation = annotatedClass.getAnnotation(Feature2JUnitOptions.class);
                GeneratorOptions generatorOptions;
                if (optionsAnnotation != null) {
                    generatorOptions = new GeneratorOptions(
                            optionsAnnotation.shouldBeAbstract(),
                            optionsAnnotation.classSuffix(),
                            optionsAnnotation.addSourceLineAnnotations(),
                            optionsAnnotation.addSourceLineBeforeStepCalls(),
                            optionsAnnotation.failScenariosWithNoSteps(),
                            optionsAnnotation.failRulesWithNoScenarios(),
                            optionsAnnotation.tagForScenariosWithNoSteps().trim(),
                            optionsAnnotation.tagForRulesWithNoScenarios().trim()
                    );
                } else {
                    generatorOptions = new GeneratorOptions();
                }

                String subclassFullyQualifiedName = annotatedClass.getQualifiedName() + generatorOptions.getClassSuffix();

                TestSubclassCreator subclassGenerator = new TestSubclassCreator(processingEnv, generatorOptions);

                PrintWriter out = null;
                try {

                    JavaFile javaFile = subclassGenerator.createTestSubclass(annotatedClass, targetAnnotation);
                    JavaFileObject subclassFile = filer.createSourceFile(subclassFullyQualifiedName);

                    out = new PrintWriter(subclassFile.openWriter());
                    javaFile.writeTo(out);
                } catch (Throwable t) {
                    logError("An error occurred while processing annotated element - '" + annotatedClass.getQualifiedName() + "'");
                    String rootCauseMessage = ExceptionUtils.getRootCauseMessage(t);
                    logError("Root cause message: " + rootCauseMessage);
                    String stackTrace = ExceptionUtils.getStackTrace(t);
                    logError("Stack trace: \n", stackTrace);
                } finally {
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

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_17;
    }
}
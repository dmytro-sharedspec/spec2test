package dev.spec2test.feature2junit;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import dev.spec2test.common.GeneratorOptions;
import dev.spec2test.common.LoggingSupport;
import java.io.IOException;
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

/**
 * Annotation processor that generates JUnit test subclasses for classes annotated with {@link Feature2JUnit} annotation.
 */
@SupportedAnnotationTypes("dev.spec2test.feature2junit.Feature2JUnit")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class Feature2JUnitGenerator extends AbstractProcessor implements LoggingSupport {

    static final String defaultSuffixForGeneratedClass = "Test";

    /**
     * Default constructor.
     */
    public Feature2JUnitGenerator() {
        super();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (roundEnv.processingOver() || roundEnv.errorRaised()) {
            return false;
        }

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

                Feature2JUnit targetAnnotation = annotatedClass.getAnnotation(Feature2JUnit.class);

                Feature2JUnitOptions optionsAnnotation = annotatedClass.getAnnotation(Feature2JUnitOptions.class);
                GeneratorOptions generatorOptions;
                if (optionsAnnotation != null) {
                    generatorOptions = new GeneratorOptions(
                            optionsAnnotation.shouldBeAbstract(),
                            optionsAnnotation.classSuffixIfAbstract(),
                            optionsAnnotation.classSuffixIfConcrete(),
                            optionsAnnotation.addSourceLineAnnotations(),
                            optionsAnnotation.addSourceLineBeforeStepCalls(),
                            optionsAnnotation.failScenariosWithNoSteps(),
                            optionsAnnotation.failRulesWithNoScenarios(),
                            optionsAnnotation.tagForScenariosWithNoSteps().trim(),
                            optionsAnnotation.tagForRulesWithNoScenarios().trim(),
                            optionsAnnotation.addCucumberStepAnnotations()
                    );
                }
                else {
                    generatorOptions = new GeneratorOptions();
                }

                TestSubclassCreator subclassGenerator = new TestSubclassCreator(getProcessingEnv(), generatorOptions);

                JavaFile javaFile = null;
                JavaFileObject subclassFile;

                try {

                    javaFile = subclassGenerator.createTestSubclass(annotatedClass, targetAnnotation.value());

                    String subclassFullyQualifiedName = annotatedClass.getQualifiedName().toString();
                    String suffix;
                    if (generatorOptions.isShouldBeAbstract()) {
                        suffix = generatorOptions.getClassSuffixIfAbstract();
                    }
                    else {
                        suffix = generatorOptions.getClassSuffixIfConcrete();
                    }
                    subclassFullyQualifiedName += suffix;

                    Filer filer = getProcessingEnv().getFiler();
                    subclassFile = filer.createSourceFile(subclassFullyQualifiedName);
                }
                catch (IOException e) {
                    logException(e, annotatedClass);
                    continue;
                }

                PrintWriter out = null;
                try {

                    out = new PrintWriter(subclassFile.openWriter());
                    javaFile.writeTo(out);

                    logInfo("Generated test class: " + javaFile.packageName + "." + javaFile.typeSpec.name);
                }
                catch (Throwable t) {
                    logException(t, annotatedClass);
                }
                finally {
                    if (out != null) {
                        out.close();
                    }
                }

            }
        }

        logInfo("Finished, total classes processed: " + totalClassesProcessed);

        return true;
    }

    private void logException(Throwable t, TypeElement annotatedClass) {

        logError("An error occurred while processing annotated element - '" + annotatedClass.getQualifiedName() + "'");
        String rootCauseMessage = ExceptionUtils.getRootCauseMessage(t);
        logError("Root cause message: " + rootCauseMessage);
        String stackTrace = ExceptionUtils.getStackTrace(t);
        logError("Stack trace: \n", stackTrace);
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
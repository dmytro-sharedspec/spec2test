package dev.spec2test.feature2junit;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import dev.spec2test.common.fileutils.AptMessageUtils;

import dev.spec2test.feature2junit.generator.TestSubclassGenerator;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

@SupportedAnnotationTypes("dev.spec2test.feature2junit.Feature2JUnit")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class Feature2JUnitGenerator extends AbstractProcessor {

//    private CustomRegexStoryParser storyParser = new CustomRegexStoryParser();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        AptMessageUtils.message("Running " + this.getClass().getSimpleName() + " processor", processingEnv);

        for (TypeElement annotation : annotations) {

            String annotationName = annotation.getQualifiedName().toString();
            if (!annotationName.equals(Feature2JUnit.class.getName())) {
                /**
                 * not our target annotation
                 */
                continue;
            }

            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element annotatedElement : annotatedElements) {

                AptMessageUtils.message("annotatedElement.simpleName = " + annotatedElement.getSimpleName(), processingEnv);

                Feature2JUnit targetAnnotation = annotatedElement.getAnnotation(Feature2JUnit.class);
                if (targetAnnotation == null) {
                    continue; // shouldn't really happen
                }

                TypeElement annotatedClass = (TypeElement) annotatedElement;

                TestSubclassGenerator subclassGenerator = new TestSubclassGenerator(processingEnv, processingEnv);

//                Set<? extends Element> rootElements = roundEnv.getRootElements();
//                Map<String, String> options = processingEnv.getOptions();

                JavaFile javaFile;
                try {
                    javaFile = subclassGenerator.createTestSubclass(annotatedElement, targetAnnotation);
                } catch (IOException e) {
                    throw new RuntimeException("An error occurred while processing annotated element - " + annotatedClass.getSimpleName(), e);
                }

                final String suffixForGeneratedClass = "Scenarios";
                Filer filer = processingEnv.getFiler();
                String subclassFullyQualifiedName = annotatedClass.getQualifiedName() + suffixForGeneratedClass;

                JavaFileObject subclassFile = null;
                try {
                    subclassFile = filer.createSourceFile(subclassFullyQualifiedName);
                } catch (IOException e) {
                    throw new RuntimeException("An error occurred while attempting to create subclass file with a name - '" + subclassFullyQualifiedName + "', reason - " + e.getMessage(), e);
                }

                try (PrintWriter out = new PrintWriter(subclassFile.openWriter())) {
                    javaFile.writeTo(out);
                } catch (IOException e) {
                    throw new RuntimeException("An error occurred while attempting to write Java file named - '" + subclassFile.getName() + "'", e);
                }

            }
        }

        return true;
    }

}
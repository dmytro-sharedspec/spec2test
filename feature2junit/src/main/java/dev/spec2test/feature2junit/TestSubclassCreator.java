package dev.spec2test.feature2junit;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import dev.spec2test.feature2junit.gherkin.CustomGherkinParser;
import dev.spec2test.feature2junit.gherkin.FeatureProcessor;
import io.cucumber.messages.types.Feature;
import java.io.IOException;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.processing.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;

@NotThreadSafe
public class TestSubclassCreator implements MessageSupport {

    private final ProcessingEnvironment processingEnv;

    public TestSubclassCreator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public JavaFile createTestSubclass(TypeElement typeElement, Feature2JUnit targetAnnotation) throws IOException {

        CustomGherkinParser gherkinParser = new CustomGherkinParser(processingEnv);

        String featureFilePath = targetAnnotation.value();
        Feature feature = gherkinParser.parseUsingPath(featureFilePath);

        Element enclosingElement = typeElement.getEnclosingElement();
        if (enclosingElement instanceof PackageElement == false) {
            throw new ProcessingException(
                    "The class annotated with @" + Feature2JUnit.class.getSimpleName() + " must be in a package, but it is not. "
                            + "Enclosing element: " + enclosingElement);
        }
        PackageElement packageElement = (PackageElement) enclosingElement;
        String packageName = packageElement.getQualifiedName().toString();

        String subclassSimpleName = typeElement.getSimpleName() + "Scenarios";

        TypeSpec.Builder classBuilder = TypeSpec
                .classBuilder(subclassSimpleName)
                .superclass(typeElement.asType())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                /**
                 * {@link Generated} annotation
                 */
                .addAnnotation(AnnotationSpec
                        .builder(Generated.class)
                        .addMember("value", "\"" + Feature2JUnitGenerator.class.getName() + "\"")
                        .build()
                )
                /**
                 * {@link TestMethodOrder} annotation
                 */
                .addAnnotation(AnnotationSpec
                        .builder(TestMethodOrder.class)
                        .addMember("value", "$T.class", ClassName.get(MethodOrderer.OrderAnnotation.class))
                        .build()
                )
                /**
                 * {@link TestClassOrder} annotation
                 */
                .addAnnotation(AnnotationSpec
                        .builder(TestClassOrder.class)
                        .addMember("value", "$T.class", ClassName.get(ClassOrderer.OrderAnnotation.class))
                        .build()
                )
                /**
                 * {@link FeatureFilePath} annotation
                 */
                .addAnnotation(AnnotationSpec
                        .builder(FeatureFilePath.class)
                        .addMember("value", "\"" + featureFilePath + "\"")
                        .build()
                );

        FeatureProcessor featureProcessor = new FeatureProcessor(processingEnv);
        featureProcessor.processFeature(feature, classBuilder);

        TypeSpec typeSpec = classBuilder.build();

        JavaFile javaFile = JavaFile
                .builder(packageName, typeSpec)
                .indent("    ")
                .build();

        return javaFile;
    }

    @Override
    public ProcessingEnvironment getProcessingEnv() {
        return processingEnv;
    }
}
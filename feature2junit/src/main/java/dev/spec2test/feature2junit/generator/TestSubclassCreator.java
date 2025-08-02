package dev.spec2test.feature2junit.generator;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import dev.spec2test.common.fileutils.AptMessageUtils;
import dev.spec2test.feature2junit.Feature2JUnit;
import dev.spec2test.feature2junit.Feature2JUnitGenerator;
import dev.spec2test.feature2junit.FeatureFilePath;
import io.cucumber.messages.types.Feature;
import java.io.IOException;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.processing.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;

@NotThreadSafe
public class TestSubclassCreator {

    private final ProcessingEnvironment processingEnv;

    public TestSubclassCreator(ProcessingEnvironment processingEnv, ProcessingEnvironment env) {
        this.processingEnv = processingEnv;
    }

    public JavaFile createTestSubclass(Element annotatedElement, Feature2JUnit targetAnnotation) throws IOException {

        AptMessageUtils.message("Creating a test subclass... ", processingEnv);

        String featureFilePath = targetAnnotation.value();

        CustomGherkinParser gherkinParser = new CustomGherkinParser(processingEnv, processingEnv);

        Feature feature = gherkinParser.parseUsingPath(featureFilePath);

        TypeElement typeElement = (TypeElement) annotatedElement;

        Element enclosingElement = typeElement.getEnclosingElement();
        PackageElement packageElement =
                enclosingElement instanceof PackageElement ? (PackageElement) enclosingElement : null;
        AptMessageUtils.message("package = " + packageElement.getQualifiedName(), processingEnv);
        String packageName = packageElement.getQualifiedName().toString();

        Name baseClassName = typeElement.getSimpleName();
        String subclassSimpleName = baseClassName + "Scenarios";
        TypeSpec.Builder classBuilder = TypeSpec
                .classBuilder(subclassSimpleName)
                .superclass(typeElement.asType())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
//        classBuilder.addStaticBlock(CodeBlock.of("\n\n"));
//        classBuilder.addInitializerBlock(CodeBlock.of("\n"));
//        classBuilder.addCode("// TODO: not yet implemented \n");

        /**
         * add {@link Generated} annotation
         */
        AnnotationSpec generatedAnnotation = AnnotationSpec
                .builder(Generated.class)
                .addMember("value", "\"" + Feature2JUnitGenerator.class.getName() + "\"")
                .build();
        classBuilder.addAnnotation(generatedAnnotation);

        /**
         * add {@link TestMethodOrder} annotation
         * @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
         */
        AnnotationSpec testMethodOrderAnnotation = AnnotationSpec
                .builder(TestMethodOrder.class)
                .addMember("value", "$T.class", ClassName.get(MethodOrderer.OrderAnnotation.class))
                .build();
        classBuilder.addAnnotation(testMethodOrderAnnotation);
        /**
         * add {@link TestClassOrder} annotation
         * @TestClassOrder(ClassOrderer.OrderAnnotation.class)
         */
        AnnotationSpec testClassOrderAnnotation = AnnotationSpec
                .builder(TestClassOrder.class)
                .addMember("value", "$T.class", ClassName.get(ClassOrderer.OrderAnnotation.class))
                .build();
        classBuilder.addAnnotation(testClassOrderAnnotation);

        /**
         * add {@link FeatureFilePath} annotation
         */
        AnnotationSpec featureFilePathAnnotation = AnnotationSpec
                .builder(FeatureFilePath.class)
                .addMember("value", "\"" + featureFilePath + "\"")
                .build();
        classBuilder.addAnnotation(featureFilePathAnnotation);

        try {

            FeatureProcessor.processFeature(feature, classBuilder, processingEnv);

        }
        catch (Throwable t) {
            AptMessageUtils.messageError(
                    "An error occurred while generating test subclass for " + typeElement.getSimpleName() + ": "
                            + t.getMessage(), processingEnv);
            throw new RuntimeException(
                    "An error occurred while generating test subclass for " + typeElement.getSimpleName(), t);
        }

//        List<MethodSpec> methodSpecs = classBuilder.methodSpecs;
//        MethodSpec firstMethodSpec = methodSpecs.get(0);
//        firstMethodSpec.javadoc = CodeBlock.builder();
//                .add("This class is generated by the ")
//                .add("$T", Feature2JUnitGenerator.class)
//                .add(" annotation processor.\n")
//                .add("It contains test methods for the scenarios defined in the feature file: ")
//                .add("$S", featureFilePath)
//                .build();

        TypeSpec typeSpec = classBuilder.build();

        JavaFile javaFile = JavaFile
                .builder(packageName, typeSpec)
                .indent("    ")
                .build();

        return javaFile;
    }

}
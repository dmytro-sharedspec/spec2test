package dev.spec2test.feature2junit;

import com.squareup.javapoet.*;
import dev.spec2test.common.LoggingSupport;
import dev.spec2test.common.ProcessingException;
import dev.spec2test.feature2junit.gherkin.FeatureFileParser;
import dev.spec2test.feature2junit.gherkin.FeatureProcessor;
import dev.spec2test.feature2junit.gherkin.utils.JavaDocUtils;
import dev.spec2test.feature2junit.gherkin.utils.TagUtils;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Tag;
import org.junit.jupiter.api.*;

import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.processing.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Creates a JUnit test subclass for a given type element annotated with {@link Feature2JUnit}.
 */
@NotThreadSafe
public class TestSubclassCreator implements LoggingSupport {

    private final ProcessingEnvironment processingEnv;

    /**
     * Constructor for TestSubclassCreator.
     *
     * @param processingEnv the processing environment used for annotation processing
     */
    public TestSubclassCreator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    /**
     * Creates a JUnit test subclass for the given type element annotated with {@link Feature2JUnit}.
     *
     * @param typeElement      the type element to create a test subclass for
     * @param targetAnnotation the annotation containing the feature file path
     * @return a {@link JavaFile} representing the generated test subclass
     * @throws IOException if an error occurs during file generation
     */
    public JavaFile createTestSubclass(TypeElement typeElement, Feature2JUnit targetAnnotation) throws IOException {

        Element enclosingElement = typeElement.getEnclosingElement();
        if (enclosingElement instanceof PackageElement == false) {
            throw new ProcessingException(
                    "The class annotated with @" + Feature2JUnit.class.getSimpleName() + " must be in a package, but it is not. "
                            + "Enclosing element: " + enclosingElement);
        }

        PackageElement packageElement = (PackageElement) enclosingElement;
        String packageName = packageElement.getQualifiedName().toString();
        String annotatedClassName = typeElement.getSimpleName().toString();

        FeatureFileParser gherkinParser = new FeatureFileParser(processingEnv);

        String featureFilePath = targetAnnotation.value();

        String featureFilePathForParsing;
        if (featureFilePath == null || featureFilePath.isBlank()) {
//            moduleAndPkg = packageName.replaceAll("\\.", "/");
            featureFilePathForParsing = packageName.replaceAll("\\.", "/")
                    + "/" + annotatedClassName + ".feature";
        } else {
            featureFilePathForParsing = featureFilePath;
        }
        Feature feature = gherkinParser.parseUsingPath(featureFilePathForParsing);

//        String subclassSimpleName = typeElement.getSimpleName() + "Scenarios";
        String subclassSimpleName = typeElement.getSimpleName() + "Test";

        TypeSpec.Builder classBuilder = TypeSpec
                .classBuilder(subclassSimpleName)
                .superclass(typeElement.asType())
//                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
                .addModifiers(Modifier.PUBLIC);

        List<Tag> tags = feature.getTags();
        if (tags != null && !tags.isEmpty()) {
            AnnotationSpec jUnitTagsAnnotation = TagUtils.toJUnitTagsAnnotation(tags);
            classBuilder.addAnnotation(jUnitTagsAnnotation);
        }

        /**
         * put feature text into an initializer block
         */
        //                .addJavadoc(CodeBlock.of(feature.getKeyword() + ": " + feature.getName()))
        //                .addJavadoc(CodeBlock.of("\n" + feature.getDescription()))
        String featureTextJavaDoc = JavaDocUtils.toJavaDocContent(feature.getKeyword(), feature.getName(), feature.getDescription());
//        classBuilder.addInitializerBlock(CodeBlock.of(featureTextJavaDoc));
        classBuilder.addJavadoc(CodeBlock.of(featureTextJavaDoc));

        /**
         * {@link org.junit.jupiter.api.DisplayName} annotation
         */
        String featureFileName = featureFilePathForParsing.substring(
                featureFilePathForParsing.lastIndexOf("/") + 1,
                featureFilePathForParsing.lastIndexOf(".")
        );
        classBuilder.addAnnotation(AnnotationSpec
                .builder(DisplayName.class)
                .addMember("value", "\"" + featureFileName + "\"")
                .build()
        );

        /**
         * {@link Generated} annotation
         */
        classBuilder.addAnnotation(AnnotationSpec
                .builder(Generated.class)
                .addMember("value", "\"" + Feature2JUnitGenerator.class.getName() + "\"")
                .build()
        );
        /**
         * {@link TestMethodOrder} annotation
         */
        classBuilder.addAnnotation(AnnotationSpec
                .builder(TestMethodOrder.class)
                .addMember("value", "$T.class", ClassName.get(MethodOrderer.OrderAnnotation.class))
                .build()
        );
        if (feature.getChildren().stream().anyMatch(child -> child.getRule().isPresent())) {
            /**
             * {@link TestClassOrder} annotation
             */
            classBuilder.addAnnotation(AnnotationSpec
                    .builder(TestClassOrder.class)
                    .addMember("value", "$T.class", ClassName.get(ClassOrderer.OrderAnnotation.class))
                    .build()
            );
        }
        /**
         * {@link FeatureFilePath} annotation
         */
        String featureFilePathForAnnotation;
        if (featureFilePath == null || featureFilePath.isBlank()) {
            featureFilePathForAnnotation = packageName.replaceAll("\\.", "/") + "/" + annotatedClassName + ".feature";
        } else {
            featureFilePathForAnnotation = featureFilePath;
        }
        classBuilder.addAnnotation(AnnotationSpec
                .builder(FeatureFilePath.class)
                .addMember("value", "\"" + featureFilePathForAnnotation + "\"")
                .build()
        );

        FeatureProcessor featureProcessor = new FeatureProcessor(processingEnv);
        featureProcessor.processFeature(feature, classBuilder);

        /**
         * add createDataTable method
         */
        List<MethodSpec> methodSpecs = classBuilder.methodSpecs;
        Optional<MethodSpec> methodWithDataTableParameter = methodSpecs.stream()
                .filter(methodSpec -> methodSpec.parameters.stream()
                        .anyMatch(parameterSpec -> parameterSpec.name.equals("dataTable"))).findFirst();
        if (methodWithDataTableParameter.isPresent()) {
//            MethodSpec getTableConverterMethod = TableUtils.createGetTableConverterMethod(processingEnv);
//            classBuilder.addMethod(getTableConverterMethod);
//            MethodSpec createDataTableMethod = TableUtils.createDataTableMethod(processingEnv);
//            classBuilder.addMethod(createDataTableMethod);
        }

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
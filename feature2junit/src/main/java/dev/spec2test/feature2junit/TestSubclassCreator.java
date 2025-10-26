package dev.spec2test.feature2junit;

import com.squareup.javapoet.*;
import dev.spec2test.common.GeneratorOptions;
import dev.spec2test.common.LoggingSupport;
import dev.spec2test.common.OptionsSupport;
import dev.spec2test.common.ProcessingException;
import dev.spec2test.feature2junit.gherkin.FeatureFileParser;
import dev.spec2test.feature2junit.gherkin.FeatureProcessor;
import dev.spec2test.feature2junit.gherkin.utils.*;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Tag;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;

import javax.annotation.processing.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Creates a JUnit test subclass for a given type element annotated with {@link Feature2JUnit}.
 */
class TestSubclassCreator implements LoggingSupport, OptionsSupport {

    private final ProcessingEnvironment processingEnv;

    @Getter
    private final GeneratorOptions options;

    protected FeatureFileParser featureFileParser;

    /**
     * Constructor for TestSubclassCreator.
     *
     * @param generatorOptions the generator options to use for test generation
     * @param processingEnv    the processing environment used for annotation processing
     */
    public TestSubclassCreator(ProcessingEnvironment processingEnv, GeneratorOptions generatorOptions) {
        this.processingEnv = processingEnv;
        this.options = generatorOptions;

        featureFileParser = new FeatureFileParser(processingEnv);
    }

    /**
     * Creates a JUnit test subclass for the given type element annotated with {@link Feature2JUnit}.
     *
     * @param typeElement     the type element to create a test subclass for
     * @param featureFilePath the feature file path
     * @return a {@link JavaFile} representing the generated test subclass
     * @throws IOException if an error occurs during file generation
     */
    public JavaFile createTestSubclass(TypeElement typeElement, String featureFilePath) throws IOException {

        String packageName;
        Element enclosingElement = typeElement.getEnclosingElement();

        if (enclosingElement != null) {

            if (enclosingElement instanceof PackageElement == false) {
                throw new ProcessingException(
                        "The class annotated with @" + Feature2JUnit.class.getSimpleName() + " must have package as " +
                                "its enclosing element, but was - " + enclosingElement);
            }

            PackageElement packageElement = (PackageElement) enclosingElement;
            packageName = packageElement.getQualifiedName().toString();

        } else {
            packageName = "";
        }

        String annotatedClassName = typeElement.getSimpleName().toString();
        String featureFilePathForParsing;

        if (StringUtils.isBlank(featureFilePath)) {
            /*
             * the assumed path is that of the "<package_name><className>.feature"
             */
            featureFilePathForParsing = "";
            if (StringUtils.isNotBlank(packageName)) {
                featureFilePathForParsing = packageName.replaceAll("\\.", "/") + "/";
            }
            featureFilePathForParsing += annotatedClassName + ".feature";

        } else {
            // feature file path specified explicitly
            featureFilePathForParsing = featureFilePath;
        }

        Feature feature = featureFileParser.parseUsingPath(featureFilePathForParsing);

        String suffixToApply;
        if (options.isShouldBeAbstract()) {
            suffixToApply = options.getClassSuffixIfAbstract();
        } else {
            suffixToApply = options.getClassSuffixIfConcrete();
        }

        String subclassSimpleName = typeElement.getSimpleName() + suffixToApply;

        TypeSpec.Builder classBuilder = TypeSpec
                .classBuilder(subclassSimpleName)
                .superclass(typeElement.asType())
                .addModifiers(Modifier.PUBLIC);

        if (options.isShouldBeAbstract()) {
            classBuilder.addModifiers(Modifier.ABSTRACT);
        }

        /**
         * put feature text into an initializer block
         */
        //                .addJavadoc(CodeBlock.of(feature.getKeyword() + ": " + feature.getName()))
        //                .addJavadoc(CodeBlock.of("\n" + feature.getDescription()))
        //        String featureTextJavaDoc = JavaDocUtils.toJavaDocContent(feature.getKeyword(), feature.getName(), feature.getDescription());
        String featureTextJavaDoc = JavaDocUtils.toJavaDoc(feature.getKeyword(), feature.getName(), feature.getDescription());
        classBuilder.addInitializerBlock(CodeBlock.of(featureTextJavaDoc));
        //        classBuilder.addJavadoc(CodeBlock.of(featureTextJavaDoc));

        FeatureProcessor featureProcessor = new FeatureProcessor(processingEnv, options, typeElement);
        featureProcessor.processFeature(feature, classBuilder);

        /**
         * add createDataTable method
         */
        List<MethodSpec> methodSpecs = classBuilder.methodSpecs;
        boolean featureHasStepWithDataTable = FeatureStepUtils.featureHasStepWithDataTable(feature);
        if (featureHasStepWithDataTable) {
            //        Optional<MethodSpec> methodWithDataTableParameter = methodSpecs.stream()
            //                .filter(methodSpec -> methodSpec.parameters.stream()
            //                        .anyMatch(parameterSpec -> parameterSpec.name.equals("dataTable"))).findFirst();
            Set<String> allInheritedMethodNames = ElementMethodUtils.getAllInheritedMethodNames(processingEnv, typeElement);
            boolean alreadyHasCreateDataTable = allInheritedMethodNames.contains("createDataTable");
            if (!alreadyHasCreateDataTable) {
                if (options.isShouldBeAbstract()) {
                    MethodSpec getTableConverterMethod = TableUtils.createGetTableConverterMethod(processingEnv);
                    classBuilder.addMethod(getTableConverterMethod);
                }
                MethodSpec createDataTableMethod = TableUtils.createDataTableMethod(processingEnv);
                classBuilder.addMethod(createDataTableMethod);
            }
        }

        /**
         * add JavaDoc instructions on how to use this class
         */
        String classJavaDoc;
        if (classBuilder.modifiers.contains(Modifier.ABSTRACT)) {
            classJavaDoc = """
                    To implement tests in this generated class, extend it and implement all abstract methods.
                    """;
        } else {
            classJavaDoc = """
                    To implement tests in this generated class, move any methods with failing assumptions into the base
                    class and implement them.
                    """;
        }
        classBuilder.addJavadoc(CodeBlock.of(classJavaDoc));

        addClassAnnotations(feature, classBuilder, featureFilePathForParsing, featureFilePath, packageName, annotatedClassName);

        TypeSpec typeSpec = classBuilder.build();

        JavaFile javaFile = JavaFile
                .builder(packageName, typeSpec)
                .indent("    ")
                .build();

        return javaFile;
    }

    private static void addClassAnnotations(
            Feature feature,
            TypeSpec.Builder classBuilder,
            String featureFilePathForParsing,
            String featureFilePath,
            String packageName,
            String annotatedClassName) {

        List<Tag> tags = feature.getTags();
        if (tags != null && !tags.isEmpty()) {
            AnnotationSpec jUnitTagsAnnotation = TagUtils.toJUnitTagsAnnotation(tags);
            classBuilder.addAnnotation(jUnitTagsAnnotation);
        }

        /**
         * {@link DisplayName} annotation
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
                //                .addMember("comments", "\"GWT methods have been created with failing assumptions. Copy these into the base class and implement them.\"")
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
    }

    @Override
    public ProcessingEnvironment getProcessingEnv() {
        return processingEnv;
    }
}
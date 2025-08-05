package dev.spec2test.feature2junit;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import dev.spec2test.common.LoggingSupport;
import dev.spec2test.common.ProcessingException;
import dev.spec2test.feature2junit.gherkin.FeatureFileParser;
import dev.spec2test.feature2junit.gherkin.FeatureProcessor;
import dev.spec2test.feature2junit.gherkin.utils.JavaDocUtils;
import dev.spec2test.feature2junit.gherkin.utils.TableUtils;
import dev.spec2test.feature2junit.gherkin.utils.TagUtils;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Tag;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.processing.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;

@NotThreadSafe
public class TestSubclassCreator implements LoggingSupport {

    private final ProcessingEnvironment processingEnv;

    public TestSubclassCreator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public JavaFile createTestSubclass(TypeElement typeElement, Feature2JUnit targetAnnotation) throws IOException {

        FeatureFileParser gherkinParser = new FeatureFileParser(processingEnv);

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
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

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
        String featureTextJavaDoc = JavaDocUtils.toJavaDoc(feature.getKeyword(), feature.getName(), feature.getDescription());
        classBuilder.addInitializerBlock(CodeBlock.of(featureTextJavaDoc));

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
        classBuilder.addAnnotation(AnnotationSpec
                .builder(FeatureFilePath.class)
                .addMember("value", "\"" + featureFilePath + "\"")
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
            MethodSpec getTableConverterMethod = TableUtils.createGetTableConverterMethod(processingEnv);
            classBuilder.addMethod(getTableConverterMethod);
            MethodSpec createDataTableMethod = TableUtils.createDataTableMethod(processingEnv);
            classBuilder.addMethod(createDataTableMethod);
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
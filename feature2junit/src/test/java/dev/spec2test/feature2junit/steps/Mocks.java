package dev.spec2test.feature2junit.steps;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import dev.spec2test.common.GeneratorOptions;
import dev.spec2test.feature2junit.Feature2JUnit;
import dev.spec2test.feature2junit.Feature2JUnitGenerator;
import dev.spec2test.feature2junit.Feature2JUnitOptions;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.mockito.Mockito;

@UtilityClass
public class Mocks {

    static Feature2JUnitGenerator generator(ProcessingEnvironment processingEnvironment) {

        Feature2JUnitGenerator generator = Mockito.mock(Feature2JUnitGenerator.class);
        Mockito.when(generator.process(Mockito.any(), Mockito.any())).thenCallRealMethod();

        Mockito.when(generator.getProcessingEnv()).thenReturn(processingEnvironment);

        return generator;
    }

    static TypeElement feature2junitAnnotationTypeMirror() {

        TypeElement annotationType = Mockito.mock(TypeElement.class);
        Name annotationName = Mockito.mock(Name.class);
        Mockito.when(annotationName.toString()).thenReturn(Feature2JUnit.class.getName());
        Mockito.when(annotationType.getQualifiedName()).thenReturn(annotationName);
        return annotationType;
    }

    static Feature2JUnit feature2junit() {

        Feature2JUnit f2j = Mockito.mock(Feature2JUnit.class);
        //        Mockito.when(f2j.value()).thenReturn("features/MockedFeaturePath.feature");
        return f2j;
    }

    static Feature2JUnitOptions feature2junitOptions() {

        Feature2JUnitOptions options = Mockito.mock(Feature2JUnitOptions.class);

        GeneratorOptions defaultOptions = new GeneratorOptions();

        Mockito.when(options.shouldBeAbstract()).thenReturn(defaultOptions.isShouldBeAbstract());
        // todo - rest

        return options;
    }

    static TypeElement annotatedBaseClass(Feature2JUnit feature2junitAnnotation, Feature2JUnitOptions options) {

        TypeElement annotatedClass = Mockito.mock(TypeElement.class);

        Name simpleName = Mockito.mock(Name.class);
        String simpleClassName = "MockedAnnotatedTestClass";
        Mockito.when(simpleName.toString()).thenReturn(simpleClassName);
        Mockito.when(annotatedClass.getSimpleName()).thenReturn(simpleName);

        Name qualifiedName = Mockito.mock(Name.class);
        Mockito.when(qualifiedName.toString()).thenReturn("com.example." + simpleClassName);
        Mockito.when(annotatedClass.getQualifiedName()).thenReturn(qualifiedName);

        TypeMirror annotatedClassMirror = Mockito.mock(TypeMirror.class);
        Mockito.when(annotatedClass.asType()).thenReturn(annotatedClassMirror);
        // annotatedClassMirror
        TypeName typeNameMock = Mockito.mock(TypeName.class);
        Mockito.when(annotatedClassMirror.accept(Mockito.any(), Mockito.any()))
                .thenReturn(typeNameMock);
        Mockito.when(typeNameMock.isPrimitive()).thenReturn(false);

        Mockito.when(annotatedClass.getAnnotation(Feature2JUnit.class)).thenReturn(feature2junitAnnotation);
        Mockito.when(annotatedClass.getAnnotation(Feature2JUnitOptions.class)).thenReturn(options);
        return annotatedClass;
    }

//    @SneakyThrows(IOException.class)
    static ProcessingEnvironment processingEnvironment() {

        ProcessingEnvironment processingEnvironment = Mockito.mock(ProcessingEnvironment.class);

//        FileObject specFile = Mockito.mock(FileObject.class);
//        Mockito.when(filer.getResource(Mockito.any(), Mockito.any(), Mockito.any()))
//                .thenReturn(specFile);
//
//        Mockito.when(specFile.getCharContent(Mockito.anyBoolean()))
//                .thenReturn("Feature: Mocked feature file content");

        return processingEnvironment;
    }

    @SneakyThrows(IOException.class)
    static void returnFeatureContent(ProcessingEnvironment processingEnvironment, String featureContent) {

        Filer filer = processingEnvironment.getFiler();

        FileObject specFile = Mockito.mock(FileObject.class);
        Mockito.when(filer.getResource(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(specFile);

        Mockito.when(specFile.getCharContent(Mockito.anyBoolean()))
                .thenReturn(featureContent);
    }

    static RoundEnvironment roundEnvironment(TypeElement annotatedBaseClass, TypeElement feature2junitAnnotationType) {

        RoundEnvironment roundEnv = Mockito.mock(RoundEnvironment.class);

        Set mockedAnnotatedElements = Set.of(annotatedBaseClass);
        Mockito.when(roundEnv.getElementsAnnotatedWith(feature2junitAnnotationType))
                .thenReturn(mockedAnnotatedElements);

        return roundEnv;
    }

    public static Filer filer(ProcessingEnvironment processingEnvironment) {

        Filer filer = Mockito.mock(Filer.class);
        Mockito.when(processingEnvironment.getFiler()).thenReturn(filer);

        return filer;
    }

    @SneakyThrows(IOException.class)
    public static StringWriter generatedClassWriter(Filer filer) {

        JavaFileObject generatedJavaFile = Mockito.mock(JavaFileObject.class);
        Mockito.when(filer.createSourceFile(Mockito.any())).thenReturn(generatedJavaFile);

        StringWriter stringWriter = new StringWriter();
        Mockito.when(generatedJavaFile.openWriter()).thenReturn(stringWriter);

        return stringWriter;
    }
}

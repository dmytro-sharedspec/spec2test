package dev.spec2test.feature2junit.gherkin.utils;

import com.squareup.javapoet.AnnotationSpec;
import dev.spec2test.common.SourceLine;
import io.cucumber.messages.types.Location;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LocationUtils {

    public static AnnotationSpec toJUnitTagsAnnotation(Location location) {

        AnnotationSpec.Builder annotationSpecBuilder;

        annotationSpecBuilder = AnnotationSpec.builder(SourceLine.class)
                .addMember("value", "$L", location.getLine());

        AnnotationSpec annotationSpec = annotationSpecBuilder.build();
        return annotationSpec;
    }

}

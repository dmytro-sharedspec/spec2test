package dev.spec2test.feature2junit.gherkin.utils;

import com.squareup.javapoet.AnnotationSpec;
import io.cucumber.messages.types.Tag;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.Tags;

@UtilityClass
public class TagUtils {

    public static AnnotationSpec toJUnitTagsAnnotation(List<Tag> tags) {

        AnnotationSpec.Builder annotationSpecBuilder;

        if (tags.size() > 1) {
            /**
             * use {@link Tags}
             */
            annotationSpecBuilder = AnnotationSpec.builder(Tags.class);

            for (Tag tag : tags) {

                String tagName = tag.getName().trim();
                tagName = tagName.startsWith("@") ? tagName.substring(1) : tagName;

                AnnotationSpec tagAnnotationSpec = AnnotationSpec.builder(org.junit.jupiter.api.Tag.class)
                        .addMember("value", "\"" + tagName + "\"")
                        .build();
                annotationSpecBuilder.addMember("value", "$L", tagAnnotationSpec);
            }

        } else {
            /**
             * use {@link org.junit.jupiter.api.Tag}
             */
            Tag tag = tags.get(0);

            String tagName = tag.getName().trim();
            tagName = tagName.startsWith("@") ? tagName.substring(1) : tagName;

            annotationSpecBuilder = AnnotationSpec.builder(org.junit.jupiter.api.Tag.class)
                    .addMember("value", "\"" + tagName + "\"");
        }

        AnnotationSpec annotationSpec = annotationSpecBuilder.build();
        return annotationSpec;
    }

}

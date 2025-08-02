package dev.spec2test.feature2junit.gherkin;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import dev.spec2test.feature2junit.MessageSupport;
import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.Step;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInfo;

@RequiredArgsConstructor
public class BackgroundProcessor implements MessageSupport {

    @Getter
    private final ProcessingEnvironment processingEnv;

    MethodSpec.Builder processFeatureBackground(Background background, TypeSpec.Builder classBuilder) {

        return processFeatureBackground(background, classBuilder, "featureBackground");
    }

    public MethodSpec.Builder processRuleBackground(Background background, TypeSpec.Builder classBuilder) {

        return processFeatureBackground(background, classBuilder, "ruleBackground");
    }

    private MethodSpec.Builder processFeatureBackground(
            Background background,
            TypeSpec.Builder classBuilder,
            String backgroundMethodName) {

        logInfo("Processing background: " + background.getName());

        List<MethodSpec> allMethodSpecs = classBuilder.methodSpecs;

        List<Step> backgroundSteps = background.getSteps();
        List<MethodSpec> backgroundStepsMethodSpecs = new ArrayList<>(backgroundSteps.size());

        MethodSpec.Builder featureBackgroundMethodBuilder = MethodSpec
                .methodBuilder(backgroundMethodName)
                .addModifiers(Modifier.PUBLIC);

        addJUnitAnnotations(featureBackgroundMethodBuilder, background);

        featureBackgroundMethodBuilder.addParameter(TestInfo.class, "testInfo");

        for (Step scenarioStep : backgroundSteps) {

            MethodSpec stepMethodSpec =
                    StepProcessor.processStep(scenarioStep, featureBackgroundMethodBuilder, backgroundStepsMethodSpecs);
            backgroundStepsMethodSpecs.add(stepMethodSpec);

            String stepMethodName = stepMethodSpec.name;
            MethodSpec existingMethodSpec =
                    allMethodSpecs.stream().filter(methodSpec -> methodSpec.name.equals(stepMethodName))
                            .findFirst()
                            .orElse(null);

            if (existingMethodSpec == null) {
                // If the method already exists, we can skip creating it again
                classBuilder.addMethod(stepMethodSpec);
            }
        }

        return featureBackgroundMethodBuilder;
    }

    private static void addJUnitAnnotations(MethodSpec.Builder scenarioMethodBuilder, Background background) {

        AnnotationSpec displayNameAnnotation = AnnotationSpec
                .builder(DisplayName.class)
                .addMember("value", "\"Background: " + background.getName() + "\"")
                .build();

        AnnotationSpec testAnnotation = AnnotationSpec
                .builder(BeforeEach.class)
                .build();
        scenarioMethodBuilder
                .addAnnotation(testAnnotation)
                .addAnnotation(displayNameAnnotation);
    }

}

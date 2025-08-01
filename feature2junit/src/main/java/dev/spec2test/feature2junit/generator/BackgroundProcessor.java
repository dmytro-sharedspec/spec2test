package dev.spec2test.feature2junit.generator;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.Step;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInfo;

public class BackgroundProcessor {

    static MethodSpec.Builder processBackground(Background background, TypeSpec.Builder classBuilder) {

        String backgroundMethodName = "featureBackground";

        return processBackground(background, classBuilder, backgroundMethodName);
    }

    public static MethodSpec.Builder processRuleBackground(
            int ruleCount,
            Background background,
            TypeSpec.Builder classBuilder) {

        String backgroundMethodName = "rule_" + ruleCount + "_background";

        return processBackground(background, classBuilder, backgroundMethodName);
    }

    private static MethodSpec.Builder processBackground(
            Background background,
            TypeSpec.Builder classBuilder,
            String backgroundMethodName) {
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

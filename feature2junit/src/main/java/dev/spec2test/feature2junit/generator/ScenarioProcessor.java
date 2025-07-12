package dev.spec2test.feature2junit.generator;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Step;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class ScenarioProcessor {

    static MethodSpec.Builder processScenario(int scenarioCount, Scenario scenario, TypeSpec.Builder classBuilder) {

        List<MethodSpec> allMethodSpecs = classBuilder.methodSpecs;

        List<Step> scenarioSteps = scenario.getSteps();
        List<MethodSpec> scenarioStepsMethodSpecs = new ArrayList<>(scenarioSteps.size());

        String scenarioMethodName = "scenario_" + (scenarioCount + 1);
        MethodSpec.Builder scenarioMethodBuilder = MethodSpec
                .methodBuilder(scenarioMethodName)
                .addModifiers(Modifier.PUBLIC);

        addJUnitAnnotations(scenarioMethodBuilder, scenario);

        scenarioMethodBuilder.addParameter(TestInfo.class, "testInfo");

        for (Step scenarioStep : scenarioSteps) {

            MethodSpec stepMethodSpec =
                    StepProcessor.processStep(scenarioStep, scenarioMethodBuilder, scenarioStepsMethodSpecs);
            scenarioStepsMethodSpecs.add(stepMethodSpec);

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

        return scenarioMethodBuilder;
    }

    private static void addJUnitAnnotations(MethodSpec.Builder scenarioMethodBuilder, Scenario scenario) {

        AnnotationSpec displayNameAnnotation = AnnotationSpec
                .builder(DisplayName.class)
                .addMember("value", "\"Scenario: " + scenario.getName() + "\"")
                .build();

        AnnotationSpec testAnnotation = AnnotationSpec
                .builder(Test.class)
                .build();
        scenarioMethodBuilder
                .addAnnotation(testAnnotation)
                .addAnnotation(displayNameAnnotation);
    }

}

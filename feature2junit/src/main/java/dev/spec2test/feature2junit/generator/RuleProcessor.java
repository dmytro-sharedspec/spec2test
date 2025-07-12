package dev.spec2test.feature2junit.generator;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.RuleChild;
import io.cucumber.messages.types.Scenario;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

public class RuleProcessor {

    static void processRule(int featureRuleCount, Rule rule, TypeSpec.Builder classBuilder) {

        String ruleName = rule.getName();

        TypeSpec.Builder nestedRuleClassBuilder = TypeSpec
                .classBuilder("Rule_" + featureRuleCount)
                //.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
                .addModifiers(Modifier.PUBLIC);

        /**
         * add {@link org.junit.jupiter.api.Nested} annotation
         */
        nestedRuleClassBuilder.addAnnotation(
                AnnotationSpec.builder(Nested.class).build()
        );
        /**
         * add {@link DisplayName} annotation
         */
        nestedRuleClassBuilder.addAnnotation(
                AnnotationSpec.builder(DisplayName.class)
                        .addMember("value", "\"Rule: " + ruleName + "\"")
                        .build()
        );

        List<RuleChild> children = rule.getChildren();

        int ruleScenarioCount = 0;

        for (RuleChild child : children) {

            if (child.getScenario().isPresent()) {

                Scenario scenario = child.getScenario().get();

                MethodSpec.Builder scenarioMethodBuilder = ScenarioProcessor.processScenario(ruleScenarioCount++, scenario, classBuilder);
                MethodSpec scenarioMethod = scenarioMethodBuilder.build();
                nestedRuleClassBuilder.addMethod(scenarioMethod);
            }
            else {
                throw new IllegalArgumentException("Unsupported rule child type: " + child);
            }
        }

        TypeSpec nestedRuleClassSpec = nestedRuleClassBuilder.build();
        classBuilder.addType(nestedRuleClassSpec);
    }

}

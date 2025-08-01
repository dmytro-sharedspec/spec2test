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
import org.junit.jupiter.api.Order;

public class RuleProcessor {

    static void processRule(int featureRuleCount, Rule rule, TypeSpec.Builder classBuilder) {

        String ruleName = rule.getName();

        TypeSpec.Builder nestedRuleClassBuilder = TypeSpec
                .classBuilder("Rule_" + featureRuleCount)
                //.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
                .addModifiers(Modifier.PUBLIC);

        AnnotationSpec orderAnnotation = AnnotationSpec
                .builder(Order.class)
                .addMember("value", "" + ruleNumber)
                .build();
        nestedRuleClassBuilder.addAnnotation(orderAnnotation);

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

        int ruleScenarioNumber = 0;

        for (RuleChild child : children) {

            if (child.getScenario().isPresent()) {

                Scenario scenario = child.getScenario().get();

                ruleScenarioNumber++;
                MethodSpec.Builder scenarioMethodBuilder =
                        ScenarioProcessor.processScenario(ruleScenarioNumber, scenario, classBuilder);
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

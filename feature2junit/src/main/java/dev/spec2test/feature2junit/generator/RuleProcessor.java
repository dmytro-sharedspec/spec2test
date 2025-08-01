package dev.spec2test.feature2junit.generator;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import dev.spec2test.common.fileutils.AptMessageUtils;
import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.RuleChild;
import io.cucumber.messages.types.Scenario;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;

public class RuleProcessor {

    static void processRule(
            int ruleNumber, Rule rule, TypeSpec.Builder classBuilder,
            ProcessingEnvironment processingEnv) {

        String ruleName = rule.getName();

        TypeSpec.Builder nestedRuleClassBuilder = TypeSpec
                .classBuilder("Rule_" + ruleNumber)
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
            else if (child.getBackground().isPresent()) {

                Background background = child.getBackground().get();

                AptMessageUtils.message("Processing rule background: " + background.getName(), processingEnv);
                MethodSpec.Builder ruleBackgroundMethodBuilder =
                        BackgroundProcessor.processRuleBackground(ruleNumber, background, classBuilder);

                MethodSpec backgroundMethod = ruleBackgroundMethodBuilder.build();
                nestedRuleClassBuilder.addMethod(backgroundMethod);
            }
            else {
                throw new IllegalArgumentException("Unsupported rule child type: " + child);
            }
        }

        TypeSpec nestedRuleClassSpec = nestedRuleClassBuilder.build();
        classBuilder.addType(nestedRuleClassSpec);
    }

}

package dev.spec2test.feature2junit.gherkin;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import dev.spec2test.common.MessageSupport;
import dev.spec2test.common.ProcessingException;
import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.RuleChild;
import io.cucumber.messages.types.Scenario;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;

@RequiredArgsConstructor
public class RuleProcessor implements MessageSupport {

    @Getter
    private final ProcessingEnvironment processingEnv;

    void processRule(int ruleNumber, Rule rule, TypeSpec.Builder classBuilder) {

        String ruleName = rule.getName();

        TypeSpec.Builder nestedRuleClassBuilder = TypeSpec
                .classBuilder("Rule_" + ruleNumber)
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
                ScenarioProcessor scenarioProcessor = new ScenarioProcessor(processingEnv);
                MethodSpec.Builder scenarioMethodBuilder = scenarioProcessor.processScenario(ruleScenarioNumber, scenario, classBuilder);

                MethodSpec scenarioMethod = scenarioMethodBuilder.build();
                nestedRuleClassBuilder.addMethod(scenarioMethod);
            }
            else if (child.getBackground().isPresent()) {

                Background background = child.getBackground().get();

                BackgroundProcessor backgroundProcessor = new BackgroundProcessor(processingEnv);
                MethodSpec.Builder ruleBackgroundMethodBuilder = backgroundProcessor.processRuleBackground(background, classBuilder);

                MethodSpec backgroundMethod = ruleBackgroundMethodBuilder.build();
                nestedRuleClassBuilder.addMethod(backgroundMethod);
            }
            else {
                throw new ProcessingException("Unsupported rule child type: " + child);
            }
        }

        TypeSpec nestedRuleClassSpec = nestedRuleClassBuilder.build();
        classBuilder.addType(nestedRuleClassSpec);
    }

}

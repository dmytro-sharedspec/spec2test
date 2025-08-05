package dev.spec2test.feature2junit.gherkin;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import dev.spec2test.common.LoggingSupport;
import dev.spec2test.common.ProcessingException;
import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.FeatureChild;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.Scenario;
import java.util.List;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.processing.ProcessingEnvironment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Processes a Gherkin feature and generates corresponding JUnit test methods.
 */
@RequiredArgsConstructor
@NotThreadSafe
public class FeatureProcessor implements LoggingSupport {

    @Getter
    private final ProcessingEnvironment processingEnv;

    /**
     * Processes a Gherkin feature and generates JUnit test methods for its children.
     * @param feature the Gherkin feature to process
     * @param classBuilder the TypeSpec.Builder for the class being generated
     */
    public void processFeature(Feature feature, TypeSpec.Builder classBuilder) {

        List<FeatureChild> children = feature.getChildren();

        int featureRuleCount = 0;
        int featureScenarioCount = 0;

        for (FeatureChild child : children) {

            if (child.getBackground().isPresent()) {

                BackgroundProcessor backgroundProcessor = new BackgroundProcessor(processingEnv);

                Background background = child.getBackground().get();
                MethodSpec.Builder featureBackgroundMethodBuilder = backgroundProcessor.processFeatureBackground(background, classBuilder);

                MethodSpec backgroundMethod = featureBackgroundMethodBuilder.build();
                classBuilder.addMethod(backgroundMethod);
            }
            else if (child.getRule().isPresent()) {

                featureRuleCount++;
                Rule rule = child.getRule().get();
                RuleProcessor ruleProcessor = new RuleProcessor(processingEnv);
                ruleProcessor.processRule(featureRuleCount, rule, classBuilder);
            }
            else if (child.getScenario().isPresent()) {

                Scenario scenario = child.getScenario().get();
                featureScenarioCount++;
                ScenarioProcessor scenarioProcessor = new ScenarioProcessor(processingEnv);
                MethodSpec.Builder scenarioMethodBuilder = scenarioProcessor.processScenario(featureScenarioCount, scenario, classBuilder);

                MethodSpec scenarioMethod = scenarioMethodBuilder.build();
                classBuilder.addMethod(scenarioMethod);
            }
            else {
                throw new ProcessingException("Unsupported child element type for feature: " + child);
            }

        }
    }

}

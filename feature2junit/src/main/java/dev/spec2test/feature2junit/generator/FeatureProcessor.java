package dev.spec2test.feature2junit.generator;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import dev.spec2test.common.fileutils.AptMessageUtils;
import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.FeatureChild;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.Scenario;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;

public class FeatureProcessor {

    static void processFeature(Feature feature, TypeSpec.Builder classBuilder, ProcessingEnvironment processingEnv) {

        //String featureName = feature.getName();
        List<FeatureChild> children = feature.getChildren();

        int featureRuleCount = 0;
        int featureScenarioCount = 0;
        for (FeatureChild child : children) {

            if (child.getBackground().isPresent()) {
                // Process background
                AptMessageUtils.message("Processing background", processingEnv);
                Background background = child.getBackground().get();

                AptMessageUtils.message("Processing background: " + background.getName(), processingEnv);
                MethodSpec.Builder featureBackgroundMethodBuilder = BackgroundProcessor.processBackground(background, classBuilder);

                MethodSpec backgroundMethod = featureBackgroundMethodBuilder.build();
                classBuilder.addMethod(backgroundMethod);
            }
            else if (child.getRule().isPresent()) {
                // Process rule
                featureRuleCount++;
                Rule rule = child.getRule().get();
                AptMessageUtils.message("Processing rule: " + rule.getName(), processingEnv);
                RuleProcessor.processRule(featureRuleCount, rule, classBuilder, processingEnv);
            }
            else if (child.getScenario().isPresent()) {
                // Process feature scenario
                Scenario scenario = child.getScenario().get();
                featureScenarioCount++;
                MethodSpec.Builder scenarioMethodBuilder =
                        ScenarioProcessor.processScenario(featureScenarioCount, scenario, classBuilder);

                List<MethodSpec> methodSpecs = classBuilder.methodSpecs;
                boolean firstMethod = methodSpecs.isEmpty();
                if (firstMethod) {
                    scenarioMethodBuilder.addJavadoc(CodeBlock.of("bla bla bla"));
                }

                MethodSpec scenarioMethod = scenarioMethodBuilder.build();
                classBuilder.addMethod(scenarioMethod);
            }
            else {
                throw new IllegalArgumentException("Unsupported feature child type: " + child);
            }

        }
    }

}

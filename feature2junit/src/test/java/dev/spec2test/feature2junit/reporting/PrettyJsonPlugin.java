package dev.spec2test.feature2junit.reporting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * A Cucumber plugin that generates pretty-printed JSON reports.
 * This plugin creates the same JSON structure as the standard json plugin,
 * but formats it with indentation for better readability.
 *
 * Usage: pretty-json:target/cucumber-report/report.json
 */
public class PrettyJsonPlugin implements ConcurrentEventListener {

    private final String outputPath;
    private final List<FeatureResult> features = new ArrayList<>();
    private FeatureResult currentFeature;
    private ScenarioResult currentScenario;

    public PrettyJsonPlugin(String outputPath) {
        // Remove "pretty-json:" prefix if present
        this.outputPath = outputPath.replace("pretty-json:", "");
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        publisher.registerHandlerFor(TestCaseFinished.class, this::handleTestCaseFinished);
        publisher.registerHandlerFor(TestStepStarted.class, this::handleTestStepStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::handleTestStepFinished);
        publisher.registerHandlerFor(TestRunFinished.class, this::handleTestRunFinished);
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        TestCase testCase = event.getTestCase();
        URI featureUri = testCase.getUri();

        // Find or create feature
        currentFeature = features.stream()
                .filter(f -> f.uri.equals(featureUri.toString()))
                .findFirst()
                .orElseGet(() -> {
                    FeatureResult feature = new FeatureResult();
                    feature.uri = featureUri.toString();
                    feature.name = extractFeatureName(testCase);
                    feature.id = sanitizeId(feature.name);
                    feature.line = testCase.getLocation().getLine();
                    features.add(feature);
                    return feature;
                });

        // Create scenario
        currentScenario = new ScenarioResult();
        currentScenario.name = testCase.getName();
        currentScenario.id = currentFeature.id + ";" + sanitizeId(currentScenario.name);
        currentScenario.line = testCase.getLocation().getLine();
        currentScenario.type = "scenario";
        currentScenario.startTime = event.getInstant().toEpochMilli();

        currentFeature.elements.add(currentScenario);
    }

    private void handleTestCaseFinished(TestCaseFinished event) {
        // Scenario finished
    }

    private void handleTestStepStarted(TestStepStarted event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            PickleStepTestStep pickleStep = (PickleStepTestStep) event.getTestStep();

            StepResult step = new StepResult();
            step.name = pickleStep.getStep().getText();
            step.keyword = pickleStep.getStep().getKeyword();
            step.line = pickleStep.getStep().getLine();

            currentScenario.steps.add(step);
        }
    }

    private void handleTestStepFinished(TestStepFinished event) {
        if (event.getTestStep() instanceof PickleStepTestStep && !currentScenario.steps.isEmpty()) {
            StepResult step = currentScenario.steps.get(currentScenario.steps.size() - 1);
            Result result = event.getResult();

            step.result = new TestResult();
            step.result.status = result.getStatus().toString().toLowerCase();
            step.result.duration = result.getDuration() != null ? result.getDuration().toNanos() : 0;

            if (result.getError() != null) {
                step.result.error_message = result.getError().getMessage();
            }
        }
    }

    private void handleTestRunFinished(TestRunFinished event) {
        writeJsonReport();
    }

    private void writeJsonReport() {
        try {
            Path path = Paths.get(outputPath);
            Files.createDirectories(path.getParent());

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

            // Configure Jackson's default pretty printer for better array formatting
            // Custom separator with no space before colon, but space after
            com.fasterxml.jackson.core.util.Separators customSeparators =
                com.fasterxml.jackson.core.util.Separators.createDefaultInstance()
                    .withObjectFieldValueSpacing(com.fasterxml.jackson.core.util.Separators.Spacing.AFTER);

            com.fasterxml.jackson.core.util.DefaultPrettyPrinter printer =
                new com.fasterxml.jackson.core.util.DefaultPrettyPrinter()
                    .withArrayIndenter(com.fasterxml.jackson.core.util.DefaultIndenter.SYSTEM_LINEFEED_INSTANCE)
                    .withObjectIndenter(com.fasterxml.jackson.core.util.DefaultIndenter.SYSTEM_LINEFEED_INSTANCE)
                    .withSeparators(customSeparators);

            mapper.setDefaultPrettyPrinter(printer);

            try (Writer writer = new FileWriter(path.toFile())) {
                mapper.writeValue(writer, features);
            }
        } catch (IOException e) {
            System.err.println("Failed to write pretty JSON report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String extractFeatureName(TestCase testCase) {
        String name = testCase.getName();
        // Try to extract feature name from test case name
        if (name.contains(".")) {
            return name.substring(0, name.indexOf("."));
        }
        return "Feature";
    }

    private String sanitizeId(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    // Data classes for JSON structure
    static class FeatureResult {
        public String uri;
        public String id;
        public String keyword = "Feature";
        public String name;
        public int line;
        public String description = "";
        public List<ScenarioResult> elements = new ArrayList<>();
    }

    static class ScenarioResult {
        public String id;
        public String keyword = "Scenario";
        public String name;
        public int line;
        public String description = "";
        public String type;
        public List<StepResult> steps = new ArrayList<>();
        public long startTime;
    }

    static class StepResult {
        public String keyword;
        public String name;
        public int line;
        public TestResult result;
    }

    static class TestResult {
        public String status;
        public long duration;
        public String error_message;
    }
}

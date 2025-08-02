package dev.spec2test.feature2junit.gherkin;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import dev.spec2test.common.LoggingSupport;
import dev.spec2test.common.ProcessingException;
import dev.spec2test.feature2junit.gherkin.utils.ParameterNamingUtils;
import dev.spec2test.feature2junit.gherkin.utils.TableUtils;
import io.cucumber.messages.types.DataTable;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.TableCell;
import io.cucumber.messages.types.TableRow;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@RequiredArgsConstructor
public class ScenarioProcessor implements LoggingSupport {

    @Getter
    private final ProcessingEnvironment processingEnv;

    MethodSpec.Builder processScenario(int scenarioNumber, Scenario scenario, TypeSpec.Builder classBuilder) {

        List<MethodSpec> allMethodSpecs = classBuilder.methodSpecs;

        List<Step> scenarioSteps = scenario.getSteps();
        List<MethodSpec> scenarioStepsMethodSpecs = new ArrayList<>(scenarioSteps.size());

        String scenarioMethodName = "scenario_" + scenarioNumber;
        MethodSpec.Builder scenarioMethodBuilder = MethodSpec
                .methodBuilder(scenarioMethodName)
                //                .addParameter(TestInfo.class, "testInfo")
                .addModifiers(Modifier.PUBLIC);

        List<Examples> examples = scenario.getExamples();
        List<String> scenarioParameterNames;
        List<String> testMethodParameterNames;

        if (examples != null && !examples.isEmpty()) {

            scenarioMethodBuilder.addComment("This scenario has examples: " + examples);
            scenarioParameterNames = addJUnitAnnotationsForParameterizedTest(scenarioMethodBuilder, scenario);
            testMethodParameterNames = new ArrayList<>(scenarioParameterNames.size());

            for (String scenarioParameterName : scenarioParameterNames) {
                String methodParameterName = ParameterNamingUtils.toMethodParameterName(scenarioParameterName);
                testMethodParameterNames.add(methodParameterName);
                scenarioMethodBuilder.addParameter(String.class, methodParameterName);
            }
        }
        else {
            scenarioParameterNames = null;
            testMethodParameterNames = null;

            addJUnitAnnotationsForSingleTest(scenarioMethodBuilder, scenario);
        }

        addOrderAnnotation(scenarioMethodBuilder, scenarioNumber);
        addDisplayNameAnnotation(scenarioMethodBuilder, scenario);

        for (Step scenarioStep : scenarioSteps) {

            List<MethodSpec> methodSpecs = classBuilder.methodSpecs;
            String javaDoc = methodSpecs.isEmpty() ? "First method javadoc comment" : null;

            StepProcessor stepProcessor = new StepProcessor(processingEnv);
            MethodSpec stepMethodSpec = stepProcessor.processStep(
                    scenarioStep, scenarioMethodBuilder, scenarioStepsMethodSpecs,
                    scenarioParameterNames, testMethodParameterNames, javaDoc
            );
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

    private void addDisplayNameAnnotation(MethodSpec.Builder scenarioMethodBuilder, Scenario scenario) {

        AnnotationSpec displayNameAnnotation = AnnotationSpec
                .builder(DisplayName.class)
                .addMember("value", "\"Scenario: " + scenario.getName() + "\"")
                .build();
        scenarioMethodBuilder.addAnnotation(displayNameAnnotation);
    }

    private void addOrderAnnotation(MethodSpec.Builder scenarioMethodBuilder, int scenarioNumber) {

        AnnotationSpec orderAnnotation = AnnotationSpec
                .builder(Order.class)
                .addMember("value", "" + scenarioNumber)
                .build();
        scenarioMethodBuilder.addAnnotation(orderAnnotation);
    }

    private void addOrderAnnotation(MethodSpec.Builder scenarioMethodBuilder, Scenario scenario) {

        AnnotationSpec displayNameAnnotation = AnnotationSpec
                .builder(DisplayName.class)
                .addMember("value", "\"Scenario: " + scenario.getName() + "\"")
                .build();
        scenarioMethodBuilder.addAnnotation(displayNameAnnotation);
    }

    private void addJUnitAnnotationsForSingleTest(MethodSpec.Builder scenarioMethodBuilder, Scenario scenario) {

        AnnotationSpec testAnnotation = AnnotationSpec
                .builder(Test.class)
                .build();
        scenarioMethodBuilder.addAnnotation(testAnnotation);
    }

    private List<String> addJUnitAnnotationsForParameterizedTest(
            MethodSpec.Builder scenarioMethodBuilder,
            Scenario scenario) {

        List<Examples> examples = scenario.getExamples();
        if (examples.size() > 1) {
            throw new ProcessingException(
                    "Having more than 1 Examples section for a Scenario Outline is not currently supported, total examples "
                            + examples.size());
        }

        Examples examplesTable = examples.get(0);

        /**
         * convert Examples into data table so that we can format it easily with pipe characters
         */
        TableRow tableHeader = examplesTable.getTableHeader().get();
        List<TableRow> tableBody = examplesTable.getTableBody();
        List<TableRow> allRows = new ArrayList<>(tableBody.size() + 1);
        allRows.add(tableHeader);
        allRows.addAll(tableBody);

        Location examplesTableLocation = examplesTable.getLocation();
        DataTable examplesDataTable = new DataTable(examplesTableLocation, allRows);

        List<Integer> maxColumnLengths = TableUtils.workOutMaxColumnLength(examplesDataTable);

        StringBuilder textBlockSB = new StringBuilder();
        textBlockSB.append("\"\"\"\n");

        for (TableRow row : allRows) {

            List<TableCell> rowCells = row.getCells();
            List<String> paddedCellValues = new ArrayList<>(rowCells.size());

            for (int i = 0; i < rowCells.size(); i++) {
                TableCell cell = rowCells.get(i);
                String value = cell.getValue();
                int maxColumnLength = maxColumnLengths.get(i);
                String paddedValue = StringUtils.rightPad(value, maxColumnLength);
                paddedCellValues.add(paddedValue);
            }

            String rowLine = String.join(" | ", paddedCellValues);
            textBlockSB.append(rowLine + "\n");
        }

        textBlockSB.append("\"\"\"");

        String textBlock = textBlockSB.toString();

        AnnotationSpec parameterizedTestAnnotation = AnnotationSpec
                .builder(ParameterizedTest.class)
                .addMember("name", "\"Example {index}: [{arguments}]\"")
                .build();
        scenarioMethodBuilder
                .addAnnotation(parameterizedTestAnnotation);

        AnnotationSpec csvSourceAnnotation = AnnotationSpec
                .builder(CsvSource.class)
                .addMember("useHeadersInDisplayName", "true")
                .addMember("delimiter", "'|'")
                .addMember("textBlock", textBlock)
                .build();
        scenarioMethodBuilder
                .addAnnotation(csvSourceAnnotation);

        List<String> headerCells = examplesTable.getTableHeader().get().getCells().stream().map(TableCell::getValue).toList();
        return headerCells;
    }

}

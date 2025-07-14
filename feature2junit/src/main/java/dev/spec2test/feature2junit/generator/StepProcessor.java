package dev.spec2test.feature2junit.generator;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.messages.types.DataTable;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.TableCell;
import io.cucumber.messages.types.TableRow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;

public class StepProcessor {

    //    private static Pattern parameterPattern = Pattern.compile("(?<parameter>(\"|\')(.+?)(\"|\'))");
    private static Pattern parameterPattern = Pattern.compile("(?<parameter>(\")([^\"]+?)(\"))");

    private record MethodSignatureAttributes(
            String gwtAnnotationValue,
            String methodName,
            List<String> parameterValues
    ) {

    }

    public static MethodSpec processStep(
            Step step, MethodSpec.Builder scenarioMethodBuilder,
            List<MethodSpec> scenarioStepsMethodSpecs) {

        // feature file location
        long stepLine = step.getLocation().getLine();

        // use only the first line of the step text
        String stepText = step.getKeyword() + " " + step.getText();
        String[] lines = stepText.trim().split("\\n");
        String stepFirstLine = lines[0].trim();

        // create potential new method to add to the test class
        MethodSignatureAttributes stepMethodSignatureAttributes = extractMethodSignature(stepFirstLine);
        String stepMethodName = stepMethodSignatureAttributes.methodName;
        List<String> parameterValues = stepMethodSignatureAttributes.parameterValues;

        MethodSpec.Builder stepMethodBuilder = MethodSpec
                .methodBuilder(stepMethodName)
                .addModifiers(Modifier.PROTECTED, Modifier.ABSTRACT);

        // add GWT annotation to our potential new method
        AnnotationSpec annotationSpec = buildGWTAnnotation(scenarioStepsMethodSpecs,
                stepMethodName,
                stepLine, parameterValues,
                stepMethodSignatureAttributes
        );
        stepMethodBuilder.addAnnotation(annotationSpec);

        for (int j = 0; j < parameterValues.size(); j++) {
            String parameterName = "p" + (j + 1);
            ParameterSpec parameterSpec = ParameterSpec
                    .builder(String.class, parameterName)
                    .build();
            stepMethodBuilder.addParameter(parameterSpec);
        }

        // check if step has a data table
        if (step.getDataTable().isPresent()) {
            String parameterName = "p" + (parameterValues.size()); // data table is the last parameter
            ParameterSpec dataTableParameterSpec = ParameterSpec
                    .builder(io.cucumber.datatable.DataTable.class, parameterName)
                    .build();
            stepMethodBuilder.addParameter(dataTableParameterSpec);
        }

        // add a call to the step method in the scenario method
        addACallToTheStepMethod(scenarioMethodBuilder, stepMethodName, parameterValues, step);

        MethodSpec stepMethodSpec = stepMethodBuilder.build();
        return stepMethodSpec;
    }

    private static void addACallToTheStepMethod(
            MethodSpec.Builder scenarioMethodBuilder,
            String stepMethodName,
            List<String> parameterValues, Step step) {
        /**
         * replace all occurrences of '$' with a '$L' placeholders and replace back with '$'
         */
        StringBuilder methodNameWithPlaceholdersSB = new StringBuilder();
        int searchingFrom = 0;
        int totalDollarSigns = 0;
        int indexOfDollarSign = stepMethodName.indexOf('$', searchingFrom);
        while (indexOfDollarSign > -1) {

            String beforeDollarSign = stepMethodName.substring(searchingFrom, indexOfDollarSign);
            methodNameWithPlaceholdersSB.append(beforeDollarSign);

            methodNameWithPlaceholdersSB.append("$L"); // placeholder for parameter
            totalDollarSigns++;

            searchingFrom = indexOfDollarSign + 1;
            indexOfDollarSign = stepMethodName.indexOf('$', searchingFrom);
        }
        if (searchingFrom < stepMethodName.length()) {
            String afterDollarSign = stepMethodName.substring(searchingFrom);
            methodNameWithPlaceholdersSB.append(afterDollarSign);
        }
        String methodNameWithPlaceholders = methodNameWithPlaceholdersSB.toString();
        String[] formatArgs = new String[totalDollarSigns];
        Arrays.fill(formatArgs, "$");

        // construct parameter values
        StringBuilder parameterValuesSB = new StringBuilder();
        for (int j = 0; j < parameterValues.size(); j++) {
            if (j > 0) {
                parameterValuesSB.append(", ");
            }
            parameterValuesSB.append("\"");
            String parameterValue = parameterValues.get(j);
            parameterValuesSB.append(parameterValue);
            parameterValuesSB.append("\"");
        }

        if (step.getDataTable().isPresent()) {

            if (!parameterValues.isEmpty()) {
                parameterValuesSB.append(", ");
            }

            DataTable dataTableMsg = step.getDataTable().get();
            List<Integer> maxColumnLength = workOutMaxColumnLength(dataTableMsg);
            String dataTableAsString = convertDataTableToString(dataTableMsg, maxColumnLength);

            parameterValuesSB.append("createDataTable(");
            parameterValuesSB.append("\"\"\"\n");
            parameterValuesSB.append(dataTableAsString);
//            parameterValuesSB.append("|column1|column2|\n");
//            parameterValuesSB.append("|value1 |value2 |\n");
            parameterValuesSB.append("\n\"\"\"");
            parameterValuesSB.append(")");
        }

        String parameterValuesPart = parameterValuesSB.toString();
        CodeBlock codeBlock =
                CodeBlock.of(methodNameWithPlaceholders + "(" + parameterValuesPart + ")", (Object[]) formatArgs);

        scenarioMethodBuilder.addStatement(codeBlock);
    }

    private static List<Integer> workOutMaxColumnLength(DataTable dataTableMsg) {

        List<TableRow> rows = dataTableMsg.getRows();
        List<Integer> maxColumnLength = new ArrayList<>();

        for (TableRow row : rows) {
            List<TableCell> cells = row.getCells();
            for (int i = 0; i < cells.size(); i++) {

                TableCell cellValue = cells.get(i);
                String cellText = cellValue.getValue();

                if (maxColumnLength.size() <= i) {
                    maxColumnLength.add(cellText.length());
                }
                else {
                    int currentMaxLength = maxColumnLength.get(i);
                    if (cellText.length() > currentMaxLength) {
                        maxColumnLength.set(i, cellText.length());
                    }
                }
            }
        }

        return maxColumnLength;
    }

    private static String convertDataTableToString(DataTable dataTableMsg, List<Integer> maxColumnLength) {

        StringBuilder sb = new StringBuilder();

        List<TableRow> rows = dataTableMsg.getRows();

        for (int i = 0; i < rows.size(); i++) {

            TableRow row = rows.get(i);
            List<TableCell> cells = row.getCells();

            sb.append("|");

            for (int columnIndex = 0; columnIndex < cells.size(); columnIndex++) {

                TableCell cellValue = cells.get(columnIndex);
                String value = cellValue.getValue();
                sb.append(value);
                boolean needToPad = columnIndex < maxColumnLength.size()
                        && maxColumnLength.get(columnIndex) > value.length();
                if (needToPad) {
                    int paddingLength = maxColumnLength.get(columnIndex) - value.length();
                    String padding = StringUtils.repeat(" ", paddingLength);
                    sb.append(padding);
                }
                sb.append("|");
            }

            if (i < rows.size() - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private static AnnotationSpec buildGWTAnnotation(
            List<MethodSpec> scenarioStepsMethodSpecs,
            String stepMethodName,
            long stepLine,
            List<String> parameterValues,
            MethodSignatureAttributes signatureAttributes) {
        AnnotationSpec.Builder annotationSpecBuilder;
        if (stepMethodName.startsWith("given")) {
            annotationSpecBuilder = AnnotationSpec.builder(Given.class);
        }
        else if (stepMethodName.startsWith("when")) {
            annotationSpecBuilder = AnnotationSpec.builder(When.class);
        }
        else if (stepMethodName.startsWith("then")) {
            annotationSpecBuilder = AnnotationSpec.builder(Then.class);
        }
        else if (stepMethodName.startsWith("and") || stepMethodName.startsWith("but")) {
            // 'And' is a special case, which is worked out using previous non And step keyword
            if (scenarioStepsMethodSpecs.isEmpty()) {
                throw new IllegalArgumentException(
                        "Step on line - " + stepLine
                                + " starts with 'And', but there are no previous scenario steps defined");
            }
            MethodSpec lastScenarioMethodSpec = scenarioStepsMethodSpecs.get(scenarioStepsMethodSpecs.size() - 1);

            List<AnnotationSpec> methodAnnotationSpecs = lastScenarioMethodSpec.annotations;
            Class<?> gwtAnnotation = null;
            for (AnnotationSpec methodAnnotationSpec : methodAnnotationSpecs) {
                String annotationName = methodAnnotationSpec.type.toString();
                if (annotationName.equals(Given.class.getName())) {
                    gwtAnnotation = Given.class;
                    break;
                }
                else if (annotationName.equals(When.class.getName())) {
                    gwtAnnotation = When.class;
                    break;
                }
                else if (annotationName.equals(Then.class.getName())) {
                    gwtAnnotation = Then.class;
                    break;
                }
                else {
                    continue; // skip
                }
            }
            if (gwtAnnotation == null) {
                throw new IllegalArgumentException(
                        "Step on line - " + stepLine
                                + " starts with 'And', but there are no previous scenario steps defined that have a step annotation");
            }

            annotationSpecBuilder = AnnotationSpec.builder(gwtAnnotation);

        }
        else {
            throw new IllegalArgumentException(
                    "Step method name does not start with a valid keyword (Given, When, Then, And): "
                            + stepMethodName);
        }

        String[] args = new String[parameterValues.size()];
        for (int j = 0; j < parameterValues.size(); j++) {
            args[j] = "$p" + (j + 1);
        }

        String annotationValueWithMarkers = signatureAttributes.gwtAnnotationValue;
        String[] words = annotationValueWithMarkers.split("\\s+");
        String[] stepTitleWords = Arrays.copyOfRange(words, 1, words.length); // trim the keyword
        String stepAnnotationValueTrimmed = StringUtils.join(stepTitleWords, " ");
        annotationSpecBuilder.addMember("value", "\"" + stepAnnotationValueTrimmed + "\"", (Object[]) args);
        AnnotationSpec annotationSpec = annotationSpecBuilder.build();

        return annotationSpec;
    }

    private static MethodSignatureAttributes extractMethodSignature(String stepFirstLine) {

        StringBuilder gwtAnnotationValueSB = new StringBuilder();
        StringBuilder methodNameSB = new StringBuilder();

        int lastParameterEnd = 0;

        List<String> parameterValues = new ArrayList<>();

        Matcher matcher = parameterPattern.matcher(stepFirstLine);

        while (matcher.find()) {

            int parameterStart = matcher.start("parameter");
            int parameterEnd = matcher.end("parameter");

            int searchStartPos = lastParameterEnd;
            if (searchStartPos < parameterStart) {
                String before = stepFirstLine.substring(searchStartPos, parameterStart);
                gwtAnnotationValueSB.append(before);
                methodNameSB.append(before);
            }

//                String parameterMarker = "$parameter" + parameterValues.size();
            String parameterMarker = "$L";
            gwtAnnotationValueSB.append(parameterMarker);
            methodNameSB.append("$P" + (parameterValues.size() + 1));

            String parameterValue = matcher.group("parameter");
            parameterValue = parameterValue.substring(1, parameterValue.length() - 1); // Remove the quotes

            parameterValues.add(parameterValue);

            lastParameterEnd = parameterEnd;
        }

        if (lastParameterEnd < stepFirstLine.length()) {
            // There is some text after the last parameter
            String after = stepFirstLine.substring(lastParameterEnd);
            gwtAnnotationValueSB.append(after);
            methodNameSB.append(after);
        }

        String firstLineWithParameterMarkersForMethodName = methodNameSB.toString();
        String stepMethodName = MethodNamingUtils.getStepMethodName(firstLineWithParameterMarkersForMethodName);

        MethodSignatureAttributes signatureAttributes = new MethodSignatureAttributes(
                gwtAnnotationValueSB.toString(),
                stepMethodName,
                parameterValues
        );
        return signatureAttributes;
    }

}
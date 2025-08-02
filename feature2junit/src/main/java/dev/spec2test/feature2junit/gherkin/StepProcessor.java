package dev.spec2test.feature2junit.gherkin;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import dev.spec2test.feature2junit.MessageSupport;
import dev.spec2test.feature2junit.gherkin.utils.MethodNamingUtils;
import dev.spec2test.feature2junit.gherkin.utils.TableUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.messages.types.DataTable;
import io.cucumber.messages.types.Step;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
public class StepProcessor implements MessageSupport {

    @Getter
    private final ProcessingEnvironment processingEnv;

    //        private static Pattern parameterPattern = Pattern.compile("(?<parameter>(\"|\')(.+?)(\"|\'))");
    private static final Pattern parameterPattern = Pattern.compile("(?<parameter>(\")(?<parameterValue>[^\"]+?)(\"))");

    private record MethodSignatureAttributes(
            String stepPattern,
            String methodName,
            List<String> parameterValues
    ) {

    }

    public MethodSpec processStep(
            Step step, MethodSpec.Builder scenarioMethodBuilder,
            List<MethodSpec> scenarioStepsMethodSpecs) {

        return processStep(step, scenarioMethodBuilder, scenarioStepsMethodSpecs, null, null, null);
    }

    public MethodSpec processStep(
            Step step,
            MethodSpec.Builder scenarioMethodBuilder,
            List<MethodSpec> scenarioStepsMethodSpecs,
            List<String> scenarioParameterNames,
            List<String> testMethodParameterNames,
            String javaDoc
    ) {

        long stepLine = step.getLocation().getLine();

        /**
         * use only the first line of the step text for creating a method name
         */
        String stepText = step.getKeyword() + " " + step.getText();
        String[] lines = stepText.trim().split("\\n");
        String stepFirstLine = lines[0].trim();

        /**
         * create a potential new method to add to the test class
         * it won't be actually added if a method with exactly the same signature already exists
         */
        MethodSignatureAttributes stepMethodSignatureAttributes = extractMethodSignature(stepFirstLine, scenarioParameterNames);
        String stepMethodName = stepMethodSignatureAttributes.methodName;
        MethodSpec.Builder stepMethodBuilder = MethodSpec
                .methodBuilder(stepMethodName)
                .addModifiers(Modifier.PROTECTED, Modifier.ABSTRACT);

        if (javaDoc != null) {
            stepMethodBuilder.addJavadoc(javaDoc);
        }

        AnnotationSpec annotationSpec = buildGWTAnnotation(scenarioStepsMethodSpecs,
                stepMethodName,
                stepLine, stepMethodSignatureAttributes
        );
        stepMethodBuilder.addAnnotation(annotationSpec);
        /**
         * construct our method parameter
         */
        List<String> parameterValues = stepMethodSignatureAttributes.parameterValues;
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
        addACallToTheStepMethod(scenarioMethodBuilder,
                stepMethodName,
                parameterValues,
                step,
                scenarioParameterNames,
                testMethodParameterNames);

        MethodSpec stepMethodSpec = stepMethodBuilder.build();
        return stepMethodSpec;
    }

    private void addACallToTheStepMethod(
            MethodSpec.Builder scenarioMethodBuilder,
            String stepMethodName,
            List<String> parameterValues, Step step,
            List<String> scenarioParameterNames, List<String> testMethodParameterNames
    ) {
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
        String[] formatArgs = new String[totalDollarSigns]; // todo - implement above replacement using regexp
        Arrays.fill(formatArgs, "$");

        /**
         * construct parameter values
         */
        StringBuilder parameterValuesSB = new StringBuilder();
        for (int j = 0; j < parameterValues.size(); j++) {
            if (j > 0) {
                parameterValuesSB.append(", ");
            }
            String parameterValue = parameterValues.get(j);
            /**
             * in case of scenario with Examples section we check if parameter value is actually a reference
             * to a scenario parameter - if so, we replace it with the reference to the Scenario's test method parameter
             */
            String scenarioParameter = getScenarioParameter(parameterValue, scenarioParameterNames, testMethodParameterNames);
            if (scenarioParameter != null) {
                /**
                 * no quote marks in this case as we are passing a reference to a Scenario test method parameter
                 */
                parameterValuesSB.append(scenarioParameter);
            }
            else {
                parameterValuesSB.append("\"");
                parameterValuesSB.append(parameterValue);
                parameterValuesSB.append("\"");
            }
        }

        if (step.getDataTable().isPresent()) {

            if (!parameterValues.isEmpty()) {
                parameterValuesSB.append(", ");
            }

            DataTable dataTableMsg = step.getDataTable().get();
            List<Integer> maxColumnLength = TableUtils.workOutMaxColumnLength(dataTableMsg);
            String dataTableAsString = TableUtils.convertDataTableToString(dataTableMsg, maxColumnLength);

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

    private String getScenarioParameter(
            String parameterValue, List<String> scenarioParameterNames, List<String> testMethodParameterNames) {

        if (scenarioParameterNames == null || scenarioParameterNames.isEmpty()) {
            return null; // no scenario parameters defined
        }

        if (parameterValue.startsWith("<") && parameterValue.endsWith(">") && parameterValue.length() > 2) {

            String valueWithoutBrackets = parameterValue.substring(1, parameterValue.length() - 1);
            int indexOfParameterName = scenarioParameterNames.indexOf(valueWithoutBrackets);
            if (indexOfParameterName > -1) {
                return testMethodParameterNames.get(indexOfParameterName);
            }
        }

        return null; // not a scenario parameter
    }

    private AnnotationSpec buildGWTAnnotation(
            List<MethodSpec> scenarioStepsMethodSpecs,
            String stepMethodName,
            long stepLine,
            MethodSignatureAttributes signatureAttributes) {

        List<String> parameterValues = signatureAttributes.parameterValues;

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

        String stepPattern = signatureAttributes.stepPattern;

        String[] args = new String[parameterValues.size()];
        for (int j = 0; j < parameterValues.size(); j++) {
            args[j] = "$p" + (j + 1);
        }
        String stepPatternWithMarkers =
                stepPattern.replaceAll("\s\\$p[0-9]{1,2}(\s|$)", " \\$L$1");

        String[] words = stepPatternWithMarkers.split("\\s+");
        String[] stepTitleWords = Arrays.copyOfRange(words, 1, words.length); // trim the keyword
        String stepAnnotationValueTrimmed = StringUtils.join(stepTitleWords, " ");

        annotationSpecBuilder.addMember("value", "\"" + stepAnnotationValueTrimmed + "\"", (Object[]) args);
        AnnotationSpec annotationSpec = annotationSpecBuilder.build();

        return annotationSpec;
    }

    private MethodSignatureAttributes extractMethodSignature(
            String stepFirstLine,
            List<String> scenarioParameterNames) {

        List<String> parameterValues = new ArrayList<>();

        String stepPattern = processWithParameterPattern(
                stepFirstLine, parameterPattern, parameterValues);

        if (scenarioParameterNames != null && !scenarioParameterNames.isEmpty()) {
            // process scenario parameters
            String paramsPatternPart = StringUtils.join(scenarioParameterNames, "|");
            Pattern scenarioParametersPattern = Pattern.compile(
//                    "(?<parameter>(<)([^\s>])([^>]*?)(>))"
                    "(?<parameter>(?<parameterValue>(<)(" + paramsPatternPart + ")(>)))"
            );
            stepPattern = processWithParameterPattern(stepPattern,
                    scenarioParametersPattern,
                    parameterValues);
        }

        String stepMethodName = MethodNamingUtils.getStepMethodName(stepPattern);

        MethodSignatureAttributes signatureAttributes = new MethodSignatureAttributes(
                stepPattern,
                stepMethodName,
                parameterValues
        );
        return signatureAttributes;
    }

    private record AnnotationPatternAttributes(
            String stepAnnotationPattern,
            String gwtAnnotationValue
    ) {

    }

    private String processWithParameterPattern(
            String stepFirstLine,
            Pattern parameterPattern,
            List<String> parameterValues) {

        int lastParameterEnd = 0;

        StringBuilder stepAnnotationPatternSB = new StringBuilder();

        Matcher matcher = parameterPattern.matcher(stepFirstLine);

        while (matcher.find()) {

            int parameterStart = matcher.start("parameter");
            int parameterEnd = matcher.end("parameter");

            int searchStartPos = lastParameterEnd;
            if (searchStartPos < parameterStart) {
                String before = stepFirstLine.substring(searchStartPos, parameterStart);
//                gwtAnnotationValueSB.append(before);
                stepAnnotationPatternSB.append(before);
            }

//            String parameterMarker = "$L";
//            gwtAnnotationValueSB.append(parameterMarker);
            stepAnnotationPatternSB.append("$p" + (parameterValues.size() + 1));

            String parameterValue = matcher.group("parameterValue");
//            parameterValue = parameterValue.substring(1, parameterValue.length() - 1); // Remove the quotes

            parameterValues.add(parameterValue);

            lastParameterEnd = parameterEnd;
        }

        if (lastParameterEnd < stepFirstLine.length()) {
            // There is some text after the last parameter
            String after = stepFirstLine.substring(lastParameterEnd);
//            gwtAnnotationValueSB.append(after);
            stepAnnotationPatternSB.append(after);
        }

        String stepAnnotationPattern = stepAnnotationPatternSB.toString();
        return stepAnnotationPattern;
    }

}
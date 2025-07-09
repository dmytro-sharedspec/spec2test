package dev.spec2test.feature2junit.generator;

import com.squareup.javapoet.*;
import dev.spec2test.common.fileutils.AptMessageUtils;
import dev.spec2test.feature2junit.Feature2JUnit;
import dev.spec2test.feature2junit.FeatureFilePath;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.messages.types.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.processing.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NotThreadSafe
class TestSubclassGenerator {

    private final ProcessingEnvironment processingEnv;

    public TestSubclassGenerator(ProcessingEnvironment processingEnv, ProcessingEnvironment env) {
        this.processingEnv = processingEnv;
    }

    JavaFile createTestSubclass(Element annotatedElement, Feature2JUnit targetAnnotation) throws IOException {

        AptMessageUtils.message("Creating a test subclass... ", processingEnv);

        String featureFilePath = targetAnnotation.value();

        CustomGherkinParser gherkinParser = new CustomGherkinParser(processingEnv, processingEnv);

        Feature feature = gherkinParser.parseUsingPath(featureFilePath);

        TypeElement typeElement = (TypeElement) annotatedElement;

        Element enclosingElement = typeElement.getEnclosingElement();
        PackageElement packageElement = enclosingElement instanceof PackageElement ? (PackageElement) enclosingElement : null;
        AptMessageUtils.message("package = " + packageElement.getQualifiedName(), processingEnv);
        String packageName = packageElement.getQualifiedName().toString();

        TypeSpec typeSpec;
        try {
            typeSpec = generateClass(typeElement, featureFilePath, feature);
        } catch (Throwable t) {
            AptMessageUtils.messageError("An error occurred while generating test subclass for " + typeElement.getSimpleName() + ": " + t.getMessage(), processingEnv);
            throw new RuntimeException("An error occurred while generating test subclass for " + typeElement.getSimpleName(), t);
        }

        JavaFile javaFile = JavaFile
                .builder(packageName, typeSpec)
                .indent("    ")
                .build();

        return javaFile;
    }

    private TypeSpec generateClass(TypeElement baseClassElement, String featureFilePath, Feature feature) {

        Name baseClassName = baseClassElement.getSimpleName();
        String subclassSimpleName = baseClassName + "Scenarios";

        TypeSpec.Builder classBuilder = TypeSpec
                .classBuilder(subclassSimpleName)
                .superclass(baseClassElement.asType())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        AnnotationSpec featureFilePathAnnotation = AnnotationSpec
                .builder(FeatureFilePath.class)
                .addMember("value", "\"" + featureFilePath + "\"")
                .build();
        classBuilder.addAnnotation(featureFilePathAnnotation);

        AnnotationSpec generatedAnnotation = AnnotationSpec
                .builder(Generated.class)
                .addMember("value", "\"" + Feature2JUnitGenerator.class.getName() + "\"")
                .build();
        classBuilder.addAnnotation(generatedAnnotation);

//        builder.addField(FieldSpec.builder(TypeName.INT, "myField", Modifier.PRIVATE).build());

        List<FeatureChild> children = feature.getChildren();

        int featureRuleCount = 0;
        int featureScenarioCount = 0;
        for (FeatureChild child : children) {

            if (child.getRule().isPresent()) {
                // Process rule
                featureRuleCount++;
                Rule rule = child.getRule().get();
                processRule(featureRuleCount, rule, classBuilder);
//                ScenarioToMethodConverter.processScenario(child.getScenario(), classBuilder);
            } else if (child.getScenario().isPresent()) {
                // Process feature scenario
                Scenario scenario = child.getScenario().get();
                featureScenarioCount++;
                processScenario(featureScenarioCount, scenario, classBuilder);
            } else {
                throw new IllegalArgumentException("Unsupported feature child type: " + child);
            }

        }

        TypeSpec test = classBuilder.build();
        return test;
    }

    private void processRule(int featureRuleCount, Rule rule, TypeSpec.Builder classBuilder) {

        String ruleName = rule.getName();
        AptMessageUtils.message("Processing rule: " + ruleName, processingEnv);

        List<RuleChild> children = rule.getChildren();

        int ruleScenarioCount = 0;

        for (RuleChild child : children) {

            if (child.getScenario().isPresent()) {

                Scenario scenario = child.getScenario().get();



            } else {
                throw new IllegalArgumentException("Unsupported rule child type: " + child);
            }
        }

//        List<Scenario> scenarios = (List<Scenario>) children;
//
//        if (scenarios.isEmpty()) {
//            AptMessageUtils.message("Rule '" + ruleName + "' has no scenarios", processingEnv);
//            return;
//        }
//
//        AnnotationSpec displayNameAnnotation = AnnotationSpec
//                .builder(DisplayName.class)
//                .addMember("value", "\"Rule: " + ruleName + "\"")
//                .build();
//        classBuilder.addAnnotation(displayNameAnnotation);
//
//        for (int i = 0; i < scenarios.size(); i++) {
//            Scenario scenario = scenarios.get(i);
//            processScenario(i, scenario, classBuilder);
//        }
    }

    private static Pattern parameterPattern = Pattern.compile("(?<parameter>\"(.+?)\")");

    private static void processScenario(int i, Scenario scenario, TypeSpec.Builder subclassBuilder) {

        List<MethodSpec> allMethodSpecs = subclassBuilder.methodSpecs;

        List<Step> scenarioSteps = scenario.getSteps();

        List<MethodSpec> scenarioStepsMethodSpecs = new ArrayList<>(scenarioSteps.size());

        MethodSpec.Builder scenarioMethodBuilder;
        {
            AnnotationSpec displayNameAnnotation = AnnotationSpec
                    .builder(DisplayName.class)
                    .addMember("value", "\"Scenario: " + scenario.getName() + "\"")
                    .build();
            AnnotationSpec testAnnotation = AnnotationSpec
                    .builder(Test.class)
                    .build();
            String scenarioMethodName = "scenario_" + (i + 1);
            scenarioMethodBuilder = MethodSpec
                    .methodBuilder(scenarioMethodName)
                    .addAnnotation(testAnnotation)
                    .addAnnotation(displayNameAnnotation)
                    .addModifiers(Modifier.PUBLIC);
        }

        for (Step scenarioStep : scenarioSteps) {

            String stepText = scenarioStep.getKeyword() + " " + scenarioStep.getText();
            String[] lines = stepText.split("\\n");
            String stepFirstLine = lines[0].trim();

            List<String> parameterValues = new ArrayList<>();

            StringBuilder annotationValueSB = new StringBuilder();
            StringBuilder methodNameSB = new StringBuilder();
            Matcher matcher = parameterPattern.matcher(stepFirstLine);
            int lastParameterEnd = 0;
            while (matcher.find()) {
                int parameterStart = matcher.start("parameter");
                int parameterEnd = matcher.end("parameter");

                int searchStartPos = lastParameterEnd;
                if (searchStartPos < parameterStart) {
                    String before = stepFirstLine.substring(searchStartPos, parameterStart);
                    annotationValueSB.append(before);
                    methodNameSB.append(before);
                }

//                String parameterMarker = "$parameter" + parameterValues.size();
                String parameterMarker = "$L";
                annotationValueSB.append(parameterMarker);
                methodNameSB.append("$P" + (parameterValues.size() + 1));

                String parameterValue = matcher.group("parameter");
                parameterValue = parameterValue.substring(1, parameterValue.length() - 1); // Remove the quotes

                parameterValues.add(parameterValue);

                lastParameterEnd = parameterEnd;
            }

            if (lastParameterEnd < stepFirstLine.length()) {
                // There is some text after the last parameter
                String after = stepFirstLine.substring(lastParameterEnd);
                annotationValueSB.append(after);
                methodNameSB.append(after);
            }

            String firstLineWithParameterMarkersForMethodName = methodNameSB.toString();

            String stepMethodName = getStepMethodName(firstLineWithParameterMarkersForMethodName);

            AnnotationSpec.Builder annotationSpecBuilder;
            if (stepMethodName.startsWith("given")) {
                annotationSpecBuilder = AnnotationSpec.builder(Given.class);
            } else if (stepMethodName.startsWith("when")) {
                annotationSpecBuilder = AnnotationSpec.builder(When.class);
            } else if (stepMethodName.startsWith("then")) {
                annotationSpecBuilder = AnnotationSpec.builder(Then.class);
            } else if (stepMethodName.startsWith("and")) {
                // 'And' is a special case, which is worked out using previous non And step keyword
                if (scenarioStepsMethodSpecs.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Step method name starts with 'And', but there are no previous scenario steps defined: " + stepMethodName);
                }
                MethodSpec lastScenarioMethodSpec = scenarioStepsMethodSpecs.get(scenarioStepsMethodSpecs.size() - 1);
                List<AnnotationSpec> methodAnnotationSpecs = lastScenarioMethodSpec.annotations;
                Class<?> gwtAnnotation = null;
                for (AnnotationSpec methodAnnotationSpec : methodAnnotationSpecs) {
                    String annotationName = methodAnnotationSpec.type.toString();
                    if (annotationName.equals(Given.class.getName())) {
                        gwtAnnotation = Given.class;
                        break;
                    } else if (annotationName.equals(When.class.getName())) {
                        gwtAnnotation = When.class;
                        break;
                    } else if (annotationName.equals(Then.class.getName())) {
                        gwtAnnotation = Then.class;
                        break;
                    } else {
                        continue; // skip
                    }
                }
                if (gwtAnnotation == null) {
                    throw new IllegalArgumentException(
                            "Step method name starts with 'And', but there are no previous scenario steps defined that have a step annotation: " + stepMethodName);
                }

                annotationSpecBuilder = AnnotationSpec.builder(gwtAnnotation);

            } else {
                throw new IllegalArgumentException(
                        "Step method name does not start with a valid keyword (Given, When, Then, And): " + stepMethodName);
            }

            String[] args = new String[parameterValues.size()];
            for (int j = 0; j < parameterValues.size(); j++) {
                args[j] = "$p" + (j + 1);
            }
            String annotationValueWithMarkers = annotationValueSB.toString();
            String[] words = annotationValueWithMarkers.split("\\s+");
            String[] stepTitleWords = Arrays.copyOfRange(words, 1, words.length); // trim the keyword
            String stepAnnotationValueTrimmed = StringUtils.join(stepTitleWords, " ");
            annotationSpecBuilder.addMember("value", "\"" + stepAnnotationValueTrimmed + "\"", (Object[]) args);
            AnnotationSpec annotationSpec = annotationSpecBuilder.build();

            MethodSpec.Builder stepMethodBuilder = MethodSpec
                    .methodBuilder(stepMethodName)
                    .addAnnotation(annotationSpec)
                    .addModifiers(Modifier.PROTECTED, Modifier.ABSTRACT);

            for (int j = 0; j < parameterValues.size(); j++) {
                String parameterName = "p" + (j + 1);
                ParameterSpec parameterSpec = ParameterSpec
                        .builder(String.class, parameterName)
                        .build();
                stepMethodBuilder.addParameter(parameterSpec);
            }

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
            String parameterValuesPart = parameterValuesSB.toString();

            CodeBlock codeBlock = CodeBlock.of(methodNameWithPlaceholders + "(" + parameterValuesPart + ")", (Object[]) formatArgs);

            scenarioMethodBuilder.addStatement(codeBlock);
//            scenarioMethodBuilder
//                    .addStatement(stepMethodName + "(\"bla1\", \"bla2\")");

            MethodSpec stepMethodSpec = stepMethodBuilder.build();

            MethodSpec existingMethodSpec = allMethodSpecs.stream().filter(methodSpec -> methodSpec.name.equals(stepMethodName))
                    .findFirst()
                    .orElse(null);

            if (existingMethodSpec != null) {
                // If the method already exists, we can skip creating it again
                scenarioStepsMethodSpecs.add(existingMethodSpec);
                continue;
            } else {

                scenarioStepsMethodSpecs.add(stepMethodSpec);
                subclassBuilder.addMethod(stepMethodSpec);
            }

        }

        MethodSpec scenarioMethod = scenarioMethodBuilder.build();
        subclassBuilder.addMethod(scenarioMethod);
    }

    private static String getStepMethodName(String stepFirstLine) {

        StringBuilder methodNameBuilder = new StringBuilder();

        String[] words = stepFirstLine.split("\\s+");
        for (int i = 0; i < words.length; i++) {

            String word = words[i];

            // Remove invalid characters
            StringBuilder sanitizedWordBuilder = new StringBuilder();
            for (char c : word.toCharArray()) {

                if (sanitizedWordBuilder.length() == 0) {
                    // nothing added yet - so check if char is suitable as a starting char
                    if (Character.isJavaIdentifierStart(c)) {
                        char wordFirstChar;
                        if (methodNameBuilder.length() == 0) {
                            // first word in method name - so use lower case
                            wordFirstChar = Character.toLowerCase(c);
                        } else {
                            // not the first word - so use upper case
                            wordFirstChar = Character.toUpperCase(c);
                        }
                        sanitizedWordBuilder.append(wordFirstChar);
                    } else {
                        // skip
                    }

                } else {

                    if (Character.isJavaIdentifierPart(c)) {
                        // always convert to lower case
                        char charInLowerCase = Character.toLowerCase(c);
                        sanitizedWordBuilder.append(charInLowerCase);
                    } else {
                        // skip
                    }
                }

            }

            String sanitizedWord = sanitizedWordBuilder.toString();
            methodNameBuilder.append(sanitizedWord);

        }

        String sanitizedMethodName = methodNameBuilder.toString();
        return sanitizedMethodName;
    }

}
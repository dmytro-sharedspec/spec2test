package dev.spec2test.feature2junit.gherkin.utils;

import com.squareup.javapoet.MethodSpec;
import dev.spec2test.common.ProcessingException;

import java.util.List;

/**
 * Utility class for generating method names from Gherkin step definitions.
 */
public class MethodNamingUtils {

    /**
     * Generates a method name from the first line of a Gherkin step definition.
     *
     * @param stepFirstLine            the first line of the Gherkin step definition
     * @param scenarioStepsMethodSpecs a list of MethodSpec objects representing the scenario steps
     * @return a sanitized method name suitable for use in Java code
     */
    public static String getStepMethodName(String stepFirstLine, List<MethodSpec> scenarioStepsMethodSpecs) {

        StringBuilder methodNameBuilder = new StringBuilder();

        String[] words = stepFirstLine.split("\\s+");
        for (int i = 0; i < words.length; i++) {

            String word = words[i];

            if (i == 0 && word.equalsIgnoreCase("and") || word.equalsIgnoreCase("but")) {
                word = getPreviousGWTStepWord(stepFirstLine, scenarioStepsMethodSpecs);
            }

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

    private static String getPreviousGWTStepWord(String stepFirstLine, List<MethodSpec> scenarioStepsMethodSpecs) {

        /**
         * need to replace the 'And' or 'But' keywords with one from GWT as those are just aliases
         */
        if (scenarioStepsMethodSpecs.isEmpty()) {
            throw new ProcessingException(
                    "Step's first line - '" + stepFirstLine
                            + "' starts with 'And', but there are no previous scenario steps defined");
        }

        MethodSpec lastScenarioMethodSpec = scenarioStepsMethodSpecs.get(scenarioStepsMethodSpecs.size() - 1);
        String lastMethodName = lastScenarioMethodSpec.name;
        if (lastMethodName.startsWith("given")) {
            return "given";
        } else if (lastMethodName.startsWith("when")) {
            return "when";
        } else if (lastMethodName.startsWith("then")) {
            return "then";
        } else {
            throw new ProcessingException(
                    "Step's first line - '" + stepFirstLine
                            + "' starts with 'And', but the previous step is not a GWT step");
        }
    }
}

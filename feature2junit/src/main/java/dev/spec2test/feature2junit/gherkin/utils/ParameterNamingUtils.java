package dev.spec2test.feature2junit.gherkin.utils;

/**
 * Utility class for generating method parameter names from Gherkin scenario parameters.
 */
public class ParameterNamingUtils {

    /**
     * Generates a method parameter name from a Gherkin scenario parameter.
     * @param scenarioParameter the scenario parameter to convert
     * @return a sanitized method parameter name suitable for use in Java code
     */
    public static String toMethodParameterName(String scenarioParameter) {

        StringBuilder parameterNameBuilder = new StringBuilder();

        String[] words = scenarioParameter.split("\\s+");
        for (int i = 0; i < words.length; i++) {

            String word = words[i];

            // Remove invalid characters
            StringBuilder sanitizedWordBuilder = new StringBuilder();
            for (char c : word.toCharArray()) {

                if (sanitizedWordBuilder.length() == 0) {
                    // nothing added yet - so check if char is suitable as a starting char
                    if (Character.isJavaIdentifierStart(c)) {
                        char wordFirstChar;
                        if (parameterNameBuilder.length() == 0) {
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
            parameterNameBuilder.append(sanitizedWord);

        }

        String sanitizedMethodName = parameterNameBuilder.toString();
        return sanitizedMethodName;
    }
}

package dev.spec2test.feature2junit.gherkin.naming;

public class MethodNamingUtils {

    public static String getStepMethodName(String stepFirstLine) {

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

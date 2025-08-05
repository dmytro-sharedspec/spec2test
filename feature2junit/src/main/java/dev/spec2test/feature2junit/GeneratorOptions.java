package dev.spec2test.feature2junit;

import javax.annotation.processing.ProcessingEnvironment;

/**
 * Enum representing various generator options that can be set via the processing environment.
 */
public enum GeneratorOptions {

    /**
     * If set to true, the generator will add {@link dev.spec2test.common.SourceLine} annotation to test methods and
     * nested test classes.
     */
    addSourceLineAnnotations,

    /**
     * If set to true, the generator will add source location into java comment just before a call to each step method
     */
    addSourceLineBeforeStepCalls;

    /**
     * Checks if the specified generator option is set in the processing environment.
     * @param processingEnv the processing environment to check for the option
     * @return true if the option is set, false otherwise
     */
    public boolean isSet(ProcessingEnvironment processingEnv) {

        String optionName = this.name();
        String optionValue = processingEnv.getOptions().get(optionName);

        if (optionValue != null) {
            optionValue = optionValue.trim().toLowerCase();
            return "true".equals(optionValue) || "yes".equals(optionValue);
        }

        return false;
    };

}

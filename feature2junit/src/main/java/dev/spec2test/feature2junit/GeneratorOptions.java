package dev.spec2test.feature2junit;

import javax.annotation.processing.ProcessingEnvironment;

public enum GeneratorOptions {

    addSourceLineAnnotations,

    addSourceLineBeforeStepCalls;

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

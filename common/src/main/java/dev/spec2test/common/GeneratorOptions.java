package dev.spec2test.common;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Options for the generator that can be used to customize the generated test classes.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class GeneratorOptions {

    /**
     * If set to true, the generated test class will be abstract, and it will contain abstract method declarations for
     * that would be required to run the Feature file test.
     */
    private final boolean shouldBeAbstract;

    /**
     * Suffix that will be used for the name of the generated test class.
     */
    private final String classSuffix;

    /**
     * If set to true, the generator will add {@link dev.spec2test.common.SourceLine} annotation to test methods and
     * nested test classes containing line numbers where these elements appear in the Feature file.
     */
    private final boolean addSourceLineAnnotations;

    /**
     * If set to true, the generator will add source location as a java comment just before a call to each step method
     * inside the test methods.
     */
    private final boolean addSourceLineBeforeStepCalls;

    /**
     * @return default generator options.
     */
    public static GeneratorOptions defaultOptions() {
        return GeneratorOptions.builder()
                .shouldBeAbstract(false)
                .addSourceLineAnnotations(false)
                .addSourceLineBeforeStepCalls(false)
                .classSuffix("Test")
                .build();
    }
}

package dev.spec2test.common;

import lombok.Getter;

/**
 * Options for the generator that can be used to customize the generated test classes.
 */
@Getter
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
     * If set to true, the generator will add a call to a failing JUnit assumption for scenarios that have no steps.
     */
    private final boolean failScenariosWithNoSteps;

    /**
     * If set to true, the generator will add a failing test method for rules that have no scenarios.
     */
    private final boolean failRulesWithNoScenarios;

    /**
     * The value for JUnit's @{@link org.junit.jupiter.api.Tag} annotation that will be added to scenarios that do not
     * contain any steps. If an empty or blank value is specified, no tag will be added.
     */
    private final String tagForScenariosWithNoSteps;

    /**
     * The value for JUnit's @{@link org.junit.jupiter.api.Tag} annotation that will be added to failing test method
     * that was added for rules that do not contain any scenarios.
     * If an empty or blank value is specified, no tag will be added.
     */
    private final String tagForRulesWithNoScenarios;

    public GeneratorOptions() {
        this.shouldBeAbstract = false;
        this.classSuffix = "Test";
        this.addSourceLineAnnotations = false;
        this.addSourceLineBeforeStepCalls = false;
        this.failScenariosWithNoSteps = true;
        this.failRulesWithNoScenarios = true;
        this.tagForScenariosWithNoSteps = "new";
        this.tagForRulesWithNoScenarios = "new";
    }

    public GeneratorOptions(boolean shouldBeAbstract,
                            String classSuffix,
                            boolean addSourceLineAnnotations,
                            boolean addSourceLineBeforeStepCalls,
                            boolean failScenariosWithNoSteps,
                            boolean failRulesWithNoScenarios,
                            String tagForScenariosWithNoSteps,
                            String tagForRulesWithNoScenarios) {
        this.shouldBeAbstract = shouldBeAbstract;
        this.classSuffix = classSuffix;
        this.addSourceLineAnnotations = addSourceLineAnnotations;
        this.addSourceLineBeforeStepCalls = addSourceLineBeforeStepCalls;
        this.failScenariosWithNoSteps = failScenariosWithNoSteps;
        this.failRulesWithNoScenarios = failRulesWithNoScenarios;
        this.tagForScenariosWithNoSteps = tagForScenariosWithNoSteps;
        this.tagForRulesWithNoScenarios = tagForRulesWithNoScenarios;
    }

}

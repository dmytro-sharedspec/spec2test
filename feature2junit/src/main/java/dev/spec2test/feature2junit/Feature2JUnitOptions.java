package dev.spec2test.feature2junit;

import java.lang.annotation.*;

/**
 * Annotation to specify options for when generating a test for classes annotated with {@link Feature2JUnit}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Inherited
public @interface Feature2JUnitOptions {

    /**
     * If set to true, the generator will generated test class will be abstract and will include abstract method
     * signatures for all methods required for the feature file to run. Implementing subclasses will then need to
     * implement these methods.
     *
     * @return true if the generated test class should be abstract, false otherwise
     */
    boolean shouldBeAbstract() default false;

    /**
     * Suffix that will be used for the name of the generated test class.
     *
     * @return the suffix for the generated test class name
     */
    String classSuffix() default "Test";

    /**
     * If set to true, the generator will add {@link dev.spec2test.common.SourceLine} annotation to test methods and
     * nested test classes containing line numbers where these elements appear in the Feature file.
     *
     * @return true if source line annotations should be added, false otherwise
     */
    boolean addSourceLineAnnotations() default false;

    /**
     * If set to true, the generator will add source location as a java comment just before a call to each step method
     * inside the test methods.
     *
     * @return true if source line comments should be added before step calls, false otherwise
     */
    boolean addSourceLineBeforeStepCalls() default false;

    /**
     * If set to true, the generator will add a call to a failing JUnit assumption for scenarios that have no steps.
     *
     * @return true if scenarios with no steps should fail, false otherwise
     */
    boolean failScenariosWithNoSteps() default true;

    /**
     * If set to true, the generator will add a failing test method for rules that have no scenarios.
     *
     * @return true if rules with no scenarios should fail, false otherwise
     */
    boolean failRulesWithNoScenarios() default true;

    /**
     * The value for JUnit's @{@link org.junit.jupiter.api.Tag} annotation that will be added to scenarios that do not
     * contain any steps. If an empty or blank value is specified, no tag will be added.
     *
     * @return true if features with no rules should fail, false otherwise
     */
    String tagForScenariosWithNoSteps() default "new";

    /**
     * The value for JUnit's @{@link org.junit.jupiter.api.Tag} annotation that will be added to failing test method
     * that was added for rules that do not contain any scenarios.
     * If an empty or blank value is specified, no tag will be added.
     *
     * @return the tag for rules with no scenarios
     */
    String tagForRulesWithNoScenarios() default "new";
}
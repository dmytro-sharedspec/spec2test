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
     * @return true if the generated test class should be abstract, false otherwise
     */
    boolean shouldBeAbstract() default false;

    /**
     * Suffix that will be used for the name of the generated test class.
     * @return the suffix for the generated test class name
     */
    String classSuffix() default "Test";

    /**
     * If set to true, the generator will add {@link dev.spec2test.common.SourceLine} annotation to test methods and
     * nested test classes containing line numbers where these elements appear in the Feature file.
     * @return true if source line annotations should be added, false otherwise
     */
    boolean addSourceLineAnnotations() default false;

    /**
     * If set to true, the generator will add source location as a java comment just before a call to each step method
     * inside the test methods.
     * @return true if source line comments should be added before step calls, false otherwise
     */
    boolean addSourceLineBeforeStepCalls() default false;
}
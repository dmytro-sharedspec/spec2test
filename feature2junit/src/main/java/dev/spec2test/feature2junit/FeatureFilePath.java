package dev.spec2test.feature2junit;

import java.lang.annotation.*;

/**
 *
 */
@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface FeatureFilePath {

    /**
     * @return the path to the feature file based on which the test was generated.
     */
    String value();
}

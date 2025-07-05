package dev.spec2test.spec2junit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FeatureFilePath {

    /**
     * Path to the feature file based on which the test was generated.
     */
    String value();
}

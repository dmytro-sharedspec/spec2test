package dev.spec2test.feature2junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FeatureFilePath {

    /**
     * @return  the path to the feature file based on which the test was generated.
     */
    String value();
}

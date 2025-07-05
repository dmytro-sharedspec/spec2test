package dev.spec2test.spec2junit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Spec2JTest {

    /**
     * path to feature file
     */
    String value();

//    String featureFilePath();
}

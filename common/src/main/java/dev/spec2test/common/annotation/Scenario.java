package dev.spec2test.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scenario {

    /**
     * @return the title of the scenario.
     */
    String value();

    /**
     * @return the line number in the spec file where the scenario is defined.
     */
    int lineNumber();
}

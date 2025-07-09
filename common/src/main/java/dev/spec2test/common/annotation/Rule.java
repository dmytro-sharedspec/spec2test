package dev.spec2test.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Rule {

    /**
     * @return the title of the rule.
     */
    String value();

    /**
     * @return the line number in the spec file where the rule is defined.
     */
    int lineNumber();
}

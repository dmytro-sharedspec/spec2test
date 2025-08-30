package dev.spec2test.common;

import javax.lang.model.element.TypeElement;

/**
 * Contains supporting methods for generator options.
 */
public interface BaseTypeSupport {

    /**
     * @return generator options.
     */
    TypeElement getBaseType();
}
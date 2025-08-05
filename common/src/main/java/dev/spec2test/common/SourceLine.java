package dev.spec2test.common;

/**
 * Used to annotate generated class element with location of content in the source file that was used to generate
 * the element.
 */
public @interface SourceLine {

    /**
     * The line number in the source file where the content is located.
     * @return the line number
     */
    long value();

    // long column() default 0;

}
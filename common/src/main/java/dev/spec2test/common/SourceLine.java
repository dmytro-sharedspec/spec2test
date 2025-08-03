package dev.spec2test.common;

/**
 * Used to annotate generated class element with location of content in the source file that was used to generate the element.
 */
public @interface SourceLine {

    long value();

//    long line();
//
//    long column() default 0;

}
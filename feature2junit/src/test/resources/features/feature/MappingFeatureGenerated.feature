Feature: Mapping Feature to @Generated annotation
  As a developer
  I want to verify that the @Generated annotation is correctly added to the generated test class
  So that tools can recognize this as generated code and apply appropriate policies

  Rule: @Generated annotation marks the class as generated code
    The @Generated annotation is added to every generated test class.
    The annotation value is always "dev.spec2test.feature2junit.Feature2JUnitGenerator".
    This identifies which annotation processor generated the code.

  Rule: @Generated annotation is from javax.annotation.processing package
    The annotation is javax.annotation.processing.Generated.
    This is the standard Java annotation for marking generated code.
    It is part of the Java annotation processing API.

  Rule: @Generated annotation is added at the class level
    The annotation appears on the generated test class itself.
    It is positioned after @DisplayName and before @TestMethodOrder in the annotation list.
    The annotation does not include comments or date members (only the value member is set).

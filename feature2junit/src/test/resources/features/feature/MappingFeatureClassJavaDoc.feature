Feature: Mapping Feature to class-level JavaDoc
  As a developer
  I want to verify that appropriate JavaDoc instructions are added to the generated test class
  So that users understand how to use the generated code

  Rule: Class JavaDoc contains usage instructions
    A JavaDoc comment is added to the generated test class.
    The JavaDoc explains how to use the generated class.
    The instructions differ based on whether the class is abstract or concrete.

  Rule: Abstract class JavaDoc instructs to extend and implement
    When the shouldBeAbstract option is true, the JavaDoc says:
    "To implement tests in this generated class, extend it and implement all abstract methods."
    This guides users to create a subclass and provide implementations for step methods.

  Rule: Concrete class JavaDoc instructs to move failing methods to base class
    When the shouldBeAbstract option is false, the JavaDoc says:
    "To implement tests in this generated class, move any methods with failing assumptions into the base class and implement them."
    This guides users to override methods in the base class that was annotated with @Feature2JUnit.

  Rule: Class JavaDoc appears before class annotations
    The JavaDoc comment is positioned above all class-level annotations.
    It is the first documentation element on the class.
    It appears before @Tag, @DisplayName, @Generated, etc.

  Rule: Class JavaDoc is always added
    Regardless of the feature content, the usage JavaDoc is always present.
    Every generated test class includes these instructions.
    The JavaDoc helps developers who may be new to the generated code pattern.

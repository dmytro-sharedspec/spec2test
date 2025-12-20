Feature: Mapping Feature to class modifiers
  As a developer
  I want to verify that the correct class modifiers are applied to the generated test class
  So that the class visibility and inheritance behavior matches the configuration

  Rule: Generated class is always public
    Every generated test class has the public modifier.
    This ensures the test class can be accessed by the test framework.
    The public modifier is always present regardless of other options.

  Rule: Generated class extends the annotated base class
    The generated class uses "extends" to inherit from the class annotated with @Feature2JUnit.
    The superclass is set to the TypeElement of the annotated class.
    This establishes the inheritance relationship between generated and user-defined code.

  Rule: Abstract modifier is controlled by shouldBeAbstract option
    When shouldBeAbstract option is true, the class has both public and abstract modifiers.
    When shouldBeAbstract option is false, the class has only the public modifier (concrete class).
    The abstract modifier determines whether step methods are abstract or have default implementations.

  Rule: Abstract class supports Pattern A (extend and implement)
    Abstract classes contain abstract step methods.
    Users must create a concrete subclass to implement the abstract methods.
    This pattern separates the generated structure from user implementation.

  Rule: Concrete class supports Pattern B (override base methods)
    Concrete classes contain step methods that call methods on the base class.
    Users implement step methods in the base class (the one annotated with @Feature2JUnit).
    This pattern keeps all code (generated and user-written) closer together.

  Rule: Class name is derived from annotated class name plus suffix
    The generated class name is: <AnnotatedClassName> + suffix.
    When shouldBeAbstract is true, the suffix is from classSuffixIfAbstract option (default: "Scenarios").
    When shouldBeAbstract is false, the suffix is from classSuffixIfConcrete option (default: "Test").
    For example: CartFeature → CartFeatureScenarios (abstract) or CartFeatureTest (concrete).

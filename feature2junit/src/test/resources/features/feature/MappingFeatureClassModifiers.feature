Feature: Mapping Feature to class modifiers
  As a developer
  I want to verify that the correct class modifiers are applied to the generated test class
  So that the class visibility and inheritance behavior matches the configuration

  Rule: generated class extends the annotated base class

    Scenario: simple case
      Given the following annotated base class:
      """
      package dev.spec2test.feature2junit;

      @Feature2JUnit("test.feature")
      public class CartFeature {
      }
      """
      And the following feature file:
      """
      Feature: shopping cart
        Scenario: add item to cart
          Given a test step
      """
      When the generator is run
      Then the content of the generated class should be:
      """
      import dev.spec2test.feature2junit.FeatureFilePath;
      import javax.annotation.processing.Generated;
      import org.junit.jupiter.api.Assertions;
      import org.junit.jupiter.api.DisplayName;
      import org.junit.jupiter.api.MethodOrderer;
      import org.junit.jupiter.api.Order;
      import org.junit.jupiter.api.Tag;
      import org.junit.jupiter.api.Test;
      import org.junit.jupiter.api.TestMethodOrder;

      /**
       * To implement tests in this generated class, extend it and implement all abstract methods.
       */
      @DisplayName("shopping cart")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @FeatureFilePath("/CartFeature.feature")
      public abstract class CartFeatureScenarios extends CartFeature {
          {
              /**
               * Feature: shopping cart
               */
          }

          @Test
          @Order(1)
          @DisplayName("Scenario: add item to cart")
          @Tag("new")
          public abstract void scenario_1();
      }
      """


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

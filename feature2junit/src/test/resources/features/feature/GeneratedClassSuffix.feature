Feature: setting generated class suffix
  As a developer configuring the code generator for my project
  I want to control the suffix appended to generated test class names
  So that I can maintain consistent naming conventions that match my team's code organization patterns

  Rule: Generated class name is base class name plus suffix

    Scenario: with default suffix
      Given the following base class:
      """
      package com.example.cart;

      @Feature2JUnit("cart.feature")
      public abstract class CartFeature {
      }
      """
      And the following feature file:
      """
      Feature: Shopping Cart
        Scenario: Add item
      """
      When the generator is run
      Then the content of the generated class should be:
      """
      package com.example.cart;

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
       * Feature: Shopping Cart
       */
      @DisplayName("cart")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @FeatureFilePath("cart.feature")
      public abstract class CartFeatureScenarios extends CartFeature {

          @Test
          @Order(1)
          @DisplayName("Scenario: Add item")
          @Tag("new")
          public void scenario_1() {
              Assertions.fail("Scenario has no steps");
          }
      }
      """

    Scenario: with custom suffix
      Given the following base class:
      """
      package com.example.payment;

      @Feature2JUnit("payment.feature")
      @Feature2JUnitOptions(
        classSuffixIfAbstract = "TestCases"
      )
      public class PaymentFeature {
      }
      """
      And the following feature file:
      """
      Feature: Payment Processing
        Scenario: Process payment
      """
      When the generator is run
      Then the content of the generated class should be:
      """
      package com.example.payment;

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
       * Feature: Payment Processing
       */
      @DisplayName("payment")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @FeatureFilePath("payment.feature")
      public abstract class PaymentFeatureTestCases extends PaymentFeature {

          @Test
          @Order(1)
          @DisplayName("Scenario: Process payment")
          @Tag("new")
          public void scenario_1() {
              Assertions.fail("Scenario has no steps");
          }
      }
      """

  Rule: Abstract classes use "Scenarios" suffix by default
    When shouldBeAbstract option is true, the generated class uses "Scenarios" as the default suffix.
    This default can be overridden using the classSuffixIfAbstract configuration option.
    This naming convention signals that the class contains abstract scenario methods to be implemented.

  Rule: Concrete classes use "Test" suffix by default
    When shouldBeAbstract option is false, the generated class uses "Test" as the default suffix.
    This default can be overridden using the classSuffixIfConcrete configuration option.
    This naming convention follows JUnit convention where executable test classes end in "Test".

  Rule: Suffix is configurable via options annotation
    Developers can customize the suffix using @Feature2JUnitOptions annotation.
    The classSuffixIfAbstract parameter controls the suffix for abstract generated classes.
    The classSuffixIfConcrete parameter controls the suffix for concrete generated classes.
    Custom suffixes allow teams to align with their specific naming standards.


Feature: Mapping Feature to @DisplayName annotation
  As a developer
  I want to verify that the @DisplayName annotation is correctly added to the generated test class
  So that test reports show the feature file name as the display name

  Rule: @DisplayName annotation uses the name of the feature file (not the feature title inside the file)

    Scenario: file file is in the root directory
      Given the following base class:
      """
      @Feature2JUnit("ShoppingCart.feature")
      public abstract class FeatureTestBase extends  {

      }
      """
      And the following feature file:
      """
      Feature: managing shopping cart
        As a customer
        I want to manage my shopping cart
        So that I can purchase items
      """

      When the generator is run

      Then the content of the generated class should be:
      """
      import dev.spec2test.feature2junit.FeatureFilePath;
      import javax.annotation.processing.Generated;
      import org.junit.jupiter.api.DisplayName;
      import org.junit.jupiter.api.MethodOrderer;
      import org.junit.jupiter.api.TestMethodOrder;

      /**
       * To implement tests in this generated class, extend it and implement all abstract methods.
       */
      @DisplayName("ShoppingCart")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @FeatureFilePath("ShoppingCart.feature")
      public abstract class MockedAnnotatedTestClassScenarios extends  {
          {
              /**
               * Feature: managing shopping cart
               *   As a customer
               *   I want to manage my shopping cart
               *   So that I can purchase items
               */
          }
      }
      """

    Scenario: feature file is in a subdirectory
      Given the following base class:
      """
      @Feature2JUnit("features/ShoppingCart.feature")
      public abstract class FeatureTestBase extends  {

      }
      """
      And the following feature file:
      """
      Feature: Online Shopping Cart
        As a customer
        I want to manage my shopping cart
        So that I can purchase items
      """

      When the generator is run

      Then the content of the generated class should be:
      """
      import dev.spec2test.feature2junit.FeatureFilePath;
      import javax.annotation.processing.Generated;
      import org.junit.jupiter.api.DisplayName;
      import org.junit.jupiter.api.MethodOrderer;
      import org.junit.jupiter.api.TestMethodOrder;

      /**
       * To implement tests in this generated class, extend it and implement all abstract methods.
       */
      @DisplayName("ShoppingCart")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @FeatureFilePath("features/ShoppingCart.feature")
      public abstract class MockedAnnotatedTestClassScenarios extends  {
          {
              /**
               * Feature: Online Shopping Cart
               *   As a customer
               *   I want to manage my shopping cart
               *   So that I can purchase items
               */
          }
      }
      """

  Rule: @DisplayName annotation is always added to the generated class even if there is no feature section inside the feature file

    Scenario: DisplayName is present even when feature file has no Feature keyword
      Given the following base class:
      """
      @Feature2JUnit("specs/EmptySpec.feature")
      public abstract class FeatureTestBase {

      }
      """
      And the following feature file:
      """
      """

      When the generator is run

      Then the content of the generated class should be:
      """
      import dev.spec2test.feature2junit.FeatureFilePath;
      import javax.annotation.processing.Generated;
      import org.junit.jupiter.api.DisplayName;
      import org.junit.jupiter.api.MethodOrderer;
      import org.junit.jupiter.api.TestMethodOrder;

      /**
       * To implement tests in this generated class, extend it and implement all abstract methods.
       */
      @DisplayName("EmptySpec")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @FeatureFilePath("specs/EmptySpec.feature")
      public abstract class FeatureTestBaseScenarios extends FeatureTestBase {
          {
              /**
               *
               */
          }
      }
      """

  Rule: Feature file path determines the display name value
    The display name is extracted from the feature file path by taking the filename without extension.
    For path "specs/cart.feature", the display name becomes "cart".
    For path "features/user/authentication.feature", the display name becomes "authentication".
    Nested directories are ignored - only the final filename (without .feature extension) is used.
    The feature title (text after "Feature:" keyword) does not affect the @DisplayName value.

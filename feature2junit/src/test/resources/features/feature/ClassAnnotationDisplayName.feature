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
       * Feature: managing shopping cart
       *   As a customer
       *   I want to manage my shopping cart
       *   So that I can purchase items
       */
      @DisplayName("ShoppingCart")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @FeatureFilePath("ShoppingCart.feature")
      public abstract class MockedAnnotatedTestClassScenarios extends FeatureTestBase {
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
       * Feature: Online Shopping Cart
       *   As a customer
       *   I want to manage my shopping cart
       *   So that I can purchase items
       */
      @DisplayName("ShoppingCart")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @FeatureFilePath("features/ShoppingCart.feature")
      public abstract class MockedAnnotatedTestClassScenarios extends FeatureTestBase {
      }
      """

  Rule: @DisplayName annotation is always added to the generated class even if there is no feature section inside the feature file

    Scenario: an empty feature file
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

      @DisplayName("EmptySpec")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @FeatureFilePath("specs/EmptySpec.feature")
      public abstract class MockedAnnotatedTestClassScenarios extends FeatureTestBase {
      }
      """

    Scenario: a feature file with only one rule
      Given the following base class:
      """
      @Feature2JUnit("specs/RuleOnly.feature")
      public abstract class FeatureTestBase {

      }
      """
      And the following feature file:
      """
      Rule: only a rule is present
        This feature file has no Feature section, only a Rule.
      """

      When the generator is run

      Then the content of the generated class should be:
      """
      import dev.spec2test.feature2junit.FeatureFilePath;
      import javax.annotation.processing.Generated;
      import org.junit.jupiter.api.DisplayName;
      import org.junit.jupiter.api.MethodOrderer;
      import org.junit.jupiter.api.TestMethodOrder;

      @DisplayName("RuleOnly")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @FeatureFilePath("specs/RuleOnly.feature")
      public abstract class MockedAnnotatedTestClassScenarios extends FeatureTestBase {
      }
      """

    Scenario: a feature file with only one scenario
      Given the following base class:
        """
        @Feature2JUnit("specs/SingleScenario.feature")
        public abstract class FeatureTestBase {

        }
        """
      And the following feature file:
        """
        Scenario: only a scenario is present
            Given this feature file has no Feature section, only a Scenario.
        """

      When the generator is run

      Then the content of the generated class should be:
        """
        import dev.spec2test.feature2junit.FeatureFilePath;
        import javax.annotation.processing.Generated;
        import org.junit.jupiter.api.DisplayName;
        import org.junit.jupiter.api.MethodOrderer;
        import org.junit.jupiter.api.TestMethodOrder;

        @DisplayName("SingleScenario")
        @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        @FeatureFilePath("specs/SingleScenario.feature")
        public abstract class MockedAnnotatedTestClassScenarios extends FeatureTestBase {
        }
        """

  Rule: feature description (text after "Feature:" keyword) does not affect the @DisplayName value

    Scenario: DisplayName is file name even when feature has different description
      Given the following base class:
      """
      @Feature2JUnit("specs/payment.feature")
      public abstract class FeatureTestBase {

      }
      """
      And the following feature file:
      """
      Feature: Processing Credit Card Payments and Refunds
        As a payment processor
        I want to handle various payment scenarios
        So that customers can complete transactions successfully
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
       * Feature: Processing Credit Card Payments and Refunds
       *   As a payment processor
       *   I want to handle various payment scenarios
       *   So that customers can complete transactions successfully
       */
      @DisplayName("payment")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @FeatureFilePath("specs/payment.feature")
      public abstract class MockedAnnotatedTestClassScenarios extends FeatureTestBase {
      }
      """



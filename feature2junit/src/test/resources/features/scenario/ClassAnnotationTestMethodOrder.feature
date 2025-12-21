Feature: adding @TestMethodOrder annotation
  As a developer maintaining executable specifications
  I want @TestMethodOrder annotation to ensure scenarios execute in their defined sequence
  So that test behavior matches the logical flow documented in the feature file

  Rule: @TestMethodOrder annotation is added to every generated test class if it has at least one scenario

    Scenario: feature with no scenarios
      Given the following feature file:
      """
      Feature: Empty Feature
        This feature has no scenarios yet
      """
      When the generator is run
      Then the content of the generated class should be:
      """
      import dev.spec2test.feature2junit.FeatureFilePath;
      import javax.annotation.processing.Generated;
      import org.junit.jupiter.api.DisplayName;

      /**
       * Feature: Empty Feature
       *   This feature has no scenarios yet
       */
      @DisplayName("MockedAnnotatedTestClass")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @FeatureFilePath("MockedAnnotatedTestClass.feature")
      public abstract class MockedAnnotatedTestClassScenarios extends MockedAnnotatedTestClass {
      }
      """

    Scenario: feature with just one scenario
      Given the following feature file:
      """
      Feature: Test Ordering
        Scenario: First scenario
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
       * Feature: Test Ordering
       */
      @DisplayName("MockedAnnotatedTestClass")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @FeatureFilePath("MockedAnnotatedTestClass.feature")
      public abstract class MockedAnnotatedTestClassScenarios extends MockedAnnotatedTestClass {
          @Test
          @Order(1)
          @DisplayName("Scenario: First scenario")
          @Tag("new")
          public void scenario_1() {
              Assertions.fail("Scenario has no steps");
          }
      }
      """

  Rule: Each scenario method gets @Order(1), @Order(2), etc., matching their position in the feature file.

    Scenario: multiple scenarios get sequential @Order annotations
      Given the following feature file:
      """
      Feature: Sequential Scenarios
        Scenario: First test
        Scenario: Second test
        Scenario: Third test
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
       * Feature: Sequential Scenarios
       */
      @DisplayName("MockedAnnotatedTestClass")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @FeatureFilePath("MockedAnnotatedTestClass.feature")
      public abstract class MockedAnnotatedTestClassScenarios extends MockedAnnotatedTestClass {
          @Test
          @Order(1)
          @DisplayName("Scenario: First test")
          @Tag("new")
          public void scenario_1() {
              Assertions.fail("Scenario has no steps");
          }

          @Test
          @Order(2)
          @DisplayName("Scenario: Second test")
          @Tag("new")
          public void scenario_2() {
              Assertions.fail("Scenario has no steps");
          }

          @Test
          @Order(3)
          @DisplayName("Scenario: Third test")
          @Tag("new")
          public void scenario_3() {
              Assertions.fail("Scenario has no steps");
          }
      }
      """


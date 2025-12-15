Feature: mapping Rule sections
  As a developer
  I want to verify that the content of the "Rule" section is mapped to a nested inner test class
  So that everything under the "Rule" section is grouped together in the generated test code and can be easily run together

  Rule: "Rule:" keyword should be mapped to a block comment in the generated class followed by the feature name
  and description lines verbatim

    Scenario: with just the keyword
      Given the following feature file:
      """
      Feature:
      """
      When the generator is run
      Then the content of generated class should be:
      """
      import dev.spec2test.feature2junit.FeatureFilePath;
      import javax.annotation.processing.Generated;
      import org.junit.jupiter.api.DisplayName;
      import org.junit.jupiter.api.MethodOrderer;
      import org.junit.jupiter.api.TestMethodOrder;

      /**
       * To implement tests in this generated class, extend it and implement all abstract methods.
       */
      @DisplayName("MockedAnnotatedTestClass")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @FeatureFilePath("/MockedAnnotatedTestClass.feature")
      public abstract class MockedAnnotatedTestClassScenarios extends  {
          {
              /**
               * Feature:
               */
          }
      }
      """

    Scenario: with the keyword and name
      Given the following feature file:
      """
      Feature: feature name
      """

      When the generator is run

      Then the content of generated class should be:
      """
      import dev.spec2test.feature2junit.FeatureFilePath;
      import javax.annotation.processing.Generated;
      import org.junit.jupiter.api.DisplayName;
      import org.junit.jupiter.api.MethodOrderer;
      import org.junit.jupiter.api.TestMethodOrder;

      /**
       * To implement tests in this generated class, extend it and implement all abstract methods.
       */
      @DisplayName("MockedAnnotatedTestClass")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @FeatureFilePath("/MockedAnnotatedTestClass.feature")
      public abstract class MockedAnnotatedTestClassScenarios extends  {
          {
              /**
               * Feature: feature name
               */
          }
      }
      """

    Scenario: with the keyword, name and description lines
      Given the following feature file:
      """
      Feature: feature name
        feature description line 1
        feature description line 2
      """

      When the generator is run

      Then the content of generated class should be:
      """
      import dev.spec2test.feature2junit.FeatureFilePath;
      import javax.annotation.processing.Generated;
      import org.junit.jupiter.api.DisplayName;
      import org.junit.jupiter.api.MethodOrderer;
      import org.junit.jupiter.api.TestMethodOrder;

      /**
       * To implement tests in this generated class, extend it and implement all abstract methods.
       */
      @DisplayName("MockedAnnotatedTestClass")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @FeatureFilePath("/MockedAnnotatedTestClass.feature")
      public abstract class MockedAnnotatedTestClassScenarios extends  {
          {
              /**
               * Feature: feature name2
               *   feature description line 1
               *   feature description line 2
               */
          }
      }
      """
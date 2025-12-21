Feature: mapping Feature section
  As a developer
  I want to verify that the content of the "Feature" section is inserted into the javadoc comment of the generated class
  So that I can understand more easily and quickly the purpose of the feature and for whom it is intended

  Rule: "Feature:" keyword should be mapped to a javadoc comment in the generated class followed by the feature name
  and description lines verbatim

    Scenario: with the keyword and name
      Given the following feature file:
      """
      Feature: feature name
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
       * Feature: feature name
       */
      @DisplayName("MockedAnnotatedTestClass")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @FeatureFilePath("/MockedAnnotatedTestClass.feature")
      public abstract class MockedAnnotatedTestClassScenarios extends MockedAnnotatedTestClass {
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

      Then the content of the generated class should be:
      """
      import dev.spec2test.feature2junit.FeatureFilePath;
      import javax.annotation.processing.Generated;
      import org.junit.jupiter.api.DisplayName;
      import org.junit.jupiter.api.MethodOrderer;
      import org.junit.jupiter.api.TestMethodOrder;

      /**
       * Feature: feature name
       *   feature description line 1
       *   feature description line 2
       */
      @DisplayName("MockedAnnotatedTestClass")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @FeatureFilePath("/MockedAnnotatedTestClass.feature")
      public abstract class MockedAnnotatedTestClassScenarios extends MockedAnnotatedTestClass {
      }
      """
Feature: feature keyword mapping

  Rule: test rule

    Scenario: feature keyword mapping
      Given the following base class:
      """
      @Feature2Junit
      public class FeatureBaseClass {
      }
      """
      And the following feature file:
      """
      Feature: feature title
          feature description line 1
          feature description line 2

          Rule: test rule
      """

      When the generator is run

      Then the content of generated class should be:
      """
      import dev.spec2test.feature2junit.FeatureFilePath;
      import javax.annotation.processing.Generated;
      import org.junit.jupiter.api.Assertions;
      import org.junit.jupiter.api.ClassOrderer;
      import org.junit.jupiter.api.DisplayName;
      import org.junit.jupiter.api.MethodOrderer;
      import org.junit.jupiter.api.Nested;
      import org.junit.jupiter.api.Order;
      import org.junit.jupiter.api.Tag;
      import org.junit.jupiter.api.Test;
      import org.junit.jupiter.api.TestClassOrder;
      import org.junit.jupiter.api.TestMethodOrder;

      /**
       * To implement tests in this generated class, extend it and implement all abstract methods.
       */
      @DisplayName("MockedAnnotatedTestClass")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @TestClassOrder(ClassOrderer.OrderAnnotation.class)
      @FeatureFilePath("/MockedAnnotatedTestClass.feature")
      public abstract class MockedAnnotatedTestClassScenarios extends  {
          {
              /**
               * Feature: feature title
               *     feature description line 1
               *     feature description line 2
               */
          }

          @Nested
          @Order(1)
          @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
          @DisplayName("Rule: test rule")
          public class Rule_1 {
              @Test
              @Tag("new")
              public void noScenariosInRule() {
                  Assertions.fail("Rule doesn't have any scenarios");
              }
          }
      }
      """
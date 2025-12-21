Feature: MappingRule
  As a developer
  I want to verify that the content of the "Rule" section is mapped to a nested inner test class
  So that all scenarios under a particular "Rule" can be all easily run together

  Rule: rule section should be mapped to nested inner class and rule name should be mapped to the value in display name annotation

    Scenario: with just the keyword
      Given the following feature file:
      """
      Feature: feature with rule

        Rule:
      """
      When the generator is run
      Then the content of the generated class should be:
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
               * Feature: feature with rule
               */
          }

          @Nested
          @Order(1)
          @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
          @DisplayName("Rule:")
          public class Rule_1 {
              @Test
              @Tag("new")
              public void noScenariosInRule() {
                  Assertions.fail("Rule doesn't have any scenarios");
              }
          }
      }
      """

    Scenario: with the keyword and name
      Given the following feature file:
      """
      Feature: feature with rule

        Rule: rule name
      """
      When the generator is run
      Then the content of the generated class should be:
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
               * Feature: feature with rule
               */
          }

          @Nested
          @Order(1)
          @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
          @DisplayName("Rule: rule name")
          public class Rule_1 {
              @Test
              @Tag("new")
              public void noScenariosInRule() {
                  Assertions.fail("Rule doesn't have any scenarios");
              }
          }
      }
      """

  Rule: rule description lines should be mapped to JavaDoc comment on the nested inner test class

    Scenario: with the keyword, name and description lines
      Given the following feature file:
      """
      Feature: feature with rule
        Rule: rule name
          rule description line 1
          rule description line 2
      """

      When the generator is run

      Then the content of the generated class should be:
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
               * Feature: feature with rule
               */
          }

          /**
           * rule description line 1
           * rule description line 2
           */
          @Nested
          @Order(1)
          @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
          @DisplayName("Rule: rule name")
          public class Rule_1 {
              @Test
              @Tag("new")
              public void noScenariosInRule() {
                  Assertions.fail("Rule doesn't have any scenarios");
              }
          }
      }
      """
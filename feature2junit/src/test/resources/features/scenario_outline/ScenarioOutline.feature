Feature: ScenarioOutline
  As a test developer using Gherkin
  I want Scenario Outlines to be converted to parameterized JUnit tests
  So that I can write data-driven tests with Examples tables

  Rule: Scenario Outline is converted to a method with @ParameterizedTest annotation instead of @Test

    Scenario: A basic scenario outline generates @ParameterizedTest method
      Given the following feature file:
      """
      Feature: Calculator
        Scenario Outline: Adding numbers
          Given I have <a> and <b>
          When I add them
          Then the result is <sum>
          Examples:
            | a | b | sum |
            | 1 | 2 | 3   |
      """
      When the generator is run
      Then the content of the generated class should be:
      """
      import dev.spec2test.feature2junit.FeatureFilePath;
      import io.cucumber.java.en.Given;
      import io.cucumber.java.en.Then;
      import io.cucumber.java.en.When;
      import java.lang.String;
      import javax.annotation.processing.Generated;
      import org.junit.jupiter.api.DisplayName;
      import org.junit.jupiter.api.MethodOrderer;
      import org.junit.jupiter.api.Order;
      import org.junit.jupiter.api.TestMethodOrder;
      import org.junit.jupiter.params.ParameterizedTest;
      import org.junit.jupiter.params.provider.CsvSource;

      /**
       * Feature: Calculator
       */
      @DisplayName("MockedAnnotatedTestClass")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @FeatureFilePath("MockedAnnotatedTestClass.feature")
      public abstract class MockedAnnotatedTestClassScenarios extends MockedAnnotatedTestClass {
          @Given("^I have (?<p1>.*) and (?<p2>.*)$")
          public abstract void givenIHave$p1And$p2(String p1, String p2);

          @When("^I add them$")
          public abstract void whenIAddThem();

          @Then("^the result is (?<p1>.*)$")
          public abstract void thenTheResultIs$p1(String p1);

          @ParameterizedTest(
                  name = "Example {index}: [{arguments}]"
          )
          @CsvSource(
                  useHeadersInDisplayName = true,
                  delimiter = '|',
                  textBlock = \"\"\"
                          a | b | sum
                          1 | 2 | 3
                          \"\"\"
          )
          @Order(1)
          @DisplayName("Scenario: Adding numbers")
          public void scenario_1(String a, String b, String sum) {
              /**
               * Given I have <a> and <b>
               */
              givenIHave$p1And$p2(a, b);
              /**
               * When I add them
               */
              whenIAddThem();
              /**
               * Then the result is <sum>
               */
              thenTheResultIs$p1(sum);
          }
      }
      """

  @forDocumentationOnly
  Rule: Examples table is converted to @CsvSource annotation with pipe-delimited data

  @forDocumentationOnly
  Rule: Examples table column headers become method parameters with String type

  @forDocumentationOnly
  Rule: @ParameterizedTest name format is "Example {index}: [{arguments}]"

  Rule: Examples table is formatted with aligned columns using pipe separators

    Scenario: Different length values are properly aligned
      Given the following feature file:
      """
      Feature: Formatting
        Scenario Outline: Mixed lengths
          Given <shortValue> and <mediumValue> and <veryLongValue>
          Examples:
            | shortValue | mediumValue | veryLongValue      |
            | x          | medium      | this is very long  |
            | abc        | test        | y                  |
      """
      When the generator is run
      Then the content of the generated class should be:
      """
      import dev.spec2test.feature2junit.FeatureFilePath;
      import io.cucumber.java.en.Given;
      import java.lang.String;
      import javax.annotation.processing.Generated;
      import org.junit.jupiter.api.DisplayName;
      import org.junit.jupiter.api.MethodOrderer;
      import org.junit.jupiter.api.Order;
      import org.junit.jupiter.api.TestMethodOrder;
      import org.junit.jupiter.params.ParameterizedTest;
      import org.junit.jupiter.params.provider.CsvSource;

      /**
       * Feature: Formatting
       */
      @DisplayName("MockedAnnotatedTestClass")
      @Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @FeatureFilePath("MockedAnnotatedTestClass.feature")
      public abstract class MockedAnnotatedTestClassScenarios extends MockedAnnotatedTestClass {
          @Given("^(?<p1>.*) and (?<p2>.*) and (?<p3>.*)$")
          public abstract void given$p1And$p2And$p3(String p1, String p2, String p3);

          @ParameterizedTest(
                  name = "Example {index}: [{arguments}]"
          )
          @CsvSource(
                  useHeadersInDisplayName = true,
                  delimiter = '|',
                  textBlock = \"\"\"
                          shortValue | mediumValue | veryLongValue
                          x          | medium      | this is very long
                          abc        | test        | y
                          \"\"\"
          )
          @Order(1)
          @DisplayName("Scenario: Mixed lengths")
          public void scenario_1(String shortvalue, String mediumvalue, String verylongvalue) {
              /**
               * Given <shortValue> and <mediumValue> and <veryLongValue>
               */
              given$p1And$p2And$p3(shortvalue, mediumvalue, verylongvalue);
          }
      }
      """

  Rule: Having more than one Examples section is not supported and should generate an error

    Scenario: Scenario Outline with two Examples sections should fail during generation
      Given the following feature file:
      """
      Feature: Multiple Examples
        Scenario Outline: Test with multiple examples
          Given value <value>
          Examples:
            | value |
            | 1     |
          Examples:
            | value |
            | 2     |
      """
      When the generator is run
      Then the generator should report an error:
      """
      ERROR: Multiple Examples sections are not supported. Only one Examples section is allowed per Scenario Outline but found = 2
      """

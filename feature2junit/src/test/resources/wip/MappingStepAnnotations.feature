Feature: Mapping step annotations and metadata
  As a developer
  I want to understand how annotations, JavaDoc, and tags are added to generated step methods
  So that I can control the metadata and documentation of my tests

  Rule: Cucumber step annotations are generated with regex patterns when enabled
    When addCucumberStepAnnotations option is enabled, each method receives @Given, @When, or @Then annotation.
    The annotation value is a regex pattern: ^step text with (?<p1>.*) placeholders$
    Parameters are captured as named groups: (?<p1>.*), (?<p2>.*), etc.
    The step keyword is trimmed from the annotation pattern.

    Scenario: Given step with Cucumber annotation enabled
      Given addCucumberStepAnnotations option is true
      And the step is "Given user exists"
      Then the method has annotation '@Given("^user exists$")'
      And the pattern is anchored with ^ and $

    Scenario: When step with one parameter
      Given addCucumberStepAnnotations option is true
      And the step is 'When user "Alice" logs in'
      Then the method has annotation '@When("^user (?<p1>.*) logs in$")'
      And parameter is captured as named group (?<p1>.*)

    Scenario: Then step with two parameters
      Given addCucumberStepAnnotations option is true
      And the step is 'Then order "12345" has status "shipped"'
      Then the method has annotation '@Then("^order (?<p1>.*) has status (?<p2>.*)$")'
      And first parameter is (?<p1>.*)
      And second parameter is (?<p2>.*)

    Scenario: Step with three parameters
      Given addCucumberStepAnnotations option is true
      And the step is 'Given user "John" in group "Admin" has permission "write"'
      Then the annotation includes (?<p1>.*), (?<p2>.*), (?<p3>.*)
      And parameter names match method signature

    Scenario: Cucumber annotations disabled
      Given addCucumberStepAnnotations option is false
      And the step is 'When user "Alice" logs in'
      Then no @Given, @When, or @Then annotation is added
      And the method has no Cucumber annotation

    Scenario: Step keyword is not included in annotation pattern
      Given addCucumberStepAnnotations option is true
      And the step is "Given the system is ready"
      Then the annotation is '@Given("^the system is ready$")'
      And "Given" keyword is not in the pattern
      And only the step text is in the pattern

  Rule: Method calls in scenario methods include JavaDoc comments
    Each step method call in a scenario method is preceded by a JavaDoc comment.
    The comment contains the original step text as it appears in the feature file.
    When addSourceLineAnnotations option is enabled, the comment includes the source line number.

    Scenario: Step with JavaDoc comment
      Given a step "Given user exists"
      Then the method call is preceded by:
        """
        /**
         * Given user exists
         */
        """
      And the comment preserves the original step text

    Scenario: Step with source line annotation enabled
      Given addSourceLineAnnotations option is true
      And the step is "When user logs in" at line 42
      Then the JavaDoc comment includes the line number
      And the format is:
        """
        /**
         * When user logs in
         * @see feature.feature:42
         */
        """

    Scenario: Step with source line annotation disabled
      Given addSourceLineAnnotations option is false
      And the step is "Then user sees dashboard"
      Then the JavaDoc comment has no line number
      And only the step text is included

    Scenario: Multiple steps have individual JavaDoc comments
      Given a scenario with 3 steps
      Then each step method call has its own JavaDoc comment
      And comments appear immediately before their method calls

    Scenario: JavaDoc comment preserves step keyword
      Given a step "Given user is authenticated"
      Then the JavaDoc includes "Given user is authenticated"
      And the keyword "Given" is preserved in the comment

  Rule: Scenario tags are converted to JUnit @Tag annotations
    Tags from the feature file are extracted and added as @Tag annotations.
    Multiple tags are supported on scenarios.
    Tags can be applied at Feature, Rule, or Scenario level.

    Scenario: Scenario with one tag
      Given a scenario tagged with "@smoke"
      Then the test method has annotation '@Tag("smoke")'

    Scenario: Scenario with multiple tags
      Given a scenario tagged with "@smoke @regression @api"
      Then the test method has annotations:
        """
        @Tag("smoke")
        @Tag("regression")
        @Tag("api")
        """
      And each tag becomes a separate @Tag annotation

    Scenario: Tag without @ symbol in annotation
      Given a scenario tagged with "@integration"
      Then the annotation is '@Tag("integration")'
      And the @ symbol is removed from the tag value

    Scenario: Inherited tags from Feature level
      Given the feature is tagged with "@feature-tag"
      And a scenario in that feature
      Then the scenario inherits the feature tag
      And has '@Tag("feature-tag")' annotation

    Scenario: Inherited tags from Rule level
      Given a rule is tagged with "@rule-tag"
      And a scenario in that rule
      Then the scenario inherits the rule tag
      And has '@Tag("rule-tag")' annotation

    Scenario: Scenario with no tags
      Given a scenario with no tags
      And the feature has no tags
      Then no @Tag annotations are added

  Rule: Empty scenarios can be configured to fail or be tagged
    When failScenariosWithNoSteps option is enabled, empty scenarios generate a method that calls Assertions.fail().
    When tagForScenariosWithNoSteps option is set, empty scenarios are annotated with the specified tag.
    Empty scenarios are scenarios with no Given/When/Then steps.

    Scenario: Empty scenario with fail option enabled
      Given failScenariosWithNoSteps option is true
      And a scenario with no steps
      Then the generated method calls "Assertions.fail()"
      And the method body contains fail message

    Scenario: Empty scenario with tag option set
      Given tagForScenariosWithNoSteps option is "empty"
      And a scenario with no steps
      Then the method has annotation '@Tag("empty")'

    Scenario: Empty scenario with both options set
      Given failScenariosWithNoSteps option is true
      And tagForScenariosWithNoSteps option is "wip"
      Then the method calls "Assertions.fail()"
      And the method has annotation '@Tag("wip")'

    Scenario: Empty scenario with no options set
      Given failScenariosWithNoSteps option is false
      And tagForScenariosWithNoSteps option is not set
      And a scenario with no steps
      Then the method body is empty
      And no fail() call is generated
      And no special tag is added

    Scenario: Scenario with only a scenario name is considered empty
      Given a scenario with title but no steps:
        """
        Scenario: Future feature to implement
        """
      Then it is treated as an empty scenario
      And configured options apply

  Rule: Scenario methods are named sequentially
    Scenario methods are named: scenario_1, scenario_2, scenario_3, etc.
    Each scenario method is annotated with @Order(n) where n is the scenario number.
    Display name annotation uses format: "Scenario: [scenario name]"

    Scenario: First scenario in feature
      Given the first scenario is "User logs in"
      Then the method name is "scenario_1"
      And has annotation "@Order(1)"
      And has annotation '@DisplayName("Scenario: User logs in")'

    Scenario: Second scenario in feature
      Given the second scenario is "User logs out"
      Then the method name is "scenario_2"
      And has annotation "@Order(2)"
      And has annotation '@DisplayName("Scenario: User logs out")'

    Scenario: Scenario numbering is independent per Rule
      Given a feature with Rule_1 containing 2 scenarios
      And Rule_2 containing 2 scenarios
      Then Rule_1 scenarios are numbered scenario_1, scenario_2
      And Rule_2 scenarios are numbered scenario_1, scenario_2
      And numbering restarts within each nested class

  Rule: Background steps generate @BeforeEach methods
    Feature-level backgrounds generate a method named featureBackground().
    Rule-level backgrounds generate a method named ruleBackground().
    Both are annotated with @BeforeEach to run before each test scenario.

    Scenario: Feature with Background
      Given a feature has Background:
        """
        Background:
          Given system is initialized
          And database is clean
        """
      Then a method "featureBackground()" is generated
      And it has annotation "@BeforeEach"
      And it calls the background step methods

    Scenario: Rule with Background
      Given a rule has Background:
        """
        Background:
          Given user is logged in
        """
      Then a method "ruleBackground()" is generated
      And it has annotation "@BeforeEach"
      And it is in the nested Rule class

    Scenario: Feature and Rule both have Backgrounds
      Given the feature has a Background
      And a rule has a Background
      Then the feature class has "featureBackground()"
      And the rule nested class has "ruleBackground()"
      And both are annotated with @BeforeEach

  Rule: Rules generate nested test classes
    Each Rule in the feature file generates a nested inner class: Rule_1, Rule_2, etc.
    Nested classes are annotated with @Nested and @TestMethodOrder.
    Scenarios within rules become test methods in the nested class.

    Scenario: Feature with one Rule
      Given a feature has one Rule "Free shipping rules"
      Then a nested class "Rule_1" is generated
      And it has annotation "@Nested"
      And it has annotation "@TestMethodOrder(OrderAnnotation.class)"
      And it has annotation '@DisplayName("Rule: Free shipping rules")'

    Scenario: Feature with multiple Rules
      Given a feature has 3 Rules
      Then nested classes "Rule_1", "Rule_2", "Rule_3" are generated
      And each has @Nested annotation
      And each has @TestMethodOrder annotation

    Scenario: Rule scenarios are methods in nested class
      Given a Rule with 2 scenarios
      Then the nested Rule class contains 2 test methods
      And they are named scenario_1 and scenario_2
      And they are annotated with @Test or @ParameterizedTest

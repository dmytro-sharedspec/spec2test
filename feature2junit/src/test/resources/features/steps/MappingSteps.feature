Feature: mapping steps
  As a developer
  I want to verify that steps are mapped to abstract method signatures and calls to those methods are inserted into
  the scenario test methods
  So that I can implement the step methods in a subclass and have the scenario test methods call them

  Rule: Step text is converted to method name using camelCase convention
    The step keyword (Given, When, Then) becomes the first word in lowercase.
    Each subsequent word in the step text is split by whitespace and converted to camelCase:
    - First character of each word is capitalized
    - Remaining characters are lowercase
    - Only Java identifier-compliant characters are retained
    - Non-identifier characters are skipped entirely

  Rule: And, But, and * keywords inherit the previous step's keyword
    When a step starts with "And", "But", or "*", it replaces the keyword with the previous step's keyword (Given, When, or Then).
    The method name generation uses the inherited keyword as the first word.
    If there is no previous step, processing throws an exception.

  Rule: Parameters in double quotes are extracted from step text
    String parameters enclosed in double quotes are detected using regex pattern: (?<parameter>(\")(?<parameterValue>[^\"]+?)(\"))
    Extracted parameters are replaced with placeholders: $p1, $p2, etc.
    Method signatures receive parameters named: p1, p2, p3, etc., all of type String

  Rule: Scenario Outline parameters are extracted from step text
    Parameters in angle brackets (e.g., <parameterName>) are detected when processing Scenario Outlines.
    These parameters reference column names from the Examples table.
    When generating method calls, scenario parameters are passed as variable references (unquoted).
    Regular string parameters are passed as literal strings (quoted).

  Rule: Scenario Outline Examples table column names are converted to method parameter names
    Column headers from Examples table are split by whitespace.
    First word is converted to lowercase.
    Subsequent words have their first character capitalized, rest lowercase (camelCase).
    Only Java identifier-compliant characters are retained.

  Rule: DataTable parameters are added as the last parameter when present
    When a step has a DataTable, a parameter of type io.cucumber.datatable.DataTable named "dataTable" is added.
    The DataTable is formatted with pipe delimiters and passed via createDataTable() helper method.
    Column widths are calculated for proper alignment.

  Rule: DocString parameters are added as the last parameter when present
    When a step has a DocString, a parameter of type String named "docString" is added.
    Triple quotes in DocString content are escaped: """ becomes \"""
    For Scenario Outlines, scenario parameter references in DocStrings are replaced using .replaceAll() chain.

  Rule: Cucumber step annotations are generated with regex patterns when enabled
    When addCucumberStepAnnotations option is enabled, each method receives @Given, @When, or @Then annotation.
    The annotation value is a regex pattern: ^step text with (?<p1>.*) placeholders$
    Parameters are captured as named groups: (?<p1>.*), (?<p2>.*), etc.
    The step keyword is trimmed from the annotation pattern.

  Rule: Scenario methods are named sequentially
    Scenario methods are named: scenario_1, scenario_2, scenario_3, etc.
    Each scenario method is annotated with @Order(n) where n is the scenario number.
    Display name annotation uses format: "Scenario: [scenario name]"

  Rule: Scenario Outlines generate parameterized tests
    Scenarios with Examples tables are annotated with @ParameterizedTest.
    Examples table is converted to @CsvSource with pipe delimiter (|).
    Test method parameters match the column names from Examples table header.
    Display name format: "Example {index}: [{arguments}]"

  Rule: Background steps generate @BeforeEach methods
    Feature-level backgrounds generate a method named featureBackground().
    Rule-level backgrounds generate a method named ruleBackground().
    Both are annotated with @BeforeEach to run before each test scenario.

  Rule: Rules generate nested test classes
    Each Rule in the feature file generates a nested inner class: Rule_1, Rule_2, etc.
    Nested classes are annotated with @Nested and @TestMethodOrder.
    Scenarios within rules become test methods in the nested class.

  Rule: Step method signatures are deduplicated
    Before adding a step method to the generated class, the generator checks if a method with the same name already exists.
    If the method exists in the current class or base class, generation is skipped.
    This prevents duplicate method declarations.

  Rule: Method calls in scenario methods include javadoc comments
    Each step method call in a scenario method is preceded by a javadoc comment.
    The comment contains the original step text as it appears in the feature file.
    When addSourceLineAnnotations option is enabled, the comment includes the source line number.

  Rule: Scenario tags are converted to JUnit @Tag annotations
    Tags from the feature file are extracted and added as @Tag annotations.
    Multiple tags are supported on scenarios.

  Rule: Empty scenarios can be configured to fail or be tagged
    When failScenariosWithNoSteps option is enabled, empty scenarios generate a method that calls Assertions.fail().
    When tagForScenariosWithNoSteps option is set, empty scenarios are annotated with the specified tag.
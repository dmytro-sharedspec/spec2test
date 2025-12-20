Feature: Mapping steps - Overview and Index
  As a developer
  I want to understand how Gherkin steps are mapped to Java method signatures and method calls
  So that I can implement the step methods in a subclass and have the scenario test methods call them

  Background:
    This feature file serves as an overview and index to detailed step mapping documentation.
    The complete step mapping specification has been split into focused feature files for clarity.

  Rule: Step mapping documentation structure
    The step mapping rules are organized into the following detailed feature files:

    1. MappingStepMethodNames.feature
       - How step keywords (Given/When/Then) and step text are converted to method names
       - camelCase conversion rules
       - And/But/* keyword inheritance
       - Method deduplication logic

    2. MappingStepParameters.feature
       - How quoted strings in steps are extracted and become method parameters
       - Placeholder generation ($p1, $p2, etc.)
       - Parameter naming conventions (p1, p2, p3)
       - All parameters are typed as String

    3. MappingStepDataTables.feature
       - How DataTable arguments are added as method parameters
       - DataTable formatting with pipe delimiters
       - Column width calculation and alignment
       - createDataTable() helper method usage

    4. MappingStepDocStrings.feature
       - How DocString arguments are added as method parameters
       - Triple quote escaping (""" becomes \""")
       - Multi-line content preservation
       - Scenario Outline parameter replacement in DocStrings

    5. MappingStepScenarioOutlines.feature
       - How angle bracket parameters (<param>) work in Scenario Outlines
       - Difference between quoted literals and scenario parameters
       - Examples table column name conversion to camelCase
       - @ParameterizedTest and @CsvSource generation

    6. MappingStepAnnotations.feature
       - Cucumber step annotations (@Given/@When/@Then) with regex patterns
       - Method call JavaDoc comments with original step text
       - Tag conversion to JUnit @Tag annotations
       - Empty scenario handling (fail or tag options)
       - @BeforeEach methods for Background steps
       - @Nested classes for Rules
       - Sequential scenario numbering

  Rule: Quick reference - Common mapping patterns

    Scenario: Simple step without parameters
      Given step text: "Given user exists"
      Then method name: "givenUserExists()"
      And method call: "givenUserExists()"

    Scenario: Step with quoted parameter
      Given step text: 'When user "Alice" logs in'
      Then method name: "whenUser$p1LogsIn(String p1)"
      And method call: 'whenUserP1LogsIn("Alice")'

    Scenario: Scenario Outline with parameter
      Given step text: "Given user <username> exists"
      And Examples column: "username"
      Then method name: "givenUser$p1Exists(String p1)"
      And method call: "givenUserP1Exists(username)"
      And parameter is passed unquoted

    Scenario: Step with DataTable
      Given step text: "Given the following users exist:" with DataTable
      Then method name: "givenTheFollowingUsersExist(DataTable dataTable)"
      And method call uses: "createDataTable(...)"

    Scenario: Step with DocString
      Given step text: "Given document contains:" with DocString
      Then method name: "givenDocumentContains(String docString)"
      And multi-line content is preserved

  Rule: Generation options that affect step mapping

    Option: addCucumberStepAnnotations
    - When enabled: Adds @Given/@When/@Then annotations with regex patterns
    - When disabled: No Cucumber annotations on step methods

    Option: addSourceLineAnnotations
    - When enabled: JavaDoc comments include feature file line numbers
    - When disabled: JavaDoc comments contain only step text

    Option: failScenariosWithNoSteps
    - When enabled: Empty scenarios generate Assertions.fail() call
    - When disabled: Empty scenarios generate empty method body

    Option: tagForScenariosWithNoSteps
    - When set: Empty scenarios get the specified @Tag annotation
    - When not set: No special tag for empty scenarios

  Rule: Key architectural decisions

    Decision: Per-feature step methods (not global step library)
    - Each feature has its own set of step methods
    - No sharing of steps across features
    - Avoids step ambiguity and discovery overhead

    Decision: Step method deduplication
    - Methods are checked against current class and base class hierarchy
    - Duplicate method names are skipped during generation
    - Allows step reuse through inheritance

    Decision: Parameter extraction order
    - Quoted parameters are extracted first
    - Scenario Outline parameters are handled separately
    - DataTable or DocString is always the last parameter

    Decision: All string parameters use type String
    - Quoted parameters: String
    - Scenario Outline parameters: String
    - DocString parameters: String
    - Type conversion is the responsibility of step implementation
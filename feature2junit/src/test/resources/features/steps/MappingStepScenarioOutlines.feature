Feature: Mapping Scenario Outlines to parameterized tests
  As a developer
  I want to understand how Scenario Outline parameters are handled differently from regular parameters
  So that I can write data-driven tests with Examples tables

  Rule: Scenario Outline parameters are extracted from step text
    Parameters in angle brackets (e.g., <parameterName>) are detected when processing Scenario Outlines.
    These parameters reference column names from the Examples table.
    When generating method calls, scenario parameters are passed as variable references (unquoted).
    Regular string parameters are passed as literal strings (quoted).

    Scenario: Step with one scenario parameter
      Given a Scenario Outline step is "Given user <username> exists"
      And Examples table has column "username"
      Then the method signature is "givenUser$p1Exists(String p1)"
      And the method call is "givenUserP1Exists(username)"
      And "username" is passed as a variable reference (unquoted)

    Scenario: Step with two scenario parameters
      Given a Scenario Outline step is "When user <username> has role <role>"
      And Examples table has columns "username" and "role"
      Then the method signature is "whenUser$p1HasRole$p2(String p1, String p2)"
      And the method call is "whenUserP1HasRoleP2(username, role)"
      And both are unquoted variable references

    Scenario: Step with mixed quoted and scenario parameters
      Given a Scenario Outline step is 'When user <username> clicks "Submit" button'
      And Examples table has column "username"
      Then the method signature is "whenUser$p1Clicks$p2Button(String p1, String p2)"
      And the method call is 'whenUserP1ClicksP2Button(username, "Submit")'
      And <username> becomes unquoted variable "username"
      And "Submit" becomes quoted literal '"Submit"'

    Scenario: Step with scenario parameter followed by quoted parameter
      Given a Scenario Outline step is 'Given order <orderId> has status "pending"'
      And Examples table has column "orderId"
      Then parameter 1 is scenario parameter <orderId>
      And parameter 2 is literal string "pending"
      And the call is 'givenOrderP1HasStatusP2(orderId, "pending")'

    Scenario: Step with quoted parameter followed by scenario parameter
      Given a Scenario Outline step is 'When "Admin" creates user <username>'
      And Examples table has column "username"
      Then parameter 1 is literal string "Admin"
      And parameter 2 is scenario parameter <username>
      And the call is 'whenP1CreatesUserP2("Admin", username)'

    Scenario: Step with multiple scenario parameters and quoted parameters
      Given a Scenario Outline step is 'Then user <username> in group <group> receives message "Welcome"'
      And Examples table has columns "username" and "group"
      Then parameter 1 is <username> (unquoted)
      And parameter 2 is <group> (unquoted)
      And parameter 3 is "Welcome" (quoted)
      And the call is 'thenUserP1InGroupP2ReceivesMessageP3(username, group, "Welcome")'

  Rule: Scenario Outline Examples table column names are converted to method parameter names
    Column headers from Examples table are split by whitespace.
    First word is converted to lowercase.
    Subsequent words have their first character capitalized, rest lowercase (camelCase).
    Only Java identifier-compliant characters are retained.

    Scenario: Single-word column name
      Given Examples table has column "username"
      Then the parameter name is "username"

    Scenario: Two-word column name with space
      Given Examples table has column "user name"
      Then the parameter name is "userName"

    Scenario: Multi-word column name
      Given Examples table has column "customer email address"
      Then the parameter name is "customerEmailAddress"

    Scenario: Column name with special characters
      Given Examples table has column "user's name"
      Then the parameter name is "usersName"
      And the apostrophe is removed

    Scenario: Column name with numbers
      Given Examples table has column "user123 id"
      Then the parameter name is "user123Id"

    Scenario: Column name with underscores
      Given Examples table has column "user_name"
      Then the parameter name is "user_name"
      And underscores are treated as non-identifier characters

    Scenario: Column name all uppercase
      Given Examples table has column "USERNAME"
      Then the parameter name is "username"
      And all letters are converted to lowercase

    Scenario: Column name with mixed case
      Given Examples table has column "UserName"
      Then the parameter name is "userName"
      And first character is lowercased

  Rule: Scenario Outlines generate parameterized tests
    Scenarios with Examples tables are annotated with @ParameterizedTest.
    Examples table is converted to @CsvSource with pipe delimiter (|).
    Test method parameters match the column names from Examples table header.
    Display name format: "Example {index}: [{arguments}]"

    Scenario: Scenario Outline with single Examples table
      Given a Scenario Outline with Examples:
        | username | password |
        | alice    | pass123  |
        | bob      | secret   |
      Then the method is annotated with "@ParameterizedTest"
      And the method has annotation '@CsvSource(delimiter = \'|\', value = { ... })'
      And the method parameters are "String username, String password"

    Scenario: Scenario Outline with three columns
      Given a Scenario Outline with Examples:
        | id | name  | status |
        | 1  | Alice | active |
        | 2  | Bob   | inactive |
      Then @CsvSource contains two data rows
      And each row is pipe-delimited
      And method has three parameters: "id, name, status"

    Scenario: CsvSource formatting with pipe delimiter
      Given a Scenario Outline with Examples:
        | user | role  |
        | John | Admin |
        | Jane | User  |
      Then the @CsvSource value contains '"|user|role|", "|John|Admin|", "|Jane|User|"'
      And pipes are used as delimiters
      And header row is included

    Scenario: Display name with index and arguments
      Given a Scenario Outline test
      Then the @ParameterizedTest has display name annotation
      And the format is '"Example {index}: [{arguments}]"'
      And {index} is replaced with example number
      And {arguments} is replaced with example values

  Rule: Scenario Outline method naming
    Scenario Outline methods follow the same naming convention as regular scenarios.
    They are named sequentially: scenario_1, scenario_2, etc.
    Each is annotated with @Order(n).

    Scenario: First Scenario Outline in feature
      Given the first scenario is a Scenario Outline
      Then the method name is "scenario_1"
      And it has annotation "@Order(1)"

    Scenario: Multiple Scenario Outlines
      Given the feature has 3 Scenario Outlines
      Then they are named "scenario_1", "scenario_2", "scenario_3"
      And they have @Order(1), @Order(2), @Order(3)

    Scenario: Mix of Scenarios and Scenario Outlines
      Given the feature has regular Scenario at position 1
      And Scenario Outline at position 2
      And regular Scenario at position 3
      Then method names are "scenario_1", "scenario_2", "scenario_3"
      And both types follow the same numbering sequence

  Rule: Scenario Outline parameters work with DataTables and DocStrings
    Scenario parameters can appear within DataTable cells and DocString content.
    For DocStrings, .replaceAll() chains are used for substitution.
    For DataTables, the scenario parameters are embedded in the createDataTable() call.

    Scenario: Scenario Outline step with DataTable containing parameters
      Given a Scenario Outline step:
        """
        Given user <username> has data:
          | field | value      |
          | name  | <fullName> |
          | email | <email>    |
        """
      And Examples columns are "username", "fullName", "email"
      Then the DataTable contains parameter placeholders
      And scenario variables are used in createDataTable call

    Scenario: Scenario Outline step with DocString containing parameters
      Given a Scenario Outline step:
        """
        When user <username> submits:
          ```
          Name: <name>
          Email: <email>
          ```
        """
      And Examples columns are "username", "name", "email"
      Then the method call includes '.replaceAll("<name>", name)'
      And includes '.replaceAll("<email>", email)'
      And placeholders are replaced with actual values

  Rule: Empty Examples table handling
    Scenario Outlines require at least one Examples table.
    An Examples table with only headers (no data rows) results in zero test executions.

    Scenario: Examples table with header only
      Given a Scenario Outline with Examples:
        | username | password |
      And the Examples table has no data rows
      Then the @CsvSource contains only the header row
      And the parameterized test executes zero times

    Scenario: Examples table with data rows
      Given a Scenario Outline with Examples:
        | username | password |
        | alice    | pass123  |
      Then the @CsvSource contains header and one data row
      And the parameterized test executes once

  Rule: Multiple Examples tables in a Scenario Outline
    A Scenario Outline can have multiple Examples tables.
    Each Examples table contributes rows to the same @CsvSource.
    Column headers must match across all Examples tables.

    Scenario: Scenario Outline with two Examples tables
      Given a Scenario Outline with first Examples:
        | username | role  |
        | alice    | admin |
      And second Examples:
        | username | role |
        | bob      | user |
      Then the @CsvSource combines both tables
      And contains 2 data rows total
      And column headers must be consistent

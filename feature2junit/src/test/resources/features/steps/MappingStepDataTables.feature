Feature: Mapping DataTables to method parameters
  As a developer
  I want to understand how DataTables in steps are converted to method parameters
  So that I can work with tabular data in my step implementations

  Rule: DataTable parameters are added as the last parameter when present
    When a step has a DataTable, a parameter of type io.cucumber.datatable.DataTable named "dataTable" is added.
    The DataTable is formatted with pipe delimiters and passed via createDataTable() helper method.
    Column widths are calculated for proper alignment.

    Scenario: Step with DataTable and no quoted parameters
      Given the step is:
        """
        Given the following users exist:
          | name  | role  |
          | Alice | Admin |
          | Bob   | User  |
        """
      Then the method signature is "givenTheFollowingUsersExist(DataTable dataTable)"
      And the parameter type is "io.cucumber.datatable.DataTable"
      And the parameter name is "dataTable"

    Scenario: Step with DataTable and one quoted parameter
      Given the step is:
        """
        When user "Alice" has permissions:
          | permission | enabled |
          | read       | true    |
          | write      | false   |
        """
      Then the method signature is "whenUser$p1HasPermissions(String p1, DataTable dataTable)"
      And parameter 1 is "p1" of type "String"
      And parameter 2 is "dataTable" of type "DataTable"
      And DataTable is always the last parameter

    Scenario: Step with DataTable and multiple quoted parameters
      Given the step is:
        """
        Then order "12345" for customer "Bob" contains:
          | product | quantity |
          | Laptop  | 1        |
          | Mouse   | 2        |
        """
      Then the method signature is "thenOrder$p1ForCustomer$p2Contains(String p1, String p2, DataTable dataTable)"
      And parameters are ordered: "p1, p2, dataTable"

  Rule: DataTable is formatted with pipe delimiters
    The DataTable content is passed using the createDataTable() helper method.
    Rows are separated by newlines and columns by pipe characters (|).
    Proper spacing is maintained for readability.

    Scenario: DataTable with two columns
      Given a DataTable:
        | name  | age |
        | Alice | 30  |
        | Bob   | 25  |
      Then the formatted output includes createDataTable call
      And columns are aligned with spaces

    Scenario: DataTable with three columns
      Given a DataTable:
        | id | name    | status  |
        | 1  | Product | active  |
        | 2  | Service | pending |
      Then the formatted output includes pipe delimiters
      And each column width matches the longest value in that column

    Scenario: DataTable with single column
      Given a DataTable:
        | permission |
        | read       |
        | write      |
        | delete     |
      Then the formatted output has one column
      And pipes are placed before and after the column

    Scenario: DataTable with varying column widths
      Given a DataTable:
        | short | very long column name | mid    |
        | x     | value                 | abc    |
        | y     | another value         | defghi |
      Then column widths are calculated for alignment
      And padding spaces are added to shorter values

  Rule: DataTable helper method is used for table creation
    The generator uses a createDataTable() helper method to convert string representation to DataTable object.
    This helper method handles the parsing and DataTable instantiation.

    Scenario: Single DataTable generates one createDataTable call
      Given a step with one DataTable
      Then the method call uses "createDataTable(...)"
      And the call appears once in the scenario method

    Scenario: Multiple steps with DataTables in same scenario
      Given scenario has step 1 with a DataTable
      And scenario has step 2 with a DataTable
      Then each step generates a separate createDataTable call
      And both calls use the same helper method

  Rule: DataTable works with all step keywords
    DataTables can appear in Given, When, Then, And, But, and * steps.
    The parameter handling is consistent across all step types.

    Scenario: DataTable in Given step
      Given a Given step has a DataTable
      Then the method signature includes "DataTable dataTable"
      And the keyword prefix is "given"

    Scenario: DataTable in When step
      Given a When step has a DataTable
      Then the method signature includes "DataTable dataTable"
      And the keyword prefix is "when"

    Scenario: DataTable in Then step
      Given a Then step has a DataTable
      Then the method signature includes "DataTable dataTable"
      And the keyword prefix is "then"

    Scenario: DataTable in And step
      Given an And step has a DataTable
      Then the method signature includes "DataTable dataTable"
      And the keyword is inherited from previous step

  Rule: Empty DataTable is handled correctly
    DataTables with only headers or completely empty are still passed as parameters.

    Scenario: DataTable with headers only
      Given a DataTable:
        | name | email |
      Then the method receives a DataTable parameter
      And the DataTable has one row (header)

    Scenario: DataTable with headers and no data rows
      Given a DataTable with columns but zero data rows
      Then the createDataTable call includes only the header
      And the parameter is still of type DataTable

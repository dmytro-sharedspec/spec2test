Feature: Mapping DocStrings to method parameters
  As a developer
  I want to understand how DocStrings in steps are converted to method parameters
  So that I can work with multi-line text content in my step implementations

  Rule: DocString parameters are added as the last parameter when present
    When a step has a DocString, a parameter of type String named "docString" is added.
    Triple quotes in DocString content are escaped: """ becomes \"""
    The DocString parameter is always the last parameter in the method signature.

    Scenario: Step with DocString and no quoted parameters
      Given the step is:
        """
        Given the following JSON exists:
          ```
          {
            "name": "Alice",
            "role": "Admin"
          }
          ```
        """
      Then the method signature is "givenTheFollowingJsonExists(String docString)"
      And the parameter type is "String"
      And the parameter name is "docString"

    Scenario: Step with DocString and one quoted parameter
      Given the step is:
        """
        When user "Alice" submits document:
          ```
          This is a multi-line
          document content
          ```
        """
      Then the method signature is "whenUser$p1SubmitsDocument(String p1, String docString)"
      And parameter 1 is "p1" of type "String"
      And parameter 2 is "docString" of type "String"
      And DocString is always the last parameter

    Scenario: Step with DocString and multiple quoted parameters
      Given the step is:
        """
        Then response for request "GET" to "/api/users" is:
          ```
          {
            "status": "success"
          }
          ```
        """
      Then the method signature is "thenResponseForRequest$p1To$p2Is(String p1, String p2, String docString)"
      And parameters are ordered: "p1, p2, docString"

  Rule: Triple quotes in DocString content are escaped
    DocStrings use triple quotes (""") as delimiters.
    If the content contains triple quotes, they must be escaped to \""".
    This prevents premature string termination in generated code.

    Scenario: DocString containing triple quotes
      Given the DocString content is:
        """
        Example of triple quotes: """
        More content here
        """
      Then the escaped content is:
        """
        Example of triple quotes: \"""
        More content here
        """
      And the method call uses the escaped version

    Scenario: DocString with multiple occurrences of triple quotes
      Given the DocString content has """ at the beginning
      And the DocString content has """ in the middle
      And the DocString content has """ at the end
      Then all occurrences are escaped to \"""
      And the string is properly delimited

    Scenario: DocString without triple quotes
      Given the DocString content is:
        """
        Simple text
        without triple quotes
        """
      Then no escaping is needed
      And the content is passed as-is

  Rule: DocString preserves multi-line content
    DocStrings maintain line breaks and formatting from the feature file.
    Indentation and whitespace are preserved.

    Scenario: DocString with multiple lines
      Given the DocString is:
        """
        Line 1
        Line 2
        Line 3
        """
      Then the content has 3 lines
      And line breaks are preserved in the String parameter

    Scenario: DocString with indented content
      Given the DocString is:
        """
          Indented line 1
            More indented line 2
          Back to first indent
        """
      Then the indentation is preserved
      And spaces are maintained in the String parameter

    Scenario: DocString with empty lines
      Given the DocString is:
        """
        First paragraph

        Second paragraph after blank line
        """
      Then the empty line is preserved
      And the String contains the blank line

  Rule: DocString works with all step keywords
    DocStrings can appear in Given, When, Then, And, But, and * steps.
    The parameter handling is consistent across all step types.

    Scenario: DocString in Given step
      Given a Given step has a DocString
      Then the method signature includes "String docString"
      And the keyword prefix is "given"

    Scenario: DocString in When step
      Given a When step has a DocString
      Then the method signature includes "String docString"
      And the keyword prefix is "when"

    Scenario: DocString in Then step
      Given a Then step has a DocString
      Then the method signature includes "String docString"
      And the keyword prefix is "then"

    Scenario: DocString in And step
      Given an And step has a DocString
      Then the method signature includes "String docString"
      And the keyword is inherited from previous step

  Rule: DocString and DataTable cannot both be present in a single step
    A step can have either a DocString or a DataTable, but not both.
    This is a Gherkin language constraint.

    Scenario: Step with only DocString
      Given a step has a DocString
      And the step has no DataTable
      Then the method signature ends with "String docString"

    Scenario: Step with only DataTable
      Given a step has a DataTable
      And the step has no DocString
      Then the method signature ends with "DataTable dataTable"

  Rule: DocString content type markers are preserved
    DocStrings can have optional content type markers (e.g., ```json, ```xml).
    These markers are part of the content passed to the method.

    Scenario: DocString with JSON content type
      Given the DocString has content type "json":
        ```json
        {"key": "value"}
        ```
      Then the content type marker is included
      And the parameter receives the full content

    Scenario: DocString with XML content type
      Given the DocString has content type "xml":
        ```xml
        <root>value</root>
        ```
      Then the content type marker is included

    Scenario: DocString with no content type
      Given the DocString has no content type marker:
        ```
        Plain text content
        ```
      Then the content is passed without a type marker

  Rule: DocString parameter replacement in Scenario Outlines
    For Scenario Outlines, scenario parameter references in DocStrings are replaced using .replaceAll() chain.
    Placeholders like <paramName> are substituted with actual values from Examples table.

    Scenario: DocString with single scenario parameter
      Given a Scenario Outline step with DocString:
        """
        When user submits data:
          ```
          Username: <username>
          ```
        """
      And Examples table has column "username"
      Then the method call includes ".replaceAll(\"<username>\", username)"
      And the DocString parameter is dynamically replaced

    Scenario: DocString with multiple scenario parameters
      Given a Scenario Outline step with DocString:
        """
        Given document with:
          ```
          Name: <name>
          Email: <email>
          Role: <role>
          ```
        """
      And Examples table has columns "name", "email", "role"
      Then the method call chains multiple replaceAll calls
      And each placeholder is replaced with corresponding variable

    Scenario: DocString with no scenario parameters in Scenario Outline
      Given a Scenario Outline step with DocString:
        """
        When user submits:
          ```
          Static content
          ```
        """
      And the DocString has no <placeholders>
      Then no replaceAll calls are generated
      And the DocString is passed as a literal string

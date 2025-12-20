Feature: Mapping quoted strings to method parameters
  As a developer
  I want to understand how quoted strings in steps are extracted and converted to method parameters
  So that I can write parameterized step implementations

  Rule: Parameters in double quotes are extracted from step text
    String parameters enclosed in double quotes are detected using regex pattern: (?<parameter>(\")(?<parameterValue>[^\"]+?)(\"))
    Extracted parameters are replaced with placeholders: $p1, $p2, etc.
    Method signatures receive parameters named: p1, p2, p3, etc., all of type String

    Scenario: Step with one quoted parameter
      Given the step is 'Given user "John" exists'
      Then the parameter "John" is extracted
      And the method signature is "givenUser$p1Exists(String p1)"
      And the method call is 'givenUserP1Exists("John")'

    Scenario: Step with two quoted parameters
      Given the step is 'When user "Alice" sends message "Hello"'
      Then parameter 1 is "Alice"
      And parameter 2 is "Hello"
      And the method signature is "whenUser$p1SendsMessage$p2(String p1, String p2)"
      And the method call is 'whenUserP1SendsMessageP2("Alice", "Hello")'

    Scenario: Step with three quoted parameters
      Given the step is 'Then order "12345" for customer "Bob" has status "shipped"'
      Then parameter 1 is "12345"
      And parameter 2 is "Bob"
      And parameter 3 is "shipped"
      And the method signature is "thenOrder$p1ForCustomer$p2HasStatus$p3(String p1, String p2, String p3)"
      And the method call is 'thenOrderP1ForCustomerP2HasStatusP3("12345", "Bob", "shipped")'

    Scenario: Step with no parameters
      Given the step is "Given system is ready"
      Then no parameters are extracted
      And the method signature is "givenSystemIsReady()"
      And the method call is "givenSystemIsReady()"

    Scenario: Step with parameter containing spaces
      Given the step is 'When I search for "hello world"'
      Then the parameter is "hello world"
      And the method signature is "whenISearchFor$p1(String p1)"
      And the method call is 'whenISearchForP1("hello world")'

    Scenario: Step with parameter containing special characters
      Given the step is 'Given password is "P@ssw0rd!"'
      Then the parameter is "P@ssw0rd!"
      And the method signature is "givenPasswordIs$p1(String p1)"
      And the method call is 'givenPasswordIsP1("P@ssw0rd!")'

    Scenario: Step with empty quoted parameter
      Given the step is 'When field is set to ""'
      Then the parameter is ""
      And the method signature is "whenFieldIsSetTo$p1(String p1)"
      And the method call is 'whenFieldIsSetToP1("")'

    Scenario: Step with parameter at the beginning
      Given the step is 'Given "Admin" role is assigned'
      Then the parameter is "Admin"
      And the method signature is "given$p1RoleIsAssigned(String p1)"
      And the method call is 'givenP1RoleIsAssigned("Admin")'

    Scenario: Step with parameter at the end
      Given the step is 'When user logs in as "Guest"'
      Then the parameter is "Guest"
      And the method signature is "whenUserLogsInAs$p1(String p1)"
      And the method call is 'whenUserLogsInAsP1("Guest")'

    Scenario: Step with consecutive quoted parameters
      Given the step is 'Given user "John" "Doe" is registered'
      Then parameter 1 is "John"
      And parameter 2 is "Doe"
      And the method signature is "givenUser$p1$p2IsRegistered(String p1, String p2)"
      And the method call is 'givenUserP1P2IsRegistered("John", "Doe")'

  Rule: Parameter placeholders are consistently numbered
    Parameters are numbered sequentially starting from 1.
    The placeholder format in method names is $p1, $p2, $p3, etc.
    Method parameter names are p1, p2, p3, etc.

    Scenario: Parameter numbering is sequential
      Given the step is 'When I transfer "100" from "Account A" to "Account B"'
      Then placeholder 1 is "$p1" for value "100"
      And placeholder 2 is "$p2" for value "Account A"
      And placeholder 3 is "$p3" for value "Account B"
      And parameter names are "p1, p2, p3"

    Scenario: Parameter names in method signature match placeholders
      Given the step is 'Given product "Laptop" costs "999"'
      Then the method name contains "$p1" and "$p2"
      And the method signature contains "String p1, String p2"
      And the call passes arguments in order: "Laptop", "999"

  Rule: All extracted parameters are of type String
    Regardless of the content of quoted parameters (numbers, dates, etc.), they are always typed as String.
    Type conversion is the responsibility of the step implementation.

    Scenario: Numeric parameter is typed as String
      Given the step is 'When quantity is set to "42"'
      Then the parameter type is "String"
      And the method signature is "whenQuantityIsSetTo$p1(String p1)"

    Scenario: Boolean-like parameter is typed as String
      Given the step is 'Given feature flag is "true"'
      Then the parameter type is "String"
      And the method signature is "givenFeatureFlagIs$p1(String p1)"

    Scenario: Date-like parameter is typed as String
      Given the step is 'When date is set to "2024-12-20"'
      Then the parameter type is "String"
      And the method signature is "whenDateIsSetTo$p1(String p1)"
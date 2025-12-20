Feature: Mapping step keywords and text to method names
  As a developer
  I want to understand how step keywords and text are converted to method names
  So that I can predict the generated method signatures

  Rule: Step text is converted to method name using camelCase convention
    The step keyword (Given, When, Then) becomes the first word in lowercase.
    Each subsequent word in the step text is split by whitespace and converted to camelCase:
    - First character of each word is capitalized
    - Remaining characters are lowercase
    - Only Java identifier-compliant characters are retained
    - Non-identifier characters are skipped entirely

    Scenario: Simple step with one word
      Given the step text is "Given user"
      Then the method name is "givenUser"

    Scenario: Step with multiple words
      Given the step text is "When the user clicks the button"
      Then the method name is "whenTheUserClicksTheButton"

    Scenario: Step with non-identifier characters
      Given the step text is "Then the user's profile is displayed"
      Then the method name is "thenTheUsersProfileIsDisplayed"
      And non-identifier character "'" is skipped

    Scenario: Step with numbers
      Given the step text is "Given user123 exists"
      Then the method name is "givenUser123Exists"

    Scenario: Step with special characters
      Given the step text is "When user@email.com logs in"
      Then the method name is "whenUserEmailComLogsIn"

  Rule: And, But, and * keywords inherit the previous step's keyword
    When a step starts with "And", "But", or "*", it replaces the keyword with the previous step's keyword (Given, When, or Then).
    The method name generation uses the inherited keyword as the first word.
    If there is no previous step, processing throws an exception.

    Scenario: And keyword inherits from Given
      Given the previous step keyword is "Given"
      And the current step is "And the user is authenticated"
      Then the method name is "givenTheUserIsAuthenticated"
      And the keyword is inherited from previous "Given"

    Scenario: And keyword inherits from When
      Given the previous step keyword is "When"
      And the current step is "And the user submits the form"
      Then the method name is "whenTheUserSubmitsTheForm"

    Scenario: But keyword inherits from Then
      Given the previous step keyword is "Then"
      And the current step is "But the password is not visible"
      Then the method name is "thenThePasswordIsNotVisible"

    Scenario: Asterisk keyword inherits from previous step
      Given the previous step keyword is "Given"
      And the current step is "* the system is ready"
      Then the method name is "givenTheSystemIsReady"

    Scenario: Multiple And keywords chain inheritance
      Given the first step is "Given user exists"
      And the second step is "And user is active"
      And the third step is "And user has permissions"
      Then all three steps use "given" as the method prefix

  Rule: Step method signatures are deduplicated
    Before adding a step method to the generated class, the generator checks if a method with the same name already exists.
    If the method exists in the current class or base class, generation is skipped.
    This prevents duplicate method declarations.

    Scenario: Same step appears multiple times in one feature
      Given the step "Given user exists" appears in scenario 1
      And the step "Given user exists" appears in scenario 2
      Then the method "givenUserExists" is generated only once

    Scenario: Step method exists in base class
      Given the base class has method "givenUserExists"
      And the feature contains step "Given user exists"
      Then the method "givenUserExists" is not generated
      And the existing base class method is called

    Scenario: Step method exists in ancestor class
      Given the ancestor class has method "givenUserIsAuthenticated"
      And the feature contains step "Given user is authenticated"
      Then the method is not duplicated
      And the inherited method is used
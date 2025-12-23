Feature: ScenarioEmpty
  As a test developer using Gherkin
  I want to configure how empty Scenarios (Scenarios without steps) are handled
  So that I can control test behavior and tag incomplete specifications

  Rule: Empty Scenarios generate a failing test method when failScenariosWithNoSteps option is enabled

  Rule: The failing test method contains Assertions.fail("Scenario has no steps")

  Rule: Empty Scenarios can be tagged with a custom tag using the tagForScenariosWithNoSteps option

  Rule: When tagForScenariosWithNoSteps is not set, empty Scenarios get a default tag "new"

  Rule: The custom tag for empty Scenarios works independently of the failScenariosWithNoSteps option

  Rule: A Scenario is considered empty if it has no Given/When/Then/And/But steps

  Rule: A Scenario with only a name but no steps is treated as an empty Scenario

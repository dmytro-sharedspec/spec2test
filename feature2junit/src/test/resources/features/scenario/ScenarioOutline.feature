Feature: ScenarioOutline
  As a test developer using Gherkin
  I want Scenario Outlines to be converted to parameterized JUnit tests
  So that I can write data-driven tests with Examples tables

  Rule: Scenario Outline is converted to a method with @ParameterizedTest annotation instead of @Test

  Rule: Examples table is converted to @CsvSource annotation with pipe-delimited data

  Rule: Examples table column headers become method parameters with String type

  Rule: @CsvSource uses useHeadersInDisplayName=true to include column headers in test names

  Rule: @ParameterizedTest name format is "Example {index}: [{arguments}]"

  Rule: Examples table is formatted with aligned columns using pipe separators

  Rule: Each row in Examples table (excluding header) generates one parameterized test execution

  Rule: Having more than one Examples section is not supported and should generate an error

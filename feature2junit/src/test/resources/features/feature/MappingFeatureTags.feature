Feature: Mapping Feature tags to @Tag annotations
  As a developer
  I want to verify that feature-level tags are correctly converted to JUnit @Tag annotations
  So that I can filter and organize tests based on their tags

  Rule: Feature tags are converted to JUnit @Tag annotations on the class
    Tags applied at the Feature level become @Tag annotations on the generated test class.
    Each Gherkin tag becomes a separate @Tag annotation.
    The @ symbol from the Gherkin tag is removed in the annotation value.

  Rule: Multiple feature tags result in multiple @Tag annotations
    If a feature has tags @smoke, @regression, @api, the class gets three @Tag annotations.
    Each @Tag annotation contains one tag value.
    The tags are processed and added individually to the class.

  Rule: Feature tags are inherited by all scenarios in the feature
    Tags on the Feature level apply to all scenarios within that feature.
    Nested Rules and their scenarios also inherit feature-level tags.
    This is standard Gherkin behavior that is preserved in the generated code.

  Rule: @Tag annotations are added only when feature has tags
    If the feature has no tags, no @Tag annotations are added to the class.
    The tag check verifies that the tags list is not null and not empty.
    Features without tags result in a class without @Tag annotations.

  Rule: Feature tag annotations appear before @DisplayName
    When feature tags are present, the @Tag annotations are the first annotations on the class.
    They appear before @DisplayName, @Generated, and other class-level annotations.
    The ordering ensures tags are prominent in the generated code.

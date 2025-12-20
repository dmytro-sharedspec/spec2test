Feature: Mapping Feature to @DisplayName annotation
  As a developer
  I want to verify that the @DisplayName annotation is correctly added to the generated test class
  So that test reports show the feature file name as the display name

  Rule: @DisplayName annotation uses the feature file name (not the feature title)
    The @DisplayName annotation value is derived from the feature file path.
    The file name is extracted from the path by:
    - Finding the last "/" in the feature file path
    - Taking the substring between the last "/" and the last "."
    - This becomes the display name value

  Rule: @DisplayName annotation is always added to the generated class
    Regardless of whether the feature has a name or description, the @DisplayName annotation is always present.
    The annotation is added at the class level.
    It appears before other annotations like @Generated and @TestMethodOrder.

  Rule: Feature file path determines the display name value
    The display name is extracted from the feature file path by taking the filename without extension.
    For path "specs/cart.feature", the display name becomes "cart".
    For path "features/user/authentication.feature", the display name becomes "authentication".
    Nested directories are ignored - only the final filename (without .feature extension) is used.
    The feature title (text after "Feature:" keyword) does not affect the @DisplayName value.

Feature: Mapping Feature to @FeatureFilePath annotation
  As a developer
  I want to verify that the @FeatureFilePath annotation is correctly added to the generated test class
  So that the generated code retains a reference to its source feature file

  Rule: @FeatureFilePath annotation records the source feature file path
    The @FeatureFilePath annotation is added to every generated test class.
    The annotation value is the path to the feature file used for generation.
    This creates traceability from generated code back to the specification.

  Rule: @FeatureFilePath value is resolved based on how the path was specified
    When the @Feature2JUnit annotation has an explicit path, that path is used.
    When the @Feature2JUnit annotation value is blank, the path is constructed from package and class name.
    The constructed path format is: "<package>/<className>.feature" (with dots replaced by slashes).

  Rule: Explicit feature file paths are used as-is in @FeatureFilePath
    If @Feature2JUnit("specs/cart.feature"), then @FeatureFilePath("/specs/cart.feature").
    If @Feature2JUnit("features/user/login.feature"), then @FeatureFilePath("/features/user/login.feature").
    The path value is preserved exactly as specified in the trigger annotation.

  Rule: Constructed paths use package and class name when path is blank
    If package is "com.example.tests" and class is "CartFeature", path is "com/example/tests/CartFeature.feature".
    The package name dots are converted to forward slashes.
    The annotated class name (not the generated class name) is used.
    The .feature extension is appended automatically.

  Rule: @FeatureFilePath annotation is added at the class level
    The annotation appears on the generated test class.
    It is positioned after @TestMethodOrder (and @TestClassOrder if present) in the annotation list.
    It is always the last annotation in the list of standard annotations.

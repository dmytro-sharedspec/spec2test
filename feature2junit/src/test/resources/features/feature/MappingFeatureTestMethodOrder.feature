Feature: Mapping Feature to @TestMethodOrder annotation
  As a developer
  I want to verify that the @TestMethodOrder annotation is correctly added to the generated test class
  So that test scenarios execute in the order they appear in the feature file

  Rule: @TestMethodOrder annotation controls scenario execution order
    The @TestMethodOrder annotation is added to every generated test class.
    The annotation value is MethodOrderer.OrderAnnotation.class.
    This ensures scenarios run in the order specified by their @Order annotations.

  Rule: @TestMethodOrder uses OrderAnnotation strategy
    The MethodOrderer.OrderAnnotation.class is from org.junit.jupiter.api.MethodOrderer.
    This ordering strategy uses the @Order annotation on each test method.
    Each scenario method gets @Order(1), @Order(2), etc., matching their position in the feature file.

  Rule: @TestMethodOrder annotation is always added
    Regardless of the number of scenarios, the annotation is always present.
    Even features with only one scenario get the @TestMethodOrder annotation.
    The annotation is added at the class level alongside @DisplayName and @Generated.

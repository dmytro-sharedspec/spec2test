Feature: adding @TestMethodOrder annotation
  As a developer maintaining executable specifications
  I want @TestMethodOrder annotation to ensure scenarios execute in their defined sequence
  So that test behavior matches the logical flow documented in the feature file

  Rule: @TestMethodOrder annotation is added to every generated test class.
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

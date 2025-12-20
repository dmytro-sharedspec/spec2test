Feature: Mapping Feature to @TestClassOrder annotation
  As a developer
  I want to verify that the @TestClassOrder annotation is conditionally added to the generated test class
  So that nested Rule classes execute in the order they appear in the feature file

  Rule: @TestClassOrder annotation is added only when the feature contains Rules
    The annotation is added if and only if the feature has at least one Rule element.
    Features with only Scenarios (no Rules) do not get the @TestClassOrder annotation.
    The presence of Rule elements triggers the addition of this annotation.

  Rule: @TestClassOrder uses OrderAnnotation strategy
    The annotation value is ClassOrderer.OrderAnnotation.class.
    This is from org.junit.jupiter.api.ClassOrderer.
    Each nested Rule class gets @Order(1), @Order(2), etc., matching their position in the feature file.

  Rule: @TestClassOrder controls nested class execution order
    The annotation ensures that Rule_1, Rule_2, Rule_3, etc., execute in order.
    Without this annotation, nested test classes might execute in an undefined order.
    The annotation works in conjunction with @Order annotations on each nested Rule class.

  Rule: @TestClassOrder is added at the class level
    The annotation appears on the main generated test class (not on nested Rule classes).
    It is positioned after @TestMethodOrder in the annotation list.
    It only appears when needed (when Rules are present).

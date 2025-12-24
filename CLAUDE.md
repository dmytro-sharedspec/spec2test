# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**spec2junit** is a compile-time code generator that converts natural-language behavior specifications (Gherkin/JBehave) into pure JUnit 5 test code. It eliminates runtime step discovery and regex glue patterns by generating strongly-typed Java test classes during compilation via annotation processing.

**Key principles:**
- Compile-time safety: Undefined steps become compiler errors, not runtime failures
- No regex glue: Eliminates brittle annotation patterns
- Plain JUnit 5: No custom test runners required
- Per-feature steps: No global step library to avoid ambiguity

## Build Commands

**IMPORTANT: Running Tests**
- **ALWAYS use IntelliJ IDEA's MCP server to run tests**, NOT Maven
- Use the `mcp__jetbrains__get_run_configurations` tool to list available run configurations
- Use the `mcp__jetbrains__execute_run_configuration` tool to execute specific tests
- To run ALL tests in the feature2junit module, run the test class: `Feature2JunitGeneratorAllTests`
- This provides better integration, faster feedback, and proper IDE support
- **Never use `mvn test` commands** unless explicitly requested by the user

**IMPORTANT: Compilation**
- **DO NOT run `mvn clean compile` or `mvn compile`** after making code changes
- IntelliJ IDEA is configured to build automatically when files are saved
- The annotation processor runs automatically during IntelliJ's build process
- Only use Maven build commands when explicitly requested by the user

### Standard Build
```bash
mvn clean install
```

### Compile Only
```bash
mvn clean compile
```

### Run All Tests
```bash
mvn clean test
```

### Run Tests for a Specific Module
```bash
# Test feature2junit module only
mvn clean test -pl feature2junit

# Test common module only
mvn clean test -pl common
```

### Run a Single Test Class
```bash
mvn test -Dtest=MappingFeatureTest -pl feature2junit
```

### Build Without Tests
```bash
mvn clean install -DskipTests
```

### Generate Javadocs
```bash
mvn javadoc:javadoc
```

### Build with Verbose Output (for debugging annotation processor)
```bash
mvn clean compile -X
```

### Deploy (requires GPG and credentials)
```bash
mvn clean deploy
```

## Multi-Module Architecture

This is a Maven multi-module project with these modules:

### 1. `common/`
Foundation module containing shared infrastructure:
- **GeneratorOptions**: Immutable configuration object controlling code generation behavior
- **Interface traits**: LoggingSupport, OptionsSupport, BaseTypeSupport (mixin pattern)
- **SourceLine**: Annotation for tracking feature file line numbers
- **ProcessingException**: Custom exception for annotation processing errors

### 2. `feature2junit/` (PRIMARY MODULE)
Annotation processor for Cucumber `.feature` files. This is the most mature and actively developed module.

**Public API:**
- `@Feature2JUnit("path/to/file.feature")` - Marks a class for test generation
- `@Feature2JUnitOptions` - Configures generation behavior (inheritable)

**Processing pipeline:**
```
Feature2JUnitGenerator (APT entry point)
  └→ TestSubclassCreator (orchestration)
      ├→ FeatureFileParser (Gherkin parsing)
      └→ FeatureProcessor (top-level)
          ├→ BackgroundProcessor (@BeforeEach generation)
          ├→ RuleProcessor (@Nested classes)
          └→ ScenarioProcessor (@Test/@ParameterizedTest)
              └→ StepProcessor (step method generation)
```

**Key processors location:**
- Entry: `src/main/java/dev/spec2test/feature2junit/Feature2JUnitGenerator.java`
- Orchestration: `src/main/java/dev/spec2test/feature2junit/TestSubclassCreator.java`
- Most complex: `src/main/java/dev/spec2test/feature2junit/gherkin/StepProcessor.java` (~478 lines)

**Utilities:** Located in `src/main/java/dev/spec2test/feature2junit/gherkin/utils/`
- MethodNamingUtils, ParameterNamingUtils, JavaDocUtils, TableUtils, TagUtils, etc.

### 3. `story2junit/`
Annotation processor for JBehave `.story` files. Less mature than feature2junit.

### 4. `examples/` (currently disabled)
Commented out in parent POM. Contains usage examples for feature2junit.

**Module build order:** common → feature2junit/story2junit (parallel) → examples

## Code Architecture

### Annotation Processing Flow
1. User annotates a class with `@Feature2JUnit("specs/cart.feature")`
2. During `javac`, Feature2JUnitGenerator runs
3. Parser reads .feature file using Cucumber's Gherkin parser
4. Processors convert Gherkin AST to JavaPoet code model
5. Generated test class written to `target/generated-test-sources/test-annotations/`

### Two Generation Patterns

**Pattern A - Abstract (default):**
```
UserFeature.java (@Feature2JUnit, abstract marker)
  ↓ generates
UserFeatureScenarios.java (abstract test class with abstract step methods)
  ↓ user creates
UserFeatureTest.java (implements abstract step methods)
```

**Pattern B - Concrete:**
```
UserFeature.java (@Feature2JUnit, implements step methods)
  ↓ generates
UserFeatureTest.java (concrete test class, calls base methods)
```

### Key Architectural Patterns

1. **Mixin Traits Pattern**: Processors implement LoggingSupport, OptionsSupport, BaseTypeSupport interfaces instead of inheritance
2. **Delegation over Inheritance**: Processors compose child processors rather than extending base classes
3. **Immutability**: GeneratorOptions is immutable (all fields final, no setters)
4. **Type-Safe Code Generation**: Uses JavaPoet, not string concatenation
5. **Layered Processing**: Generator → Creator → Parser → Processors → Utilities

### Gherkin Mapping

The codebase includes comprehensive test features documenting the Gherkin-to-JUnit mapping:
- `feature2junit/src/test/resources/features/MappingFeature.feature` - Feature-level mappings
- `feature2junit/src/test/resources/features/MappingRule.feature` - Rule mappings
- `feature2junit/src/test/resources/features/MappingScenario.feature` - Scenario mappings
- `feature2junit/src/test/resources/features/steps/MappingSteps.feature` - Step mappings

Key mappings:
- Feature → JUnit test class
- Background → @BeforeEach method
- Rule → @Nested test class
- Scenario → @Test method
- Scenario Outline → @ParameterizedTest with @CsvSource
- Steps → Method calls with extracted parameters
- Tags → @Tag annotations
- DataTables → DataTable objects
- DocStrings → String parameters

## Technology Stack

- **Java:** 17+
- **Build:** Maven 3.x
- **Code Generation:** JavaPoet 1.13.0
- **Gherkin Parsing:** Cucumber Java 7.23.0
- **Testing:** JUnit 5.10.2, Mockito 5.18.0, Cucumber JUnit Platform Engine 7.14.0
- **APT Registration:** Google Auto Service 1.1.1

## Common Development Tasks

### Adding a New Gherkin Element Processor
1. Create processor class in `feature2junit/src/main/java/dev/spec2test/feature2junit/gherkin/`
2. Implement LoggingSupport, OptionsSupport, BaseTypeSupport
3. Add processing logic in parent processor
4. Add utilities to `utils/` if needed
5. Add test cases in `src/test/`

### Modifying Generation Behavior
1. Add option to `common/src/main/java/dev/spec2test/common/GeneratorOptions.java`
2. Add annotation parameter to `Feature2JUnitOptions.java`
3. Update GeneratorOptions construction in `Feature2JUnitGenerator.process()`
4. Use option in relevant processor
5. Update tests

### Debugging Generated Code
- Generated sources: `target/generated-test-sources/test-annotations/`
- Enable verbose Maven output: `mvn clean compile -X`
- Processor logs prefixed with `[Feature2JUnitGenerator]`
- Use @SourceLine annotations for navigation back to .feature files

### Working with Step Processing
Most step-related logic is in StepProcessor.java:478. Key responsibilities:
- Extract parameters from quoted text using regex: `(?<parameter>(\")(?<parameterValue>[^\"]+?)(\"))`
- Handle DataTables and DocStrings
- Generate method signatures
- Deduplicate methods (check base class hierarchy)
- Optional Cucumber annotation generation (@Given, @When, @Then)

## Working with Files

**IMPORTANT: When the user asks you to update, modify, or change a file, do it immediately without asking for permission.**

The user expects you to make changes directly when requested. Only ask clarifying questions if the requirements are ambiguous, not for permission to proceed.

## Working with Cucumber .feature Files

**IMPORTANT: Feature File Naming Convention**

When creating a new `.feature` file, always follow this naming rule for IntelliJ IDEA integration:

The first line of the feature file should be:
```gherkin
Feature: FeatureFileName
```

Where `FeatureFileName` is the name of the feature file WITHOUT the `.feature` extension.

**Example:**
If creating a file named `MappingStepDataTables.feature`, the first line must be:
```gherkin
Feature: MappingStepDataTables
```

**Why this matters:**
- IntelliJ IDEA uses the Feature name as a node in the Run tool window
- This convention ensures the feature file name is clearly visible when running tests
- It provides consistency between the file name and the feature name displayed in test results

**Additional .feature File Guidelines:**
- Place test feature files in `feature2junit/src/test/resources/features/`
- Feature files serve as living documentation of the Gherkin-to-JUnit mapping
- Always run tests using IntelliJ IDEA's MCP server tools, not Maven commands

## Git Workflow

**IMPORTANT: Always stage changes immediately after making them.**

Whenever you create OR modify a file, you MUST run `git add <file>` to stage it to the git index. This applies to:
- New files created
- Modified files (source code, tests, documentation, feature files, etc.)
- Deleted files

Example:
```bash
# After creating or modifying files
git add path/to/modified/file.java
git add path/to/modified/test.feature

# Or stage multiple files at once
git add file1.java file2.feature file3.md
```

This ensures all changes are tracked and ready for commit.

## Important Notes

- **Always build common first**: It's the foundation for other modules
- **feature2junit is primary**: Most mature and actively developed
- **story2junit is experimental**: Less mature, fewer features
- **Examples are disabled**: Comment is in parent pom.xml line 38
- **No .cursorrules**: This project doesn't have AI assistant rules configured
- **Comprehensive README**: 1800+ lines of detailed documentation in README.md
- **Self-documenting tests**: Test .feature files serve as living documentation of the Gherkin-to-JUnit mapping

## Testing Strategy

The project uses a self-hosting approach - Cucumber tests validate the Cucumber-to-JUnit generator:
- Test features in `feature2junit/src/test/resources/features/`
- Test implementations verify generated code correctness
- MappingSteps.feature documents the complete mapping specification

## Generated Code Location

By default: `target/generated-test-sources/test-annotations/`

Can be customized via GeneratorOptions.placeGeneratedClassNextToAnnotatedClass to place generated files next to annotated classes.
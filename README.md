# spec2test

**spec2test** turns human-readable Gherkin specs into **pure JUnit** test code at **compile time**.
No regex “glue,” no runtime step discovery. Your `.feature` files become first-class Java code that compiles, runs, and fails fast.

> Built around an annotation-processor approach (`feature2junit`) that parses feature files during `javac`, generating JUnit test skeletons where each **Given/When/Then** is converted into a strongly-named Java method call.

---

## Why spec2test?

* **Compile-time safety:** Eliminates “undefined step” surprises at runtime—mismatches surface as **compiler errors**.
* **No regex glue:** Avoids brittle annotation regexes and accidental ambiguous matches.
* **Spec-driven automation:** The **text in your feature** drives the generated method names and call sequence—**per-feature** scope, not a global step library.
* **Simpler CI:** Fewer moving parts; tests are plain JUnit. If it compiles, it’s wired.
* **IDE-friendly:** Navigate, search, and refactor using standard Java tooling. *(Step renames happen by changing the spec; the generator updates method names accordingly.)*

---

## How it works (at a glance)

1. You point the **annotation processor** at a `.feature` file (e.g., via `@Feature2JUnit("path/to/feature.feature")`).
2. During compilation, `feature2junit` parses the feature and generates:

   * A **JUnit test class** (one per feature).
   * For each Scenario, a **`@Test`** method that calls **per-step methods** derived from step text.
   * **Typed parameters** for step arguments (quotes in steps become `String` params; numbers become `int/long/double`, etc.). <!-- TODO: Confirm numeric typing rules -->
3. You implement automation **per feature** (no shared global step library).
   Common patterns:

   * **Delegate pattern (recommended):** Generated step methods call into your hand-written `*Steps` class.
   * **Direct fill-in (prototype):** Temporarily implement TODOs in generated code (be aware: regenerated on compile). <!-- TODO: Confirm your preferred pattern -->

---

## Example

### Input (`src/test/java/specs/login.feature`)

```gherkin
Feature: Login
  Scenario: Valid login
    Given the user is on the login page
    When they log in with "alice" and "secret"
    Then they see the dashboard
```

### Trigger (marker source)

```java
// src/test/java/specs/LoginFeature.java
import com.yourorg.spec2test.Feature2JUnit; // TODO: confirm package

@Feature2JUnit("specs/login.feature")
public interface LoginFeature { }
```

### Generated (simplified)

```java
// target/generated-sources/spec2test/specs/LoginFeatureTest.java
// @Generated – do not edit
class LoginFeatureTest {

  @org.junit.jupiter.api.Test
  void scenario_valid_login() {
    step_the_user_is_on_the_login_page();
    step_they_log_in_with("alice", "secret");
    step_they_see_the_dashboard();
  }

  // Per-feature, spec-driven step methods:
  void step_the_user_is_on_the_login_page() { /* TODO implement */ }

  void step_they_log_in_with(String username, String password) { /* TODO implement */ }

  void step_they_see_the_dashboard() { /* TODO implement */ }
}
```

> Change the step text in the `.feature` file → rerun compile → the corresponding method names in the generated class change accordingly. If your delegate can’t be found or signatures don’t match, you get a **compile-time** error—not a runtime “undefined step”.

---

## Installation

> **Requirements:** Java **17+** (TBD), Maven/Gradle with **annotation processing** enabled, IDE with APT enabled (e.g., IntelliJ).
> **Project layout:** By default, features can live under `src/test/java` or `src/test/resources`. <!-- TODO: Confirm your default path (you mentioned `src/test/java`) -->

### Maven (example)

```xml
<dependencies>
  <!-- spec2test annotation + SPI -->
  <dependency>
    <groupId>com.yourorg.spec2test</groupId>      <!-- TODO -->
    <artifactId>feature2junit</artifactId>        <!-- TODO -->
    <version>0.1.0</version>                      <!-- TODO -->
    <scope>provided</scope>
  </dependency>
</dependencies>

<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.13.0</version>
      <configuration>
        <annotationProcessorPaths>
          <path>
            <groupId>com.yourorg.spec2test</groupId>  <!-- TODO -->
            <artifactId>feature2junit</artifactId>    <!-- TODO -->
            <version>0.1.0</version>                  <!-- TODO -->
          </path>
        </annotationProcessorPaths>
        <compilerArgs>
          <!-- Optional processor args -->
          <!-- <arg>-AfeatureRoot=src/test/java</arg> -->  <!-- TODO: confirm option names -->
        </compilerArgs>
      </configuration>
    </plugin>
  </plugins>
</build>
```

### Gradle (example – Kotlin DSL)

```kotlin
dependencies {
    compileOnly("com.yourorg.spec2test:feature2junit:0.1.0") // TODO
    annotationProcessor("com.yourorg.spec2test:feature2junit:0.1.0")
    testAnnotationProcessor("com.yourorg.spec2test:feature2junit:0.1.0")
}
```

> **IntelliJ:** Enable *Annotation Processing* in **Settings → Build, Execution, Deployment → Compiler → Annotation Processors**.

---

## Usage

1. **Create or place** a `.feature` file under your chosen root (e.g., `src/test/java/specs/...`).
2. **Add a marker type** annotated with `@Feature2JUnit("relative/path/to/feature.feature")`.
3. **Build** the project. The generator writes JUnit sources under your build’s generated-sources dir.
4. **Implement steps** (per feature):

   * **Delegate pattern (recommended):**

     ```java
     // hand-written class you own
     public final class LoginFeatureSteps {
       public void the_user_is_on_the_login_page() { /* open login page */ }
       public void they_log_in_with(String u, String p) { /* fill & submit */ }
       public void they_see_the_dashboard() { /* assert dashboard visible */ }
     }
     ```

     And in the generated test, call your delegate (pattern varies; see Configuration). <!-- TODO: confirm how delegate is wired (naming convention? injection?) -->
   * **Direct fill-in (prototype):** Replace TODOs in the generated class.
5. **Run tests** with your normal JUnit runner (IDE, Maven/Gradle, CI).

---

## Configuration (processor options)

<!-- TODO: Fill in actual option names / defaults -->

* `featureRoot` – where to resolve relative `.feature` paths (default: `src/test/java`).
* `outputPackage` – override generated package.
* `naming.case` – method/class name strategy (e.g., snake\_case vs camelCase).
* `tables.mode` – how to map Data Tables (e.g., `List<Map<String,String>>` vs typed records).
* `scenarioOutlines` – generate parameterized tests for `Scenario Outline`.

---

## What it is / What it isn’t

**It is**

* A **compile-time** bridge from Gherkin to **plain JUnit**.
* A per-feature, spec-driven test skeleton generator.

**It isn’t**

* A Cucumber/JBehave runner (no runtime step discovery, no regex glue).
* A shared step catalog. Steps are **scoped to a feature** by design.

**Trade-off:** You get deterministic, per-feature wiring and compile-time feedback at the cost of **less cross-feature step reuse** and **more generated code** (which your build handles automatically).

---

## Failure modes & ergonomics

* **Changed step text?** Recompile → generator updates method names. If your delegate no longer matches, you get a **compile error** (early, fast).
* **Wrong parameter shape?** Compile-time signature mismatch highlights the line. <!-- TODO: confirm typing rules -->
* **No delegate found?** Compile fails instead of a runtime “undefined step”.

---

## Roadmap

* [ ] Confirm & document **delegate wiring** (naming convention, service loader, DI hook).
* [ ] First-class **Scenario Outline** → JUnit parameterized tests.
* [ ] Richer **Data Table** mappings (typed records, converters).
* [ ] Gradle plugin & Maven starter archetype.
* [ ] IntelliJ File Watcher / APT refresh tips.
* [ ] Example projects (web UI, API, service).

---

## Contributing

Issues and PRs welcome. Please include:

* The `.feature` example
* The generated code (from `target/generated-sources` or `build/generated`)
* Your build tool and JDK version

---

## License

<!-- TODO: MIT/Apache-2.0? -->

`TBD`

---

### Status badge ideas (optional)

* Build: CI ✅
* Java: 17+
* License: MIT / Apache-2.0 (TBD)

---

## Appendix: Cucumber/JBehave vs spec2test

| Topic             | Cucumber/JBehave                        | spec2test                               |
| ----------------- | --------------------------------------- | --------------------------------------- |
| Wiring            | Regex in annotations; runtime discovery | **Compile-time** generated JUnit        |
| Failure surface   | Often runtime “undefined step”          | **Compiler errors** on mismatch         |
| Step granularity  | Shared/global libraries                 | **Per-feature scoped**                  |
| Tooling           | Custom runner & plugins                 | Plain JUnit (IDE/CI native)             |
| Refactor strategy | Rename step text + update regex         | Change spec → generator updates methods |

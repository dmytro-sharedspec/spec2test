# spec2test

**spec2test** turns natural-language Gherkin specs into **pure JUnit** test code at **compile time**.
No regex “glue,” no runtime step discovery. Your `.feature` files become first-class Java code that compiles, runs, and fails fast.

> Built around an annotation-processor approach (`feature2junit`) that parses feature files during `javac`, generating JUnit test skeletons where each **Given/When/Then** is converted into a strongly-named Java method call.

---

## Why spec2test?

* **Compile-time safety:** Eliminates “undefined step” surprises at runtime—mismatches surface as **compiler errors**.

* **No regex glue:** Avoids brittle annotation regexes and accidental ambiguous matches.

* **Plain JUnit 5:** No Cucumber runner—tests run as standard JUnit 5, which makes execution more straightforward and is more friendly to developer tooling and CI. Use find usages, run individual Scenarios/Rules, set breakpoints, and debug like any JUnit test.

* **Spec-driven automation:** The **text in your feature** drives the generated method names and call sequence—**per-feature** scope, not a global step library.

* **Simpler CI:** Fewer moving parts; tests are plain JUnit. If it compiles, it’s wired.&#x20;

* **TDD-friendly:** Enables straightforward, **iterative** test‑first development—even before any application or test code exists. Start with an abstract, implementation‑free spec (e.g., only Rule and/or Scenario titles). The generator creates a failing JUnit method for each empty Rule/Scenario, so you immediately have red tests to drive development.

  * Iterate: list Rules → add Scenario titles under the first Rule → pick one Scenario and add concrete steps in the Gherkin feature (still red; the generator turns them into failing step methods in the test) → implement those step methods → then implement just enough application code to make it pass (green) → repeat for the next Scenario. When all Scenarios under a Rule are green, move on to the next Rule.
  * Keep discovering: add new Scenario or Rule titles anytime; they show up as failing tests until implemented.

---

## How it works (at a glance)

1. Create an **abstract marker class** annotated with `@Feature2JUnit("relative/path/to.feature")` — this points the annotation processor at the feature.
2. During compilation, `feature2junit` parses the feature and generates:

   * A **JUnit test class** (one per feature).
   * For each Scenario, a `@Test` method that calls **per-step methods** derived from step text.
   * Parts of step's text that are wrapped in double quotes become step method arguments. [DocStrings](https://cucumber.io/docs/gherkin/reference/#doc-strings) and [Data Tables](https://cucumber.io/docs/gherkin/reference/#data-tables) are also supported. 
   * Gherkin `Rule` elements are generated as nested test classes, and `Rule` and `Scenario` titles populate JUnit's `@DisplayName` annotations.
3. You implement automation **per feature** (no shared global step library).
   See **Usage examples** for the two supported patterns.

---

## Usage example

1. **Create or place** a `.feature` file under your chosen directory (e.g., `src/test/resources/specs/...`).

**Example** (`specs/cart.feature`):

```gherkin
Feature: Online shopping cart

  Scenario: Update quantity updates subtotal
    Given my cart contains "Wireless Headphones" with quantity "1" and unit price "60.00"
    When I change the quantity to "2"
    Then my cart subtotal is "120.00"

  Rule: Free shipping applies to orders over €50

    Scenario: Show free-shipping banner when threshold is met
      Given my cart subtotal is "55.00"
      When I view the cart
      Then I see the "Free shipping" banner
```

2. **Add a marker class** annotated with `@Feature2JUnit("specs/cart.feature")`.

```java
package org.mycompany.app;

import dev.spec2test.feature2junit.Feature2JUnit;

@Feature2JUnit("specs/cart.feature")
public abstract class CartFeature {
    // Marker class: no members required
}
```

2. **Build** the project. The generator writes JUnit sources under your build’s generated-sources dir.
3. **Choose a usage pattern** (per feature):

   **Pattern A (default) — Extend the generated abstract test class (implement abstract methods)**

   * First compile: the generator produces an **abstract** test class; each step method is **abstract** (no body).

   * You create a subclass and implement the step methods.

   * Rebuild and run; the subclass is executed by JUnit.

<details>
 
<summary>Generated class:</summary>

```java
package org.mycompany.app;

import dev.spec2test.feature2junit.FeatureFilePath;
import java.lang.String;
import javax.annotation.processing.Generated;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * To implement tests in this generated class, extend it and implement all abstract methods.
 */
@Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@FeatureFilePath("specs/cart.feature")
public abstract class CartFeatureScenarios extends CartFeature {
    {
        /**
         * Feature: online shopping cart
         */
    }

    public abstract void givenMyCartContains$p1WithQuantity$p2AndUnitPrice$p3(String p1, String p2, String p3);

    public abstract void whenIChangeTheQuantityTo$p1(String p1);

    public abstract void thenMyCartSubtotalIs$p1(String p1);

    @Test
    @Order(1)
    @DisplayName("Scenario: update quantity updates subtotal")
    public void scenario_1() {
        /**
         * Given my cart contains "Wireless Headphones" with quantity "1" and unit price "60.00"
         */
        givenMyCartContains$p1WithQuantity$p2AndUnitPrice$p3("Wireless Headphones", "1", "60.00");
        /**
         * When I change the quantity to "2"
         */
        whenIChangeTheQuantityTo$p1("2");
        /**
         * Then my cart subtotal is "120.00"
         */
        thenMyCartSubtotalIs$p1("120.00");
    }

    public abstract void givenMyCartSubtotalIs$p1(String p1);

    public abstract void whenIViewTheCart();

    public abstract void thenISeeThe$p1Banner(String p1);

    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Rule: free shipping applies to orders over €50")
    public class Rule_1 {
        @Test
        @Order(1)
        @DisplayName("Scenario: show free-shipping banner when threshold is met")
        public void scenario_1() {
            /**
             * Given my cart subtotal is "55.00"
             */
            givenMyCartSubtotalIs$p1("55.00");
            /**
             * When I view the cart
             */
            whenIViewTheCart();
            /**
             * Then I see the "Free shipping" banner
             */
            thenISeeThe$p1Banner("Free shipping");
        }
    }
}
```

</details>

<details>

<summary>Your implementation:</summary>

```java
package org.mycompany.app;

public class CartFeatureTest extends CartFeatureScenarios {

    @Override
    public void givenMyCartContains$p1WithQuantity$p2AndUnitPrice$p3(String p1, String p2, String p3) {
       /* real implementation here */
    }

    @Override
    public void whenIChangeTheQuantityTo$p1(String p1) {
       /* real implementation here */
    }

    @Override
    public void thenMyCartSubtotalIs$p1(String p1) {
       /* real implementation here */
    }

    @Override
    public void givenMyCartSubtotalIs$p1(String p1) {
        /* real implementation here */
    }

    @Override
    public void whenIViewTheCart() {
        /* real implementation here */
    }

    @Override
    public void thenISeeThe$p1Banner(String p1) {
        /* real implementation here */
    }
}
```

</details>

**Pattern B — Generated test class is concrete (copy failing step methods into the base class)**

* First compile: the generator produces a **concrete** test class; each step method contains a **failing assertion** (e.g., `org.junit.jupiter.api.Assertions.fail("Step is not yet implemented")`).
* Copy the generated step methods (signatures and names) into the **base marker class** annotated with `@Feature2JUnit`, and provide real implementations there.
* Rebuild; the generated test now calls your implementations, and it no longer generates the failing method stubs
* You run the generated test class directly

<details>
 
<summary>Generated (first pass):</summary>

```java
package org.mycompany.app;

import dev.spec2test.feature2junit.FeatureFilePath;
import java.lang.String;
import javax.annotation.processing.Generated;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * To implement tests in this generated class, move any methods with failing assumptions into the base
 * class and implement them.
 */
@Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@FeatureFilePath("specs/cart.feature")
public class CartFeatureTest extends CartFeature {
    {
        /**
         * Feature: online shopping cart
         */
    }

    public void givenMyCartContains$p1WithQuantity$p2AndUnitPrice$p3(String p1, String p2,
            String p3) {
        Assertions.fail("Step is not yet implemented");
    }

    public void whenIChangeTheQuantityTo$p1(String p1) {
        Assertions.fail("Step is not yet implemented");
    }

    public void thenMyCartSubtotalIs$p1(String p1) {
        Assertions.fail("Step is not yet implemented");
    }

    @Test
    @Order(1)
    @DisplayName("Scenario: update quantity updates subtotal")
    public void scenario_1() {
        /**
         * Given my cart contains "Wireless Headphones" with quantity "1" and unit price "60.00"
         */
        givenMyCartContains$p1WithQuantity$p2AndUnitPrice$p3("Wireless Headphones", "1", "60.00");
        /**
         * When I change the quantity to "2"
         */
        whenIChangeTheQuantityTo$p1("2");
        /**
         * Then my cart subtotal is "120.00"
         */
        thenMyCartSubtotalIs$p1("120.00");
    }

    public void givenMyCartSubtotalIs$p1(String p1) {
        Assertions.fail("Step is not yet implemented");
    }

    public void whenIViewTheCart() {
        Assertions.fail("Step is not yet implemented");
    }

    public void thenISeeThe$p1Banner(String p1) {
        Assertions.fail("Step is not yet implemented");
    }

    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Rule: free shipping applies to orders over €50")
    public class Rule_1 {
        @Test
        @Order(1)
        @DisplayName("Scenario: show free-shipping banner when threshold is met")
        public void scenario_1() {
            /**
             * Given my cart subtotal is "55.00"
             */
            givenMyCartSubtotalIs$p1("55.00");
            /**
             * When I view the cart
             */
            whenIViewTheCart();
            /**
             * Then I see the "Free shipping" banner
             */
            thenISeeThe$p1Banner("Free shipping");
        }
    }
}
```

</details>

<details>

<summary>Your implementation:</summary>

```java
package org.mycompany.app;

import dev.spec2test.feature2junit.Feature2JUnit;
import dev.spec2test.feature2junit.Feature2JUnitOptions;

@Feature2JUnitOptions(
    shouldBeAbstract = false
)
@Feature2JUnit("specs/cart.feature")
public class CartFeature {

    public void givenMyCartContains$p1WithQuantity$p2AndUnitPrice$p3(String p1, String p2, String p3) {
        /* real implementation here */
    }

    public void whenIChangeTheQuantityTo$p1(String p1) {
        /* real implementation here */
    }

    public void thenMyCartSubtotalIs$p1(String p1) {
        /* real implementation here */
    }

    public void givenMyCartSubtotalIs$p1(String p1) {
        /* real implementation here */
    }

    public void whenIViewTheCart() {
        /* real implementation here */
    }

    public void thenISeeThe$p1Banner(String p1) {
        /* real implementation here */
    }

}
```

</details>

<details>

 <summary>Generated class (second pass):</summary>

```java
package org.mycompany.app;

import dev.spec2test.feature2junit.FeatureFilePath;
import javax.annotation.processing.Generated;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * To implement tests in this generated class, move any methods with failing assumptions into the base
 * class and implement them.
 */
@Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@FeatureFilePath("specs/cart.feature")
public class CartFeatureTest extends CartFeature {
    {
        /**
         * Feature: online shopping cart
         */
    }

    @Test
    @Order(1)
    @DisplayName("Scenario: update quantity updates subtotal")
    public void scenario_1() {
        /**
         * Given my cart contains "Wireless Headphones" with quantity "1" and unit price "60.00"
         */
        givenMyCartContains$p1WithQuantity$p2AndUnitPrice$p3("Wireless Headphones", "1", "60.00");
        /**
         * When I change the quantity to "2"
         */
        whenIChangeTheQuantityTo$p1("2");
        /**
         * Then my cart subtotal is "120.00"
         */
        thenMyCartSubtotalIs$p1("120.00");
    }

    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Rule: free shipping applies to orders over €50")
    public class Rule_1 {
        @Test
        @Order(1)
        @DisplayName("Scenario: show free-shipping banner when threshold is met")
        public void scenario_1() {
            /**
             * Given my cart subtotal is "55.00"
             */
            givenMyCartSubtotalIs$p1("55.00");
            /**
             * When I view the cart
             */
            whenIViewTheCart();
            /**
             * Then I see the "Free shipping" banner
             */
            thenISeeThe$p1Banner("Free shipping");
        }
    }
}
```

</details>

### When to use which pattern

**Pattern A — Generated test class is abstract**

* **Use when:** You want a strict separation between generated and hand‑written code; or when you prefer clear override points and standard OO patterns.
* **Benefits:** Enables parallel implementations of the same Scenarios—for example, a REST-based subclass (faster, less timeout-prone) and a UI-driven subclass (slower, more timeout-prone). This can enable you to keep a fast test suite for local development and a deeper end-to-end CI suite when you need maximum confidence.
  &#x20;
* **Trade‑offs:** One extra subclass per feature to maintain.

**Pattern B — Generated test class is concrete**

* **Use when:** You want the most straightforward **TDD flow** with immediate red tests; you prefer fewer classes; step implementations should live next to the marker/base class for simplicity.
* **Benefits:** Fast start, failing stubs make the next task obvious;  step implementations are reusable via inheritance.
* **Trade‑offs:** Initial one‑time **copy** of method stubs and whenever step text changes.

---

## Configuration

All configuration is provided via the `@Feature2JUnitOptions` annotation. You can place this annotation:

* **On the marker class** (applies to that feature only).
* **On a shared base test class** (options are **inherited** by subclasses/marker classes in your test hierarchy).

A typical option controls whether the generated test class should be **abstract or concrete**, which maps directly to the two usage patterns above. For the complete list of options and defaults, refer to the `@Feature2JUnitOptions` JavaDoc or the annotation source code.

<details>

<summary>Example — per‑feature options on the marker class</summary>

```java
import dev.spec2test.feature2junit.Feature2JUnit;
import dev.spec2test.feature2junit.Feature2JUnitOptions;

@Feature2JUnitOptions(shouldBeAbstract = true) // or false for concrete + failing bodies
@Feature2JUnit("specs/cart.feature")
public abstract class CartFeature { }
```

</details>

<details>

 <summary>Example — inherited options via a base class</summary>

```java
import dev.spec2test.feature2junit.Feature2JUnit;
import dev.spec2test.feature2junit.Feature2JUnitOptions;

@Feature2JUnitOptions(shouldBeAbstract = false)
public abstract class BaseFeatureOptions { }

@Feature2JUnit("specs/cart.feature")
public abstract class CartFeature extends BaseFeatureOptions { }
```

</details>

---

## Details of mapping Gherkin → Jnit  

All elements of Gherkin are supported, please refer to below sections for details


<details>

<summary>Feature</summary>

+ The feature’s keyword, title, and description lines appear in a block comment at the top of the generated class.

<table>
  <tr>
    <th align="left">Gherkin</th>
    <th align="left">JUnit</th>
  </tr>
  <tr>
    <td valign="top" class="diffTable" style="padding: 0px; font-size: larger;"><pre><code class="language-gherkin" data-lang="gherkin">

```gherkin
Feature: Shopping cart totals and shipping

  As a shopper
  I want the cart to keep item totals accurate and show free shipping when eligible
  So that I can check out with confidence and avoid extra costs
```
  </code></pre>
    </td>
    <td valign="top">
     <pre>
       <code class="language-java" data-lang="java">

```java
 public class CartFeatureScenarios extends CartFeature {
    {
        /**
         * Feature: Shopping cart totals and shipping
         *   As a shopper
         *   I want the cart to keep item totals accurate and show free shipping when eligible
         *   So that I can check out with confidence and avoid extra costs
         */
    }
}
```
 
</code></pre></td>
</tr>
</table>
 
</details>

<details>

<summary>Rule</summary>

+ Rule sections are mapped to nested test classes inside the generated test class.

+ Rule's keyword & title are put into 
the value of the @DisplayName JUnit annotation.

+ If a rule additionally has description lines then those are put into
a JavaDoc comment above the nested class.

<table>
  <tr>
    <th align="left">Gherkin</th>
    <th align="left">JUnit</th>
  </tr>
  <tr>
    <td valign="top" class="diffTable" style="padding: 0px; font-size: larger;"><pre><code class="language-gherkin" data-lang="gherkin">

```gherkin
Feature: Shopping cart totals and shipping

  Rule: Cannot checkout with an empty cart

  Rule: Free shipping applies when subtotal is at least €50
    Orders at or above €50 show a "Free shipping" banner; lower subtotals show the shipping cost.
```
  </code></pre>
    </td>
    <td valign="top">
     <pre>
       <code class="language-java" data-lang="java">

```java
public class CartFeatureScenarios extends CartFeature {
    {
        /**
         * Feature: Shopping cart totals and shipping
         */
    }

    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Rule: Cannot checkout with an empty cart")
    public class Rule_1 {
        @Test
        public void noScenariosInRule() {
            Assertions.fail("Rule doesn't have any scenarios");
        }
    }

    /**
     * Orders at or above €50 show a "Free shipping" banner; lower subtotals show the shipping cost.
     */
    @Nested
    @Order(2)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Rule: Free shipping applies when subtotal is at least €50")
    public class Rule_2 {
        @Test
        public void noScenariosInRule() {
            Assertions.fail("Rule doesn't have any scenarios");
        }
    }
}
```
 
</code></pre></td>
</tr>
</table>
 
</details>

<details>

<summary>Scenario</summary>

+ Scenario sections are mapped to test methods that are annotated with JUnit's @Test annotation.

+ Scenario's keyword & title are put into the value of the @DisplayName annotation.

+ If scenario additionally has description lines then those are put into a JavaDoc comment
  above the test method.

<table>
  <tr>
    <th align="left">Gherkin</th>
    <th align="left">JUnit</th>
  </tr>
  <tr>
    <td valign="top" class="diffTable" style="padding: 0px; font-size: larger;"><pre><code class="language-gherkin" data-lang="gherkin">

```gherkin
Feature: Shopping cart totals and shipping

  Scenario: Update quantity updates subtotal

  Rule: Free shipping applies when subtotal is at least €50

    Scenario: Show free-shipping banner when threshold is met
      It covers the visual banner only; actual shipping cost calculation is out of scope.
```
  </code></pre>
    </td>
    <td valign="top">
     <pre>
       <code class="language-java" data-lang="java">

```java

public class CartFeatureScenarios extends CartFeature {
    {
        /**
         * Feature: Shopping cart totals and shipping
         */
    }

    @Test
    @Order(1)
    @DisplayName("Scenario: Update quantity updates subtotal")
    public void scenario_1() {
        Assertions.fail("Scenario has no steps");
    }

    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Rule: Free shipping applies when subtotal is at least €50")
    public class Rule_1 {
        /**
         * It covers the visual banner only; actual shipping cost calculation is out of scope.
         */
        @Test
        @Order(1)
        @DisplayName("Scenario: Show free-shipping banner when threshold is met")
        public void scenario_1() {
            Assertions.fail("Scenario has no steps");
        }
    }
}

```
 
</code></pre></td>
</tr>
</table>
 
</details>

<details>

<summary>Given, When, Then, And & But steps</summary>

#### Step keywords → method prefixes

+ Given … → method starts with given…

+ When … → when…

+ Then … → then…

+ And … / But … → inherit the previous step’s keyword (e.g., after a When, And becomes when…)

#### Method name derivation (from step text)

+ Take the step’s **plain text** (minus any quoted arguments), split into words, and **CamelCase** them.

+ **Invalid Java identifier** characters (at the start or in the middle) are **removed**.

+ The keyword prefix (`given/when/then`) is prepended.

+ Where the step had quoted arguments, the method name includes **positional placeholders** to indicate argument slots (e.g., `$p1`, `$p2`, …).

> Resulting shape: 
`given` + `CamelCasedWordsAroundArgsWithPlaceholders`
> 
> e.g., `givenMyCartContains$p1WithQuantity$p2AndUnitPrice$p3(...)` 

<table>
  <tr>
    <th align="left">Gherkin</th>
    <th align="left">JUnit</th>
  </tr>
  <tr>
    <td valign="top" class="diffTable" style="padding: 0px; font-size: larger;"><pre><code class="language-gherkin" data-lang="gherkin">

```gherkin
Feature: Shopping cart totals and shipping

  Scenario: Update quantity updates subtotal
    Given my cart contains "Wireless Headphones" with quantity "1" and unit price "60.00"
    When I change the quantity to "2"
    Then my cart subtotal is "120.00"

  Rule: Free shipping applies when subtotal is at least €50

    Scenario: Show free-shipping banner when threshold is met
      Given my cart subtotal is "55.00"
      When I view the cart
      Then I see the "Free shipping" banner
```
  </code></pre>
    </td>
    <td valign="top">
     <pre>
       <code class="language-java" data-lang="java">

```java

public class CartFeatureScenarios extends CartFeature {
    {
        /**
         * Feature: Shopping cart totals and shipping
         */
    }

    @Test
    @Order(1)
    @DisplayName("Scenario: Update quantity updates subtotal")
    public void scenario_1() {
        Assertions.fail("Scenario has no steps");
    }

    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Rule: Free shipping applies when subtotal is at least €50")
    public class Rule_1 {
        /**
         * It covers the visual banner only; actual shipping cost calculation is out of scope.
         */
        @Test
        @Order(1)
        @DisplayName("Scenario: Show free-shipping banner when threshold is met")
        public void scenario_1() {
            Assertions.fail("Scenario has no steps");
        }
    }
}

```
 
</code></pre></td>
</tr>
</table>
 
</details>

---

## Installation

> **Requirements:** Java **17+**, JUnit 5, Maven/Gradle with **annotation processing** enabled, IDE with APT enabled (e.g., IntelliJ).

<details>

<summary>Maven example</summary>

```xml
<dependencies>
  <!-- spec2test annotation + SPI -->
  <dependency>
    <groupId>com.yourorg.spec2test</groupId>
    <artifactId>feature2junit</artifactId>
    <version>0.1.4</version>
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
          <!-- specifying annotation processor paths like this is usually optional -->
          <!-- as they are typically auto discovered from the classpath            -->
          <path>
            <groupId>com.yourorg.spec2test</groupId>
            <artifactId>feature2junit</artifactId>
            <version>0.1.4</version>
          </path>
        </annotationProcessorPaths>
      </configuration>
    </plugin>
  </plugins>
</build>
```

</details>

---

## What it is / What it isn’t

**It is**

* A **compile-time** bridge from Gherkin to **plain JUnit**.
* A per-feature, spec-driven test skeleton generator.

**It isn’t**

* A Cucumber/JBehave runner (no runtime step discovery, no regex glue).
* A shared step catalog. Steps are **scoped to a feature** by design.

---

## Contributing

Issues and PRs welcome. Please include:

* The `.feature` example
* The generated code (from `target/generated-sources`)
* Your build tool and JDK version

---

## License

GNU General Public License v3.0

---

## Appendix: Cucumber/JBehave vs spec2test

| Topic                     | Cucumber/JBehave                                              | spec2test                                           |
| ------------------------- | ------------------------------------------------------------- | --------------------------------------------------- |
| Wiring                    | Regex in annotations; runtime discovery                       | **Compile-time** generated JUnit                    |
| Failure surface           | Often runtime “undefined step”                                | **Compiler errors** on mismatch                     |
| Step scope                | Shared/global libraries                                       | **Per-feature scoped**                              |
| Step refactoring strategy | Search & replace text (often via complex regular expressions) | Compiler errors, method rename & inline refactoring |
| Test Runner               | Custom runner & plugins                                       | Plain JUnit                                         |
|                           |                                                               |                                                     |

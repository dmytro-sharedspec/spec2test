package dev.spec2test.feature2junit.tests.troubleshooting;

import dev.spec2test.feature2junit.FeatureFilePath;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.*;

import javax.annotation.processing.Generated;

/**
 * Feature: customer management
 */
@DisplayName("MockedAnnotatedTestClass")
@Generated("dev.spec2test.feature2junit.Feature2JUnitGenerator")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@FeatureFilePath("MockedAnnotatedTestClass.feature")
public abstract class MockedTestClassScenarios {
    @Given("^customer database is connected$")
    public abstract void givenCustomerDatabaseIsConnected();

    @BeforeEach
    @DisplayName("Background:")
    public void featureBackground(TestInfo testInfo) {
        /**
         * Given customer database is connected
         */
        givenCustomerDatabaseIsConnected();
    }

    @When("^new customer is created$")
    public abstract void whenNewCustomerIsCreated();

    @Then("^customer should exist in database$")
    public abstract void thenCustomerShouldExistInDatabase();

    @Test
    @Order(3)
    @DisplayName("Scenario: create customer at feature level")
    public void scenario_1() {
        /**
         * When new customer is created
         */
        whenNewCustomerIsCreated();
        /**
         * Then customer should exist in database
         */
        thenCustomerShouldExistInDatabase();
    }

    @When("^customer details are updated$")
    public abstract void whenCustomerDetailsAreUpdated();

    @Then("^changes should be saved$")
    public abstract void thenChangesShouldBeSaved();

    @When("^customer is deleted$")
    public abstract void whenCustomerIsDeleted();

    @Then("^customer should be removed$")
    public abstract void thenCustomerShouldBeRemoved();

    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Rule: customer updates")
    public class Rule_1 {
        @Test
        @Order(2)
        @DisplayName("Scenario: update customer details")
        public void scenario_1() {
            /**
             * When customer details are updated
             */
            whenCustomerDetailsAreUpdated();
            /**
             * Then changes should be saved
             */
            thenChangesShouldBeSaved();
        }

        @Test
        @Order(1)
        @DisplayName("Scenario: delete customer at feature level")
        public void scenario_2() {
            /**
             * When customer is deleted
             */
            whenCustomerIsDeleted();
            /**
             * Then customer should be removed
             */
            thenCustomerShouldBeRemoved();
        }
    }
}
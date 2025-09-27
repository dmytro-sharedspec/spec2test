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

    public void thenMyCartSubtotalWouldBe$p1(String p1) {
        /* real implementation here */
    }

    public void whenIViewTheCart() {
        /* real implementation here */
    }

    public void thenISeeThe$p1Banner(String p1) {
        /* real implementation here */
    }

}
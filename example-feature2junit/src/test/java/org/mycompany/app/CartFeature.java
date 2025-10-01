package org.mycompany.app;

import dev.spec2test.feature2junit.Feature2JUnit;
import dev.spec2test.feature2junit.Feature2JUnitOptions;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.DataTableTypeRegistry;
import io.cucumber.datatable.DataTableTypeRegistryTableConverter;

import java.util.Locale;
import java.util.Map;

@Feature2JUnitOptions(
//    shouldBeAbstract = false
        shouldBeAbstract = true,
        addCucumberStepAnnotations = false
)
@Feature2JUnit("specs/cart.feature")
public class CartFeature {


    public CartFeature() {

        DataTableTypeRegistry dataTableRegistry = new DataTableTypeRegistry(Locale.ENGLISH);

        dataTableRegistry.defineDataTableType(new DataTableType(
                CartItem.class,
                (Map<String, String> row) ->
                        new CartItem(
                                row.get("name"),
                                Integer.parseInt(row.get("qty")),
                                Double.parseDouble(row.get("price"))
                        ))
        );
        DataTable.TableConverter tableConverter = new DataTableTypeRegistryTableConverter(dataTableRegistry);

    }


//    public void givenMyCartContains$p1WithQuantity$p2AndUnitPrice$p3(String p1, String p2, String p3) {
//        /* real implementation here */
//    }
//
//    public void whenIChangeTheQuantityTo$p1(String p1) {
//        /* real implementation here */
//    }
//
//    public void thenMyCartSubtotalIs$p1(String p1) {
//        /* real implementation here */
//    }
//
//    public void thenMyCartSubtotalWouldBe$p1(String p1) {
//        /* real implementation here */
//    }
//
//    public void whenIViewTheCart() {
//        /* real implementation here */
//    }
//
//    public void thenISeeThe$p1Banner(String p1) {
//        /* real implementation here */
//    }

}
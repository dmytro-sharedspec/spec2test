package org.mycompany.app;

import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.DataTableTypeRegistry;
import io.cucumber.datatable.DataTableTypeRegistryTableConverter;

import java.util.Locale;
import java.util.Map;

public class CartFeatureTest extends CartFeatureScenarios {

    protected DataTableTypeRegistry dataTableRegistry;

    protected DataTable.TableConverter tableConverter;

    public CartFeatureTest() {

        dataTableRegistry = new DataTableTypeRegistry(Locale.ENGLISH);

        dataTableRegistry.defineDataTableType(new DataTableType(
                CartItem.class,
                (Map<String, String> row) ->
                        new CartItem(
                                row.get("name"),
                                Integer.parseInt(row.get("qty")),
                                Double.parseDouble(row.get("price"))
                        ))
        );
        tableConverter = new DataTableTypeRegistryTableConverter(dataTableRegistry);

    }

    @Override
    protected DataTable.TableConverter getTableConverter() {
        return tableConverter;
    }
}
package dev.spec2test.feature2junit.generator.tables;

import io.cucumber.messages.types.DataTable;
import io.cucumber.messages.types.TableCell;
import io.cucumber.messages.types.TableRow;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class TableUtils {

    public static List<Integer> workOutMaxColumnLength(DataTable dataTableMsg) {

        List<TableRow> rows = dataTableMsg.getRows();
        List<Integer> maxColumnLength = new ArrayList<>();

        for (TableRow row : rows) {
            List<TableCell> cells = row.getCells();
            for (int i = 0; i < cells.size(); i++) {

                TableCell cellValue = cells.get(i);
                String cellText = cellValue.getValue();

                if (maxColumnLength.size() <= i) {
                    maxColumnLength.add(cellText.length());
                }
                else {
                    int currentMaxLength = maxColumnLength.get(i);
                    if (cellText.length() > currentMaxLength) {
                        maxColumnLength.set(i, cellText.length());
                    }
                }
            }
        }

        return maxColumnLength;
    }

    public static String convertDataTableToString(DataTable dataTableMsg, List<Integer> maxColumnLength) {

        StringBuilder sb = new StringBuilder();

        List<TableRow> rows = dataTableMsg.getRows();

        for (int i = 0; i < rows.size(); i++) {

            TableRow row = rows.get(i);
            List<TableCell> cells = row.getCells();

            sb.append("|");

            for (int columnIndex = 0; columnIndex < cells.size(); columnIndex++) {

                TableCell cellValue = cells.get(columnIndex);
                String value = cellValue.getValue();
                sb.append(value);
                boolean needToPad = columnIndex < maxColumnLength.size()
                        && maxColumnLength.get(columnIndex) > value.length();
                if (needToPad) {
                    int paddingLength = maxColumnLength.get(columnIndex) - value.length();
                    String padding = StringUtils.repeat(" ", paddingLength);
                    sb.append(padding);
                }
                sb.append("|");
            }

            if (i < rows.size() - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}

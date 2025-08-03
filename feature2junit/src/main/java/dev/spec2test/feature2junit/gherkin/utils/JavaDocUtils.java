package dev.spec2test.feature2junit.gherkin.utils;

import javax.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class JavaDocUtils {

    public static String toJavaDoc(String keyword, @Nullable String name, @Nullable String description) {

        StringBuilder javaDocSB = new StringBuilder()
                .append("/**")
                .append("\n * ").append(keyword).append(": ");

        if (StringUtils.isNotBlank(name)) {
            javaDocSB.append(name);
        }

        if (StringUtils.isNotBlank(description)) {
            String[] lines = description.split("\n");
            for (String line : lines) {
                javaDocSB.append("\n * ").append(line);
            }
        }
        javaDocSB.append("\n */\n");

        return javaDocSB.toString();
    }

    public static String trimLeadingAndTrailingWhitespace(String multiLineString) {

        String[] lines = multiLineString.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmedLine = line.trim();
            lines[i] = trimmedLine;
        }
        String trimmedLines = StringUtils.join(lines, "\n");
        return trimmedLines;
    }

    public static String multiLineStringAsJavaDoc(String multiLineString) {

        StringBuilder sb = new StringBuilder();
        sb.append("/**\n");
        for (String line : multiLineString.split("\n")) {
            sb.append(" * ").append(line).append("\n");
        }
        sb.append(" */\n");
        return sb.toString();
    }

}

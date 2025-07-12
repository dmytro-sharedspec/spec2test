package dev.spec2test.common.fileutils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;

public class AptFileUtils {

    public static String loadFileContent(String featureFilePath, ProcessingEnvironment processingEnv) {

        AptMessageUtils.message("featureFilePath = " + featureFilePath, processingEnv);
//        String fullFeatureFilePath = "src/test/resources/" + featureFilePath;

        String featureContent;

        File currentDir = new File(".");
        String absolutePath = currentDir.getAbsolutePath();
        AptMessageUtils.message("Current directory absolute path = " + absolutePath, processingEnv);

        try {
            FileObject specFile = processingEnv.getFiler()
//                    .getResource(StandardLocation.CLASS_PATH, "", fullFeatureFilePath);
//                    .getResource(StandardLocation.CLASS_PATH, "", featureFilePath);
                    .getResource(StandardLocation.CLASS_PATH, "", featureFilePath); // works from IDE & maven lifecycle build goal
            CharSequence charContent = specFile.getCharContent(false);
            featureContent = charContent.toString();

//            Path path = Paths.get(fullFeatureFilePath);
////            Path path = Paths.get(featureFilePath);
//            featureContent = Files.readString(path);

        } catch (IOException e) {
            AptMessageUtils.messageError(e.getMessage(), processingEnv);
            e.printStackTrace(System.err);
            throw new RuntimeException("Unable to find story file using path - " + featureFilePath, e);
        }

        return featureContent;
    }

    public static String convertInputStreamToString(InputStream inputStream) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append(System.lineSeparator());
            }
        }
        return result.toString(); // Trim to remove trailing newline
    }

}
package dev.spec2test.spec2junit.parser.story;

import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.RegexStoryParser;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;

public class CustomRegexStoryParser extends RegexStoryParser {

    private final ProcessingEnvironment processingEnv;
    private final ProcessingEnvironment env;

    public CustomRegexStoryParser(ProcessingEnvironment processingEnv, ProcessingEnvironment env) {

        this.processingEnv = processingEnv;
        this.env = env;
    }

    public Story parseUsingStoryPath(String storyPath) {

        String fileContent = loadFileContent(storyPath);

        Story story = parseStory(fileContent);
        return story;
    }

    private String loadFileContent(String featureFilePath) {

        message("featureFilePath = " + featureFilePath);
        String fullFeatureFilePath = "src/test/resources/" + featureFilePath;

        String featureContent;

        File currentDir = new File(".");
        String absolutePath = currentDir.getAbsolutePath();
        message("Current directory absolute path = " + absolutePath);

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
            messageError(e.getMessage());
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

    @Override
    public Story parseStory(String storyAsText) {

        try {

            // Assuming the parseStory method is defined in CustomRegexStoryParser
            Story story = super.parseStory(storyAsText);
            // Do something with the parsed story
            return story;
        } catch (Throwable e) {
            messageError("Error parsing story:\n" + e.getMessage());
            throw e;
        }
    }

    private void message(String message) {

//        System.out.println("### " + message);
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.MANDATORY_WARNING, "### " + message);
    }

    private void messageError(String message) {

//        System.out.println("### " + message);
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR, "### " + message);
    }
}
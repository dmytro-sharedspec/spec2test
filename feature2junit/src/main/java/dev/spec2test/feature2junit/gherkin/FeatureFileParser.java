package dev.spec2test.feature2junit.gherkin;

import dev.spec2test.common.LoggingSupport;
import dev.spec2test.common.ProcessingException;
import io.cucumber.gherkin.GherkinDialectProvider;
import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import lombok.Getter;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Parses a Gherkin feature file and extracts the Feature object.
 */
public class FeatureFileParser implements LoggingSupport {

    @Getter
    private final ProcessingEnvironment processingEnv;

    private GherkinParser gherkinParser;

    /**
     * Creates a FeatureFileParser instance.
     *
     * @param processingEnv the processing environment used to access the file system
     */
    public FeatureFileParser(ProcessingEnvironment processingEnv) {

        this.processingEnv = processingEnv;

        gherkinParser = GherkinParser.builder().includePickles(false).build();
    }

    /**
     * Parses a Gherkin feature file using the provided file path and returns the Feature object.
     *
     * @param featureFilePath the path to the feature file, relative to the classpath
     * @return the Feature object extracted from the Gherkin document
     * @throws IOException if an error occurs while reading the file or parsing the Gherkin document
     */
    public Feature parseUsingPath(String featureFilePath) throws IOException {

        String fileContent = loadFileContent(featureFilePath);
        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

        GherkinDialectProvider.additionalFeatureKeywords = Set.of("Narrative"); // todo - parameterize this

        Stream<Envelope> envelopeStream = gherkinParser.parse(featureFilePath, inputStream);

        Envelope gherkinDocEnvelope = envelopeStream.filter(
                        envelope -> envelope.getGherkinDocument().isPresent()
                )
                .findFirst().orElseThrow(() -> new ProcessingException("Could not find 'Feature' keyword or one of its synonyms"));
        GherkinDocument gherkinDocument = gherkinDocEnvelope.getGherkinDocument().orElseThrow();

        Feature feature = gherkinDocument.getFeature().orElseThrow();
        return feature;
    }

    String loadFileContent(String featureFilePath) throws IOException {

//        logWarning("Loading feature file, featureFilePath = '" + featureFilePath + "'");

        Filer filer = processingEnv.getFiler();
        try {

            FileObject specFile = null;
//            try {
//                specFile = filer.getResource(StandardLocation.SOURCE_PATH, "", featureFilePath);
//                logWarning("Found feature file in SOURCE_PATH: " + featureFilePath);
//            } catch (FileNotFoundException e) {
//                // silently ignore this attempted location
//                logWarning("Could not find feature file in SOURCE_PATH: " + featureFilePath);
//            }
//            if (specFile == null) {
//                try {
//                    specFile = filer.getResource(StandardLocation.SOURCE_OUTPUT, "", featureFilePath);
//                    logWarning("Found feature file in SOURCE_OUTPUT: " + featureFilePath);
//                } catch (FileNotFoundException e) {
//                    // silently ignore this attempted location
//                    logWarning("Could not find feature file in SOURCE_OUTPUT: " + featureFilePath);
//                }
//            }
//            if (specFile == null) {
//                try {
//                    specFile = filer.getResource(StandardLocation.CLASS_OUTPUT, "", featureFilePath);
//                    logWarning("Found feature file in CLASS_OUTPUT: " + featureFilePath);
//                } catch (FileNotFoundException e) {
//                    // silently ignore this attempted location
//                    logWarning("Could not find feature file in CLASS_OUTPUT: " + featureFilePath);
//                }
//            }

//            if (specFile == null) {
            // works from IDE & maven lifecycle build goal
            specFile = filer.getResource(StandardLocation.CLASS_PATH, "", featureFilePath);
//            logWarning("Found feature file in CLASS_PATH: " + featureFilePath);
//            }

//            logWarning("Reading feature file content from: " + specFile.toUri());
            CharSequence charContent = specFile.getCharContent(false);
            String featureContent = charContent.toString();

            return featureContent;

        } catch (FileNotFoundException e) {
            logWarning("Could not find feature file in CLASS_PATH: " + featureFilePath);
            throw e;
        }
    }
}
package dev.spec2test.feature2junit.generator;

import dev.spec2test.common.fileutils.AptFileUtils;
import dev.spec2test.common.fileutils.AptMessageUtils;
import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

class CustomGherkinParser {

    private final ProcessingEnvironment processingEnv;

    private GherkinParser gherkinParser;

    public CustomGherkinParser(ProcessingEnvironment processingEnv, ProcessingEnvironment env) {

        this.processingEnv = processingEnv;

        gherkinParser = GherkinParser.builder().build();
    }

    public Feature parseUsingPath(String featureFilePath) {

        String fileContent = AptFileUtils.loadFileContent(featureFilePath, processingEnv);

        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
//        Path path = Paths.get(fullFeatureFilePath);
//        Path path = Paths.get("src/test/resources/" + featureFilePath);

        Stream<Envelope> envelopeStream;
        try {
//            envelopeStream = gherkinParser.parse(path);

            envelopeStream = gherkinParser.parse(featureFilePath, inputStream);

            AptMessageUtils.message("Envelope stream created successfully ", processingEnv);

        }
        catch (IOException e) {
            AptMessageUtils.messageError(e.getMessage(), processingEnv);
            throw new RuntimeException(e);
        }

//        envelopeStream.forEach(envelope -> {
//            // Process each envelope
//            AptMessageUtils.message("Envelope: " + envelope, processingEnv);
//        });

        Envelope gherkinDocEnvelope = envelopeStream.filter(
                        envelope -> envelope.getGherkinDocument().isPresent()
                )
                .findFirst().orElseThrow();
        GherkinDocument gherkinDocument = gherkinDocEnvelope.getGherkinDocument().orElseThrow();

        Feature feature = gherkinDocument.getFeature().orElseThrow();
        return feature;
    }

//    public Story parseStory(String storyAsText) {
//
//        try {
//
//            // Assuming the parseStory method is defined in CustomRegexStoryParser
//
//
//            Story story = super.parseStory(storyAsText);
//            // Do something with the parsed story
//            return story;
//        } catch (Throwable e) {
//            messageError("Error parsing story:\n" + e.getMessage());
//            throw e;
//        }
//    }

}
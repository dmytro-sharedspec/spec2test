package dev.spec2test.feature2junit.gherkin;

import dev.spec2test.common.LoggingSupport;
import dev.spec2test.common.ProcessingException;
import io.cucumber.gherkin.GherkinDialectProvider;
import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import lombok.Getter;

public class FeatureFileParser implements LoggingSupport {

    @Getter
    private final ProcessingEnvironment processingEnv;

    private GherkinParser gherkinParser;

    public FeatureFileParser(ProcessingEnvironment processingEnv) {

        this.processingEnv = processingEnv;

        gherkinParser = GherkinParser.builder().includePickles(false).build();
    }

    public Feature parseUsingPath(String featureFilePath) throws IOException {

        String fileContent = loadFileContent(featureFilePath);
        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

        GherkinDialectProvider.additionalFeatureKeywords = Set.of("Narrative");

        Stream<Envelope> envelopeStream = gherkinParser.parse(featureFilePath, inputStream);

        Envelope gherkinDocEnvelope = envelopeStream.filter(
                        envelope -> envelope.getGherkinDocument().isPresent()
                )
                .findFirst().orElseThrow(() -> new ProcessingException("Could not find 'Feature' keyword or one of its synonyms"));
        GherkinDocument gherkinDocument = gherkinDocEnvelope.getGherkinDocument().orElseThrow();

        Feature feature = gherkinDocument.getFeature().orElseThrow();
        return feature;
    }

    private String loadFileContent(String featureFilePath) throws IOException {

        FileObject specFile = processingEnv.getFiler().getResource(StandardLocation.CLASS_PATH, "", featureFilePath); // works from IDE & maven lifecycle build goal
        CharSequence charContent = specFile.getCharContent(false);

        String featureContent = charContent.toString();
        return featureContent;
    }
}
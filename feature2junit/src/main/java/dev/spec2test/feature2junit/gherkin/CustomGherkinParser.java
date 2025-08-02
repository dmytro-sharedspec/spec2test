package dev.spec2test.feature2junit.gherkin;

import dev.spec2test.common.fileutils.AptFileUtils;
import dev.spec2test.feature2junit.MessageSupport;
import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;

public class CustomGherkinParser implements MessageSupport {

    private final ProcessingEnvironment processingEnv;

    private GherkinParser gherkinParser;

    public CustomGherkinParser(ProcessingEnvironment processingEnv) {

        this.processingEnv = processingEnv;

        gherkinParser = GherkinParser.builder().build();
    }

    public Feature parseUsingPath(String featureFilePath) throws IOException {

        String fileContent = AptFileUtils.loadFileContent(featureFilePath, processingEnv);
        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

        Stream<Envelope> envelopeStream = gherkinParser.parse(featureFilePath, inputStream);

        Envelope gherkinDocEnvelope = envelopeStream.filter(
                        envelope -> envelope.getGherkinDocument().isPresent()
                )
                .findFirst().orElseThrow();
        GherkinDocument gherkinDocument = gherkinDocEnvelope.getGherkinDocument().orElseThrow();

        Feature feature = gherkinDocument.getFeature().orElseThrow();
        return feature;
    }

    @Override
    public ProcessingEnvironment getProcessingEnv() {
        return processingEnv;
    }
}
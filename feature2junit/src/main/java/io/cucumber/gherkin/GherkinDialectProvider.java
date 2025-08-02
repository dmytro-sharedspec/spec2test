package io.cucumber.gherkin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Provides access to Gherkin dialects.
 * <p>
 * The default dialect is "en" (English).
 */
public class GherkinDialectProvider {

    private final String defaultDialectName;
    private GherkinDialect defaultDialect;

    public GherkinDialectProvider(String defaultDialectName) {
        this.defaultDialectName = requireNonNull(defaultDialectName);
    }

    public GherkinDialectProvider() {
        this("en");
    }

    public GherkinDialect getDefaultDialect() {
        if (defaultDialect == null) {
            this.defaultDialect = getDialect(defaultDialectName)
                    .orElseThrow(() -> new ParserException.NoSuchLanguageException(defaultDialectName, null));
            this.defaultDialect = extendGherkinDialect(defaultDialect);
        }
        return defaultDialect;
    }

    public Optional<GherkinDialect> getDialect(String language) {
        requireNonNull(language);
        Optional<GherkinDialect> dialectResult = Optional.ofNullable(GherkinDialects.DIALECTS.get(language));
        if (dialectResult.isPresent()) {
            GherkinDialect gherkinDialect = dialectResult.get();
            gherkinDialect = extendGherkinDialect(gherkinDialect);
            dialectResult = Optional.of(gherkinDialect);
        }
        return dialectResult;
    }

    private static GherkinDialect extendGherkinDialect(GherkinDialect gherkinDialect) {

        List<String> featureKeywords = gherkinDialect.getFeatureKeywords();
        if (!featureKeywords.contains("Narrative")) {
            List<String> extendedFeatureKeywords = new ArrayList<>(featureKeywords);
            extendedFeatureKeywords.add(0, "Narrative");
            featureKeywords = Collections.unmodifiableList(extendedFeatureKeywords);
        }

        GherkinDialect extendedDialect = new GherkinDialect(
                gherkinDialect.getLanguage(),
                gherkinDialect.getName(),
                gherkinDialect.getNativeName(),
                featureKeywords,
                gherkinDialect.getRuleKeywords(),
                gherkinDialect.getScenarioKeywords(),
                gherkinDialect.getScenarioOutlineKeywords(),
                gherkinDialect.getBackgroundKeywords(),
                gherkinDialect.getExamplesKeywords(),
                gherkinDialect.getGivenKeywords(),
                gherkinDialect.getWhenKeywords(),
                gherkinDialect.getThenKeywords(),
                gherkinDialect.getAndKeywords(),
                gherkinDialect.getButKeywords()
        );

        return extendedDialect;
    }

    public Set<String> getLanguages() {
        return GherkinDialects.DIALECTS.keySet();
    }
}

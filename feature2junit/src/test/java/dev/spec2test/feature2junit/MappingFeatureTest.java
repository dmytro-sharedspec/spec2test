package dev.spec2test.feature2junit;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.jupiter.api.Disabled;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.Suite;

@Disabled
@Suite
@IncludeEngines("cucumber")
@ConfigurationParameter(key = Constants.FEATURES_PROPERTY_NAME, value = "src/test/resources/features/MappingFeature.feature")
@ConfigurationParameter(
        key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty"
                + ", html:target/cucumber-report/MappingFeature.html"
                + ", json:target/cucumber-report/MappingFeature.json"
                + ", dev.spec2test.feature2junit.reporting.PrettyJsonPlugin:target/cucumber-report/MappingFeature.json"
        //", dev.spec2test.feature2junit.reporting.PrettyJsonPlugin:target/cucumber-report/MappingFeature.json"
)
public class MappingFeatureTest {

}

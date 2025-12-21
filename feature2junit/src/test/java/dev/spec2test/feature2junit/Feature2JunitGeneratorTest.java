package dev.spec2test.feature2junit;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
//@ConfigurationParameter(key = Constants.FEATURES_PROPERTY_NAME,value = "src/test/resources/features/MappingFeature.feature")
//@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "dev/spec2test/feature2junit/steps2")
//@ConfigurationParameter(key = Constants.FILTER_TAGS_PROPERTY_NAME,value = "@googleSearch")
//@ConfigurationParameter(key = Constants.EXECUTION_DRY_RUN_PROPERTY_NAME,value = "false")
//@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "junit" )
public class Feature2JunitGeneratorTest {

}

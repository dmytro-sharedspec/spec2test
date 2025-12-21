package dev.spec2test.feature2junit;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
//@SelectClasspathResource("features")
//@SelectClasspathResource("features/feature")
@SelectClasspathResource("features/rule")
public class Feature2JunitGeneratorTest {

}

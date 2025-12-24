package dev.spec2test.feature2junit.tests.troubleshooting;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@IncludeTags(value = {"troubleshoot", "wip"})
public class RunTaggedScenariosTest {

}

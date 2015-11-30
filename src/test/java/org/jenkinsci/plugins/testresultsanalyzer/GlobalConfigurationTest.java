package org.jenkinsci.plugins.testresultsanalyzer;

import com.gargoylesoftware.htmlunit.html.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

public class GlobalConfigurationTest
{
	@Rule
	public JenkinsRule j = new JenkinsRule();

	@Test
	public void ConfigurationSectionExistsTest() throws Exception{
		HtmlPage page = j.createWebClient().goTo("configure");
		assertEquals("Did not find noOfBuilds element", 1, page.getElementsByName("noOfBuilds").size());
		assertEquals("Did not find showAllBuilds element", 1, page.getElementsByName("showAllBuilds").size());
		assertEquals("Did not find showBuildTime element", 1, page.getElementsByName("showBuildTime").size());
		assertEquals("Did not find showLineGraph element", 1, page.getElementsByName("showLineGraph").size());
		assertEquals("Did not find showBarGraph element", 1, page.getElementsByName("showBarGraph").size());
		assertEquals("Did not find showPieGraph element", 1, page.getElementsByName("showPieGraph").size());
		assertEquals("Did not find chartDataType element", 1, page.getElementsByName("chartDataType").size());
	}

	
}


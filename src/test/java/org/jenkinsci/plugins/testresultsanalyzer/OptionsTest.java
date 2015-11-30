package org.jenkinsci.plugins.testresultsanalyzer;

import hudson.model.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.jvnet.hudson.test.*;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;
import hudson.model.FreeStyleProject;

public class OptionsTest
{
	@Rule
	public JenkinsRule j = new JenkinsRule();

	@Test
	public void optionsSectionExistsTest() throws Exception {
		FreeStyleProject p = j.createFreeStyleProject();

		//don't actually care about the build, we just need it to make the next part not throw an exception
		p.getBuildersList().add(new FailureBuilder());
		FreeStyleBuild build=p.scheduleBuild2(0).get();
		j.assertBuildStatus(Result.FAILURE,build);
		WebClient web = j.createWebClient();
		HtmlPage page = web.getPage(p, "test_results_analyzer");
		assertEquals("Did not find settingsmenubutton element", 1, page.getElementsByIdAndOrName("settingsmenubutton").size());
		assertEquals("Did not find settingsmenu element", 1, page.getElementsByIdAndOrName("settingsmenu").size());
		assertEquals("Did not find chartDataType element", 1, page.getElementsByIdAndOrName("chartDataType").size());
		assertTrue(page.getElementById("settingsmenu").getAttribute("style").contains("none"));

		web.waitForBackgroundJavaScript(10000);
		//DomElement button = page.getElementById("settingsmenubutton");
		//button.click();
		//System.out.println(page.asXml());
		ScriptResult result = page.executeJavaScript("$j('#settingsmenubutton').click();");
		web.waitForBackgroundJavaScript(10000);
		assertFalse(page.getElementById("settingsmenu").getAttribute("style").contains("none"));

	}
}


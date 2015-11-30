package org.jenkinsci.plugins.testresultsanalyzer;

import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.FailureBuilder;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

public class AnalyzerPageTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    private HtmlPage setupFreeStyle() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();
        project.getBuildersList().add(new FailureBuilder());
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        jenkinsRule.assertBuildStatus(Result.FAILURE, build);
        WebClient wc = jenkinsRule.createWebClient();
        return wc.getPage(project, "test_results_analyzer");
    }

    @Test
    public void OneTestTwoBuildsTrue() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj = {\"builds\":[\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\": \"edu.illinois.cs427.mp3\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();";
        job.executeJavaScript(javaScriptCommand);
        DomElement exclamation_mark = (DomElement) job.getByXPath("//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]").get(0);
        assert(exclamation_mark.getAttribute("style").contains("inline-block"));
    }

    @Test
    public void OneTestTwoBuildsFalse() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj = {\"builds\":[\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\": \"edu.illinois.cs427.mp3\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();";
        job.executeJavaScript(javaScriptCommand);
        DomElement exclamation_mark = (DomElement) job.getByXPath("//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]").get(0);
        assert(exclamation_mark.getAttribute("style").contains("none"));
    }


    @Test
    public void OneTestOneBuildFalse1() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj = {\"builds\":[\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\": \"edu.illinois.cs427.mp3\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();";
        job.executeJavaScript(javaScriptCommand);
        DomElement exclamation_mark = (DomElement) job.getByXPath("//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]").get(0);
        assert(exclamation_mark.getAttribute("style").contains("none"));
    }


    @Test
    public void OneTestOneBuildFalse2() throws Exception {
        HtmlPage job = setupFreeStyle();
		String javaScriptCommand = "var Obj = {\"builds\":[\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\": \"edu.illinois.cs427.mp3\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();";
        job.executeJavaScript(javaScriptCommand);
        DomElement exclamation_mark = (DomElement) job.getByXPath("//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]").get(0);
        assert(exclamation_mark.getAttribute("style").contains("none"));   
    }

    
    @Test
    public void OneTestMultipleBuildsTrue() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.0432}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\": \"edu.illinois.cs427.mp3\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();";
        job.executeJavaScript(javaScriptCommand);
        DomElement exclamation_mark = (DomElement) job.getByXPath("//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]").get(0);
        assert(exclamation_mark.getAttribute("style").contains("inline-block"));
    }

    @Test
    public void OneTestMultipleBuildsFalse() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.0432}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\": \"edu.illinois.cs427.mp3\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();";
        job.executeJavaScript(javaScriptCommand);
        DomElement exclamation_mark = (DomElement) job.getByXPath("//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]").get(0);
        assert(exclamation_mark.getAttribute("style").contains("none"));
    }

    @Test
    public void MultipleTestsMultipleBuildsFalse() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs427.mp3\",\"type\":\"package\"},{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.392}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs512.mp0\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();";
        job.executeJavaScript(javaScriptCommand);
        DomElement cs427_exclamation_mark = (DomElement) job.getByXPath("//*[contains(@class, 'cs427')]//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]").get(0);
        assert(cs427_exclamation_mark.getAttribute("style").contains("none"));
        DomElement cs512_exclamation_mark = (DomElement) job.getByXPath("//*[contains(@class, 'cs512')]//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]").get(0);
        assert(cs512_exclamation_mark.getAttribute("style").contains("inline-block"));
    }

    /* TESTS for filterTests() */
    
    @Test
    public void OneTestFilter1() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj = {\"builds\":[\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\": \"edu.illinois.cs427.mp3\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#filter\").val(\"illinois\");filterTests();";
        job.executeJavaScript(javaScriptCommand);
        DomElement row = (DomElement) job.getByXPath("//*[contains(@class, 'cs427')]").get(0);
        assert(row.getAttribute("style").contains("table-row"));
    }

    @Test
    public void OneTestFilter2() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj = {\"builds\":[\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\": \"edu.illinois.cs427.mp3\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#filter\").val(\"was\");filterTests();";
        job.executeJavaScript(javaScriptCommand);
        DomElement row = (DomElement) job.getByXPath("//*[contains(@class, 'cs427')]").get(0);
        assert(row.getAttribute("style").contains("none"));
    }

    @Test
    public void OneTestFilter3() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj = {\"builds\":[\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\": \"edu.illinois.cs427.mp3\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#filter\").val(\"\");filterTests();";
        job.executeJavaScript(javaScriptCommand);
        DomElement row = (DomElement) job.getByXPath("//*[contains(@class, 'cs427')]").get(0);
        assert(row.getAttribute("style").contains("table-row"));
    }

    @Test
    public void MultipleTestsFilter1() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs427.mp3\",\"type\":\"package\"},{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.392}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs512.mp0\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#filter\").val(\"512\");filterTests();";
        job.executeJavaScript(javaScriptCommand);
        DomElement row = (DomElement) job.getByXPath("//*[contains(@class, 'cs512')]").get(0);
        assert(row.getAttribute("style").contains("table-row"));
    }

    @Test
    public void MultipleTestsFilter2() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs427.mp3\",\"type\":\"package\"},{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.392}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs512.mp0\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#filter\").val(\"meaning of life\");filterTests();";
        job.executeJavaScript(javaScriptCommand);
        DomElement row = (DomElement) job.getByXPath("//*[contains(@class, 'cs512')]").get(0);
        assert(row.getAttribute("style").contains("none"));
    }

    @Test
    public void MultipleTestsFilter3() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs427.mp3\",\"type\":\"package\"},{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.392}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs512.mp0\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#filter\").val(\"\");filterTests();";
        job.executeJavaScript(javaScriptCommand);
        DomElement row = (DomElement) job.getByXPath("//*[contains(@class, 'cs512')]").get(0);
        assert(row.getAttribute("style").contains("table-row"));
    }

	@Test
    public void testFilterPass1() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalPASSED\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalPASSED\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalPASSED\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"PASSED\",\"FAILED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs427.mp3\",\"type\":\"package\"},{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalPASSED\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalPASSED\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalPASSED\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.392}],\"buildStatuses\":[\"PASSED\",\"FAILED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs512.mp0\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#passFilter\").prop(\"checked\", true);$j(\"#failFilter\").prop(\"checked\", false);$j(\"#skipFilter\").prop(\"checked\", false);filterTests();";
        job.executeJavaScript(javaScriptCommand);
        DomElement row = (DomElement) job.getByXPath("//*[contains(@class, 'cs512')]").get(0);
        assert(row.getAttribute("style").contains("table-row"));
    }

    @Test
    public void testFilterPass2() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs427.mp3\",\"type\":\"package\"},{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.392}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs512.mp0\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#passFilter\").prop(\"checked\", false);$j(\"#failFilter\").prop(\"checked\", false);$j(\"#skipFilter\").prop(\"checked\", false);filterTests();";
        job.executeJavaScript(javaScriptCommand);
        DomElement row = (DomElement) job.getByXPath("//*[contains(@class, 'cs512')]").get(0);
        assert(row.getAttribute("style").contains("none"));
    }

    @Test
    public void testFilterFail1() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs427.mp3\",\"type\":\"package\"},{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.392}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs512.mp0\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#passFilter\").prop(\"checked\", false);$j(\"#failFilter\").prop(\"checked\", true);$j(\"#skipFilter\").prop(\"checked\", false);filterTests();";
        job.executeJavaScript(javaScriptCommand);
        DomElement row = (DomElement) job.getByXPath("//*[contains(@class, 'cs512')]").get(0);
        assert(row.getAttribute("style").contains("table-row"));
    }

    @Test
    public void testFilterFail2() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs427.mp3\",\"type\":\"package\"},{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.392}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs512.mp0\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#passFilter\").prop(\"checked\", false);$j(\"#failFilter\").prop(\"checked\", false);$j(\"#skipFilter\").prop(\"checked\", false);filterTests();";
        job.executeJavaScript(javaScriptCommand);
        DomElement row = (DomElement) job.getByXPath("//*[contains(@class, 'cs512')]").get(0);
        assert(row.getAttribute("style").contains("none"));
    }

    @Test
    public void testFilterSkip1() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"SKIPPED\",\"totalFailed\":0,\"totalPassed\":0,\"totalSkipped\":1,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"SKIPPED\",\"totalFailed\":0,\"totalPassed\":0,\"totalSkipped\":1,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs427.mp3\",\"type\":\"package\"},{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"SKIPPED\",\"totalFailed\":0,\"totalPassed\":0,\"totalSkipped\":1,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":false,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"SKIPPED\",\"totalFailed\":0,\"totalPassed\":0,\"totalSkipped\":1,\"totalTests\":1,\"totalTimeTaken\":0.392}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs512.mp0\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#passFilter\").prop(\"checked\", false);$j(\"#failFilter\").prop(\"checked\", false);$j(\"#skipFilter\").prop(\"checked\", true);filterTests();";
        job.executeJavaScript(javaScriptCommand);
        DomElement row = (DomElement) job.getByXPath("//*[contains(@class, 'cs512')]").get(0);
        assert(row.getAttribute("style").contains("table-row"));
    }

    @Test
    public void testFilterSkip2() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj = {\"builds\":[\"3\",\"2\",\"1\"],\"results\":[{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":true,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":false,\"isSkipped\":true,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.023},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":true,\"name\":\"edu.illinois.cs427.mp3\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.032}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs427.mp3\",\"type\":\"package\"},{\"buildResults\":[{\"buildNumber\":\"3\",\"children\":[],\"isPassed\":false,\"isSkipped\":true,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"2\",\"children\":[],\"isPassed\":true,\"isSkipped\":true,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"PASSED\",\"totalFailed\":0,\"totalPassed\":1,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.253},{\"buildNumber\":\"1\",\"children\":[],\"isPassed\":true,\"isSkipped\":true,\"name\":\"edu.illinois.cs512.mp0\",\"status\":\"FAILED\",\"totalFailed\":1,\"totalPassed\":0,\"totalSkipped\":0,\"totalTests\":1,\"totalTimeTaken\":0.392}],\"buildStatuses\":[\"FAILED\",\"PASSED\"],\"children\":[],\"parentclass\":\"base\",\"parentname\":\"base\",\"text\":\"edu.illinois.cs512.mp0\",\"type\":\"package\"}]};treeMarkup = analyzerTemplate(Obj);$j(\".table\").html(treeMarkup);addEvents();newFailingTests();$j(\"#passFilter\").prop(\"checked\", false);$j(\"#failFilter\").prop(\"checked\", false);$j(\"#skipFilter\").prop(\"checked\", false);filterTests();";
        job.executeJavaScript(javaScriptCommand);
        DomElement row = (DomElement) job.getByXPath("//*[contains(@class, 'cs512')]").get(0);
        assert(row.getAttribute("style").contains("none"));
    }
}

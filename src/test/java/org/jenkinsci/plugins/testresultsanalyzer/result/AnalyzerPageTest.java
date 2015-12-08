package org.jenkinsci.plugins.testresultanalyzer;

import java.util.Arrays;

import hudson.model.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import static org.junit.Assert.*;
import com.gargoylesoftware.htmlunit.html.*; 
import com.gargoylesoftware.htmlunit.*;


public class AnalyzerPageTest {

    @Rule public JenkinsRule jenkinsRule = new JenkinsRule();

    private HtmlPage setupFreeStyle() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();
        project.getBuildersList().add(new FailureBuilder());
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        jenkinsRule.assertBuildStatus(Result.FAILURE, build);
        WebClient wc = jenkinsRule.createWebClient();
        HtmlPage job = wc.getPage(project, "test_results_analyzer");
        return job;
    }
    
    String singleTest_populateTable_javascript = "var Obj =   {" +
                "\"builds\":[\"2\",\"1\"]," +
                "\"results\":   [{" +
                                    "\"buildResults\":  [{" +
                                                            "\"buildNumber\":\"2\","+
                                                            "\"children\":[],"+
                                                            "\"isPassed\":false,"+
                                                            "\"isSkipped\":false,"+
                                                            "\"name\":\"edu.illinois.cs427.mp3\","+
                                                            "\"status\":\"FAILED\","+
                                                            "\"totalFailed\":1,"+
                                                            "\"totalPassed\":0,"+
                                                            "\"totalSkipped\":0,"+
                                                            "\"totalTests\":1,"+
                                                            "\"totalTimeTaken\":0.023"+
                                                        "},{" +
                                                            "\"buildNumber\":\"1\"," +
                                                            "\"children\":[]," +
                                                            "\"isPassed\":true," +
                                                            "\"isSkipped\":false," +
                                                            "\"name\":\"edu.illinois.cs427.mp3\"," +
                                                            "\"status\":\"PASSED\"," +
                                                            "\"totalFailed\":0," +
                                                            "\"totalPassed\":1," +
                                                            "\"totalSkipped\":0," +
                                                            "\"totalTests\":1," +
                                                            "\"totalTimeTaken\":0.032" +
                                                        "}]," +
                                    "\"buildStatuses\":[\"FAILED\",\"PASSED\"]," +
                                    "\"children\":[]," +
                                    "\"parentclass\":\"base\"," +
                                    "\"parentname\":\"base\"," +
                                    "\"text\": \"edu.illinois.cs427.mp3\"," +
                                    "\"type\":\"package\"" +
                                "}]" + 
            "};" +
            "treeMarkup = analyzerTemplate(Obj);" +
            "$j(\".table\").html(treeMarkup);" +
            "addEvents();";

    /* TESTS for searchTests() */
    
    @Test
    public void searchTest_matchExists() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = singleTest_populateTable_javascript + "$j(\"#filter\").val(\"illinois\");searchTests();";
        ScriptResult result = job.executeJavaScript(javaScriptCommand);
        DomElement row = (DomElement) job.getByXPath("//*[contains(@class, 'cs427')]").get(0);
        assert(row.getAttribute("style").contains("table-row"));
    }

    @Test
    public void searchTest_noMatch() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = singleTest_populateTable_javascript + "$j(\"#filter\").val(\"was\");searchTests();";
        ScriptResult result = job.executeJavaScript(javaScriptCommand);
        DomElement row = (DomElement) job.getByXPath("//*[contains(@class, 'cs427')]").get(0);
        assert(row.getAttribute("style").contains("none"));
    }

    @Test
    public void searchTest_emptyFilter() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = singleTest_populateTable_javascript + "$j(\"#filter\").val(\"\");searchTests();";
        ScriptResult result = job.executeJavaScript(javaScriptCommand);
        DomElement row = (DomElement) job.getByXPath("//*[contains(@class, 'cs427')]").get(0);
        assert(row.getAttribute("style").contains("table-row"));
    }

    
    

}

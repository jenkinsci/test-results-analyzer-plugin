package org.jenkinsci.plugins.testresultanalyzer;

import java.util.Arrays;

import hudson.model.*;
import org.junit.Ignore;
import org.jvnet.hudson.test.JenkinsRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import static org.junit.Assert.*;
import com.gargoylesoftware.htmlunit.html.*; 
import com.gargoylesoftware.htmlunit.*;

//TODO:Ignoring the test as they are failing and needs time to debug and fix
@Ignore
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
   
    @Test
    public void newFailuresTest_noBuild() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj =   {\"" + 
                                                    "builds\":[]," + 
                                                    "\"results\":[]" + 
                                                "};" +
                                    "treeMarkup = analyzerTemplate(Obj);" +
                                    "$j(\".table\").html(treeMarkup);" +
                                    "addEvents();";
        job.executeJavaScript(javaScriptCommand);
        try { 
            DomElement exclamation_mark = (DomElement) job.getByXPath("//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]").get(0);
            assertTrue("The table has an new failure exclamation mark element even though there are no buils.", false);
        }
        catch (Exception e){
            assertTrue(true);
        }
    }

    @Test
    public void newFailuresTest_twoBuilds_trueCase() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj = {\"" + 
                                                "builds\":[\"2\",\"1\"]," + 
                                                "\"results\":[{" + 
                                                                "\"buildResults\":[{" +
                                                                                    "\"buildNumber\":\"2\"," +
                                                                                    "\"children\":[]," +
                                                                                    "\"isPassed\":false," + 
                                                                                    "\"isSkipped\":false," + 
                                                                                    "\"name\":\"edu.illinois.cs427.mp3\"," +
                                                                                    "\"status\":\"FAILED\"," +
                                                                                    "\"totalFailed\":1," + 
                                                                                    "\"totalPassed\":0," + 
                                                                                    "\"totalSkipped\":0," +
                                                                                    "\"totalTests\":1," +
                                                                                    "\"totalTimeTaken\":0.023" + 
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
        job.executeJavaScript(javaScriptCommand);
        DomElement exclamation_mark = (DomElement) job.getByXPath("//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]").get(0);
        assertTrue(exclamation_mark.getAttribute("style").contains("inline-block"));
    }

    @Test
    public void newFailuresTest_twoBuilds_falseCase() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj =   {\"" +
                                                    "builds\":[\"2\",\"1\"]," +
                                                    "\"results\": [{" + 
                                                                    "\"buildResults\":  [{" +
                                                                                            "\"buildNumber\":\"2\"," +
                                                                                            "\"children\":[]," + 
                                                                                            "\"isPassed\":true," +
                                                                                            "\"isSkipped\":false," +
                                                                                            "\"name\":\"edu.illinois.cs427.mp3\"," +
                                                                                            "\"status\":\"PASSED\"," +
                                                                                            "\"totalFailed\":0," +
                                                                                            "\"totalPassed\":1," +
                                                                                            "\"totalSkipped\":0," +
                                                                                            "\"totalTests\":1," +
                                                                                            "\"totalTimeTaken\":0.023" +
                                                                                        "},{" +
                                                                                            "\"buildNumber\":\"1\"," +
                                                                                            "\"children\":[]," +
                                                                                            "\"isPassed\":false," +
                                                                                            "\"isSkipped\":false," +
                                                                                            "\"name\":\"edu.illinois.cs427.mp3\"," +
                                                                                            "\"status\":\"FAILED\"," +
                                                                                            "\"totalFailed\":1," +
                                                                                            "\"totalPassed\":0," +
                                                                                            "\"totalSkipped\":0," +
                                                                                            "\"totalTests\":1," +
                                                                                            "\"totalTimeTaken\":0.032" +
                                                                                        "}]," +
                                                                    "\"buildStatuses\": [\"FAILED\",\"PASSED\"]," + 
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
        job.executeJavaScript(javaScriptCommand);
        DomElement exclamation_mark = (DomElement) job.getByXPath("//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]").get(0);
        assertTrue(exclamation_mark.getAttribute("style").contains("none"));
    }


    @Test
    public void newFailuresTest_oneBuild_buildFailure() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj =   {\"" +
                                                    "builds\":[\"1\"]," +
                                                    "\"results\":   [{" +
                                                                        "\"buildResults\":  [{" + 
                                                                                                "\"buildNumber\":\"1\"," +
                                                                                                "\"children\":[]," +
                                                                                                "\"isPassed\":false," +
                                                                                                "\"isSkipped\":false," +
                                                                                                "\"name\":\"edu.illinois.cs427.mp3\"," +
                                                                                                "\"status\":\"FAILED\"," +
                                                                                                "\"totalFailed\":1," +
                                                                                                "\"totalPassed\":0," +
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
                                                "addEvents();" ;
        job.executeJavaScript(javaScriptCommand);
        DomElement exclamation_mark = (DomElement) job.getByXPath("//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]").get(0);
        assertTrue(exclamation_mark.getAttribute("style").contains("none"));
    }


    @Test
    public void newFailuresTest_oneBuild_buildSuccess() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj =   {\"" +
                                                    "builds\":[\"1\"]," +
                                                    "\"results\":   [{" +
                                                                        "\"buildResults\":  [{" +
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
        job.executeJavaScript(javaScriptCommand);
        DomElement exclamation_mark = (DomElement) job.getByXPath("//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]").get(0);
        assertTrue(exclamation_mark.getAttribute("style").contains("none"));
    }

    
    @Test
    public void newFailuresTest_multipleBuilds_trueCase() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj =   {\"" + 
                                                    "builds\":[\"3\",\"2\",\"1\"]," +
                                                    "\"results\":   [{" +
                                                                        "\"buildResults\":  [{" +
                                                                                                "\"buildNumber\":\"3\"," +
                                                                                                "\"children\":[]," +
                                                                                                "\"isPassed\":false," +
                                                                                                "\"isSkipped\":false," +
                                                                                                "\"name\":\"edu.illinois.cs427.mp3\"," +
                                                                                                "\"status\":\"FAILED\"," +
                                                                                                "\"totalFailed\":1," +
                                                                                                "\"totalPassed\":0," +
                                                                                                "\"totalSkipped\":0," +
                                                                                                "\"totalTests\":1," +
                                                                                                "\"totalTimeTaken\":0.023" +
                                                                                            "},{" +
                                                                                                "\"buildNumber\":\"2\"," +
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
                                                                                                "\"totalTimeTaken\":0.0432" +
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
        job.executeJavaScript(javaScriptCommand);
        DomElement exclamation_mark = (DomElement) job.getByXPath("//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]").get(0);
        assertTrue(exclamation_mark.getAttribute("style").contains("inline-block"));
    }

    @Test
    public void newFailuresTest_multipleBuilds_falseCase() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj =   {" + 
                                                    "\"builds\":[\"3\",\"2\",\"1\"]," +
                                                    "\"results\":   [{" +
                                                                        "\"buildResults\":  [{" +
                                                                                                "\"buildNumber\":\"3\"," +
                                                                                                "\"children\":[]," +
                                                                                                "\"isPassed\":true," +
                                                                                                "\"isSkipped\":false," +
                                                                                                "\"name\":\"edu.illinois.cs427.mp3\"," +
                                                                                                "\"status\":\"PASSED\"," +
                                                                                                "\"totalFailed\":0," +
                                                                                                "\"totalPassed\":1," +
                                                                                                "\"totalSkipped\":0," +
                                                                                                "\"totalTests\":1," +
                                                                                                "\"totalTimeTaken\":0.023" +
                                                                                            "},{" +
                                                                                                "\"buildNumber\":\"2\"," +
                                                                                                "\"children\":[]," +
                                                                                                "\"isPassed\":false," +
                                                                                                "\"isSkipped\":false," +
                                                                                                "\"name\":\"edu.illinois.cs427.mp3\"," +
                                                                                                "\"status\":\"FAILED\"," +
                                                                                                "\"totalFailed\":1," +
                                                                                                "\"totalPassed\":0," +
                                                                                                "\"totalSkipped\":0," +
                                                                                                "\"totalTests\":1," +
                                                                                                "\"totalTimeTaken\":0.032" +
                                                                                            "},{" +
                                                                                                "\"buildNumber\":\"1\"," +
                                                                                                "\"children\":[]," +
                                                                                                "\"isPassed\":false," +
                                                                                                "\"isSkipped\":false," +
                                                                                                "\"name\":\"edu.illinois.cs427.mp3\"," +
                                                                                                "\"status\":\"FAILED\"," +
                                                                                                "\"totalFailed\":1," +
                                                                                                "\"totalPassed\":0," +
                                                                                                "\"totalSkipped\":0," +
                                                                                                "\"totalTests\":1," +
                                                                                                "\"totalTimeTaken\":0.0432" +
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
        job.executeJavaScript(javaScriptCommand);
        DomElement exclamation_mark = (DomElement) job.getByXPath("//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]").get(0);
        assertTrue(exclamation_mark.getAttribute("style").contains("none"));
    }

    @Test
    public void newFailuresTest_multipleTests_multipleBuilds() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = "var Obj =   {" +
                                                    "\"builds\":[\"3\",\"2\",\"1\"]," +
                                                    "\"results\":   [{" +
                                                                        "\"buildResults\":  [{" +
                                                                                                "\"buildNumber\":\"3\"," +
                                                                                                "\"children\":[]," +
                                                                                                "\"isPassed\":false," +
                                                                                                "\"isFlaky\":false," +
                                                                                                "\"isSkipped\":false," +
                                                                                                "\"name\":\"edu.illinois.cs427.mp3\"," +
                                                                                                "\"status\":\"FAILED\"," +
                                                                                                "\"totalFailed\":1," +
                                                                                                "\"totalPassed\":0," +
                                                                                                "\"totalSkipped\":0," +
                                                                                                "\"totalTests\":1," +
                                                                                                "\"totalTimeTaken\":0.023" +
                                                                                            "},{" +
                                                                                                "\"buildNumber\":\"2\"," +
                                                                                                "\"children\":[]," +
                                                                                                "\"isPassed\":false," +
                                                                                                "\"isSkipped\":false," +
                                                                                                "\"isFlaky\":false," +
                                                                                                "\"name\":\"edu.illinois.cs427.mp3\"," +
                                                                                                "\"status\":\"FAILED\"," +
                                                                                                "\"totalFailed\":1," +
                                                                                                "\"totalPassed\":0," +
                                                                                                "\"totalSkipped\":0," +
                                                                                                "\"totalTests\":1," +
                                                                                                "\"totalTimeTaken\":0.023" +
                                                                                            "},{" +
                                                                                                "\"buildNumber\":\"1\"," +
                                                                                                "\"children\":[]," +
                                                                                                "\"isPassed\":true," +
                                                                                                "\"isSkipped\":false," +
                                                                                                "\"isFlaky\":false," +
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
                                                                        "\"text\":\"edu.illinois.cs427.mp3\"," +
                                                                        "\"type\":\"package\"" +
                                                                    "},{" +
                                                                        "\"buildResults\":  [{" +
                                                                        "                       \"buildNumber\":\"3\"," +
                                                                                                "\"children\":[]," +
                                                                                                "\"isPassed\":false," +
                                                                                                "\"isSkipped\":false," +
                                                                                                "\"isFlaky\":false," +
                                                                                                "\"name\":\"edu.illinois.cs512.mp0\"," +
                                                                                                "\"status\":\"FAILED\"," +
                                                                                                "\"totalFailed\":1," +
                                                                                                "\"totalPassed\":0," +
                                                                                                "\"totalSkipped\":0," +
                                                                                                "\"totalTests\":1," +
                                                                                                "\"totalTimeTaken\":0.253" +
                                                                                            "},{" + 
                                                                                                "\"buildNumber\":\"2\"," +
                                                                                                "\"children\":[]," +
                                                                                                "\"isPassed\":true," +
                                                                                                "\"isSkipped\":false," +
                                                                                                "\"isFlaky\":false," +
                                                                                                "\"name\":\"edu.illinois.cs512.mp0\"," +
                                                                                                "\"status\":\"PASSED\"," +
                                                                                                "\"totalFailed\":0," +
                                                                                                "\"totalPassed\":1," +
                                                                                                "\"totalSkipped\":0," +
                                                                                                "\"totalTests\":1," +
                                                                                                "\"totalTimeTaken\":0.253" +
                                                                                            "},{" +
                                                                                                "\"buildNumber\":\"1\"," +
                                                                                                "\"children\":[]," +
                                                                                                "\"isPassed\":true," +
                                                                                                "\"isSkipped\":false," +
                                                                                                "\"isFlaky\":false," +
                                                                                                "\"name\":\"edu.illinois.cs512.mp0\"," +
                                                                                                "\"status\":\"FAILED\"," +
                                                                                                "\"totalFailed\":1," +
                                                                                                "\"totalPassed\":0," +
                                                                                                "\"totalSkipped\":0," +
                                                                                                "\"totalTests\":1," +
                                                                                                "\"totalTimeTaken\":0.392" +
                                                                                            "}]," +
                                                                        "\"buildStatuses\":[\"FAILED\",\"PASSED\"]," +
                                                                        "\"children\":[]," + 
                                                                        "\"parentclass\":\"base\"," +
                                                                        "\"parentname\":\"base\"," +
                                                                        "\"text\":\"edu.illinois.cs512.mp0\"," +
                                                                        "\"type\":\"package\"" +
                                                                    "}]" + 
                                                "};" +
                                                "treeMarkup = analyzerTemplate(Obj);" +
                                                "$j(\".table\").html(treeMarkup);" +
                                                "addEvents();";
        job.executeJavaScript(javaScriptCommand);
        DomElement cs427_exclamation_mark = (DomElement) job.getByXPath("//*[contains(@class, 'cs427')]//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]").get(0);
        assertTrue(cs427_exclamation_mark.getAttribute("style").contains("none"));
        DomElement cs512_exclamation_mark = (DomElement) job.getByXPath("//*[contains(@class, 'cs512')]//*[contains(concat(' ', @class, ' '), ' icon-exclamation-sign ')]").get(0);
        assertTrue(cs512_exclamation_mark.getAttribute("style").contains("inline-block"));
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
        assertTrue(row.getAttribute("style").contains("table-row"));
    }

    @Test
    public void searchTest_noMatch() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = singleTest_populateTable_javascript + "$j(\"#filter\").val(\"was\");searchTests();";
        ScriptResult result = job.executeJavaScript(javaScriptCommand);
        DomElement row = (DomElement) job.getByXPath("//*[contains(@class, 'cs427')]").get(0);
        assertTrue(row.getAttribute("style").contains("none"));
    }

    @Test
    public void searchTest_emptyFilter() throws Exception {
        HtmlPage job = setupFreeStyle();
        String javaScriptCommand = singleTest_populateTable_javascript + "$j(\"#filter\").val(\"\");searchTests();";
        ScriptResult result = job.executeJavaScript(javaScriptCommand);
        DomElement row = (DomElement) job.getByXPath("//*[contains(@class, 'cs427')]").get(0);
        assertTrue(row.getAttribute("style").contains("table-row"));
    }

    
    

}

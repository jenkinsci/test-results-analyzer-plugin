package org.jenkinsci.plugins.testresultsanalyzer;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptJobManager;
import hudson.maven.MavenModuleSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class AnalyzerLinkTest {
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();
    JenkinsRule.WebClient webClient;
    HtmlPage page;

    //Note: Can't make it @BeforeClass as it requires the method to be static, which requires the jenkinsRule to be static (java.lang.Exception: The @Rule 'jenkinsRule' must not be static.)
    @Before
    public void setup() {
        webClient = jenkinsRule.createWebClient();
        //webClient.setThrowExceptionOnFailingStatusCode(false);
        //webClient.setThrowExceptionOnScriptError(false);

        MavenModuleSet project = (MavenModuleSet) jenkinsRule.getInstance().getItem("test");
        try {
            page = webClient.getPage(project, "edu.illinois.cs427$mp3/test_results_analyzer");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {

            e.printStackTrace();
        }

        //wait for scripts to load
        JavaScriptJobManager manager = page.getEnclosingWindow().getJobManager();
        while (manager.getJobCount() > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    @LocalData
    public void testLinkPackage() {
        //arrange
        String expectedLinkFragement = "/job/test/edu.illinois.cs427$mp3/3/testReport/edu.illinois.cs427.mp3";

        //act
        HtmlAnchor packageLink = page.getFirstByXPath("//div[@class='table-row base-edu_illinois_cs427_mp3']//div[@class='table-cell build-result passed']//a");
        String actualLink = packageLink.getHrefAttribute();

        //assert
        //Note: since they may be different depending on the context path (/jenkins/, etc..) check if the fragement is contained inside the link
        assertTrue(actualLink.contains(expectedLinkFragement));
    }

    @Test
    @LocalData
    public void testLinkClass() {
        //arrange
        String expectedLinkFragement = "/job/test/edu.illinois.cs427$mp3/3/testReport/edu.illinois.cs427.mp3/BookTest";

        //act
        HtmlAnchor packageLink = page.getFirstByXPath("//div[@class='table-row base-edu_illinois_cs427_mp3-BookTest']//div[@class='table-cell build-result passed']//a");
        String actualLink = packageLink.getHrefAttribute();

        //assert
        //Note: since they may be different depending on the context path (/jenkins/, etc..) check if the fragement is contained inside the link
        assertTrue(actualLink.contains(expectedLinkFragement));
    }

    @Test
    @LocalData
    public void testLinkTestCase() {
        //arrange
        String expectedLinkFragement = "/job/test/edu.illinois.cs427$mp3/3/testReport/edu.illinois.cs427.mp3/BookTest/testBookConstructor1";

        //act
        HtmlAnchor packageLink = page.getFirstByXPath("//div[@class='table-row base-edu_illinois_cs427_mp3-BookTest-testBookConstructor1']//div[@class='table-cell build-result passed']//a");
        String actualLink = packageLink.getHrefAttribute();

        //assert
        //Note: since they may be different depending on the context path (/jenkins/, etc..) check if the fragement is contained inside the link
        assertTrue(actualLink.contains(expectedLinkFragement));
    }
}

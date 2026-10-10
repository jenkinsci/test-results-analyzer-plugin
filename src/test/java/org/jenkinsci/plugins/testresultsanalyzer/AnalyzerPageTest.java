package org.jenkinsci.plugins.testresultsanalyzer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.junit.JUnitResultArchiver;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class AnalyzerPageTest {

    private JenkinsRule j;

    @BeforeEach
    void setUp(JenkinsRule j) {
        this.j = j;
    }

    /** Builds 1 and 2 pass; build 3 fails {@code testB}, which makes it a new failure. */
    private HtmlPage openAnalyzer() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                    throws InterruptedException, IOException {
                boolean failB = build.getNumber() >= 3;
                FilePath report = build.getWorkspace().child("TEST-com.example.CalculatorTest.xml");
                report.write(
                        "<testsuite name=\"com.example.CalculatorTest\" tests=\"2\" failures=\"" + (failB ? 1 : 0)
                                + "\">"
                                + "<testcase classname=\"com.example.CalculatorTest\" name=\"testA\" time=\"0.1\"/>"
                                + "<testcase classname=\"com.example.CalculatorTest\" name=\"testB\" time=\"0.2\">"
                                + (failB ? "<failure message=\"boom\">boom</failure>" : "")
                                + "</testcase></testsuite>",
                        "UTF-8");
                return true;
            }
        });
        JUnitResultArchiver archiver = new JUnitResultArchiver("*.xml");
        archiver.setAllowEmptyResults(true);
        project.getPublishersList().add(archiver);
        j.buildAndAssertSuccess(project);
        j.buildAndAssertSuccess(project);
        j.buildAndAssertStatus(Result.UNSTABLE, project);

        JenkinsRule.WebClient wc = j.createWebClient();
        HtmlPage page = wc.getPage(project, Constants.URL);
        wc.waitForBackgroundJavaScript(5_000);
        return page;
    }

    private static List<DomElement> rows(HtmlPage page) {
        return page.<DomElement>getByXPath(
                "//div[@id='tra-history']//div[contains(concat(' ', @class, ' '), ' tra-row ')]");
    }

    private static List<String> visibleRowNames(HtmlPage page) {
        return rows(page).stream()
                .filter(row -> !row.hasAttribute("hidden"))
                .map(row -> row.getAttribute("data-name"))
                .collect(Collectors.toList());
    }

    @Test
    void rendersCollapsedTreeWithNewFailureMarker() throws Exception {
        HtmlPage page = openAnalyzer();

        assertThat(rows(page), hasSize(4)); // package, class, two tests
        assertThat(visibleRowNames(page), is(List.of("com.example")));

        List<DomElement> markers = page.getByXPath("//div[@data-name='testB']//*[contains(@class, 'tra-new-failure')]");
        assertThat(markers, hasSize(1));
        assertThat(page.getByXPath("//div[@data-name='testA']//*[contains(@class, 'tra-new-failure')]"), is(empty()));

        DomElement testB = page.getFirstByXPath("//div[@data-name='testB']");
        assertThat(testB.getByXPath(".//*[contains(@class, 'tra-build--failed')]"), hasSize(1));
        assertThat(testB.getByXPath(".//*[contains(@class, 'tra-build--passed')]"), hasSize(2));
    }

    @Test
    void expandAndCollapseAll() throws Exception {
        HtmlPage page = openAnalyzer();

        page.<DomElement>getElementById("tra-expand-all").click();
        assertThat(visibleRowNames(page), is(List.of("com.example", "CalculatorTest", "testA", "testB")));

        page.<DomElement>getElementById("tra-collapse-all").click();
        assertThat(visibleRowNames(page), is(List.of("com.example")));
    }

    @Test
    void toggleShowsDirectChildren() throws Exception {
        HtmlPage page = openAnalyzer();

        DomElement toggle =
                page.getFirstByXPath("//div[@data-name='com.example']//button[contains(@class, 'tra-toggle')]");
        toggle.click();
        assertThat(toggle.getAttribute("aria-expanded"), is("true"));
        assertThat(visibleRowNames(page), is(List.of("com.example", "CalculatorTest")));
    }

    @Test
    void filterShowsMatchingRows() throws Exception {
        HtmlPage page = openAnalyzer();

        HtmlInput filter = page.getHtmlElementById("tra-filter");
        filter.setValue("testb");
        page.executeJavaScript("window.testResultsAnalyzer.applyFilter();");
        assertThat(visibleRowNames(page), is(List.of("testB")));

        filter.setValue("");
        page.executeJavaScript("window.testResultsAnalyzer.applyFilter();");
        assertThat(visibleRowNames(page), is(List.of("com.example")));
    }

    @Test
    void listsMostBrokenTests() throws Exception {
        HtmlPage page = openAnalyzer();

        DomElement worst = page.getElementById("tra-worst-tests");
        assertThat(worst.getTextContent(), containsString("com.example.CalculatorTest.testB"));
        assertThat(worst.getTextContent(), not(containsString("testA")));
    }

    @Test
    void computesRowStatistics() throws Exception {
        HtmlPage page = openAnalyzer();

        DomElement testB = page.getFirstByXPath("//div[@data-name='testB']");
        List<DomElement> numbers = testB.getByXPath(".//span[contains(concat(' ', @class, ' '), ' tra-stat ')]");
        assertThat(numbers.get(0).getTextContent(), is("67% (67%)"));
        assertThat(numbers.get(1).getTextContent(), is("1"));
    }
}

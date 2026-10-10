package org.jenkinsci.plugins.testresultsanalyzer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.tasks.junit.JUnitResultArchiver;
import java.io.IOException;
import java.util.List;
import java.util.function.IntFunction;
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
        return openAnalyzer(createProject(
                3,
                build -> suite(
                        "com.example.CalculatorTest",
                        testCase("com.example.CalculatorTest", "testA", ""),
                        testCase(
                                "com.example.CalculatorTest",
                                "testB",
                                build >= 3 ? "<failure message=\"boom\">boom</failure>" : ""))));
    }

    private HtmlPage openAnalyzer(FreeStyleProject project) throws Exception {
        JenkinsRule.WebClient wc = j.createWebClient();
        HtmlPage page = wc.getPage(project, Constants.URL);
        wc.waitForBackgroundJavaScript(5_000);
        return page;
    }

    /** A job whose build N publishes the JUnit XML returned for N. */
    private FreeStyleProject createProject(int builds, IntFunction<String> reportForBuild) throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                    throws InterruptedException, IOException {
                FilePath report = build.getWorkspace().child("TEST-report.xml");
                report.write(reportForBuild.apply(build.getNumber()), "UTF-8");
                return true;
            }
        });
        JUnitResultArchiver archiver = new JUnitResultArchiver("*.xml");
        archiver.setAllowEmptyResults(true);
        project.getPublishersList().add(archiver);
        for (int i = 0; i < builds; i++) {
            j.waitForCompletion(project.scheduleBuild2(0).waitForStart());
        }
        return project;
    }

    private static String suite(String name, String... testCases) {
        return "<testsuite name=\"" + name + "\">" + String.join("", testCases) + "</testsuite>";
    }

    private static String testCase(String className, String name, String body) {
        return "<testcase classname=\"" + className + "\" name=\"" + name + "\" time=\"0.1\">" + body + "</testcase>";
    }

    private static List<DomElement> rows(HtmlPage page) {
        return page.<DomElement>getByXPath(
                "//div[@id='tra-history']//div[contains(concat(' ', @class, ' '), ' tra-row ')]");
    }

    private static String checkedRows(HtmlPage page) {
        return page.executeJavaScript("String(document.querySelectorAll('.tra-row-select:checked').length)")
                .getJavaScriptResult()
                .toString();
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
        assertThat(markers.get(0).getAttribute("aria-label"), startsWith("New failure: failed in the latest build"));
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

    @Test
    void selectModeShowsCheckboxesAndClearsSelectionWhenDone() throws Exception {
        HtmlPage page = openAnalyzer();
        DomElement history = page.getElementById("tra-history");
        DomElement toggle = page.getElementById("tra-select-toggle");
        assertThat(history.getAttribute("class"), not(containsString("tra-history-container--selecting")));

        toggle.click();
        assertThat(toggle.getAttribute("aria-pressed"), is("true"));
        assertThat(history.getAttribute("class"), containsString("tra-history-container--selecting"));

        HtmlInput checkbox = page.getFirstByXPath("//div[@data-name='com.example']//input[@type='checkbox']");
        checkbox.setChecked(true);
        assertThat(checkedRows(page), is("4"));

        toggle.click();
        assertThat(toggle.getAttribute("aria-pressed"), is("false"));
        assertThat(history.getAttribute("class"), not(containsString("tra-history-container--selecting")));
        assertThat(checkedRows(page), is("0"));
    }

    @Test
    void sidePanelLinkOnlyForJobsWithTestResults() throws Exception {
        FreeStyleProject withTests = createProject(1, build -> suite("p.T", testCase("p.T", "a", "")));
        FreeStyleProject withoutTests = j.createFreeStyleProject();
        j.buildAndAssertSuccess(withoutTests);
        JenkinsRule.WebClient wc = j.createWebClient().withJavaScriptEnabled(false);

        String link = "//a[contains(@href, '" + Constants.URL + "')]";
        assertThat(wc.getPage(withTests).getByXPath(link), not(empty()));
        assertThat(wc.getPage(withoutTests).getByXPath(link), is(empty()));
        // The page itself still works, for existing links
        assertThat(wc.getPage(withoutTests, Constants.URL).getWebResponse().getStatusCode(), is(200));
    }

    @Test
    void errorsCountAsFailures() throws Exception {
        HtmlPage page = openAnalyzer(createProject(
                1, build -> suite("p.T", testCase("p.T", "errors", "<error message=\"npe\">npe</error>"))));
        DomElement test = page.getFirstByXPath("//div[@data-name='errors']");
        assertThat(test.getByXPath(".//*[contains(@class, 'tra-build--failed')]"), hasSize(1));
        assertThat(page.getElementById("tra-worst-tests").getTextContent(), containsString("p.T.errors"));
    }

    @Test
    void numberOfMostBrokenTestsCanBeChanged() throws Exception {
        HtmlPage page = openAnalyzer(createProject(
                1,
                build -> suite(
                        "p.T",
                        testCase("p.T", "one", "<failure>x</failure>"),
                        testCase("p.T", "two", "<failure>x</failure>"))));
        assertThat(page.getByXPath("//li[contains(@class, 'tra-worst__item')]"), hasSize(2));

        HtmlInput count = page.getHtmlElementById("tra-worst-count");
        count.setValue("1");
        count.fireEvent("change");
        assertThat(page.getByXPath("//li[contains(@class, 'tra-worst__item')]"), hasSize(1));
    }

    @Test
    void testsThatDidNotRunInTheShownBuildsAreHidden() throws Exception {
        // "removed" only ran in build 1; only the last 2 builds are shown
        FreeStyleProject project = createProject(
                3,
                build -> suite("p.T", testCase("p.T", "kept", ""), build == 1 ? testCase("p.T", "removed", "") : ""));
        HtmlPage page = openAnalyzer(project);
        page.<DomElement>getElementById("tra-options-toggle").click();
        HtmlInput allBuilds = page.getHtmlElementById("tra-all-builds");
        if (allBuilds.isChecked()) {
            allBuilds.click();
        }
        page.<HtmlInput>getHtmlElementById("tra-builds").setValue("2");
        page.<DomElement>getElementById("tra-apply").click();
        page.getWebClient().waitForBackgroundJavaScript(5_000);
        assertThat(
                page.getByXPath("//div[contains(@class, 'tra-history__header')]//span[contains(@class, 'tra-build')]"),
                hasSize(2));
        page.<DomElement>getElementById("tra-expand-all").click();
        assertThat(visibleRowNames(page), is(List.of("p", "T", "kept")));

        page.<HtmlInput>getHtmlElementById("tra-show-not-run").click();
        page.<DomElement>getElementById("tra-expand-all").click();
        assertThat(visibleRowNames(page), is(List.of("p", "T", "kept", "removed")));
    }

    @Test
    void csvExportEscapesQuotes() throws Exception {
        FreeStyleProject project =
                createProject(1, build -> suite("p.T", testCase("p.T", "takes(&quot;a, b&quot;)", "")));
        TestResultsAnalyzerAction action = project.getAction(TestResultsAnalyzerAction.class);
        action.getJsonLoadData();

        String[] lines = action.getExportCSV("false", "-1").split(System.lineSeparator());
        assertThat(lines[0], is("\"Package\",\"Class\",\"Test\",\"1\""));
        assertThat(lines[1], is("\"p\",\"T\",\"takes(\"\"a, b\"\")\",\"PASSED\""));
    }
}

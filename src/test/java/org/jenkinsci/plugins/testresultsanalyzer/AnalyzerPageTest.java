package org.jenkinsci.plugins.testresultsanalyzer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.tasks.junit.JUnitResultArchiver;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.IntFunction;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.Page;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.TestBuilder;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.kohsuke.stapler.StaplerRequest2;

/**
 * Covers the page shell and the endpoints the page reads. How the page behaves is covered by the
 * Vitest specs in src/main/frontend.
 */
@WithJenkins
class AnalyzerPageTest {

    private JenkinsRule j;

    @BeforeEach
    void setUp(JenkinsRule j) {
        this.j = j;
    }

    /** Builds 1 and 2 pass; build 3 fails {@code testB}. */
    private FreeStyleProject calculatorProject() throws Exception {
        return createProject(
                3,
                build -> suite(
                        "com.example.CalculatorTest",
                        testCase("com.example.CalculatorTest", "testA", ""),
                        testCase(
                                "com.example.CalculatorTest",
                                "testB",
                                build >= 3 ? "<failure message=\"boom\">boom</failure>" : "")));
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

    private JSONObject data(FreeStyleProject project, String query) throws Exception {
        Page page = j.createWebClient().goTo(project.getUrl() + Constants.URL + "/data?" + query, "application/json");
        return JSONObject.fromObject(page.getWebResponse().getContentAsString());
    }

    private static JSONObject child(JSONObject node, int index) {
        return node.getJSONArray("children").getJSONObject(index);
    }

    @Test
    void pageMountsTheAnalyzerWithItsConfiguration() throws Exception {
        FreeStyleProject project = calculatorProject();
        HtmlPage page = j.createWebClient().withJavaScriptEnabled(false).getPage(project, Constants.URL);

        DomElement root = page.getElementById("tra-root");
        assertThat(root.getAttribute("data-action-url"), endsWith(project.getUrl() + Constants.URL + "/"));
        JSONObject bootstrap = JSONObject.fromObject(root.getAttribute("data-bootstrap"));
        assertThat(bootstrap.getJSONObject("labels").getString("passed"), is("PASSED"));
        assertThat(bootstrap.getJSONObject("defaults").getString("chartDataType"), is("passfail"));
        assertThat(
                "theme colours are used by default",
                bootstrap.get("customColors").toString(),
                is("null"));

        assertThat(page.getByXPath("//script[contains(@src, 'js/bundles/analyzer-bundle.js')]"), hasSize(1));
        assertThat(page.getElementById("tra-options-toggle"), not(is((Object) null)));
        assertThat(page.getElementById("tra-download-csv"), not(is((Object) null)));
    }

    @Test
    void administratorRunLimitCapsHowManyBuildsCanBeChosen() throws Exception {
        configureGlobally("noOfRunsToFetch: 2");
        try {
            FreeStyleProject project = calculatorProject();
            TestResultsAnalyzerAction action = project.getAction(TestResultsAnalyzerAction.class);
            assertThat(action.getMaxChosenBuilds(), is(2));
            assertThat(JSONObject.fromObject(action.getBootstrapJson()).getInt("maxChosenBuilds"), is(2));
            // Older builds than the latest two can still be chosen, just not more than two of them
            assertThat(data(project, "buildNumbers=" + encode("1,3")).getJSONArray("builds"), hasSize(2));
            JenkinsRule.WebClient wc = j.createWebClient();
            for (String endpoint : new String[] {"/data", "/csv"}) {
                try {
                    wc.goTo(project.getUrl() + Constants.URL + endpoint + "?buildNumbers=1-3", null);
                    throw new AssertionError(endpoint + " should reject more builds than the limit");
                } catch (FailingHttpStatusCodeException e) {
                    assertThat(e.getStatusCode(), is(400));
                }
            }
        } finally {
            // The descriptor outlives the Jenkins instance of a test
            configureGlobally("");
        }
    }

    @Test
    void customStatusColoursArePassedToThePage() throws Exception {
        configureGlobally("useCustomStatusColors: {passedColor: '#00ff00', failedColor: '#ff0000',"
                + " skippedColor: '#ffff00', naColor: '#cccccc'}");
        try {
            FreeStyleProject project = calculatorProject();
            String bootstrap =
                    project.getAction(TestResultsAnalyzerAction.class).getBootstrapJson();
            assertThat(
                    JSONObject.fromObject(bootstrap)
                            .getJSONObject("customColors")
                            .getString("passed"),
                    is("#00ff00"));
        } finally {
            // The descriptor outlives the Jenkins instance of a test
            configureGlobally("");
        }
    }

    /** Saves the global configuration with the given settings over the defaults. */
    private static void configureGlobally(String overrides) {
        JSONObject form = JSONObject.fromObject("{noOfBuilds: '10', noOfRunsToFetch: 0, showAllBuilds: false,"
                + " showBuildTime: false, hideConfigurationMethods: false, showLineGraph: true,"
                + " showBarGraph: true, showPieGraph: true, runTimeLowThreshold: '0.5',"
                + " runTimeHighThreshold: '1.5', chartDataType: 'passfail'}");
        form.putAll(JSONObject.fromObject("{" + overrides + "}"));
        TestResultsAnalyzerExtension.DESCRIPTOR.configure((StaplerRequest2) null, form);
    }

    @Test
    void runTimeThresholdsThatAreNotNumbersAreIgnored() throws Exception {
        assertThat(TestResultsAnalyzerAction.parseSeconds(" 1.5 "), is(1.5));
        for (String value : new String[] {null, "", "fast", "NaN", "Infinity", "-Infinity"}) {
            assertThat(value, TestResultsAnalyzerAction.parseSeconds(value), is(0.0));
        }

        configureGlobally("runTimeLowThreshold: 'NaN', runTimeHighThreshold: 'Infinity'");
        try {
            String bootstrap = calculatorProject()
                    .getAction(TestResultsAnalyzerAction.class)
                    .getBootstrapJson();
            JSONObject json = JSONObject.fromObject(bootstrap);
            assertThat(json.getDouble("runTimeLowThreshold"), is(0.0));
            assertThat(json.getDouble("runTimeHighThreshold"), is(0.0));
        } finally {
            configureGlobally("");
        }
    }

    @Test
    void dataServesTheTreeNewestBuildFirst() throws Exception {
        FreeStyleProject project = calculatorProject();
        JSONObject data = data(project, "builds=-1&hideConfigMethods=false");

        assertThat(data.getJSONArray("builds"), is(JSONArray.fromObject("[\"3\",\"2\",\"1\"]")));
        JSONObject pkg = data.getJSONArray("results").getJSONObject(0);
        assertThat(pkg.getString("text"), is("com.example"));
        JSONObject testB = child(child(pkg, 0), 1);
        assertThat(testB.getString("text"), is("testB"));
        JSONArray results = testB.getJSONArray("buildResults");
        assertThat(results.getJSONObject(0).getString("status"), is("FAILED"));
        assertThat(results.getJSONObject(1).getString("status"), is("PASSED"));
        assertThat(
                results.getJSONObject(0).getString("url"),
                endsWith(project.getUrl() + "3/testReport/com.example/CalculatorTest/testB"));
    }

    @Test
    void dataIsLimitedToTheRequestedBuilds() throws Exception {
        // "removed" only ran in build 1
        FreeStyleProject project = createProject(
                3,
                build -> suite("p.T", testCase("p.T", "kept", ""), build == 1 ? testCase("p.T", "removed", "") : ""));

        JSONObject all = data(project, "builds=-1&hideConfigMethods=false");
        assertThat(all.getJSONArray("builds"), hasSize(3));
        assertThat(child(all.getJSONArray("results").getJSONObject(0), 0).getJSONArray("children"), hasSize(2));

        // Only the requested builds are read, so "removed" is not listed at all
        JSONObject latest = data(project, "builds=2&hideConfigMethods=false");
        assertThat(latest.getJSONArray("builds"), is(JSONArray.fromObject("[\"3\",\"2\"]")));
        JSONArray tests =
                child(latest.getJSONArray("results").getJSONObject(0), 0).getJSONArray("children");
        assertThat(tests, hasSize(1));
        assertThat(tests.getJSONObject(0).getString("text"), is("kept"));
    }

    @Test
    void dataServesTheChosenBuilds() throws Exception {
        // "flaky" fails in even builds
        FreeStyleProject project = createProject(
                6, build -> suite("p.T", testCase("p.T", "flaky", build % 2 == 0 ? "<failure message=\"x\"/>" : "")));

        // Builds that do not exist are left out
        JSONObject chosen = data(project, "buildNumbers=" + encode("1, 3-4,99") + "&hideConfigMethods=false");
        assertThat(chosen.getJSONArray("builds"), is(JSONArray.fromObject("[\"4\",\"3\",\"1\"]")));
        JSONArray results = child(child(chosen.getJSONArray("results").getJSONObject(0), 0), 0)
                .getJSONArray("buildResults");
        assertThat(results.getJSONObject(0).getString("status"), is("FAILED"));
        assertThat(results.getJSONObject(1).getString("status"), is("PASSED"));
        assertThat(results.getJSONObject(2).getString("buildNumber"), is("1"));

        // The cache follows the request rather than serving the previous one
        assertThat(
                data(project, "builds=2&hideConfigMethods=false").getJSONArray("builds"),
                is(JSONArray.fromObject("[\"6\",\"5\"]")));
        assertThat(
                data(project, "buildNumbers=2&builds=2").getJSONArray("builds"), is(JSONArray.fromObject("[\"2\"]")));
        assertThat(data(project, "buildNumbers=2-3").getJSONArray("builds"), is(JSONArray.fromObject("[\"3\",\"2\"]")));
        assertThat(data(project, "buildNumbers=50-60").getJSONArray("builds"), hasSize(0));
        // A blank selection means the latest builds
        assertThat(data(project, "builds=1&buildNumbers=").getJSONArray("builds"), is(JSONArray.fromObject("[\"6\"]")));

        // A deleted build is noticed
        project.getBuildByNumber(3).delete();
        assertThat(data(project, "buildNumbers=2-3").getJSONArray("builds"), is(JSONArray.fromObject("[\"2\"]")));
    }

    @Test
    void invalidBuildNumbersAreABadRequest() throws Exception {
        FreeStyleProject project = calculatorProject();
        JenkinsRule.WebClient wc = j.createWebClient();
        for (String endpoint : new String[] {"/data", "/csv"}) {
            for (String spec : new String[] {"abc", "1-2-3", "0", "-1", "1-99999999", ",,"}) {
                try {
                    wc.goTo(project.getUrl() + Constants.URL + endpoint + "?buildNumbers=" + encode(spec), null);
                    throw new AssertionError(endpoint + " should reject " + spec);
                } catch (FailingHttpStatusCodeException e) {
                    assertThat(spec, e.getStatusCode(), is(400));
                }
            }
        }
    }

    @Test
    void csvDownloadOfTheChosenBuilds() throws Exception {
        FreeStyleProject project = calculatorProject();
        Page page = j.createWebClient()
                .goTo(project.getUrl() + Constants.URL + "/csv?buildNumbers=" + encode("1,3"), "text/csv");
        String[] lines = page.getWebResponse().getContentAsString().split(System.lineSeparator());
        assertThat(lines[0], is("\"Package\",\"Class\",\"Test\",\"3\",\"1\""));
        assertThat(lines[2], is("\"com.example\",\"CalculatorTest\",\"testB\",\"FAILED\",\"PASSED\""));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @Test
    void errorsCountAsFailures() throws Exception {
        FreeStyleProject project = createProject(
                1, build -> suite("p.T", testCase("p.T", "errors", "<error message=\"npe\">npe</error>")));

        JSONObject data = data(project, "builds=-1&hideConfigMethods=false");
        JSONObject test = child(child(data.getJSONArray("results").getJSONObject(0), 0), 0);
        assertThat(test.getJSONArray("buildResults").getJSONObject(0).getString("status"), is("FAILED"));
    }

    @Test
    void csvDownloadEscapesValues() throws Exception {
        FreeStyleProject project =
                createProject(1, build -> suite("p.T", testCase("p.T", "takes(&quot;a, b&quot;)", "")));

        Page page = j.createWebClient().goTo(project.getUrl() + Constants.URL + "/csv?builds=-1", "text/csv");
        assertThat(page.getWebResponse().getResponseHeaderValue("Content-Disposition"), containsString("attachment"));
        String[] lines = page.getWebResponse().getContentAsString().split(System.lineSeparator());
        assertThat(lines[0], is("\"Package\",\"Class\",\"Test\",\"1\""));
        assertThat(lines[1], is("\"p\",\"T\",\"takes(\"\"a, b\"\")\",\"PASSED\""));
    }

    @Test
    void csvValuesCannotRunAsSpreadsheetFormulas() {
        assertThat(TestResultsAnalyzerAction.csvValue("=HYPERLINK(\"x\")"), is("\"'=HYPERLINK(\"\"x\"\")\""));
        assertThat(TestResultsAnalyzerAction.csvValue("+1"), is("\"'+1\""));
        assertThat(TestResultsAnalyzerAction.csvValue("-1"), is("\"'-1\""));
        assertThat(TestResultsAnalyzerAction.csvValue("@SUM(A1)"), is("\"'@SUM(A1)\""));
        assertThat(TestResultsAnalyzerAction.csvValue("a=b"), is("\"a=b\""));
        assertThat(TestResultsAnalyzerAction.csvValue(null), is("\"\""));
    }

    @Test
    void endpointsNeedReadPermissionOnTheJob() throws Exception {
        FreeStyleProject project = calculatorProject();
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy()
                .grant(Jenkins.READ)
                .everywhere()
                .to("reader", "outsider")
                .grant(Item.READ)
                .onItems(project)
                .to("reader"));

        JenkinsRule.WebClient reader = j.createWebClient().login("reader");
        reader.goTo(project.getUrl() + Constants.URL + "/data?builds=-1", "application/json");

        JenkinsRule.WebClient outsider = j.createWebClient().login("outsider");
        for (String endpoint :
                new String[] {"/data?builds=-1", "/csv?builds=-1", "/data?buildNumbers=1", "/csv?buildNumbers=1"}) {
            try {
                outsider.goTo(project.getUrl() + Constants.URL + endpoint, null);
                throw new AssertionError(endpoint + " should not be reachable");
            } catch (FailingHttpStatusCodeException e) {
                assertThat(e.getStatusCode(), is(404));
            }
        }
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
}

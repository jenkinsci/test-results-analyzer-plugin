package org.jenkinsci.plugins.testresultsanalyzer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.tasks.junit.JUnitResultArchiver;
import java.io.IOException;
import java.net.URL;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.htmlunit.HttpMethod;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.TestBuilder;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Pins the contract of the {@code data} and {@code csv} endpoints as documented in the README's REST API
 * section, calling them as a script would: a plain GET authenticated with an API token and no crumb.
 * If one of these assertions has to change, update the README too.
 */
@WithJenkins
class RestApiTest {

    private JenkinsRule j;
    private FreeStyleProject project;

    @BeforeEach
    void setUp(JenkinsRule j) throws Exception {
        this.j = j;
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy()
                .grant(Jenkins.ADMINISTER)
                .everywhere()
                .to("admin")
                .grant(Jenkins.READ)
                .everywhere()
                .to("reader", "outsider")
                .grant(Item.READ)
                .onPaths("api-job")
                .to("reader"));

        project = j.createFreeStyleProject("api-job");
        project.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                    throws InterruptedException, IOException {
                // Build 1: a passes, b passes. Build 2: a passes, b fails, c only runs here and is skipped.
                boolean second = build.getNumber() == 2;
                String xml = "<testsuite name=\"com.example.ApiTest\">"
                        + testCase("a", "")
                        + testCase("b", second ? "<failure message=\"boom\">boom</failure>" : "")
                        + (second ? testCase("c", "<skipped/>") : "")
                        + "</testsuite>";
                FilePath report = build.getWorkspace().child("TEST-report.xml");
                report.write(xml, "UTF-8");
                return true;
            }
        });
        project.getPublishersList().add(new JUnitResultArchiver("*.xml"));
        j.buildAndAssertSuccess(project);
        j.assertBuildStatus(hudson.model.Result.UNSTABLE, project.scheduleBuild2(0));
    }

    private static String testCase(String name, String body) {
        return "<testcase classname=\"com.example.ApiTest\" name=\"" + name + "\" time=\"0.25\">" + body
                + "</testcase>";
    }

    /** A GET as {@code curl -u user:token} would send it: basic auth with an API token, no crumb, no session. */
    private WebResponse get(String user, String path) throws Exception {
        JenkinsRule.WebClient wc = j.createWebClient();
        wc.getOptions().setThrowExceptionOnFailingStatusCode(false);
        wc.getOptions().setRedirectEnabled(false);
        if (user != null) {
            wc.withBasicApiToken(user);
        }
        WebRequest request =
                new WebRequest(new URL(j.getURL(), project.getUrl() + Constants.URL + "/" + path), HttpMethod.GET);
        return wc.loadWebResponse(request);
    }

    @Test
    void dataReturnsTheDocumentedJson() throws Exception {
        WebResponse response = get("reader", "data?builds=2");
        assertThat(response.getStatusCode(), is(200));
        assertThat(response.getContentType(), is("application/json"));

        JSONObject data = JSONObject.fromObject(response.getContentAsString());
        assertThat(data.keySet(), containsInAnyOrder("builds", "buildInfo", "results"));
        assertThat(
                "newest build first, as strings",
                data.getJSONArray("builds"),
                is(JSONArray.fromObject("[\"2\",\"1\"]")));

        // One entry per build, in the same order as "builds"
        JSONArray buildInfo = data.getJSONArray("buildInfo");
        assertThat(buildInfo.size(), is(2));
        for (int i = 0; i < buildInfo.size(); i++) {
            JSONObject build = buildInfo.getJSONObject(i);
            int number = 2 - i;
            assertThat(build.keySet(), containsInAnyOrder("number", "displayName", "timestamp", "url"));
            assertThat(build.getInt("number"), is(number));
            assertThat(build.getString("displayName"), is("#" + number));
            assertThat(
                    build.getLong("timestamp"),
                    is(project.getBuildByNumber(number).getTimeInMillis()));
            assertThat(build.getString("url"), is(j.getURL() + project.getUrl() + number + "/"));
        }

        JSONArray packages = data.getJSONArray("results");
        assertThat(packages.size(), is(1));
        JSONObject pkg = packages.getJSONObject(0);
        assertNode(pkg, "com.example");
        JSONObject clazz = pkg.getJSONArray("children").getJSONObject(0);
        assertNode(clazz, "ApiTest");
        JSONArray tests = clazz.getJSONArray("children");
        assertThat(tests.size(), is(3));
        for (int i = 0; i < tests.size(); i++) {
            assertNode(tests.getJSONObject(i), null);
            assertThat(
                    "tests are leaves",
                    tests.getJSONObject(i).getJSONArray("children").size(),
                    is(0));
        }

        // Cells line up with "builds": index 0 is build 2, index 1 is build 1
        JSONArray classCells = clazz.getJSONArray("buildResults");
        JSONObject latest = classCells.getJSONObject(0);
        assertThat(
                latest.keySet(),
                containsInAnyOrder(
                        "buildNumber",
                        "totalTests",
                        "totalFailed",
                        "totalPassed",
                        "totalSkipped",
                        "totalTimeTaken",
                        "status",
                        "url"));
        assertThat(latest.getString("buildNumber"), is("2"));
        assertThat(latest.getInt("totalTests"), is(3));
        assertThat(latest.getInt("totalFailed"), is(1));
        assertThat(latest.getInt("totalPassed"), is(1));
        assertThat(latest.getInt("totalSkipped"), is(1));
        assertThat(latest.getDouble("totalTimeTaken"), is(0.75));
        assertThat(latest.getString("status"), is("FAILED"));
        assertThat(latest.getString("url"), startsWith(j.getURL() + project.getUrl() + "2/"));
        assertThat(classCells.getJSONObject(1).getString("status"), is("PASSED"));

        JSONObject c = testNamed(tests, "c");
        assertThat(c.getJSONArray("buildResults").getJSONObject(0).getString("status"), is("SKIPPED"));
        // A test that did not run in a build gets a cell with only the build number and N/A
        JSONObject missing = c.getJSONArray("buildResults").getJSONObject(1);
        assertThat(missing.keySet(), containsInAnyOrder("buildNumber", "status"));
        assertThat(missing.getString("buildNumber"), is("1"));
        assertThat(missing.getString("status"), is("N/A"));
    }

    @Test
    void dataIsLimitedToTheRequestedNumberOfBuilds() throws Exception {
        JSONObject data = JSONObject.fromObject(get("reader", "data?builds=1").getContentAsString());
        assertThat(data.getJSONArray("builds"), is(JSONArray.fromObject("[\"2\"]")));
        assertThat(data.getJSONArray("buildInfo").size(), is(1));
        assertThat(
                data.getJSONArray("results")
                        .getJSONObject(0)
                        .getJSONArray("buildResults")
                        .size(),
                is(1));

        JSONObject all = JSONObject.fromObject(get("reader", "data").getContentAsString());
        assertThat(
                "no builds parameter means all builds",
                all.getJSONArray("builds").size(),
                is(2));
    }

    @Test
    void csvReturnsTheDocumentedColumns() throws Exception {
        WebResponse response = get("reader", "csv?builds=2");
        assertThat(response.getStatusCode(), is(200));
        assertThat(response.getContentType(), is("text/csv"));
        assertThat(response.getResponseHeaderValue("Content-Disposition"), containsString("test-results.csv"));
        String[] lines = response.getContentAsString().split("\\R");
        assertThat(lines[0], is("\"Package\",\"Class\",\"Test\",\"2\",\"1\""));
        assertThat(lines[1], is("\"com.example\",\"ApiTest\",\"a\",\"PASSED\",\"PASSED\""));
        assertThat(lines[2], is("\"com.example\",\"ApiTest\",\"b\",\"FAILED\",\"PASSED\""));
        assertThat(lines[3], is("\"com.example\",\"ApiTest\",\"c\",\"SKIPPED\",\"N/A\""));

        String[] durations = get("reader", "csv?builds=2&durations=true")
                .getContentAsString()
                .split("\\R");
        assertThat(durations[1], is("\"com.example\",\"ApiTest\",\"a\",\"0.25\",\"0.25\""));
    }

    @Test
    void endpointsNeedReadPermissionOnTheJob() throws Exception {
        for (String path : new String[] {"data?builds=2", "csv?builds=2"}) {
            assertThat(path, get(null, path).getStatusCode(), is(403));
            // Jenkins hides jobs a user cannot read
            assertThat(path, get("outsider", path).getStatusCode(), is(404));
            assertThat(path, get("admin", path).getStatusCode(), is(200));
        }
    }

    private static void assertNode(JSONObject node, String text) {
        assertThat(node.keySet(), containsInAnyOrder("text", "buildResults", "children"));
        if (text != null) {
            assertThat(node.getString("text"), is(text));
        }
        assertThat(node.getJSONArray("buildResults").size(), is(2));
    }

    private static JSONObject testNamed(JSONArray tests, String name) {
        for (int i = 0; i < tests.size(); i++) {
            if (tests.getJSONObject(i).getString("text").equals(name)) {
                return tests.getJSONObject(i);
            }
        }
        throw new AssertionError("no test named " + name + " in " + tests);
    }
}

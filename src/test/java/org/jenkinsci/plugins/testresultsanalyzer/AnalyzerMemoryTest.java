package org.jenkinsci.plugins.testresultsanalyzer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.tasks.junit.JUnitResultArchiver;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.management.ObjectName;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.ResultInfo;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Regression test for <a href="https://github.com/jenkinsci/test-results-analyzer-plugin/issues/226">#226</a> and
 * <a href="https://github.com/jenkinsci/test-results-analyzer-plugin/issues/203">#203</a>: the page used to bind a
 * new action into the HTTP session on every view, keeping the results it loaded reachable until the session expired.
 *
 * <p>Counts live instances rather than measuring heap size, which is too noisy to assert on.
 */
@WithJenkins
class AnalyzerMemoryTest {

    private static final Logger LOG = Logger.getLogger(AnalyzerMemoryTest.class.getName());

    private static final int BUILDS = Integer.getInteger("AnalyzerMemoryTest.builds", 3);
    private static final int TESTS = Integer.getInteger("AnalyzerMemoryTest.tests", 20);
    private static final int VIEWS = Integer.getInteger("AnalyzerMemoryTest.views", 5);
    private static final String PLUGIN_PACKAGE = AnalyzerMemoryTest.class.getPackageName() + ".";

    @Test
    void pageViewsDoNotRetainLoadedResults(JenkinsRule j) throws Exception {
        FreeStyleProject project = createProject(j);
        String actionUrl = project.getUrl() + Constants.URL + "/";
        // One session for every view, as a user leaving the page open and reloading it
        JenkinsRule.WebClient wc = j.createWebClient().withJavaScriptEnabled(false);

        long[] resultInfos = new long[VIEWS];
        long[] pluginObjects = new long[VIEWS];
        for (int view = 0; view < VIEWS; view++) {
            // What the page requests: the shell, the data for the default and for all builds, the CSV
            wc.goTo(actionUrl);
            wc.goTo(actionUrl + "data?builds=10&hideConfigMethods=false", "application/json");
            String data = wc.goTo(actionUrl + "data?builds=-1&hideConfigMethods=true", "application/json")
                    .getWebResponse()
                    .getContentAsString();
            assertThat("every test was loaded", data, containsString("\"test" + (TESTS - 1) + "\""));
            wc.goTo(actionUrl + "csv?builds=-1&durations=false", "text/csv");

            Map<String, Long> live = liveInstances();
            resultInfos[view] = live.getOrDefault(ResultInfo.class.getName(), 0L);
            // Actions, results and their cells: anything of this plugin, bar this test and its lambdas
            pluginObjects[view] = live.entrySet().stream()
                    .filter(e -> e.getKey().startsWith(PLUGIN_PACKAGE)
                            && !e.getKey().startsWith(AnalyzerMemoryTest.class.getName()))
                    .mapToLong(Map.Entry::getValue)
                    .sum();
            LOG.info(String.format(
                    "after %d views: %d ResultInfo, %d objects of the plugin live",
                    view + 1, resultInfos[view], pluginObjects[view]));
        }

        // Each view loads the results again, so anything kept per view would show up as growth
        for (int view = 1; view < VIEWS; view++) {
            assertThat(
                    "live ResultInfo after " + (view + 1) + " views",
                    resultInfos[view],
                    lessThanOrEqualTo(resultInfos[0]));
            assertThat(
                    "live objects of the plugin after " + (view + 1) + " views",
                    pluginObjects[view],
                    lessThanOrEqualTo(pluginObjects[0]));
        }
    }

    private static FreeStyleProject createProject(JenkinsRule j) throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                    throws InterruptedException, IOException {
                FilePath report = build.getWorkspace().child("TEST-report.xml");
                report.write(report(), "UTF-8");
                return true;
            }
        });
        project.getPublishersList().add(new JUnitResultArchiver("*.xml"));
        for (int i = 0; i < BUILDS; i++) {
            j.waitForCompletion(project.scheduleBuild2(0).waitForStart());
        }
        return project;
    }

    private static String report() {
        StringBuilder xml = new StringBuilder("<testsuite name=\"com.example.ExampleTest\">");
        for (int t = 0; t < TESTS; t++) {
            xml.append("<testcase classname=\"com.example.ExampleTest\" name=\"test")
                    .append(t)
                    .append("\" time=\"0.1\"/>");
        }
        return xml.append("</testsuite>").toString();
    }

    /** Live instances per class name, after a full GC. */
    private static Map<String, Long> liveInstances() throws Exception {
        String histogram = (String) ManagementFactory.getPlatformMBeanServer()
                .invoke(
                        new ObjectName("com.sun.management:type=DiagnosticCommand"),
                        "gcClassHistogram",
                        new Object[] {new String[0]},
                        new String[] {String[].class.getName()});
        Map<String, Long> instances = new HashMap<>();
        Pattern row = Pattern.compile("^\\s*\\d+:\\s+(\\d+)\\s+\\d+\\s+(\\S+)", Pattern.MULTILINE);
        Matcher m = row.matcher(histogram);
        while (m.find()) {
            instances.merge(m.group(2), Long.parseLong(m.group(1)), Long::sum);
        }
        return instances;
    }
}

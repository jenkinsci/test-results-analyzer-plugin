package org.jenkinsci.plugins.testresultsanalyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.tasks.junit.SuiteResult;
import hudson.tasks.junit.TestResultAction;
import io.jenkins.plugins.junit.storage.CaseResultSummary;
import io.jenkins.plugins.junit.storage.JunitTestResultStorage;
import io.jenkins.plugins.junit.storage.JunitTestResultStorageConfiguration;
import io.jenkins.plugins.junit.storage.JunitTestResultStorageDescriptor;
import io.jenkins.plugins.junit.storage.TestResultImpl;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;
import org.jvnet.hudson.test.TestExtension;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class StorageResultLoaderTest {

    // duplicate case names within a class are ordered by identity hash in junit, so give them identical data
    private static final String[] REPORTS = {
        "<testsuite name='s'>"
                + "<testcase classname='org.example.ATest' name='passes' time='0.1'/>"
                + "<testcase classname='org.example.ATest' name='fails' time='0.2'><failure message='x'/></testcase>"
                + "<testcase classname='org.example.ATest' name='dup' time='0.3'/>"
                + "<testcase classname='org.example.ATest' name='dup' time='0.3'/>"
                + "<testcase classname='org.example.BTest' name='skipped'><skipped/></testcase>"
                + "<testcase classname='RootTest' name='with space(1)' time='1.5'/>"
                + "</testsuite>",
        "<testsuite name='s'>"
                + "<testcase classname='org.example.ATest' name='passes' time='0.1'/>"
                + "<testcase classname='org.example.ATest' name='fails' time='0.2'/>"
                + "<testcase classname='org.example.BTest' name='skipped'><error message='y'/></testcase>"
                + "<testcase classname='org.other.CTest' name='onlyHere' time='0.7'/>"
                + "</testsuite>",
        "<testsuite name='s'>"
                + "<testcase classname='org.example.BTest' name='skipped'><skipped/></testcase>"
                + "<testcase classname='org.other.CTest' name='onlyHere' time='0.05'><failure message='z'/></testcase>"
                + "</testsuite>",
    };

    /**
     * An NUnit parameterized fixture converted to JUnit XML by the nunit plugin (#224), whose fixtures' cases share
     * a class and name, and a test retried until it passed (#233).
     */
    private static final String[] DUPLICATE_REPORTS = {
        "<testsuites>" + nunitFixture("a", true) + nunitFixture("b", false) + nunitFixture("c", false)
                + "<testsuite name='run1'>"
                + "<testcase classname='pkg.A' name='flaky' time='1'><failure message='x'/></testcase>"
                + "</testsuite>"
                + "<testsuite name='run2'><testcase classname='pkg.A' name='flaky' time='2'/></testsuite>"
                + "</testsuites>",
    };

    private static String nunitFixture(String param, boolean failSecond) {
        return "<testsuite tests='3' name='ClassLibrary1.dll.ClassLibrary1.TestClass.TestClass(&quot;" + param
                + "&quot;).TestMethod.'>"
                + "<testcase name='TestMethod(1)' time='0.25' classname='ClassLibrary1.TestClass'/>"
                + "<testcase name='TestMethod(2)' time='0.25' classname='ClassLibrary1.TestClass'>"
                + (failSecond ? "<failure message='boom'>boom</failure>" : "")
                + "</testcase>"
                + "<testcase name='TestMethod(3)' time='0.25' classname='ClassLibrary1.TestClass'/>"
                + "</testsuite>";
    }

    @BeforeEach
    void resetStorage() {
        StubStorage.SUMMARIES.clear();
        StubStorage.streamed = 0;
    }

    @AfterEach
    void resetPolicy() {
        TestResultsAnalyzerExtension.DESCRIPTOR.setDuplicateTestPolicy(DuplicateTestPolicy.DEFAULT);
    }

    @Test
    void storagePathMatchesPerBuildPath(JenkinsRule r) throws Exception {
        FreeStyleProject project = r.createFreeStyleProject("p");
        project.getBuildersList().add(new ReportWriter(REPORTS));
        project.getPublishersList().add(new JUnitResultArchiver("report.xml"));
        List<FreeStyleBuild> runs = new ArrayList<>();
        for (int i = 0; i < REPORTS.length; i++) {
            runs.add(r.buildAndAssertStatus(hudson.model.Result.UNSTABLE, project));
        }

        String allFromFiles = tree(project, "-1");
        String twoFromFiles = tree(project, "2");
        String chosenFromFiles = tree(project, "1,3");
        assertTrue(chosenFromFiles.contains("onlyHere") && !chosenFromFiles.contains("\"buildNumber\": \"2\""));

        switchToStubStorage(runs);

        assertEquals(allFromFiles, tree(project, "-1"));
        assertEquals(twoFromFiles, tree(project, "2"));
        assertEquals(chosenFromFiles, tree(project, "1,3"));
        assertTrue(StubStorage.streamed > 0, "storage fast path should have been used");
    }

    @Test
    void duplicateExecutionsAreMergedAlikeInBothPaths(JenkinsRule r) throws Exception {
        FreeStyleProject project = r.createFreeStyleProject("p");
        project.getBuildersList().add(new ReportWriter(DUPLICATE_REPORTS));
        project.getPublishersList().add(new JUnitResultArchiver("report.xml"));
        FreeStyleBuild run = r.buildAndAssertStatus(hudson.model.Result.UNSTABLE, project);
        // junit itself keeps every execution
        assertEquals(11, run.getAction(TestResultAction.class).getTotalCount());
        assertEquals(2, run.getAction(TestResultAction.class).getFailCount());

        Map<DuplicateTestPolicy, String> fromFiles = new EnumMap<>(DuplicateTestPolicy.class);
        for (DuplicateTestPolicy policy : DuplicateTestPolicy.values()) {
            TestResultsAnalyzerExtension.DESCRIPTOR.setDuplicateTestPolicy(policy);
            String tree = tree(project, "-1");
            assertDuplicatesMerged(policy, JSONObject.fromObject(tree));
            fromFiles.put(policy, tree);
        }

        switchToStubStorage(List.of(run));

        for (DuplicateTestPolicy policy : DuplicateTestPolicy.values()) {
            TestResultsAnalyzerExtension.DESCRIPTOR.setDuplicateTestPolicy(policy);
            String tree = tree(project, "-1");
            assertDuplicatesMerged(policy, JSONObject.fromObject(tree));
            // which of several executions with the same status a test links to depends on junit's ordering
            assertEquals(withoutTestUrls(fromFiles.get(policy)), withoutTestUrls(tree));
        }
        assertTrue(StubStorage.streamed > 0, "storage fast path should have been used");
    }

    private static void assertDuplicatesMerged(DuplicateTestPolicy policy, JSONObject tree) {
        boolean failedIfAny = policy == DuplicateTestPolicy.FAILED_IF_ANY_FAILED;
        String mergedStatus = failedIfAny ? "FAILED" : "PASSED";
        int failed = failedIfAny ? 1 : 0;

        JSONObject nunitPackage = child(tree.getJSONArray("results"), "ClassLibrary1");
        JSONObject nunitClass = child(nunitPackage.getJSONArray("children"), "TestClass");
        JSONArray nunitTests = nunitClass.getJSONArray("children");
        assertEquals(3, nunitTests.size());
        assertCell(child(nunitTests, "TestMethod(1)"), "PASSED", 1, 0, 0.75);
        assertCell(child(nunitTests, "TestMethod(2)"), mergedStatus, 1, failed, 0.75);
        assertCell(child(nunitTests, "TestMethod(3)"), "PASSED", 1, 0, 0.75);
        assertCell(nunitClass, mergedStatus, 3, failed, 2.25);
        assertCell(nunitPackage, mergedStatus, 3, failed, 2.25);

        JSONObject retryPackage = child(tree.getJSONArray("results"), "pkg");
        JSONObject retryClass = child(retryPackage.getJSONArray("children"), "A");
        assertCell(child(retryClass.getJSONArray("children"), "flaky"), mergedStatus, 1, failed, 3);
        assertCell(retryClass, mergedStatus, 1, failed, 3);
        assertCell(retryPackage, mergedStatus, 1, failed, 3);
    }

    private static JSONObject child(JSONArray nodes, String text) {
        for (int i = 0; i < nodes.size(); i++) {
            if (text.equals(nodes.getJSONObject(i).getString("text"))) {
                return nodes.getJSONObject(i);
            }
        }
        throw new AssertionError("no " + text + " in " + nodes);
    }

    private static void assertCell(JSONObject node, String status, int total, int failed, double duration) {
        JSONObject cell = node.getJSONArray("buildResults").getJSONObject(0);
        String name = node.getString("text");
        assertEquals(status, cell.getString("status"), name);
        assertEquals(total, cell.getInt("totalTests"), name);
        assertEquals(failed, cell.getInt("totalFailed"), name);
        assertEquals(total - failed - cell.getInt("totalSkipped"), cell.getInt("totalPassed"), name);
        assertEquals(duration, cell.getDouble("totalTimeTaken"), 0.001, name);
    }

    private static String withoutTestUrls(String tree) {
        JSONObject json = JSONObject.fromObject(tree);
        removeTestUrls(json.getJSONArray("results"));
        return json.toString(2);
    }

    private static void removeTestUrls(JSONArray nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            JSONObject node = nodes.getJSONObject(i);
            JSONArray children = node.getJSONArray("children");
            if (children.isEmpty()) {
                JSONArray cells = node.getJSONArray("buildResults");
                for (int j = 0; j < cells.size(); j++) {
                    cells.getJSONObject(j).remove("url");
                }
            }
            removeTestUrls(children);
        }
    }

    /** Captures what an external storage would hold for the given builds, then switches to it. */
    private static void switchToStubStorage(List<? extends Run<?, ?>> runs) {
        for (Run<?, ?> run : runs) {
            for (SuiteResult suite :
                    run.getAction(TestResultAction.class).getResult().getSuites()) {
                for (CaseResult c : suite.getCases()) {
                    StubStorage.SUMMARIES
                            .computeIfAbsent(run.getNumber(), b -> new ArrayList<>())
                            .add(new CaseResultSummary(
                                    run.getNumber(),
                                    suite.getName(),
                                    c.getClassName(),
                                    c.getName(),
                                    c.isFailed(),
                                    c.isSkipped(),
                                    c.getDuration()));
                }
            }
        }
        JunitTestResultStorageConfiguration.get().setStorage(new StubStorage());
    }

    public static class ReportWriter extends TestBuilder {
        private final String[] reports;

        ReportWriter(String[] reports) {
            this.reports = reports;
        }

        @Override
        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                throws IOException, InterruptedException {
            String xml = reports[(build.getNumber() - 1) % reports.length];
            build.getWorkspace().child("report.xml").write(xml, StandardCharsets.UTF_8.name());
            return true;
        }
    }

    @Test
    void sparseBuildsAreReadInRanges() {
        assertEquals("[]", describe(StorageResultLoader.ranges(List.of())));
        assertEquals("[1-5]", describe(StorageResultLoader.ranges(List.of(5, 3, 1, 4, 2))));
        // deleted or running builds in between are read through
        int near = 5 + StorageResultLoader.MAX_GAP + 1;
        assertEquals("[1-" + near + "]", describe(StorageResultLoader.ranges(List.of(1, 5, near))));
        assertEquals(
                "[12-12, 36-53, 80-80]", describe(StorageResultLoader.ranges(List.of(53, 40, 41, 36, 12, 45, 80))));
    }

    private static String describe(List<int[]> ranges) {
        return ranges.stream().map(r -> r[0] + "-" + r[1]).toList().toString();
    }

    /** The tree for the latest builds, or for the chosen builds when given a list such as {@code 1,3}. */
    private static String tree(FreeStyleProject project, String builds) throws IOException {
        StringWriter out = new StringWriter();
        boolean chosen = builds.contains(",");
        new TestResultsAnalyzerAction(project)
                .writeTreeResult(out, chosen ? null : builds, chosen ? builds : null, false);
        return JSONObject.fromObject(out.toString()).toString(2);
    }

    public static class StubStorage extends JunitTestResultStorage {
        static final TreeMap<Integer, List<CaseResultSummary>> SUMMARIES = new TreeMap<>();
        static int streamed;

        @Override
        public RemotePublisher createRemotePublisher(Run<?, ?> build) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TestResultImpl load(String job, int build) {
            return (TestResultImpl) Proxy.newProxyInstance(
                    TestResultImpl.class.getClassLoader(), new Class<?>[] {TestResultImpl.class}, (proxy, m, args) -> {
                        switch (m.getName()) {
                            case "supportsCaseResultSummaries":
                                return true;
                            case "forEachCaseResultSummary":
                                int from = (int) args[0];
                                int to = (int) args[1];
                                @SuppressWarnings("unchecked")
                                Consumer<CaseResultSummary> consumer = (Consumer<CaseResultSummary>) args[2];
                                SUMMARIES
                                        .subMap(from, true, to, true)
                                        .values()
                                        .forEach(list -> list.forEach(s -> {
                                            streamed++;
                                            consumer.accept(s);
                                        }));
                                return null;
                            default:
                                throw new UnsupportedOperationException(m.getName());
                        }
                    });
        }

        @TestExtension
        public static class DescriptorImpl extends JunitTestResultStorageDescriptor {}
    }
}

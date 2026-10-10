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
import java.util.List;
import java.util.TreeMap;
import java.util.function.Consumer;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.testresultsanalyzer.config.UserConfig;
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

    @Test
    void storagePathMatchesPerBuildPath(JenkinsRule r) throws Exception {
        FreeStyleProject project = r.createFreeStyleProject("p");
        project.getBuildersList().add(new ReportWriter());
        project.getPublishersList().add(new JUnitResultArchiver("report.xml"));
        List<FreeStyleBuild> runs = new ArrayList<>();
        for (int i = 0; i < REPORTS.length; i++) {
            runs.add(r.buildAndAssertStatus(hudson.model.Result.UNSTABLE, project));
        }

        String allFromFiles = tree(project, "-1");
        String twoFromFiles = tree(project, "2");

        // capture what an external storage would hold, then switch to it
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

        assertEquals(allFromFiles, tree(project, "-1"));
        assertEquals(twoFromFiles, tree(project, "2"));
        assertTrue(StubStorage.streamed > 0, "storage fast path should have been used");
    }

    public static class ReportWriter extends TestBuilder {
        @Override
        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                throws IOException, InterruptedException {
            String xml = REPORTS[(build.getNumber() - 1) % REPORTS.length];
            build.getWorkspace().child("report.xml").write(xml, StandardCharsets.UTF_8.name());
            return true;
        }
    }

    private static String tree(FreeStyleProject project, String noOfBuilds) throws IOException {
        StringWriter out = new StringWriter();
        new TestResultsAnalyzerAction(project).writeTreeResult(out, new UserConfig(noOfBuilds, false));
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

package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.tasks.test.TabulatedResult;
import hudson.tasks.test.TestResult;
import java.util.Map;
import java.util.TreeMap;
import org.jenkinsci.plugins.testresultsanalyzer.DuplicateTestPolicy;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.ClassResultData;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.ResultData;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.TestCaseResultData;

public class ClassInfo extends Info {

    private Map<String, TestCaseInfo> tests = new TreeMap<String, TestCaseInfo>();

    public void putBuildClassResult(Integer buildNumber, TabulatedResult classResult, String url) {
        putBuildClassResult(buildNumber, classResult, url, DuplicateTestPolicy.DEFAULT, new ResultData[0]);
    }

    /**
     * Adds a class and its tests of a build, merging them with what was already added for that build.
     *
     * @param parents the results of the enclosing package in the build, to correct when tests are merged
     */
    public void putBuildClassResult(
            Integer buildNumber,
            TabulatedResult classResult,
            String url,
            DuplicateTestPolicy policy,
            ResultData... parents) {
        ClassResultData classResultData = new ClassResultData(classResult, url);
        ResultData previous = this.buildResults.get(buildNumber);
        if (previous != null) {
            classResultData.add(previous);
        }

        ResultData[] all = new ResultData[parents.length + 1];
        all[0] = classResultData;
        System.arraycopy(parents, 0, all, 1, parents.length);
        addTests(buildNumber, classResult, url, policy, all);
        this.buildResults.put(buildNumber, classResultData);
    }

    public TestCaseInfo getOrCreateTest(String testName) {
        return tests.computeIfAbsent(testName, name -> {
            TestCaseInfo testCaseInfo = new TestCaseInfo();
            testCaseInfo.setName(name);
            return testCaseInfo;
        });
    }

    public Map<String, TestCaseInfo> getTests() {
        return tests;
    }

    private void addTests(
            Integer buildNumber,
            TabulatedResult classResult,
            String url,
            DuplicateTestPolicy policy,
            ResultData[] parents) {
        for (TestResult testCaseResult : classResult.getChildren()) {
            getOrCreateTest(testCaseResult.getDisplayName())
                    .putTestCaseResult(
                            buildNumber,
                            new CaseData(testCaseResult, url, testCaseResult.getSafeName()),
                            policy,
                            parents);
        }
    }

    @Override
    public Map<String, TestCaseInfo> getChildren() {
        return tests;
    }

    /** A case result whose URL is derived from its class when needed rather than stored per cell. */
    private static final class CaseData extends TestCaseResultData {
        private final String classUrl;
        private final String safeName;

        CaseData(TestResult testCaseResult, String classUrl, String safeName) {
            super(testCaseResult, null);
            this.classUrl = classUrl;
            this.safeName = safeName;
        }

        @Override
        public String getUrl() {
            return classUrl + "/" + safeName;
        }
    }
}

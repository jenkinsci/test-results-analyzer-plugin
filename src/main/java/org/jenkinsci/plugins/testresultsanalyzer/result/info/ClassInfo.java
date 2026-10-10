package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.tasks.test.TabulatedResult;
import hudson.tasks.test.TestResult;
import java.util.Map;
import java.util.TreeMap;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.ClassResultData;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.TestCaseResultData;

public class ClassInfo extends Info {

    private Map<String, TestCaseInfo> tests = new TreeMap<String, TestCaseInfo>();

    public void putBuildClassResult(Integer buildNumber, TabulatedResult classResult, String url) {
        ClassResultData classResultData = new ClassResultData(classResult, url);

        addTests(buildNumber, classResult, url);
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

    private void addTests(Integer buildNumber, TabulatedResult classResult, String url) {
        for (TestResult testCaseResult : classResult.getChildren()) {

            String testCaseName = testCaseResult.getDisplayName();
            TestCaseInfo testCaseInfo;
            if (tests.containsKey(testCaseName)) {
                testCaseInfo = tests.get(testCaseName);
            } else {
                testCaseInfo = new TestCaseInfo();
                testCaseInfo.setName(testCaseName);
            }

            testCaseInfo.putTestCaseResult(
                    buildNumber, new CaseData(testCaseResult, url, testCaseResult.getSafeName()));
            tests.put(testCaseName, testCaseInfo);
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

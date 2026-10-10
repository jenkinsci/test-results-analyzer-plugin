package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.tasks.test.TestResult;
import java.util.Map;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.TestCaseResultData;

public class TestCaseInfo extends Info {

    public void putTestCaseResult(Integer buildNumber, TestResult testCaseResult, String url) {
        putTestCaseResult(buildNumber, new TestCaseResultData(testCaseResult, url));
    }

    public void putTestCaseResult(Integer buildNumber, TestCaseResultData testCaseResultData) {
        setConfig(testCaseResultData.isConfig());
        this.buildResults.put(buildNumber, testCaseResultData);
    }

    @Override
    public Map<String, ? extends Info> getChildren() {
        return null;
    }
}

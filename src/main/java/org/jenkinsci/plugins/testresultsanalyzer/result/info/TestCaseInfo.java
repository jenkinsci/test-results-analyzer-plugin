package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.tasks.test.TestResult;
import java.util.Map;
import org.jenkinsci.plugins.testresultsanalyzer.DuplicateTestPolicy;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.ResultData;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.TestCaseResultData;

public class TestCaseInfo extends Info {

    public void putTestCaseResult(Integer buildNumber, TestResult testCaseResult, String url) {
        putTestCaseResult(buildNumber, new TestCaseResultData(testCaseResult, url));
    }

    public void putTestCaseResult(Integer buildNumber, TestCaseResultData testCaseResultData) {
        putTestCaseResult(buildNumber, testCaseResultData, DuplicateTestPolicy.DEFAULT, new ResultData[0]);
    }

    /**
     * Records an execution of this test in a build. A test can be executed more than once in a build, for instance
     * when retried; then the executions are merged into one result according to {@code policy}.
     *
     * @param parents the results of the enclosing class (and package) in the build, which count every execution and
     *     are corrected to count the merged result once instead
     * @return the result this execution was merged with, or {@code null} if it is the first in the build
     */
    public ResultData putTestCaseResult(
            Integer buildNumber, ResultData result, DuplicateTestPolicy policy, ResultData... parents) {
        setConfig(result.isConfig());
        ResultData previous = buildResults.get(buildNumber);
        if (previous == null) {
            buildResults.put(buildNumber, result);
            return null;
        }
        ResultData merged = TestCaseResultData.merge(previous, result, policy);
        buildResults.put(buildNumber, merged);
        if (!merged.isConfig()) { // TestNG leaves configuration methods out of class counts
            for (ResultData parent : parents) {
                parent.mergedChild(previous, result, merged);
            }
        }
        return previous;
    }

    @Override
    public Map<String, ? extends Info> getChildren() {
        return null;
    }
}

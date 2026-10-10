package org.jenkinsci.plugins.testresultsanalyzer.result.data;

import hudson.tasks.test.TestResult;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import org.jenkinsci.plugins.testresultsanalyzer.DuplicateTestPolicy;
import org.jenkinsci.plugins.testresultsanalyzer.ResultStatus;

public class TestCaseResultData extends ResultData {

    public TestCaseResultData(String name, boolean failed, boolean skipped, float duration, String url) {
        super(name, 1, failed && !skipped ? 1 : 0, failed || skipped ? 0 : 1, skipped ? 1 : 0, duration, url);
    }

    public TestCaseResultData(TestResult testResult, String url) {
        setName(testResult.getName());
        boolean doTestNg = testResult.getClass().getName().equals("hudson.plugins.testng.results.MethodResult");
        if (doTestNg) {
            try {
                Method statusMethod = testResult.getClass().getMethod("getStatus");
                Object statusReturnValue = statusMethod.invoke(testResult);
                if (statusReturnValue instanceof String) {
                    String status = ((String) statusReturnValue).toLowerCase(Locale.ENGLISH);

                    setTotalTests(1);
                    setTotalFailed(status.startsWith("fail") ? 1 : 0);
                    setTotalPassed(status.startsWith("pass") ? 1 : 0);
                    setTotalSkipped(status.startsWith("skip") ? 1 : 0);
                }
                Method configMethod = testResult.getClass().getMethod("isConfig");
                Object configReturnValue = configMethod.invoke(testResult);
                if (configReturnValue instanceof Boolean) {
                    boolean isConfig = ((Boolean) configReturnValue);
                    setConfig(isConfig);
                }
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) { // NOSONAR
                // fallback to non testng code
                doTestNg = false;
            }
        }
        if (!doTestNg) {
            setTotalTests(testResult.getTotalCount());
            setTotalFailed(testResult.getFailCount());
            setTotalPassed(testResult.getPassCount());
            setTotalSkipped(testResult.getSkipCount());
        }
        setTotalTimeTaken(testResult.getDuration());
        setUrl(url);
        evaluateStatus();
    }

    /**
     * Merges two executions of the same test in one build into a single result, whose status (and URL) is that of
     * the execution chosen by {@code policy} and whose duration is their sum.
     */
    public static TestCaseResultData merge(ResultData first, ResultData second, DuplicateTestPolicy policy) {
        ResultData chosen = policy.prefers(second.getStatus(), first.getStatus()) ? second : first;
        String status = chosen.getStatus();
        TestCaseResultData merged = new TestCaseResultData(
                first.getName(),
                ResultStatus.FAILED.getValue().equals(status),
                ResultStatus.SKIPPED.getValue().equals(status),
                first.getTotalTimeTaken() + second.getTotalTimeTaken(),
                chosen.getUrl());
        merged.setConfig(first.isConfig());
        return merged;
    }
}

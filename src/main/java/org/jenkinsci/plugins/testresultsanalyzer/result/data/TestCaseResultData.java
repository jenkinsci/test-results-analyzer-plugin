package org.jenkinsci.plugins.testresultsanalyzer.result.data;

import hudson.tasks.test.TestResult;

import java.lang.reflect.Method;

public class TestCaseResultData extends ResultData {

	public TestCaseResultData(TestResult testResult) {
		setName(testResult.getName());
		boolean doTestNg = testResult.getClass().getName().equals("hudson.plugins.testng.results.MethodResult");
		if (doTestNg) {
			try {
				Method method = testResult.getClass().getMethod("getStatus");
				Object returnValue = method.invoke(testResult);
				if (returnValue instanceof String) {
					String status = ((String) returnValue).toLowerCase();

					setPassed(status.startsWith("pass"));
					setSkipped(status.startsWith("skip"));
					setTotalTests(1);
					setTotalFailed(status.startsWith("fail") ? 1 : 0);
					setTotalPassed(status.startsWith("pass") ? 1 : 0);
					setTotalSkipped(status.startsWith("skip") ? 1 : 0);
				}
			}
			catch (Exception e) {
				// fallback to non testng code
				doTestNg = false;
			}
		}
		if (!doTestNg) {
			setPassed(testResult.isPassed());
			setSkipped(testResult.getSkipCount() == testResult.getTotalCount());
			setTotalTests(testResult.getTotalCount());
			setTotalFailed(testResult.getFailCount());
			setTotalPassed(testResult.getPassCount());
			setTotalSkipped(testResult.getSkipCount());
		}
		setTotalTimeTaken(testResult.getDuration());
		evaluateStatus();
		if ("FAILED".equalsIgnoreCase(getStatus())) {
			setFailureMessage(testResult.getErrorStackTrace());
		}
	}

}

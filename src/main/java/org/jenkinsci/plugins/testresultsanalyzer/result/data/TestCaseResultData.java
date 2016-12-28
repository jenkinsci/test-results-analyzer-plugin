package org.jenkinsci.plugins.testresultsanalyzer.result.data;

import hudson.tasks.test.TestResult;

import java.lang.reflect.Method;

public class TestCaseResultData extends ResultData {

	public TestCaseResultData(TestResult testResult, String url) {
		setName(testResult.getName());
		boolean doTestNg = testResult.getClass().getName().equals("hudson.plugins.testng.results.MethodResult");
		if (doTestNg) {
			try {
				Method statusMethod = testResult.getClass().getMethod("getStatus");
				Object statusReturnValue = statusMethod.invoke(testResult);
				if (statusReturnValue instanceof String) {
					String status = ((String) statusReturnValue).toLowerCase();

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
			}
			catch (Exception e) {
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

}

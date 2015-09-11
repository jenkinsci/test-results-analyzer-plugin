package org.jenkinsci.plugins.testresultsanalyzer.result.data;

import hudson.tasks.test.TestResult;

public class PackageResultData extends ResultData {

	public PackageResultData(TestResult packageResult) {
		setName(packageResult.getName());
		setPassed(packageResult.getFailCount() == 0);
		setSkipped(packageResult.getSkipCount() == packageResult.getTotalCount());
		setTotalTests(packageResult.getTotalCount());
		setTotalFailed(packageResult.getFailCount());
		setTotalPassed(packageResult.getPassCount());
		setTotalSkipped(packageResult.getSkipCount());
		setTotalTimeTaken(packageResult.getDuration());
		evaluateStatus();
	}

}

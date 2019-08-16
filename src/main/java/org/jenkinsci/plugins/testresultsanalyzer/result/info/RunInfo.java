package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.tasks.test.TestResult;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.TestCaseResultData;

import java.util.Map;

public class RunInfo extends Info {

	public void putRunResult(Integer buildNumber, TestResult testCaseResult, String url) {
		TestCaseResultData testCaseResultData = new TestCaseResultData(testCaseResult, url);
		setConfig(testCaseResultData.isConfig());
		this.buildResults.put(buildNumber, testCaseResultData);
	}

	@Override
	public Map<String, ? extends Info> getChildren() {
		return null;
	}
}

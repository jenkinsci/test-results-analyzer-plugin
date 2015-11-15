package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.tasks.test.TestResult;
import net.sf.json.JSONObject;

import org.jenkinsci.plugins.testresultsanalyzer.result.data.TestCaseResultData;

public class TestCaseInfo extends Info {

	public void putTestCaseResult(Integer buildNumber, TestResult testCaseResult) {
		TestCaseResultData testCaseResultData = new TestCaseResultData(testCaseResult);
		evaluateStatusses(testCaseResult);
		this.buildResults.put(buildNumber, testCaseResultData);
	}

	@Override
	protected JSONObject getChildrensJson() {

		return new JSONObject();
	}

}

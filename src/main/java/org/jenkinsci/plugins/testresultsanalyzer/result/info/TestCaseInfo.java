package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.tasks.junit.CaseResult;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.TestCaseResultData;
import net.sf.json.JSONObject;

public class TestCaseInfo extends Info {

	public void putTestCaseResult(Integer buildNumber, CaseResult testCaseResult, String url) {
		TestCaseResultData testCaseResultData = new TestCaseResultData(testCaseResult, url);
		evaluateStatusses(testCaseResult);
		this.buildResults.put(buildNumber, testCaseResultData);
	}

	@Override
	protected JSONObject getChildrensJson() {

		return new JSONObject();
	}
}

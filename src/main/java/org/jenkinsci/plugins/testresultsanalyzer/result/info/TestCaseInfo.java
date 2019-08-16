package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.model.Run;
import hudson.tasks.test.TestResult;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.TestCaseResultData;

import java.util.Map;
import java.util.TreeMap;

public class TestCaseInfo extends Info {

	private Map<String, RunInfo> runs = new TreeMap<String, RunInfo>();

	public void putTestCaseResult(Integer buildNumber, String runName, TestResult testCaseResult, String url) {
		TestCaseResultData testCaseResultData;
		if (this.buildResults.containsKey(buildNumber)) {
			testCaseResultData = (TestCaseResultData) this.buildResults.get(buildNumber);
			testCaseResultData.update(testCaseResult);
		}
		else {
			testCaseResultData = new TestCaseResultData(testCaseResult, url);
			setConfig(testCaseResultData.isConfig());
		}
		addRun(buildNumber, runName, testCaseResult, url);
		this.buildResults.put(buildNumber, testCaseResultData);
	}

	private void addRun(Integer buildNumber, String runName, TestResult testCaseResult, String url) {
		RunInfo runInfo;
		if (runs.containsKey(runName)) {
			runInfo = runs.get(runName);
		}
		else {
			runInfo = new RunInfo();
			runInfo.setName(runName);
		}

		runInfo.putRunResult(buildNumber, testCaseResult, url);
		runs.put(runName, runInfo);
	}

	@Override
	public Map<String, RunInfo> getChildren() {
		return runs;
	}
}

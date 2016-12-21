package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.tasks.test.TabulatedResult;
import hudson.tasks.test.TestResult;

import java.util.Map;
import java.util.TreeMap;

import net.sf.json.JSONObject;

import org.jenkinsci.plugins.testresultsanalyzer.config.UserConfig;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.ClassResultData;

public class ClassInfo extends Info {

	private Map<String, TestCaseInfo> tests = new TreeMap<String, TestCaseInfo>();

	public void putBuildClassResult(Integer buildNumber, TabulatedResult classResult, String url) {
		ClassResultData classResultData = new ClassResultData(classResult, url);

		addTests(buildNumber, classResult, url);
		this.buildResults.put(buildNumber, classResultData);
	}

	public Map<String, TestCaseInfo> getTests() {
		return tests;
	}
	

	private void addTests(Integer buildNumber, TabulatedResult classResult, String url) {
		for (TestResult testCaseResult : classResult.getChildren()) {

			String testCaseName = testCaseResult.getName();
			TestCaseInfo testCaseInfo;
			if (tests.containsKey(testCaseName)) {
				testCaseInfo = tests.get(testCaseName);
			}
			else {
				testCaseInfo = new TestCaseInfo();
				testCaseInfo.setName(testCaseName);
			}

			testCaseInfo.putTestCaseResult(buildNumber, testCaseResult, url + "/" + testCaseResult.getSafeName());
			tests.put(testCaseName, testCaseInfo);
		}
	}

	@Override
	protected JSONObject getChildrensJson(UserConfig userConfig) {
		JSONObject json = new JSONObject();
		for (String testName : tests.keySet()) {
			TestCaseInfo testCaseInfo = tests.get(testName);
			if(userConfig.isHideConfigMethods() && testCaseInfo.isConfig) {
				continue;
			}
			json.put(testName, tests.get(testName).getJsonObject(userConfig));
		}
		return json;
	}
}

package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.tasks.test.TabulatedResult;
import hudson.tasks.test.TestResult;

import java.util.Map;
import java.util.TreeMap;

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
	public Map<String, TestCaseInfo> getChildren() {
		return tests;
	}
}

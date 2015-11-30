package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.ClassResult;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.ClassResultData;

import java.util.Map;
import java.util.TreeMap;

public class ClassInfo extends Info {

	private Map<String, TestCaseInfo> tests = new TreeMap<String, TestCaseInfo>();

	public void putBuildClassResult(Integer buildNumber, ClassResult classResult, String url) {
		ClassResultData classResultData = new ClassResultData(classResult, url);
		
		evaluateStatusses(classResult);
		addTests(buildNumber, classResult, url);
		this.buildResults.put(buildNumber, classResultData);		
	}

	public Map<String, TestCaseInfo> getTests() {
		return tests;
	}

	public void setTests(Map<String,TestCaseInfo> tests) {
		this.tests = tests;
	}

	private void addTests(Integer buildNumber, ClassResult classResult, String url) {
		for(CaseResult testCaseResult : classResult.getChildren()) {
			String testCaseName = testCaseResult.getName();
			TestCaseInfo testCaseInfo;
			if(tests.containsKey(testCaseName)) {
				testCaseInfo = tests.get(testCaseName);
			} else {
				testCaseInfo = new TestCaseInfo();
				testCaseInfo.setName(testCaseName);
			}

			testCaseInfo.putTestCaseResult(buildNumber, testCaseResult, url + "/" + testCaseResult.getName());
			tests.put(testCaseName, testCaseInfo);
		}
	}

	protected JSONObject getChildrensJson() {
		JSONObject json = new JSONObject();
		for(String testName : tests.keySet()) {
			json.put(testName, tests.get(testName).getJsonObject());
		}
		return json;
	}
}

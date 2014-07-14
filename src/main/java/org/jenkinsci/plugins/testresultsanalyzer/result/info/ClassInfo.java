package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.ClassResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jenkinsci.plugins.testresultsanalyzer.result.data.ClassResultData;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.TestCaseResultData;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ClassInfo {
	
	private String name;
	private Map<Integer,ClassResultData> buildClassResults = new HashMap<Integer, ClassResultData>();
	private Map<String, TestCaseInfo> tests = new HashMap<String, TestCaseInfo>();
	private List<String> statuses = new ArrayList<String>(); 
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<Integer, ClassResultData> getBuildClassResults() {
		return buildClassResults;
	}
	public void setBuildClassResults(Map<Integer, ClassResultData> buildClassResults) {
		this.buildClassResults = buildClassResults;
	}
	
	public void putBuildClassResult(Integer buildNumber, ClassResultData classResultData){
		this.buildClassResults.put(buildNumber, classResultData);		
	}
	
	public void putBuildClassResult(Integer buildNumber, ClassResult classResult){
		ClassResultData classResultData = new ClassResultData(classResult);
		String status = classResultData.getStatus();
		if(!(statuses.contains(status)))
			statuses.add(status);
		addTests(buildNumber, classResult);
		this.buildClassResults.put(buildNumber, classResultData);		
	}
	
	private void addTests(Integer buildNumber, ClassResult classResult){
		for (CaseResult testCaseResult : classResult.getChildren()) {
			String testCaseName = testCaseResult.getName();
			TestCaseInfo testCaseInfo;
			if(tests.containsKey(testCaseName)){
				testCaseInfo = tests.get(testCaseName);
			} else {
				testCaseInfo = new TestCaseInfo();
				testCaseInfo.setName(testCaseName);
			}
			TestCaseResultData testCaseResultData = new TestCaseResultData(testCaseResult);
			testCaseInfo.putTestCaseResult(buildNumber, testCaseResultData);
			tests.put(testCaseName, testCaseInfo);
		}
	}
	
	public Map<String, TestCaseInfo> getTests() {
		return tests;
	}
	
	public void setTests(Map<String, TestCaseInfo> tests) {
		this.tests = tests;
	}
	
	public void putTest(String testName, TestCaseInfo testCaseInfo){
		this.tests.put(testName, testCaseInfo);
	}
	
	public void putTestResult(){
		
	}
	
	public JSONObject getJsonObject(){
		JSONObject json = new JSONObject();
		json.put("name", name);
		json.put("type", "class");
		json.put("buildStatuses", getBuildStatuses());
		json.put("builds", getBuildJson());
		json.put("children", getTestsJson());
		return json;
	}
	
	public JSONObject getBuildJson(){
		JSONObject json = new JSONObject();
		for(Integer buildNumber: buildClassResults.keySet()){
			json.put(buildNumber.toString(), buildClassResults.get(buildNumber).getJsonObject());
		}
		return json;
	}
	
	private JSONObject getTestsJson(){
		JSONObject json = new JSONObject();
		for(String testName : tests.keySet()){
			json.put(testName, tests.get(testName).getJsonObject());
		}
		return json;
	}
	
	public JSONArray getBuildStatuses(){
		JSONArray buildStatuses = new JSONArray();
		buildStatuses.addAll(statuses);
		return buildStatuses;
	}
	

}

package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jenkinsci.plugins.testresultsanalyzer.result.data.TestCaseResultData;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TestCaseInfo {
	
	private String name;
	private Map<Integer,TestCaseResultData> buildTestCaseResults = new HashMap<Integer, TestCaseResultData>();
	private List<String> statuses = new ArrayList<String>(); 
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<Integer, TestCaseResultData> getBuildTestCaseResults() {
		return buildTestCaseResults;
	}
	public void setBuildTestCaseResults(
			Map<Integer, TestCaseResultData> buildTestCaseResults) {
		this.buildTestCaseResults = buildTestCaseResults;
	}
	public void putTestCaseResult(Integer buildNumber, TestCaseResultData testCaseResutData){
		String status = testCaseResutData.getStatus();
		if(!(statuses.contains(status)))
			statuses.add(status);
		this.buildTestCaseResults.put(buildNumber, testCaseResutData);
	}	
	
	public JSONObject getJsonObject(){
		JSONObject json = new JSONObject();
		json.put("name", name);
		json.put("type", "method");
		json.put("buildStatuses", getBuildStatuses());
		json.put("builds", getBuildJson());
		return json;
	}
	
	public JSONObject getBuildJson(){
		JSONObject json = new JSONObject();
		for(Integer buildNumber: buildTestCaseResults.keySet()){
			json.put(buildNumber.toString(), buildTestCaseResults.get(buildNumber).getJsonObject());
		}
		return json;
	}
	
	public JSONArray getBuildStatuses(){
		JSONArray buildStatuses = new JSONArray();
		buildStatuses.addAll(statuses);
		return buildStatuses;
	}

}

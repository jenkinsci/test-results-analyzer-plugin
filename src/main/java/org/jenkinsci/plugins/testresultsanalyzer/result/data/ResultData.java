package org.jenkinsci.plugins.testresultsanalyzer.result.data;

import hudson.tasks.junit.PackageResult;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public abstract class ResultData {
	private String name;
	private boolean isPassed;
	private boolean isSkipped;
	private transient PackageResult packageResult;
	private int totalTests;
	private int totalFailed;
	private int totalPassed;
	private int totalSkipped;
	private List<ResultData> children = new ArrayList<ResultData>();
	private float totalTimeTaken;
	private String status;
	private String failureMessage = "";
	
	
	public String getFailureMessage() {
		return failureMessage;
	}
	public void setFailureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isPassed() {
		return isPassed;
	}
	public void setPassed(boolean isPassed) {
		this.isPassed = isPassed;
	}
	public boolean isSkipped() {
		return isSkipped;
	}
	public void setSkipped(boolean isSkipped) {
		this.isSkipped = isSkipped;
	}
	public PackageResult getPackageResult() {
		return packageResult;
	}
	public void setPackageResult(PackageResult packageResult) {
		this.packageResult = packageResult;
	}
	public int getTotalTests() {
		return totalTests;
	}
	public void setTotalTests(int totalTests) {
		this.totalTests = totalTests;
	}
	public int getTotalFailed() {
		return totalFailed;
	}
	public void setTotalFailed(int totalFailed) {
		this.totalFailed = totalFailed;
	}
	public int getTotalPassed() {
		return totalPassed;
	}
	public void setTotalPassed(int totalPassed) {
		this.totalPassed = totalPassed;
	}
	public int getTotalSkipped() {
		return totalSkipped;
	}
	public void setTotalSkipped(int totalSkipped) {
		this.totalSkipped = totalSkipped;
	}
	
	public List<ResultData> getChildren() {
		return this.children;
	}
	
	public void addChildResult(ResultData childResultData) {
		this.children.add(childResultData);
	}
	
	public void addChildResult(List<ResultData> childResults) {
		this.children.addAll(childResults);
	}
	
	public float getTotalTimeTaken() {
		return totalTimeTaken;
	}

	public void setTotalTimeTaken(float totalTimeTaken) {
		this.totalTimeTaken = totalTimeTaken;
	}
	
	protected void evaluateStatus(){
		if(isSkipped){
			status = "SKIPPED";
		} else if(isPassed){
			status = "PASSED";
		} else {
			status = "FAILED";
		}		
	}
	
	public String getStatus() {
		return status;
	}
	
	public JSONObject getJsonObject(){
		JSONObject json = new JSONObject();
		json.put("name", name);
		json.put("totalTests", totalTests);
		json.put("totalFailed", totalFailed);
		json.put("totalPassed", totalPassed);
		json.put("totalSkipped", totalSkipped);
		json.put("isPassed", isPassed);
		json.put("isSkipped", isSkipped);
		json.put("totalTimeTaken", totalTimeTaken);
		json.put("status", status);
		JSONArray testsChildren = new JSONArray();
		for(ResultData childResult : children){
			testsChildren.add(childResult.getJsonObject());
		}
		if(!(failureMessage.equalsIgnoreCase("")))
			json.put("failureMessage", failureMessage);
		json.put("children", testsChildren);
		return json;
	}

}

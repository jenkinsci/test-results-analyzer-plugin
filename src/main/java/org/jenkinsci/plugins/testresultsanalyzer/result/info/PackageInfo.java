package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.tasks.junit.PackageResult;
import hudson.tasks.junit.ClassResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jenkinsci.plugins.testresultsanalyzer.result.data.PackageResultData;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PackageInfo {
	
	private String name;
	private Map<Integer,PackageResultData> buildPackageResults = new HashMap<Integer, PackageResultData>();
	private Map<String, ClassInfo> classes = new HashMap<String, ClassInfo>();
	private List<String> statuses = new ArrayList<String>(); 
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<Integer, PackageResultData> getBuildPackageResults() {
		return buildPackageResults;
	}
	public void setBuildPackageResults(
			Map<Integer, PackageResultData> buildPackageResults) {
		this.buildPackageResults = buildPackageResults;
	}
	
	public void putPackageResult(Integer buildNumber, PackageResult packageResult){
		PackageResultData packageResultData = new PackageResultData(packageResult);
		String status = packageResultData.getStatus();
		if(!(statuses.contains(status)))
			statuses.add(status);
		addClasses(buildNumber, packageResult);
		this.buildPackageResults.put(buildNumber, packageResultData);
	}
	
	public PackageResultData getPackageResult(Integer buildNumber){
		if(this.buildPackageResults.containsKey(buildNumber)){
			return this.buildPackageResults.get(buildNumber);
		}
		return null;
		
	}
	
	public Map<String, ClassInfo> getClasses() {
		return classes;
	}
	
	public void setClasses(Map<String, ClassInfo> classes) {
		this.classes = classes;
	}
	
	public void putClass(String className, ClassInfo classInfo){
		this.classes.put(className, classInfo);
	}
	
	public void addClasses(Integer buildNumber, PackageResult packageResult){
		for (ClassResult classResult : packageResult.getChildren()) {
			String className = classResult.getName();
			ClassInfo classInfo;
			if (classes.containsKey(className)) {
				classInfo = classes.get(className);
			} else {
				classInfo = new ClassInfo();
				classInfo.setName(className);
			}
			classInfo.putBuildClassResult(buildNumber, classResult);
			classes.put(className, classInfo);
		}
	}
	
	public JSONObject getJsonObject(){
		JSONObject json = new JSONObject();
		json.put("name", name);
		json.put("type", "package");
		json.put("buildStatuses", getBuildStatuses());
		json.put("builds", getBuildJson());
		json.put("children", getClassJson());
		return json;
	}
	
	private JSONObject getBuildJson(){
		JSONObject json = new JSONObject();
		for(Integer buildNumber : buildPackageResults.keySet()){
			json.put(buildNumber.toString(), buildPackageResults.get(buildNumber).getJsonObject());
		}
		return json;
	}
	
	private JSONObject getClassJson(){
		JSONObject json = new JSONObject();
		for(String className : classes.keySet()){
			json.put(className, classes.get(className).getJsonObject());
		}
		return json;
	}
	
	public JSONArray getBuildStatuses(){
		JSONArray buildStatuses = new JSONArray();
		buildStatuses.addAll(statuses);
		return buildStatuses;
	}
	

}

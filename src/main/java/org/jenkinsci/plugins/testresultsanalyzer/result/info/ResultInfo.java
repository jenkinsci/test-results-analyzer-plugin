package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.tasks.junit.PackageResult;
import net.sf.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

public class ResultInfo {
	
	private Map<String,PackageInfo> packageResults = new TreeMap<String, PackageInfo>();
	
	public void addPackage(Integer buildNumber, PackageResult packageResult, String url) {
		String packageName = packageResult.getName();
		PackageInfo packageInfo;
		if(packageResults.containsKey(packageName)){
			packageInfo = packageResults.get(packageName);
		} else {
			packageInfo = new PackageInfo();
			packageInfo.setName(packageName);
		}
		packageInfo.putPackageResult(buildNumber, packageResult, url + "testReport/" + packageResult.getName());
		packageResults.put(packageName, packageInfo);		
	}
	
	public JSONObject getJsonObject(){
		JSONObject json = new JSONObject();
		for(String packageName: packageResults.keySet()){
			json.put(packageName, packageResults.get(packageName).getJsonObject());
		}
		return json;
	}
	
	public Map<String,PackageInfo> getPackageResults(){
		return this.packageResults;
	}
}

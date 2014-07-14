package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.tasks.junit.PackageResult;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

public class ResultInfo {
	
	private Map<String,PackageInfo> packageResults = new HashMap<String, PackageInfo>();
	
	public void addPackage(Integer buildNumber, PackageResult packageResult){
		String packageName = packageResult.getName();
		PackageInfo packageInfo;
		if(packageResults.containsKey(packageName)){
			packageInfo = packageResults.get(packageName);
		} else {
			packageInfo = new PackageInfo();
			packageInfo.setName(packageName);
		}
		packageInfo.putPackageResult(buildNumber, packageResult);
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

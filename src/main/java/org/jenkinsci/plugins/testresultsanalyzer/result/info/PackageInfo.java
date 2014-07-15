package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.tasks.junit.PackageResult;
import hudson.tasks.junit.ClassResult;

import java.util.HashMap;
import java.util.Map;

import org.jenkinsci.plugins.testresultsanalyzer.result.data.PackageResultData;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.ResultData;

import net.sf.json.JSONObject;

public class PackageInfo extends Info {	
	protected Map<String, ClassInfo> classes = new HashMap<String, ClassInfo>();
	
	public void putPackageResult(Integer buildNumber, PackageResult packageResult){
		PackageResultData packageResultData = new PackageResultData(packageResult);
		evaluateStatusses(packageResult);
		
		addClasses(buildNumber, packageResult);
		this.buildResults.put(buildNumber, packageResultData);
	}
	
	public ResultData getPackageResult(Integer buildNumber){
		if(this.buildResults.containsKey(buildNumber)){
			return this.buildResults.get(buildNumber);
		}
		return null;		
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

	protected JSONObject getChildrensJson(){
		JSONObject json = new JSONObject();
		for(String className : classes.keySet()){
			json.put(className, classes.get(className).getJsonObject());
		}
		return json;
	}

}

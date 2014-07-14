package org.jenkinsci.plugins.testresultsanalyzer;

import java.util.List;
import java.util.Set;

import org.jenkinsci.plugins.testresultsanalyzer.result.info.ResultInfo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JsTreeUtil {
	
	public JSONArray getJsTree(List<Integer> builds, ResultInfo resultInfo){
		JSONObject packageResults = resultInfo.getJsonObject();
		JSONArray treeJson = new JSONArray();
		for(Object packageName : packageResults.keySet()){
			
			JSONObject packageJson = packageResults.getJSONObject((String)packageName);
			treeJson.add(createJson(builds, packageJson));
			
		}	
		return treeJson;
	}
	
	private JSONObject getBaseJson(){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("text", "");
		jsonObject.put("data", new JSONObject());
		return jsonObject;
	}
	
	private JSONObject createJson(List<Integer> builds, JSONObject dataJson){
		JSONObject baseJson = getBaseJson();
		baseJson.put("text", dataJson.get("name"));
		baseJson.put("type", dataJson.get("type"));
		baseJson.put("buildStatuses", dataJson.get("buildStatuses"));
		JSONObject packageBuilds = dataJson.getJSONObject("builds");
		JSONObject treeDataJson = new JSONObject();
		for(Integer buildNumber: builds){
			if(packageBuilds.containsKey(buildNumber.toString())){
				JSONObject buildResult = packageBuilds.getJSONObject(buildNumber.toString());
				//String status = buildResult.getString("status");
				treeDataJson.put("build"+buildNumber.toString(), buildResult);
			} else {
				JSONObject buildStatus = new JSONObject();
				buildStatus.put("status", "N/A");
				treeDataJson.put("build"+buildNumber.toString(), buildStatus);
			}
		}
		baseJson.put("data", treeDataJson);
		
		if(dataJson.containsKey("children")){
			JSONArray childrens = new JSONArray();
			JSONObject childrenJson = dataJson.getJSONObject("children");
			@SuppressWarnings("unchecked")
			Set<String> childeSet = (Set<String>)childrenJson.keySet();
			for(String childName : childeSet){
				childrens.add(createJson(builds, childrenJson.getJSONObject(childName)));				
			}
			baseJson.put("children", childrens);
		}		
		
		return baseJson;
		
	}
	
	

}

package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.tasks.test.TestResult;

import java.lang.reflect.Method;
import java.util.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jenkinsci.plugins.testresultsanalyzer.config.UserConfig;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.ResultData;

public abstract class Info {

	protected String name;
	protected boolean isConfig = false;
	protected Map<Integer, ResultData> buildResults = new TreeMap<Integer, ResultData>(Collections.<Integer>reverseOrder());

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<Integer, ResultData> getBuildPackageResults() {
		return buildResults;
	}

	public void setBuildPackageResults(Map<Integer, ResultData> buildResults) {
		this.buildResults = buildResults;
	}

	protected abstract JSONObject getChildrensJson(UserConfig userConfig);

	protected JSONObject getBuildJson(UserConfig userConfig) {
		JSONObject json = new JSONObject();
		for (Integer buildNumber : buildResults.keySet()) {
			ResultData resultData = buildResults.get(buildNumber);
			if(userConfig.isHideConfigMethods() && resultData.isConfig())
				continue;
			json.put(buildNumber.toString(), buildResults.get(buildNumber).getJsonObject());
		}
		return json;
	}

	public boolean isConfig() {
		return isConfig;
	}

	public void setConfig(boolean config) {
		isConfig = config;
	}

	public JSONObject getJsonObject(UserConfig userConfig) {
		JSONObject json = new JSONObject();
		json.put("name", name);
		json.put("builds", getBuildJson(userConfig));
		json.put("children", getChildrensJson(userConfig));
		return json;
	}
}

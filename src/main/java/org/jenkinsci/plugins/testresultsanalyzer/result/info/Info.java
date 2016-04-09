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

	protected List<String> statuses = new ArrayList<String>();

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

	public JSONArray getBuildStatuses() {
		JSONArray buildStatuses = new JSONArray();
		buildStatuses.addAll(statuses);
		return buildStatuses;
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
		json.put("type", "package");
		json.put("buildStatuses", getBuildStatuses());
		json.put("builds", getBuildJson(userConfig));
		json.put("children", getChildrensJson(userConfig));
		return json;
	}

	protected void evaluateStatusses(TestResult testResult) {
		boolean doTestNg = testResult.getClass().getName().equals("hudson.plugins.testng.results.MethodResult");
		if (doTestNg) {
			try {
				Method method = testResult.getClass().getMethod("getStatus");
				Object returnValue = method.invoke(testResult);
				if (returnValue instanceof String) {
					String status = ((String) returnValue).toLowerCase();
					if (status.startsWith("fail")) {
						status = "FAILED";
					}
					else if (status.startsWith("pass")) {
						status = "PASSED";
					}
					else if (status.startsWith("skip")) {
						status = "SKIPPED";
					}
					if (!(statuses.contains(status))) {
						statuses.add(status);
					}
				}
			}
			catch (Exception e) {
				// fallback to non testng code
				doTestNg = false;
			}
		}
		if (!doTestNg) {
			List<String> tStatusses = new ArrayList<String>();
			if (testResult.getFailCount() > 0) {
				tStatusses.add("FAILED");
			}
			if (testResult.getPassCount() > 0) {
				tStatusses.add("PASSED");
			}
			if (testResult.getSkipCount() > 0) {
				tStatusses.add("SKIPPED");
			}
			for (String tStatus : tStatusses) {
				if (!(statuses.contains(tStatus))) {
					statuses.add(tStatus);
				}
			}
		}
	}
}

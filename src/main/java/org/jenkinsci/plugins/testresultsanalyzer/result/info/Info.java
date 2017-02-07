package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import org.jenkinsci.plugins.testresultsanalyzer.result.data.ResultData;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

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

	public ResultData getBuildResult(Integer buildNumber) {
		return buildResults.get(buildNumber);
	}

	public void setBuildPackageResults(Map<Integer, ResultData> buildResults) {
		this.buildResults = buildResults;
	}

	public abstract Map<String, ? extends Info> getChildren();

	public boolean isConfig() {
		return isConfig;
	}

	public void setConfig(boolean config) {
		isConfig = config;
	}
}

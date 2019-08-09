package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.model.Run;
import hudson.tasks.test.TabulatedResult;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

public class ResultInfo {

	private Map<String, PackageInfo> packageResults = new TreeMap<String, PackageInfo>();
	private final static Logger LOG = Logger.getLogger(ResultInfo.class.getName());

	public void addPackage(Integer buildNumber, String runName, TabulatedResult packageResult, String url) {
		String packageName = packageResult.getName();
		PackageInfo packageInfo;
		if (packageResults.containsKey(packageName)) {
			packageInfo = packageResults.get(packageName);
		}
		else {
			packageInfo = new PackageInfo();
			packageInfo.setName(packageName);
		}
		packageInfo.putPackageResult(buildNumber, runName, packageResult, url + getResultUrl(packageResult) +"/" + packageResult.getSafeName());
		packageResults.put(packageName, packageInfo);
	}

	public Map<String, PackageInfo> getPackageResults() {
		return this.packageResults;
	}

	protected String getResultUrl(TabulatedResult result){
		boolean isTestng = result.getClass().getName().startsWith("hudson.plugins.testng.results");
		if(isTestng){
			return "testngreports";
		} else {
			return "testReport";
		}
	}
}

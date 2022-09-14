package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.model.Run;
import hudson.tasks.test.TabulatedResult;
import hudson.tasks.test.TestResult;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.PackageResultData;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.ResultData;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;


public class PackageInfo extends Info {

	protected Map<String, ClassInfo> classes = new TreeMap<String, ClassInfo>();
	private final static Logger LOG = Logger.getLogger(PackageInfo.class.getName());

	public void putPackageResult(Integer buildNumber, String runName, TabulatedResult packageResult, String url) {
		PackageResultData packageResultData;
		if (this.buildResults.containsKey(buildNumber)) {
			packageResultData = (PackageResultData) this.buildResults.get(buildNumber);
			packageResultData.update(packageResult);
		}
		else {
			packageResultData = new PackageResultData(packageResult, url);
		}

		addClasses(buildNumber, runName, packageResult, url);
		this.buildResults.put(buildNumber, packageResultData);
	}

	public ResultData getPackageResult(Integer buildNumber) {
		if (this.buildResults.containsKey(buildNumber)) {
			return this.buildResults.get(buildNumber);
		}
		return null;
	}

	public Map<String, ClassInfo> getClasses(){
		return classes;
	}

	public void addClasses(Integer buildNumber, String runName, TabulatedResult packageResult, String url) {
		for (TestResult classResult : packageResult.getChildren()) {
			String className = classResult.getName();
			LOG.warning(className);
			ClassInfo classInfo;
			if (classes.containsKey(className)) {
				classInfo = classes.get(className);
			}
			else {
				classInfo = new ClassInfo();
				classInfo.setName(className);
			}
			classInfo.putBuildClassResult(buildNumber, runName, (TabulatedResult) classResult, url + "/" + classResult.getSafeName());
			//TestResult lastClassResult = classResult;
			classes.put(className, classInfo);
		}
	}

	@Override
	public Map<String, ClassInfo> getChildren() {
		return classes;
	}

}

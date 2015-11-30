package org.jenkinsci.plugins.testresultsanalyzer.result.data;

import hudson.tasks.junit.PackageResult;

public class PackageResultData extends ResultData {

	public PackageResultData(PackageResult packageResult, String url) {
		super(packageResult, url);
	}
}

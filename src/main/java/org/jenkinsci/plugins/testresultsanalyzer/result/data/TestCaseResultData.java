package org.jenkinsci.plugins.testresultsanalyzer.result.data;

import hudson.tasks.test.TestResult;

public class TestCaseResultData extends ResultData {

	public TestCaseResultData(TestResult testResult, String url){
		super(testResult, url);

		if("FAILED".equalsIgnoreCase(getStatus())){
			setFailureMessage(testResult.getErrorStackTrace());
		}
	}
	
}

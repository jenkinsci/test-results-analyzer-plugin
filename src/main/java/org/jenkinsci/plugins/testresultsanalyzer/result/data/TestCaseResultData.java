package org.jenkinsci.plugins.testresultsanalyzer.result.data;

import hudson.tasks.junit.CaseResult;

public class TestCaseResultData extends ResultData {

	public TestCaseResultData(CaseResult testResult, String url){
		super(testResult, url);

		if("FAILED".equalsIgnoreCase(getStatus())){
			setFailureMessage(testResult.getErrorStackTrace());
		}
	}
	
}

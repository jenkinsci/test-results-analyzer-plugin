package org.jenkinsci.plugins.testresultsanalyzer.result.data;

import hudson.tasks.test.TabulatedResult;


public class ClassResultData extends ResultData {

	public ClassResultData(TabulatedResult classResult, String url) {
		super(classResult, url);
	}
}

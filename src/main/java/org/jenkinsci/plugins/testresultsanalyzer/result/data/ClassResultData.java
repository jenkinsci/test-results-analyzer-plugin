package org.jenkinsci.plugins.testresultsanalyzer.result.data;

import hudson.tasks.junit.ClassResult;


public class ClassResultData extends ResultData {

	public ClassResultData(ClassResult classResult, String url) {
		super(classResult, url);
	}
}

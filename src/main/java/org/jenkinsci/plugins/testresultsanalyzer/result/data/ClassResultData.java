package org.jenkinsci.plugins.testresultsanalyzer.result.data;

import hudson.tasks.test.TabulatedResult;

public class ClassResultData extends ResultData {

    public ClassResultData(TabulatedResult classResult, String url) {
        super(classResult, url);
    }

    public ClassResultData(
            String name,
            int totalTests,
            int totalFailed,
            int totalPassed,
            int totalSkipped,
            float duration,
            String url) {
        super(name, totalTests, totalFailed, totalPassed, totalSkipped, duration, url);
    }
}

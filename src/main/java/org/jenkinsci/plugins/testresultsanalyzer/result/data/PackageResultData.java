package org.jenkinsci.plugins.testresultsanalyzer.result.data;

import hudson.tasks.test.TestResult;

public class PackageResultData extends ResultData {

    public PackageResultData(TestResult packageResult, String url) {
        super(packageResult, url);
    }

    public PackageResultData(
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

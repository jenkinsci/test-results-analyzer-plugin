package org.jenkinsci.plugins.testresultsanalyzer.result.data;

import hudson.tasks.test.TabulatedResult;
import hudson.tasks.test.TestObject;

public abstract class ResultData {

    private String name;
    private boolean isConfig = false;
    private transient TabulatedResult packageResult;
    private int totalTests;
    private int totalFailed;
    private int totalPassed;
    private int totalSkipped;
    private float totalTimeTaken;
    private String status;
    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isConfig() {
        return isConfig;
    }

    public void setConfig(boolean config) {
        isConfig = config;
    }

    public TabulatedResult getPackageResult() {
        return packageResult;
    }

    public void setPackageResult(TabulatedResult packageResult) {
        this.packageResult = packageResult;
    }

    public int getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(int totalTests) {
        this.totalTests = totalTests;
    }

    public int getTotalFailed() {
        return totalFailed;
    }

    public void setTotalFailed(int totalFailed) {
        this.totalFailed = totalFailed;
    }

    public int getTotalPassed() {
        return totalPassed;
    }

    public void setTotalPassed(int totalPassed) {
        this.totalPassed = totalPassed;
    }

    public int getTotalSkipped() {
        return totalSkipped;
    }

    public void setTotalSkipped(int totalSkipped) {
        this.totalSkipped = totalSkipped;
    }

    public float getTotalTimeTaken() {
        return totalTimeTaken;
    }

    public void setTotalTimeTaken(float totalTimeTaken) {
        this.totalTimeTaken = totalTimeTaken;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // Used for constructing mock object
    public ResultData() {}

    protected ResultData(
            String name,
            int totalTests,
            int totalFailed,
            int totalPassed,
            int totalSkipped,
            float duration,
            String url) {
        setName(name);
        setTotalTests(totalTests);
        setTotalFailed(totalFailed);
        setTotalPassed(totalPassed);
        setTotalSkipped(totalSkipped);
        setTotalTimeTaken(duration);
        setUrl(url);
        evaluateStatus();
    }

    public ResultData(TestObject result, String url) {
        setName(result.getName());
        setTotalTests(result.getTotalCount());
        setTotalFailed(result.getFailCount());
        setTotalPassed(result.getPassCount());
        setTotalSkipped(result.getSkipCount());
        setTotalTimeTaken(result.getDuration());
        setUrl(url);
        evaluateStatus();
    }

    /**
     * Adds the counts and duration of another result of the same package or class in the same build, for when it
     * was reported more than once.
     */
    public void add(ResultData other) {
        totalTests += other.totalTests;
        totalFailed += other.totalFailed;
        totalPassed += other.totalPassed;
        totalSkipped += other.totalSkipped;
        totalTimeTaken += other.totalTimeTaken;
        evaluateStatus();
    }

    /**
     * Corrects the counts of this parent result after two executions of one of its tests, which it counted
     * separately, were merged into a single result. The duration is left alone as the merged result sums them.
     */
    public void mergedChild(ResultData first, ResultData second, ResultData merged) {
        totalTests += merged.totalTests - first.totalTests - second.totalTests;
        totalFailed += merged.totalFailed - first.totalFailed - second.totalFailed;
        totalPassed += merged.totalPassed - first.totalPassed - second.totalPassed;
        totalSkipped += merged.totalSkipped - first.totalSkipped - second.totalSkipped;
        evaluateStatus();
    }

    protected void evaluateStatus() {
        if (totalSkipped == totalTests) {
            status = "SKIPPED";
        } else if (totalFailed == 0) {
            status = "PASSED";
        } else {
            status = "FAILED";
        }
    }

    public String getStatus() {
        return status;
    }
}

package org.jenkinsci.plugins.testresultsanalyzer.result;

import hudson.tasks.test.TabulatedResult;
import hudson.tasks.test.TestObject;
import hudson.tasks.test.TestResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.kohsuke.stapler.export.Exported;

public class FakeTabulatedResult extends TabulatedResult {
    private final TestObject parent;
    private final String name;
    private final List<TestResult> tests;

    public FakeTabulatedResult(TestObject parent, String name) {
        this.parent = parent;
        this.name = name;
        this.tests = new ArrayList<>();
    }

    @Override
    public Collection<? extends TestResult> getChildren() {
        return tests;
    }

    @Override
    public boolean hasChildren() {
        return !tests.isEmpty();
    }

    @Override
    public TestObject getParent() {
        return parent;
    }

    @Override
    public TestResult findCorrespondingResult(String s) {
        return null;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    @Exported
    public int getPassCount() {
        int passCount = 0;
        for (TestResult testResult : tests) {
            passCount += testResult.getPassCount();
        }
        return passCount;
    }

    @Exported
    public int getFailCount() {
        int failCount = 0;
        for (TestResult testResult : tests) {
            failCount += testResult.getFailCount();
        }
        return failCount;
    }

    @Exported
    public int getSkipCount() {
        int skipCount = 0;
        for (TestResult testResult : tests) {
            skipCount += testResult.getSkipCount();
        }
        return skipCount;
    }

    protected FakeTabulatedResult addTestResult(TestResult testResult) {
        tests.add(testResult);
        return this;
    }
}

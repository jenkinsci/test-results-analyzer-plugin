package org.jenkinsci.plugins.testresultsanalyzer.result;

import java.util.HashMap;
import java.util.Map;

public class FakePackageResult extends FakeTabulatedResult {
    private final Map<String, FakeClassResult> classResults;

    public FakePackageResult(String packageName) {
        super(null, packageName);

        classResults = new HashMap<>();
    }

    public FakePackageResult addTest(String className, String testMethod, TestStatus testStatus) {
        getClassResult(className).addTest(testMethod, testStatus);
        return this;
    }

    private FakeClassResult getClassResult(String name) {
        FakeClassResult result = classResults.get(name);
        if (result == null) {
            result = new FakeClassResult(this, name);
            classResults.put(name, result);
            addTestResult(result);
        }
        return result;
    }
}

package org.jenkinsci.plugins.testresultsanalyzer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jenkinsci.plugins.testresultsanalyzer.result.FakePackageResult;
import org.jenkinsci.plugins.testresultsanalyzer.result.TestStatus;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.ResultData;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.ClassInfo;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.PackageInfo;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.ResultInfo;
import org.junit.jupiter.api.Test;

/** Tests executed more than once in a build, see #224 and #233. */
class DuplicateTestExecutionsTest {

    private static ResultInfo load(DuplicateTestPolicy policy) {
        ResultInfo results = new ResultInfo(policy);
        results.addPackage(
                7,
                new FakePackageResult("pn")
                        .addTest("Class1", "flaky", TestStatus.Fail)
                        .addTest("Class1", "flaky", TestStatus.Pass)
                        .addTest("Class1", "skippedOnce", TestStatus.Skip)
                        .addTest("Class1", "skippedOnce", TestStatus.Pass)
                        .addTest("Class1", "alwaysSkipped", TestStatus.Skip)
                        .addTest("Class1", "alwaysSkipped", TestStatus.Skip)
                        .addTest("Class1", "stable", TestStatus.Pass),
                "someUrl/");
        return results;
    }

    @Test
    void failedIfAnyExecutionFailed() {
        ResultInfo results = load(DuplicateTestPolicy.FAILED_IF_ANY_FAILED);
        ClassInfo classInfo = results.getPackageResults().get("pn").getClasses().get("Class1");

        assertThat(classInfo.getTests().size(), is(4));
        assertResult(test(classInfo, "flaky"), "FAILED", 1, 1, 0, 0);
        assertResult(test(classInfo, "skippedOnce"), "PASSED", 1, 0, 1, 0);
        assertResult(test(classInfo, "alwaysSkipped"), "SKIPPED", 1, 0, 0, 1);
        assertResult(test(classInfo, "stable"), "PASSED", 1, 0, 1, 0);
        // each test counts once in its class and package, with the status shown for it
        assertResult(classInfo.getBuildResult(7), "FAILED", 4, 1, 2, 1);
        assertResult(results.getPackageResults().get("pn").getBuildResult(7), "FAILED", 4, 1, 2, 1);
    }

    @Test
    void passedIfAnyExecutionPassed() {
        ResultInfo results = load(DuplicateTestPolicy.PASSED_IF_ANY_PASSED);
        ClassInfo classInfo = results.getPackageResults().get("pn").getClasses().get("Class1");

        assertResult(test(classInfo, "flaky"), "PASSED", 1, 0, 1, 0);
        assertResult(test(classInfo, "skippedOnce"), "PASSED", 1, 0, 1, 0);
        assertResult(test(classInfo, "alwaysSkipped"), "SKIPPED", 1, 0, 0, 1);
        assertResult(classInfo.getBuildResult(7), "PASSED", 4, 0, 3, 1);
        assertResult(results.getPackageResults().get("pn").getBuildResult(7), "PASSED", 4, 0, 3, 1);
    }

    @Test
    void packageReportedTwiceInABuildIsMerged() {
        // as when several test result actions, or the child reports of an aggregated one, cover the same classes
        ResultInfo results = new ResultInfo(DuplicateTestPolicy.FAILED_IF_ANY_FAILED);
        results.addPackage(
                7,
                new FakePackageResult("pn")
                        .addTest("Class1", "flaky", TestStatus.Fail)
                        .addTest("Class1", "first", TestStatus.Pass),
                "someUrl/");
        results.addPackage(
                7,
                new FakePackageResult("pn")
                        .addTest("Class1", "flaky", TestStatus.Pass)
                        .addTest("Class2", "second", TestStatus.Pass),
                "someUrl/");
        PackageInfo packageInfo = results.getPackageResults().get("pn");
        ClassInfo class1 = packageInfo.getClasses().get("Class1");

        assertResult(test(class1, "flaky"), "FAILED", 1, 1, 0, 0);
        assertResult(test(class1, "first"), "PASSED", 1, 0, 1, 0);
        assertResult(class1.getBuildResult(7), "FAILED", 2, 1, 1, 0);
        assertResult(packageInfo.getClasses().get("Class2").getBuildResult(7), "PASSED", 1, 0, 1, 0);
        assertResult(packageInfo.getBuildResult(7), "FAILED", 3, 1, 2, 0);
        assertThat(packageInfo.getBuildResult(8), is(nullValue()));
    }

    private static ResultData test(ClassInfo classInfo, String name) {
        return classInfo.getTests().get(name).getBuildResult(7);
    }

    private static void assertResult(ResultData result, String status, int total, int failed, int passed, int skipped) {
        assertThat(result.getStatus(), is(status));
        assertThat(result.getTotalTests(), is(total));
        assertThat(result.getTotalFailed(), is(failed));
        assertThat(result.getTotalPassed(), is(passed));
        assertThat(result.getTotalSkipped(), is(skipped));
    }
}

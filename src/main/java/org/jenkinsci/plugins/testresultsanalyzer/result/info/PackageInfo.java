package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.tasks.test.TabulatedResult;
import hudson.tasks.test.TestResult;
import java.util.Map;
import java.util.TreeMap;
import org.jenkinsci.plugins.testresultsanalyzer.DuplicateTestPolicy;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.PackageResultData;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.ResultData;

public class PackageInfo extends Info {

    protected Map<String, ClassInfo> classes = new TreeMap<String, ClassInfo>();

    public void putPackageResult(Integer buildNumber, TabulatedResult packageResult, String url) {
        putPackageResult(buildNumber, packageResult, url, DuplicateTestPolicy.DEFAULT);
    }

    /** Adds a package and its classes of a build, merging them with what was already added for that build. */
    public void putPackageResult(
            Integer buildNumber, TabulatedResult packageResult, String url, DuplicateTestPolicy policy) {
        PackageResultData packageResultData = new PackageResultData(packageResult, url);
        ResultData previous = this.buildResults.get(buildNumber);
        if (previous != null) {
            packageResultData.add(previous);
        }

        addClasses(buildNumber, packageResult, url, policy, packageResultData);
        this.buildResults.put(buildNumber, packageResultData);
    }

    public ResultData getPackageResult(Integer buildNumber) {
        if (this.buildResults.containsKey(buildNumber)) {
            return this.buildResults.get(buildNumber);
        }
        return null;
    }

    public ClassInfo getOrCreateClass(String className) {
        return classes.computeIfAbsent(className, name -> {
            ClassInfo classInfo = new ClassInfo();
            classInfo.setName(name);
            return classInfo;
        });
    }

    public Map<String, ClassInfo> getClasses() {
        return classes;
    }

    public void addClasses(Integer buildNumber, TabulatedResult packageResult, String url) {
        addClasses(buildNumber, packageResult, url, DuplicateTestPolicy.DEFAULT);
    }

    private void addClasses(
            Integer buildNumber,
            TabulatedResult packageResult,
            String url,
            DuplicateTestPolicy policy,
            ResultData... parents) {
        for (TestResult classResult : packageResult.getChildren()) {
            getOrCreateClass(classResult.getName())
                    .putBuildClassResult(
                            buildNumber,
                            (TabulatedResult) classResult,
                            url + "/" + classResult.getSafeName(),
                            policy,
                            parents);
        }
    }

    @Override
    public Map<String, ClassInfo> getChildren() {
        return classes;
    }
}

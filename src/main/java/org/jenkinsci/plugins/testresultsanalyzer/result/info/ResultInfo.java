package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import hudson.tasks.test.TabulatedResult;
import java.util.Map;
import java.util.TreeMap;
import org.jenkinsci.plugins.testresultsanalyzer.DuplicateTestPolicy;

public class ResultInfo {

    private Map<String, PackageInfo> packageResults = new TreeMap<String, PackageInfo>();
    private final DuplicateTestPolicy duplicateTestPolicy;

    public ResultInfo() {
        this(DuplicateTestPolicy.DEFAULT);
    }

    /**
     * @param duplicateTestPolicy how to merge executions of the same test within one build
     */
    public ResultInfo(DuplicateTestPolicy duplicateTestPolicy) {
        this.duplicateTestPolicy = duplicateTestPolicy;
    }

    public DuplicateTestPolicy getDuplicateTestPolicy() {
        return duplicateTestPolicy;
    }

    public void addPackage(Integer buildNumber, TabulatedResult packageResult, String url) {
        String packageName = packageResult.getName();
        PackageInfo packageInfo;
        if (packageResults.containsKey(packageName)) {
            packageInfo = packageResults.get(packageName);
        } else {
            packageInfo = new PackageInfo();
            packageInfo.setName(packageName);
        }
        packageInfo.putPackageResult(
                buildNumber,
                packageResult,
                url + getResultUrl(packageResult) + "/" + packageResult.getSafeName(),
                duplicateTestPolicy);
        packageResults.put(packageName, packageInfo);
    }

    public PackageInfo getOrCreatePackage(String packageName) {
        return packageResults.computeIfAbsent(packageName, name -> {
            PackageInfo packageInfo = new PackageInfo();
            packageInfo.setName(name);
            return packageInfo;
        });
    }

    public Map<String, PackageInfo> getPackageResults() {
        return this.packageResults;
    }

    protected String getResultUrl(TabulatedResult result) {
        boolean isTestng = result.getClass().getName().startsWith("hudson.plugins.testng.results");
        if (isTestng) {
            return "testngreports";
        } else {
            return "testReport";
        }
    }
}

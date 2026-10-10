package org.jenkinsci.plugins.testresultsanalyzer;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.model.Job;
import hudson.tasks.junit.TestNameTransformer;
import hudson.tasks.test.TestObject;
import io.jenkins.plugins.junit.storage.CaseResultSummary;
import io.jenkins.plugins.junit.storage.FileJunitTestResultStorage;
import io.jenkins.plugins.junit.storage.JunitTestResultStorage;
import io.jenkins.plugins.junit.storage.TestResultImpl;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.ClassResultData;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.PackageResultData;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.TestCaseResultData;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.ClassInfo;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.PackageInfo;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.ResultInfo;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.TestCaseInfo;

/**
 * Builds a {@link ResultInfo} for many builds at once from a pluggable junit storage that supports
 * {@link TestResultImpl#forEachCaseResultSummary}, instead of loading each build's full test result.
 *
 * <p>Names, URLs and counts mirror what the per-build path derives from the junit
 * {@code TestResult -> PackageResult -> ClassResult -> CaseResult} tree.
 */
final class StorageResultLoader {

    private final TestResultImpl storage;

    private StorageResultLoader(TestResultImpl storage) {
        this.storage = storage;
    }

    /**
     * @return a loader if the configured junit storage is external and can stream case summaries, otherwise
     *     {@code null}
     */
    @CheckForNull
    static StorageResultLoader forJob(Job<?, ?> job, int anyBuild) {
        JunitTestResultStorage storage = JunitTestResultStorage.find();
        if (storage instanceof FileJunitTestResultStorage) {
            return null;
        }
        TestResultImpl impl = storage.load(job.getFullName(), anyBuild);
        if (impl == null || !impl.supportsCaseResultSummaries()) {
            return null;
        }
        return new StorageResultLoader(impl);
    }

    /**
     * Adds the results of the given builds to {@code resultInfo}.
     *
     * @param buildUrls absolute URL (with trailing slash) of each build to load, keyed by build number
     */
    void load(Map<Integer, String> buildUrls, ResultInfo resultInfo) {
        if (buildUrls.isEmpty()) {
            return;
        }
        int from = Collections.min(buildUrls.keySet());
        int to = Collections.max(buildUrls.keySet());
        Map<String, String> names = new HashMap<>();
        BuildAccumulator[] current = {null};
        storage.forEachCaseResultSummary(from, to, summary -> {
            String buildUrl = buildUrls.get(summary.getBuild());
            if (buildUrl == null) {
                return;
            }
            BuildAccumulator acc = current[0];
            if (acc == null || acc.build != summary.getBuild()) {
                if (acc != null) {
                    acc.flush(resultInfo);
                }
                acc = current[0] = new BuildAccumulator(summary.getBuild(), buildUrl, names);
            }
            acc.add(summary, resultInfo);
        });
        if (current[0] != null) {
            current[0].flush(resultInfo);
        }
    }

    /** Tallies the package and class rows of one build while its cases stream past. */
    private static final class BuildAccumulator {
        final int build;
        final String reportUrl;
        final Map<String, Tally> packages = new HashMap<>();
        /** Pool of names shared across builds, so millions of cells don't each hold their own copies. */
        final Map<String, String> names;

        BuildAccumulator(int build, String buildUrl, Map<String, String> names) {
            this.build = build;
            this.reportUrl = buildUrl + "testReport/";
            this.names = names;
        }

        private String pooled(String name) {
            return names.computeIfAbsent(name, n -> n);
        }

        void add(CaseResultSummary summary, ResultInfo resultInfo) {
            String packageName = summary.getPackageName();
            Tally packageTally = packages.computeIfAbsent(
                    packageName, name -> new Tally(name, reportUrl + TestObject.safe(name), null));
            String className = summary.getSimpleName();
            // ClassResults are keyed (and so summed) by their safe name within a PackageResult
            String classKey = TestObject.safe(className);
            Tally classTally = packageTally.children.computeIfAbsent(
                    classKey, key -> new Tally(className, packageTally.url + "/" + key, new HashMap<>()));
            classTally.add(summary);

            String rawName = pooled(summary.getName());
            String testName = pooled(TestNameTransformer.getTransformedName(rawName));
            TestCaseInfo testCaseInfo = resultInfo
                    .getOrCreatePackage(packageName)
                    .getOrCreateClass(className)
                    .getOrCreateTest(testName);
            testCaseInfo.putBuildResult(
                    build,
                    new CaseData(
                            rawName, summary, classTally, pooled(classTally.uniqueCaseName(safeCaseName(testName)))));
        }

        void flush(ResultInfo resultInfo) {
            for (Tally packageTally : packages.values()) {
                PackageInfo packageInfo = resultInfo.getOrCreatePackage(packageTally.name);
                for (Tally classTally : packageTally.children.values()) {
                    packageTally.addAll(classTally);
                    ClassInfo classInfo = packageInfo.getOrCreateClass(classTally.name);
                    classInfo.putBuildResult(build, classTally.toClassData());
                    classTally.caseNames = null; // no longer needed, the tally stays reachable from its cases
                }
                packageInfo.putBuildResult(build, packageTally.toPackageData());
            }
        }

        /** As {@code CaseResult#getSafeName()} before uniquifying amongst siblings. */
        private static String safeCaseName(String displayName) {
            StringBuilder buf = new StringBuilder(displayName);
            for (int i = 0; i < buf.length(); i++) {
                if (!Character.isJavaIdentifierPart(buf.charAt(i))) {
                    buf.setCharAt(i, '_');
                }
            }
            return buf.toString();
        }
    }

    /** A case result whose URL is derived from its class when needed rather than stored per cell. */
    private static final class CaseData extends TestCaseResultData {
        private final Tally classTally;
        private final String safeName;

        CaseData(String name, CaseResultSummary summary, Tally classTally, String safeName) {
            super(name, summary.isFailed(), summary.isSkipped(), summary.getDuration(), null);
            this.classTally = classTally;
            this.safeName = safeName;
        }

        @Override
        public String getUrl() {
            return classTally.url + "/" + safeName;
        }
    }

    private static final class Tally {
        final String name;
        final String url;
        /** Classes of a package, sorted as {@code PackageResult} sums them; {@code null} for a class. */
        final Map<String, Tally> children;
        /** Case safe names already used within a class, to uniquify duplicates as junit does. */
        Map<String, Integer> caseNames;

        int total;
        int failed;
        int passed;
        int skipped;
        float duration;

        Tally(String name, String url, Map<String, Integer> caseNames) {
            this.name = name;
            this.url = url;
            this.caseNames = caseNames;
            this.children = caseNames == null ? new TreeMap<>() : null;
        }

        void add(CaseResultSummary summary) {
            total++;
            if (summary.isSkipped()) {
                skipped++;
            } else if (summary.isFailed()) {
                failed++;
            } else {
                passed++;
            }
            duration += summary.getDuration();
        }

        void addAll(Tally child) {
            total += child.total;
            failed += child.failed;
            passed += child.passed;
            skipped += child.skipped;
            duration += child.duration;
        }

        String uniqueCaseName(String safeName) {
            int seen = caseNames.merge(safeName, 1, Integer::sum);
            return seen == 1 ? safeName : safeName + '_' + seen;
        }

        ClassResultData toClassData() {
            return new ClassResultData(name, total, failed, passed, skipped, duration, url);
        }

        PackageResultData toPackageData() {
            return new PackageResultData(name, total, failed, passed, skipped, duration, url);
        }
    }
}

package org.jenkinsci.plugins.testresultsanalyzer;

import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.AggregatedTestResultAction;
import hudson.tasks.test.TabulatedResult;
import hudson.tasks.test.TestResult;
import hudson.util.RunList;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.ResultData;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.ClassInfo;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.PackageInfo;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.ResultInfo;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.TestCaseInfo;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.verb.GET;

public class TestResultsAnalyzerAction extends Actionable implements Action {

    @SuppressWarnings("rawtypes")
    Job project;

    private List<Integer> builds = new ArrayList<Integer>();
    private static final Logger LOG = Logger.getLogger(TestResultsAnalyzerAction.class.getName());

    ResultInfo resultInfo;

    /** How many completed builds {@link #resultInfo} was loaded for, {@code -1} meaning all of them. */
    private int loadedBuilds;

    public TestResultsAnalyzerAction(@SuppressWarnings("rawtypes") Job project) {
        this.project = project;
    }

    /**
     * The display name for the action.
     *
     * @return the name as String
     */
    public final String getDisplayName() {
        return this.hasPermission() ? Constants.NAME : null;
    }

    /**
     * The icon for this action.
     *
     * @return the icon file as String
     */
    public final String getIconFileName() {
        return this.hasPermission() && hasTestResults() ? Constants.ICONFILENAME : null;
    }

    /**
     * Whether the job published test results recently, so jobs without tests do not get a side panel link.
     * Only the latest builds are checked to keep rendering the job page cheap.
     */
    private boolean hasTestResults() {
        return hasTestResults(project.getLastCompletedBuild()) || hasTestResults(project.getLastSuccessfulBuild());
    }

    private static boolean hasTestResults(Run<?, ?> run) {
        return run != null && run.getAction(AbstractTestResultAction.class) != null;
    }

    /**
     * The url for this action.
     *
     * @return the url as String
     */
    public String getUrlName() {
        return this.hasPermission() ? Constants.URL : null;
    }

    /**
     * Search url for this action.
     *
     * @return the url as String
     */
    public String getSearchUrl() {
        return this.hasPermission() ? Constants.URL : null;
    }

    /**
     * Checks if the user has CONFIGURE permission.
     *
     * @return true - user has permission, false - no permission.
     */
    private boolean hasPermission() {
        return project.hasPermission(Item.READ);
    }

    @SuppressWarnings("rawtypes")
    public Job getProject() {
        return this.project;
    }

    private List<Integer> getBuildList(int noOfBuilds) {
        if ((noOfBuilds <= 0) || (noOfBuilds >= builds.size())) {
            return builds;
        }

        List<Integer> buildList = new ArrayList<Integer>();

        for (int i = 0; i < noOfBuilds; i++) {
            buildList.add(builds.get(i));
        }

        return buildList;
    }

    private int getNoOfBuildRequired(String noOfbuildsNeeded) {
        int noOfBuilds;
        try {
            noOfBuilds = Integer.parseInt(noOfbuildsNeeded);
        } catch (NumberFormatException e) {
            noOfBuilds = -1;
        }
        return noOfBuilds;
    }

    public synchronized boolean isUpdated() {
        Run lastBuild = project.getLastCompletedBuild();
        if (lastBuild == null) {
            return false;
        }

        int latestBuildNumber = lastBuild.getNumber();
        return !(builds.contains(latestBuildNumber));
    }

    /**
     * Loads results for every completed build (up to the configured number of runs to fetch).
     *
     * @deprecated results are now loaded on demand for the number of builds requested, see
     *     {@link #doData(String, boolean)}
     */
    @Deprecated
    public void getJsonLoadData() {
        ensureLoaded(-1);
    }

    /**
     * Makes sure {@link #resultInfo} holds at least the latest {@code noOfBuilds} completed builds.
     *
     * @param noOfBuilds number of completed builds needed, or a non-positive number for all of them
     */
    synchronized void ensureLoaded(int noOfBuilds) {
        int needed = noOfBuilds > 0 ? noOfBuilds : -1;
        boolean enough = loadedBuilds < 0 || (needed > 0 && needed <= loadedBuilds);
        if (resultInfo != null && enough && !isUpdated()) {
            return;
        }
        load(needed);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void load(int noOfBuilds) {
        resultInfo = new ResultInfo();
        builds = new ArrayList<Integer>();
        loadedBuilds = noOfBuilds;

        RunList<Run> runs = null;
        if (getNoOfRunsToFetch() > 0) {
            runs = project.getBuilds().limit(getNoOfRunsToFetch());
        } else {
            runs = project.getBuilds();
        }
        Jenkins jenkins = Jenkins.getInstanceOrNull();
        String rootUrl = jenkins != null ? jenkins.getRootUrl() : "";
        if (rootUrl == null) {
            rootUrl = "";
        }

        List<Run> completed = new ArrayList<>();
        for (Run run : runs) {
            if (run.isBuilding()) {
                continue;
            }
            completed.add(run);
            builds.add(run.getNumber());
            if (noOfBuilds > 0 && completed.size() >= noOfBuilds) {
                break;
            }
        }
        if (completed.isEmpty()) {
            return;
        }

        StorageResultLoader storageLoader =
                StorageResultLoader.forJob(project, completed.get(0).getNumber());
        Map<Integer, String> storageBuilds = new HashMap<>();
        for (Run run : completed) {
            int buildNumber = run.getNumber();
            List<AbstractTestResultAction> testActions = run.getActions(AbstractTestResultAction.class);
            if (storageLoader != null && isOnlyJUnitAction(testActions)) {
                storageBuilds.put(buildNumber, rootUrl + run.getUrl());
                continue;
            }
            for (AbstractTestResultAction testAction : testActions) {
                if (AggregatedTestResultAction.class.isInstance(testAction)) {
                    addTestResults(buildNumber, (AggregatedTestResultAction) testAction, rootUrl);
                } else {
                    addTestResult(buildNumber, run, testAction, testAction.getResult(), rootUrl);
                }
            }
        }
        if (storageLoader != null) {
            storageLoader.load(storageBuilds, resultInfo);
        }
    }

    @SuppressWarnings("rawtypes")
    private static boolean isOnlyJUnitAction(List<AbstractTestResultAction> testActions) {
        return testActions.size() == 1 && testActions.get(0).getClass() == TestResultAction.class;
    }

    private void addTestResults(int buildNumber, AggregatedTestResultAction testAction, String rootUrl) {
        List<AggregatedTestResultAction.ChildReport> childReports = testAction.getChildReports();
        for (AggregatedTestResultAction.ChildReport childReport : childReports) {
            addTestResult(buildNumber, childReport.run, testAction, childReport.result, rootUrl);
        }
    }

    private void addTestResult(
            int buildNumber, Run run, AbstractTestResultAction testAction, Object result, String rootUrl) {
        if (run == null || result == null) {
            return;
        }

        try {
            TabulatedResult testResult = (TabulatedResult) result;
            Collection<? extends TestResult> packageResults = testResult.getChildren();
            for (TestResult packageResult : packageResults) { // packageresult
                resultInfo.addPackage(buildNumber, (TabulatedResult) packageResult, rootUrl + run.getUrl());
            }
        } catch (ClassCastException e) {
            LOG.info("Got ClassCast exception while converting results to Tabulated Result from action: "
                    + testAction.getClass().getName() + ". Ignoring as we only want test results for processing.");
        }
    }

    /**
     * The test history tree of the latest builds as JSON, for the analyzer page.
     *
     * <p>Streamed rather than returned as a {@link JSONObject}: with many builds and tests the tree has
     * millions of cells, too many to hold as {@code net.sf.json} objects.
     *
     * @param builds the number of builds, or -1 for all of them
     */
    @GET
    public synchronized HttpResponse doData(@QueryParameter String builds, @QueryParameter boolean hideConfigMethods) {
        project.checkPermission(Item.READ);
        int noOfBuilds = getNoOfBuildRequired(builds);
        ensureLoaded(noOfBuilds);
        // Writing happens after the lock is released; a reload replaces these rather than changing them
        return new TreeResponse(getBuildList(noOfBuilds), resultInfo, hideConfigMethods);
    }

    private static final class TreeResponse implements HttpResponse {
        private final List<Integer> builds;
        private final ResultInfo resultInfo;
        private final boolean hideConfigMethods;

        TreeResponse(List<Integer> builds, ResultInfo resultInfo, boolean hideConfigMethods) {
            this.builds = builds;
            this.resultInfo = resultInfo;
            this.hideConfigMethods = hideConfigMethods;
        }

        @Override
        public void generateResponse(StaplerRequest2 req, StaplerResponse2 rsp, Object node) throws IOException {
            rsp.setContentType("application/json;charset=UTF-8");
            try (Writer out = new BufferedWriter(rsp.getCompressedWriter(req), 64 * 1024)) {
                new JsTreeUtil().writeJsTree(out, builds, resultInfo, hideConfigMethods);
            }
        }
    }

    /**
     * Writes the test history tree for the requested number of builds as JSON.
     */
    synchronized void writeTreeResult(Writer out, String noOfBuildsNeeded, boolean hideConfigMethods)
            throws IOException {
        int noOfBuilds = getNoOfBuildRequired(noOfBuildsNeeded);
        ensureLoaded(noOfBuilds);
        new JsTreeUtil().writeJsTree(out, getBuildList(noOfBuilds), resultInfo, hideConfigMethods);
    }

    /**
     * Downloads the test results of the latest builds as CSV.
     *
     * @param builds the number of builds, or -1 for all of them
     * @param durations whether to export run times instead of statuses
     */
    @GET
    public void doCsv(StaplerResponse2 rsp, @QueryParameter String builds, @QueryParameter boolean durations)
            throws IOException {
        project.checkPermission(Item.READ);
        String csv = exportCsv(durations, builds);
        rsp.setContentType("text/csv;charset=UTF-8");
        rsp.setHeader("Content-Disposition", "attachment; filename=\"test-results.csv\"");
        rsp.getWriter().write(csv);
    }

    synchronized String exportCsv(boolean isTimeBased, String noOfBuildsNeeded) {
        int noOfBuilds = getNoOfBuildRequired(noOfBuildsNeeded);
        ensureLoaded(noOfBuilds);
        Map<String, PackageInfo> packageResults = resultInfo.getPackageResults();
        List<Integer> buildList = getBuildList(noOfBuilds);

        StringBuilder exportBuilder = new StringBuilder("\"Package\",\"Class\",\"Test\"");
        for (Integer buildNumber : buildList) {
            exportBuilder.append(',').append(csvValue(buildNumber.toString()));
        }
        exportBuilder.append(System.lineSeparator());
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        decimalFormat.setRoundingMode(RoundingMode.CEILING);
        for (PackageInfo pInfo : packageResults.values()) {
            String packageName = pInfo.getName();
            // loop the classes
            for (ClassInfo cInfo : pInfo.getClasses().values()) {
                String className = cInfo.getName();
                // loop the tests
                for (TestCaseInfo tInfo : cInfo.getTests().values()) {
                    String testName = tInfo.getName();
                    exportBuilder
                            .append(csvValue(packageName))
                            .append(',')
                            .append(csvValue(className))
                            .append(',')
                            .append(csvValue(testName));
                    Map<Integer, ResultData> buildPackageResults = tInfo.getBuildPackageResults();
                    for (int i = 0; i < buildList.size(); i++) {
                        Integer buildNumber = buildList.get(i);
                        String data = getCustomStatus("NA");
                        if (buildPackageResults.containsKey(buildNumber)) {
                            ResultData buildResult = buildPackageResults.get(buildNumber);
                            if (!isTimeBased) {
                                data = getCustomStatus(buildResult.getStatus());
                            } else {
                                data = decimalFormat.format(buildResult.getTotalTimeTaken());
                            }
                        }
                        exportBuilder.append(',').append(csvValue(data));
                    }
                    exportBuilder.append(System.lineSeparator());
                }
            }
        }
        return exportBuilder.toString();
    }

    /**
     * Quotes a CSV value, doubling any quotes inside it (RFC 4180).
     * Values a spreadsheet would run as a formula are prefixed with an apostrophe so they stay text.
     */
    static String csvValue(String value) {
        String text = value == null ? "" : value;
        if (!text.isEmpty() && "=+-@\t\r".indexOf(text.charAt(0)) >= 0) {
            text = "'" + text;
        }
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private String getCustomStatus(String status) {
        ResultStatus resultStatus = null;
        try {
            resultStatus = ResultStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            resultStatus = null;
        }
        if (resultStatus == null) return status;
        switch (resultStatus) {
            case PASSED:
                return getPassedRepresentation();
            case FAILED:
                return getFailedRepresentation();
            case SKIPPED:
                return getSkippedRepresentation();
            case NA:
                return getNaRepresentation();
        }
        return status;
    }

    /**
     * The configuration of the analyzer page, read by its script.
     * Used by {@code index.jelly}.
     */
    public String getBootstrapJson() {
        JSONObject labels = new JSONObject()
                .element("passed", getPassedRepresentation())
                .element("failed", getFailedRepresentation())
                .element("skipped", getSkippedRepresentation())
                .element("na", getNaRepresentation());
        JSONObject defaults = new JSONObject()
                .element("noOfBuilds", getNoOfBuilds())
                .element("showAllBuilds", getShowAllBuilds())
                .element("showBuildTime", getShowBuildTime())
                .element("hideConfigurationMethods", getHideConfigurationMethods())
                .element("showLineGraph", getShowLineGraph())
                .element("showBarGraph", getShowBarGraph())
                .element("showPieGraph", getShowPieGraph())
                .element("chartDataType", getChartDataType());
        JSONObject bootstrap = new JSONObject()
                .element("labels", labels)
                .element("runTimeLowThreshold", parseSeconds(getRunTimeLowThreshold()))
                .element("runTimeHighThreshold", parseSeconds(getRunTimeHighThreshold()))
                .element("defaults", defaults);
        if (isUseCustomStatusColors()) {
            bootstrap.element(
                    "customColors",
                    new JSONObject()
                            .element("passed", getPassedColor())
                            .element("failed", getFailedColor())
                            .element("skipped", getSkippedColor())
                            .element("na", getNaColor()));
        } else {
            // element() would drop a null value, so the key would be missing rather than null
            bootstrap.put("customColors", JSONNull.getInstance());
        }
        return bootstrap.toString();
    }

    /** A run time threshold in seconds; 0 when it is not a number, since NaN and infinity are not valid JSON. */
    static double parseSeconds(String value) {
        if (value == null) {
            return 0;
        }
        try {
            double seconds = Double.parseDouble(value.trim());
            return Double.isFinite(seconds) ? seconds : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getNoOfBuilds() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.getNoOfBuilds();
    }

    public int getNoOfRunsToFetch() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.getNoOfRunsToFetch();
    }

    public boolean getShowAllBuilds() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.getShowAllBuilds();
    }

    public boolean getShowLineGraph() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.getShowLineGraph();
    }

    public boolean getShowBarGraph() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.getShowBarGraph();
    }

    public boolean getShowPieGraph() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.getShowPieGraph();
    }

    public boolean getShowBuildTime() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.getShowBuildTime();
    }

    public boolean getHideConfigurationMethods() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.getHideConfigurationMethods();
    }

    public String getChartDataType() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.getChartDataType();
    }

    public String getRunTimeLowThreshold() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.getRunTimeLowThreshold();
    }

    public String getRunTimeHighThreshold() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.getRunTimeHighThreshold();
    }

    public boolean isUseCustomStatusNames() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.isUseCustomStatusNames();
    }

    public boolean isUseCustomStatusColors() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.isUseCustomStatusColors();
    }

    public String getPassedRepresentation() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.getPassedRepresentation();
    }

    public String getFailedRepresentation() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.getFailedRepresentation();
    }

    public String getSkippedRepresentation() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.getSkippedRepresentation();
    }

    public String getNaRepresentation() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.getNaRepresentation();
    }

    public String getPassedColor() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.getPassedColor();
    }

    public String getFailedColor() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.getFailedColor();
    }

    public String getSkippedColor() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.getSkippedColor();
    }

    public String getNaColor() {
        return TestResultsAnalyzerExtension.DESCRIPTOR.getNaColor();
    }
}

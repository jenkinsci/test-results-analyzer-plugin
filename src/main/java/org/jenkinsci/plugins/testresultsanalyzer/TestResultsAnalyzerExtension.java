package org.jenkinsci.plugins.testresultsanalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;
import hudson.model.Descriptor;
import hudson.model.Describable;

import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class TestResultsAnalyzerExtension extends TransientProjectActionFactory implements Describable<TestResultsAnalyzerExtension> {

    @Override
    public Collection<? extends Action> createFor(@SuppressWarnings("rawtypes") AbstractProject target) {

        final List<TestResultsAnalyzerAction> projectActions = target
                .getActions(TestResultsAnalyzerAction.class);
        final ArrayList<Action> actions = new ArrayList<Action>();
        if (projectActions.isEmpty()) {
            final TestResultsAnalyzerAction newAction = new TestResultsAnalyzerAction(target);
            actions.add(newAction);
            return actions;
        } else {
            return projectActions;
        }
    }

    //based on DiskUsageProjectActionFactory
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public Descriptor<TestResultsAnalyzerExtension> getDescriptor() {
        return DESCRIPTOR;
    }

    public static class DescriptorImpl extends Descriptor<TestResultsAnalyzerExtension> {

        private static final String PASSED_STATUS_COLOR = "#92D050";
        private static final String FAILED_STATUS_COLOR = "#F37A7A";
        private static final String SKIP_STATUS_COLOR = "#FDED72";
        private static final String NA_STATUS_COLOR = "#E8F5FF";
        private static final String PASSED_REPRESENTATION = "PASSED";
        private static final String FAILED_REPRESENTATION = "FAILED";
        private static final String SKIPPED_REPRESENTATION = "SKIPPED";
        private static final String NA_REPRESENTATION = "N/A";
        private String noOfBuilds = "10";
        private boolean showAllBuilds = false;
        private boolean showBuildTime = false;
        private boolean showLineGraph = true;
        private boolean showBarGraph = true;
        private boolean showPieGraph = true;
        private String runTimeLowThreshold = "0.5";
        private String runTimeHighThreshold = "1.0";

        private final String passFailString = "passfail";
        private final String runtimeString = "runtime";
        private boolean useCustomStatusNames;
        private String passedRepresentation = "PASSED";
        private String failedRepresentation = "FAILED";
        private String skippedRepresentation = "SKIPPED";
        private String naRepresentation = "N/A";

        private boolean useCustomStatusColors;
        private String passedColor = PASSED_STATUS_COLOR;
        private String failedColor = FAILED_STATUS_COLOR;
        private String skippedColor = SKIP_STATUS_COLOR;
        private String naColor = NA_STATUS_COLOR;

        //true = Show Test Runtimes in Charts instead of Passes and Failures
        private String chartDataType = passFailString;

        public DescriptorImpl() {
            //jenkins actually will edit your program's memory and set variables
            load();
        }

        @Override
        public String getDisplayName() {
            return "Test Results Analyzer";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) {
            try {
                noOfBuilds = formData.getString("noOfBuilds");
                showAllBuilds = formData.getBoolean("showAllBuilds");
                showBuildTime = formData.getBoolean("showBuildTime");
                showLineGraph = formData.getBoolean("showLineGraph");
                showBarGraph = formData.getBoolean("showBarGraph");
                showPieGraph = formData.getBoolean("showPieGraph");
                runTimeLowThreshold = formData.getString("runTimeLowThreshold");
                runTimeHighThreshold = formData.getString("runTimeHighThreshold");
                chartDataType = formData.getString("chartDataType");
                if (formData.containsKey("useCustomStatusNames")) {
                    JSONObject customData = formData.getJSONObject("useCustomStatusNames");
                    useCustomStatusNames = true;
                    passedRepresentation = customData.getString("passedRepresentation");
                    failedRepresentation = customData.getString("failedRepresentation");
                    skippedRepresentation = customData.getString("skippedRepresentation");
                    naRepresentation = customData.getString("naRepresentation");
                } else {
                    useCustomStatusNames = false;
                    passedRepresentation = PASSED_REPRESENTATION;
                    failedRepresentation = FAILED_REPRESENTATION;
                    skippedRepresentation = SKIPPED_REPRESENTATION;
                    naRepresentation = NA_REPRESENTATION;
                }
                if (formData.containsKey("useCustomStatusColors")) {
                    JSONObject customData = formData.getJSONObject("useCustomStatusColors");
                    useCustomStatusColors = true;
                    passedColor = customData.getString("passedColor");
                    failedColor = customData.getString("failedColor");
                    skippedColor = customData.getString("skippedColor");
                    naColor = customData.getString("naColor");
                } else {
                    useCustomStatusColors = false;
                    passedColor = PASSED_STATUS_COLOR;
                    failedColor = FAILED_STATUS_COLOR;
                    skippedColor = SKIP_STATUS_COLOR;
                    naColor = NA_STATUS_COLOR;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            save();
            return true;
        }

        public String getNoOfBuilds() { return noOfBuilds; }

        public boolean getShowAllBuilds() { return showAllBuilds; }

        public boolean getShowLineGraph() { return showLineGraph; }

        public boolean getShowBarGraph() { return showBarGraph; }

        public boolean getShowPieGraph() { return showPieGraph; }

        public boolean getShowBuildTime() { return showBuildTime; }

        public String getRunTimeLowThreshold() { return runTimeLowThreshold; }

        public String getRunTimeHighThreshold() { return runTimeHighThreshold; }

        public String getChartDataType() { return chartDataType; }

        public String getPassFailString() { return passFailString; }

        public String getRuntimeString() { return runtimeString; }

        public String getPassedRepresentation() {
            return passedRepresentation;
        }

        public boolean isUseCustomStatusNames() {
            return useCustomStatusNames;
        }

        public String getFailedRepresentation() {
            return failedRepresentation;
        }

        public String getSkippedRepresentation() {
            return skippedRepresentation;
        }

        public String getNaRepresentation() {
            return naRepresentation;
        }

        public boolean isUseCustomStatusColors() {
            return useCustomStatusColors;
        }

        public String getPassedColor() {
            return passedColor;
        }

        public String getFailedColor() {
            return failedColor;
        }

        public String getSkippedColor() {
            return skippedColor;
        }

        public String getNaColor() {
            return naColor;
        }

        public FormValidation doCheckPassedRepresentation(@QueryParameter String passedRepresentation){
            return valueValidation(passedRepresentation);
        }

        public FormValidation doCheckFailedRepresentation(@QueryParameter String failedRepresentation){
            return valueValidation(failedRepresentation);
        }

        public FormValidation doCheckSkippedRepresentation(@QueryParameter String skippedRepresentation){
            return valueValidation(skippedRepresentation);
        }

        public FormValidation doCheckNaRepresentation(@QueryParameter String naRepresentation){
            return valueValidation(naRepresentation);
        }

        private FormValidation valueValidation(String value) {
            if(value == "")
                return FormValidation.error("Entered value should not be empty");
            Pattern regex = Pattern.compile("[<>{}*\\\"\'$&+,:;=?@#|]");
            Matcher matcher = regex.matcher(value);
            if(matcher.find())
                return FormValidation.error("Entered value should not have special characters.");
            return FormValidation.ok();
        }

        public FormValidation doCheckPassedColor(@QueryParameter String passedColor) {
            return colorValidation(passedColor);
        }

        public FormValidation doCheckFailedColor(@QueryParameter String failedColor) {
            return colorValidation(failedColor);
        }

        public FormValidation doCheckSkippedColor(@QueryParameter String skippedColor) {
            return colorValidation(skippedColor);
        }

        public FormValidation doCheckNaColor(@QueryParameter String naColor) {
            return colorValidation(naColor);
        }

        private FormValidation colorValidation(String value) {
            if (value == "")
                return FormValidation.error("Entered value should not be empty");
            final String HEX_PATTERN = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
            Pattern regex = Pattern.compile(HEX_PATTERN);
            Matcher matcher = regex.matcher(value);
            if (!matcher.matches())
                return FormValidation.error("Entered value should be a valid HEX Color");
            return FormValidation.ok();
        }
    }
}

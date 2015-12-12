package org.jenkinsci.plugins.testresultsanalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;
import hudson.model.Descriptor;
import hudson.model.Describable;

import net.sf.json.JSONObject;
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
			} catch(Exception e) {
				e.printStackTrace();
			}
			save();
			return false;
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
	}
}

package org.jenkinsci.plugins.testresultsanalyzer;

import hudson.Extension;
/*import hudson.model.Descriptor;
import hudson.model.Describable;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;*/
import hudson.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.util.logging.*;

@Extension
public class TestResultsAnalyzerExtension extends TransientProjectActionFactory implements Describable<TestResultsAnalyzerExtension> {

	@Override
	public Collection<? extends Action> createFor(@SuppressWarnings("rawtypes") AbstractProject target) {
		final List<TestResultsAnalyzerAction> projectActions = target.getActions(TestResultsAnalyzerAction.class);

		final ArrayList<Action> actions = new ArrayList<Action>();

		if(projectActions.isEmpty()) {
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


		private final String lightGreen = "#92D050";
		private final String brightGreen = "#00FF00";
		private final String darkGreen = "#008800";
		private final String lightRed = "#F37A7A";
		private final String brightRed = "#FF0000";
		private final String darkRed = "#990000";
		private final String lightYellow = "#FDED72";
		private final String brown = "#996633";
		private final String purple = "#CC55FF";
		private final String lightBlue = "#67A4FF";
		private final String darkBlue = "#0000FF";
		private final String magenta = "#FF00FF";

		private String passedStatusColor = lightGreen;
		private String failedStatusColor = lightRed;
		private String skippedStatusColor = lightYellow;
		private String totalStatusColor = lightBlue;
		private String runtimeStatusColor = lightYellow;

		private String passedStatusText = "PASSED";
		private String failedStatusText = "FAILED";
		private String skippedStatusText = "SKIPPED";

		//true = Show Test Runtimes in Charts instead of Passes and Failures
		private boolean chartDataType = false;

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
				chartDataType = formData.getBoolean("chartDataType");
				
				passedStatusColor = formData.getString("passedStatusColor");
				failedStatusColor = formData.getString("failedStatusColor");
				skippedStatusColor = formData.getString("skippedStatusColor");
				totalStatusColor = formData.getString("totalStatusColor");
				runtimeStatusColor = formData.getString("runtimeStatusColor");

				passedStatusText = formData.getString("passedStatusText");
				failedStatusText = formData.getString("failedStatusText");
				skippedStatusText = formData.getString("skippedStatusText");
			} catch(Exception e) {
				e.printStackTrace();
			}
			save();
			return false;
		}

		/**
		 *  @brief accessor methods used by global.jelly and TestResultsAnalyzerAction.java
		 */
		public String getNoOfBuilds() {
			return noOfBuilds;
		}

		public boolean getShowAllBuilds() {
			return showAllBuilds;
		}

		public boolean getShowLineGraph() {
			return showLineGraph;
		}

		public boolean getShowBarGraph() {
			return showBarGraph;
		}

		public boolean getShowPieGraph() {
			return showPieGraph;
		}

		public boolean getShowBuildTime() {
			return showBuildTime;
		}

		public String getRunTimeLowThreshold() {
			return runTimeLowThreshold;
		}

		public String getRunTimeHighThreshold() {
			return runTimeHighThreshold;
		}

		public boolean getChartDataType() {
			return chartDataType;
		}

		public String getPassedStatusColor() {
			return passedStatusColor;
		}

		public String getFailedStatusColor() {
			return failedStatusColor;
		}

		public String getSkippedStatusColor() {
			return skippedStatusColor;
		}

		public String getTotalStatusColor() {
			return totalStatusColor;
		}

		public String getRuntimeStatusColor() {
			return runtimeStatusColor;
		}

		public String getPassedStatusText() {
			return passedStatusText;
		}

		public String getFailedStatusText() {
			return failedStatusText;
		}

		public String getSkippedStatusText() {
			return skippedStatusText;
		}

		public String getLightGreen() {
			return lightGreen;
		}

		public String getBrightGreen() {
			return brightGreen;
		}

		public String getDarkGreen() {
			return darkGreen;
		}

		public String getLightRed() {
			return lightRed;
		}

		public String getBrightRed() {
			return brightRed;
		}

		public String getDarkRed() {
			return darkRed;
		}

		public String getLightYellow() {
			return lightYellow;
		}

		public String getBrown() {
			return brown;
		}

		public String getPurple() {
			return purple;
		}

		public String getLightBlue() {
			return lightBlue;
		}

		public String getDarkBlue() {
			return darkBlue;
		}

		public String getMagenta() {
			return magenta;
		}
	}
}

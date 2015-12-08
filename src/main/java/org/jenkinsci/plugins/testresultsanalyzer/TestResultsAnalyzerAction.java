package org.jenkinsci.plugins.testresultsanalyzer;

import hudson.model.Action;
import hudson.model.Item;
import hudson.model.AbstractProject;
import hudson.model.Actionable;
import hudson.model.Run;
import hudson.tasks.test.TabulatedResult;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import hudson.util.RunList;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jenkinsci.plugins.testresultsanalyzer.result.info.ResultInfo;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.ResultData;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.ClassInfo;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.PackageInfo;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.ResultInfo;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.TestCaseInfo;
import org.kohsuke.stapler.bind.JavaScriptMethod;

public class TestResultsAnalyzerAction extends Actionable implements Action {

	@SuppressWarnings("rawtypes")
	AbstractProject project;
	private List<Integer> builds = new ArrayList<Integer>();
	private List <String> userInBuildChange = new ArrayList<String>();
	private Vector<Integer> compileFailedBuilds = new Vector<Integer>();

	ResultInfo resultInfo;
	List <String> userString;

	public TestResultsAnalyzerAction(@SuppressWarnings("rawtypes")
	AbstractProject project) {
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
		return this.hasPermission() ? Constants.ICONFILENAME : null;
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
	public AbstractProject getProject() {
		return this.project;
	}

	@JavaScriptMethod
	public JSONArray getNoOfBuilds(String noOfbuildsNeeded) {
		JSONArray jsonArray;
		int noOfBuilds = getNoOfBuildRequired(noOfbuildsNeeded);

		jsonArray = getBuildsArray(getBuildList(noOfBuilds, "no"));

		return jsonArray;
	}

	private JSONArray getBuildsArray(List<Integer> buildList) {
		JSONArray jsonArray = new JSONArray();
		for (Integer build : buildList) {
			jsonArray.add(build);
		}
		return jsonArray;
	}

	private List<Integer> getBuildList(int noOfBuilds, String showCompileFail) {
		boolean showFail = showCompileFail.equals("show");

		if ((noOfBuilds <= 0) || (noOfBuilds >= builds.size())) {
			return builds;
		}
		List<Integer> buildList = new ArrayList<Integer>();
		for (int i = (noOfBuilds - 1); i >= 0; i--) {
			int index = builds.get(i);
			if(showFail || !(compileFailedBuilds.contains(index)))
				buildList.add(index);
		}
		Collections.reverse(buildList);
		return buildList;
	}

	public List<String> getUsersList(List<Integer> buildList) {
		userString = new ArrayList<String>();

		for(int position=0; position<buildList.size(); position++) {
			userString.add(userInBuildChange.get(position));
		}
		return userString;
	}

	private int getNoOfBuildRequired(String noOfbuildsNeeded) {
		int noOfBuilds;
		try {
			noOfBuilds = Integer.parseInt(noOfbuildsNeeded);
		}
		catch (NumberFormatException e) {
			noOfBuilds = -1;
		}
		return noOfBuilds;
	}

	public boolean isUpdated() {
		int latestBuildNumber = project.getLastBuild().getNumber();
		return !(builds.contains(latestBuildNumber));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void getJsonLoadData() {
		if (isUpdated()) {
			resultInfo = new ResultInfo();
			builds = new ArrayList<Integer>();
			RunList<Run> runs = project.getBuilds();
			Iterator<Run> runIterator = runs.iterator();
			while (runIterator.hasNext()) {
				Run run = runIterator.next();
				int buildNumber = run.getNumber();
				builds.add(run.getNumber());
				List<AbstractTestResultAction> testActions = run.getActions(hudson.tasks.test.AbstractTestResultAction.class);
				for (hudson.tasks.test.AbstractTestResultAction testAction : testActions) {
					TabulatedResult testResult = (TabulatedResult) testAction.getResult();
					Collection<? extends TestResult> packageResults = testResult.getChildren();
					for (TestResult packageResult : packageResults) { // packageresult
						resultInfo.addPackage(buildNumber, (TabulatedResult) packageResult);
					}
				}
			}
			//check whether the username is Null, and set it to the old username
			for(int i=0; i < userInBuildChange.size(); i++) {
				updateEmptyUser(i);
			}
		}
	}

    @JavaScriptMethod
    public JSONObject getTreeResult(String noOfBuildsNeeded, String showCompileFail) {
        int noOfBuilds = getNoOfBuildRequired(noOfBuildsNeeded);
        List<Integer> buildList = getBuildList(noOfBuilds, showCompileFail);
        getUsersList(buildList);
        JsTreeUtil jsTreeUtils = new JsTreeUtil();
        return jsTreeUtils.getJsTree(buildList, resultInfo, userString);
    }
	
	@JavaScriptMethod
    public String getExportCSV(String timeBased) {
		boolean isTimeBased = Boolean.parseBoolean(timeBased);
        Map<String, PackageInfo> packageResults = resultInfo.getPackageResults();
        String buildsString = "";
        for (int i = 0; i < builds.size(); i++) {
            buildsString += ",\"" + Integer.toString(builds.get(i)) + "\"";
        }		
        String header = "\"Package\",\"Class\",\"Test\"";
        header += buildsString;
        String export = header + System.lineSeparator();
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.CEILING);
        for (PackageInfo pInfo : packageResults.values()) {
            String packageName = pInfo.getName();
            //loop the classes
            for (ClassInfo cInfo : pInfo.getClasses().values()) {
                String className = cInfo.getName();
                //loop the tests
                for (TestCaseInfo tInfo : cInfo.getTests().values()) {
                    String testName = tInfo.getName();
                    export += "\""+ packageName + "\",\"" + className + "\",\"" + testName+"\"";
                    for (ResultData buildResult : tInfo.getBuildPackageResults().values()) {
						if(!isTimeBased) {
							export += ",\"" + buildResult.getStatus() + "\"";
						} else {
							export += ",\"" + df.format(buildResult.getTotalTimeTaken()) + "\"";
						}
                    }
                    export += System.lineSeparator();
                }
            }
        }
        return export;
    }

	@JavaScriptMethod
	public int getTotalNoOfBuilds() {
		return builds.size();
	}

	//if the username is NULL, go back to history, find the first build with non-Null username
	private void updateEmptyUser(int i) {
		//if the username is Null
		if(userInBuildChange.get(i).equals("")) {
			//go back to history
			for(int j = i; j < userInBuildChange.size(); j++) {
				//if found the first one with non-Null username
				if(!(userInBuildChange.get(j).equals(""))) {
					userInBuildChange.set(i, userInBuildChange.get(j));
					break;
				}
			}
		} else {
			return;
		}
	}

	public String getNoOfBuilds() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getNoOfBuilds();
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

	public boolean getChartDataType() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getChartDataType();
	}

	public String getRunTimeLowThreshold() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getRunTimeLowThreshold();
	}

	public String getRunTimeHighThreshold() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getRunTimeHighThreshold();
	}

	public String getPassedStatusColor() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getPassedStatusColor();
	}

	public String getFailedStatusColor() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getFailedStatusColor();
	}

	public String getSkippedStatusColor() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getSkippedStatusColor();
	}

	public String getTotalStatusColor() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getTotalStatusColor();
	}

	public String getRuntimeStatusColor() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getRuntimeStatusColor();
	}

	public String getPassedStatusText() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getPassedStatusText();
	}

	public String getFailedStatusText() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getFailedStatusText();
	}

	public String getSkippedStatusText() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getSkippedStatusText();
	}

	public String getLightGreen() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getLightGreen();
	}

	public String getDarkGreen() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getDarkGreen();
	}

	public String getLightRed() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getLightRed();
	}

	public String getBrightRed() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getBrightRed();
	}

	public String getDarkRed() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getDarkRed();
	}

	public String getLightYellow() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getLightYellow();
	}

	public String getBrown() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getBrown();
	}

	public String getPurple() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getPurple();
	}

	public String getLightBlue() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getLightBlue();
	}

	public String getDarkBlue() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getDarkBlue();
	}

	public String getMagenta() {
		return TestResultsAnalyzerExtension.DESCRIPTOR.getMagenta();
	}
}

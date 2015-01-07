package org.jenkinsci.plugins.testresultsanalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;

import net.sf.json.JSONObject;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.ResultInfo;
import org.kohsuke.stapler.bind.JavaScriptMethod;


import jenkins.model.Jenkins;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.AbstractProject;
import hudson.model.Actionable;
import hudson.model.Run;
import hudson.security.Permission;
import hudson.tasks.junit.PackageResult;
import hudson.tasks.junit.TestResult;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.util.RunList;

public class TestResultsAnalyzerAction extends Actionable implements Action{
@SuppressWarnings("rawtypes") AbstractProject project;
	private List<Integer> builds = new ArrayList<Integer>() ;
	
	ResultInfo resultInfo;
	
	
	public TestResultsAnalyzerAction(@SuppressWarnings("rawtypes") AbstractProject project){
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
	public AbstractProject getProject(){
    	return this.project;
    }
    
   
	@JavaScriptMethod
	public JSONArray getNoOfBuilds(String noOfbuildsNeeded) {
		JSONArray jsonArray;
		int noOfBuilds = getNoOfBuildRequired(noOfbuildsNeeded);

		jsonArray = getBuildsArray(getBuildList(noOfBuilds));

		return jsonArray;
	}

	private JSONArray getBuildsArray(List<Integer> buildList) {
		JSONArray jsonArray = new JSONArray();
		for (Integer build : buildList) {
			jsonArray.add(build);
		}
		return jsonArray;
	}

	private List<Integer> getBuildList(int noOfBuilds) {
		if ((noOfBuilds <= 0) || (noOfBuilds >= builds.size())) {
			return builds;
		}
		List<Integer> buildList = new ArrayList<Integer>();
		for (int i = (noOfBuilds - 1); i >= 0; i--) {
			buildList.add(builds.get(i));
		}
		return buildList;
	}
	
	private int getNoOfBuildRequired(String noOfbuildsNeeded){
		int noOfBuilds;
		try {
			noOfBuilds = Integer.parseInt(noOfbuildsNeeded);
		} catch (NumberFormatException e) {
			noOfBuilds = -1;
		}
		return noOfBuilds;
	}
    
	public boolean isUpdated(){
		int latestBuildNumber = project.getLastBuild().getNumber();
		return !(builds.contains(latestBuildNumber));
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
					TestResult testResult = (TestResult) testAction.getResult();
					Collection<PackageResult> packageResults = testResult.getChildren();
					for (PackageResult packageResult : packageResults) { // packageresult
						resultInfo.addPackage(buildNumber, packageResult);						
					}
				}
			}
		}
	}
    @JavaScriptMethod
    public JSONObject getTreeResult(String noOfBuildsNeeded) {
        int noOfBuilds = getNoOfBuildRequired(noOfBuildsNeeded);
        List<Integer> buildList = getBuildList(noOfBuilds);

        JsTreeUtil jsTreeUtils = new JsTreeUtil();
        return jsTreeUtils.getJsTree(buildList, resultInfo);
    }
}

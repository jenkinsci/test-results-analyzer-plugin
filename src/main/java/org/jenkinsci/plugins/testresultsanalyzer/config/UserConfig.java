package org.jenkinsci.plugins.testresultsanalyzer.config;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Created by vmenon on 3/17/2016.
 */
public class UserConfig {
    private boolean hideConfigMethods = false;

    private String noOfBuildsNeeded;
    private String hideNATestsThreshold;

    @DataBoundConstructor
    public UserConfig(String noOfBuildsNeeded, boolean hideConfigMethods) {
        this.noOfBuildsNeeded = noOfBuildsNeeded;
        this.hideConfigMethods = hideConfigMethods;
    }

    public boolean isHideConfigMethods() {
        return hideConfigMethods;
    }

    public void setHideConfigMethods(boolean hideConfigMethods) {
        this.hideConfigMethods = hideConfigMethods;
    }

    public String getNoOfBuildsNeeded() {
        return noOfBuildsNeeded;
    }

    public void setNoOfBuildsNeeded(String noOfBuildsNeeded) {
        this.noOfBuildsNeeded = noOfBuildsNeeded;
    }

    public String getHideNATestsThreshold() {
        return hideNATestsThreshold;
    }

    @DataBoundSetter
    public void setHideNATestsThreshold(String hideNATestsThreshold) {
        this.hideNATestsThreshold = hideNATestsThreshold;
    }
}

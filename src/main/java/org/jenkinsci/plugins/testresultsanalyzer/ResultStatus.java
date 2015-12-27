package org.jenkinsci.plugins.testresultsanalyzer;

/**
 * Created by vmenon on 12/27/2015.
 */
public enum ResultStatus {
    PASSED("PASSED"),FAILED("FAILED"),SKIPPED("SKIPPED"),NA("N/A");

    private String value;
    private ResultStatus(String value){
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}

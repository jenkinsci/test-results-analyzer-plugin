package org.jenkinsci.plugins.testresultsanalyzer;

/**
 * How to show a test that was executed more than once in a single build, for instance by a retry mechanism or
 * by parameterized fixtures whose cases share a class and name.
 */
public enum DuplicateTestPolicy {
    /** The test failed if any of its executions failed. */
    FAILED_IF_ANY_FAILED("Failed if any execution failed", ResultStatus.FAILED, ResultStatus.PASSED),
    /** The test passed if any of its executions passed, as when failing tests are retried until they pass. */
    PASSED_IF_ANY_PASSED("Passed if any execution passed", ResultStatus.PASSED, ResultStatus.FAILED);

    public static final DuplicateTestPolicy DEFAULT = FAILED_IF_ANY_FAILED;

    private final String displayName;
    /** Statuses in order of precedence, any other status (skipped) coming last. */
    private final ResultStatus[] precedence;

    DuplicateTestPolicy(String displayName, ResultStatus... precedence) {
        this.displayName = displayName;
        this.precedence = precedence;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return whether an execution with status {@code candidate} should represent the test rather than one with
     *     status {@code current}; executions with the same status keep the current one
     */
    public boolean prefers(String candidate, String current) {
        return rank(candidate) < rank(current);
    }

    private int rank(String status) {
        for (int i = 0; i < precedence.length; i++) {
            if (precedence[i].getValue().equals(status)) {
                return i;
            }
        }
        return precedence.length;
    }
}

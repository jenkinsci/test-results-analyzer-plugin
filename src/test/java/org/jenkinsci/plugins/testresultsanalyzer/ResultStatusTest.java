package org.jenkinsci.plugins.testresultsanalyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ResultStatusTest {

    @Test
    void testGetValue() {
        assertEquals("PASSED", ResultStatus.PASSED.getValue());
        assertEquals("FAILED", ResultStatus.FAILED.getValue());
        assertEquals("SKIPPED", ResultStatus.SKIPPED.getValue());
        assertEquals("N/A", ResultStatus.NA.getValue());
    }
}

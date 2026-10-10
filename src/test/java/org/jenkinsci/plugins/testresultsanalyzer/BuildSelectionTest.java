package org.jenkinsci.plugins.testresultsanalyzer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class BuildSelectionTest {

    @Test
    void parsesNumbersAndRangesNewestFirst() {
        assertThat(BuildSelection.parse("12,36,40-43"), contains(43, 42, 41, 40, 36, 12));
        assertThat(BuildSelection.parse(" 53 , 42,36 ,12 "), contains(53, 42, 36, 12));
    }

    @Test
    void ignoresDuplicatesBlankEntriesAndReversedRanges() {
        assertThat(BuildSelection.parse("5-3, 4, ,3,"), contains(5, 4, 3));
        assertThat(BuildSelection.parse("7 - 7"), contains(7));
    }

    @Test
    void rejectsAnythingElse() {
        for (String spec : new String[] {
            null,
            "",
            " ",
            ",,",
            "abc",
            "1;2",
            "-5",
            "5-",
            "1-2-3",
            "0",
            "0-3",
            "1.5",
            "+3",
            "9999999999",
            "1e3",
            "１２",
            "lastBuild"
        }) {
            assertThrows(IllegalArgumentException.class, () -> BuildSelection.parse(spec), String.valueOf(spec));
        }
    }

    @Test
    void limitsHowManyBuildsCanBeChosen() {
        List<Integer> most = BuildSelection.parse("1-" + BuildSelection.MAX_BUILDS);
        assertThat(most, hasSize(BuildSelection.MAX_BUILDS));
        IllegalArgumentException tooMany =
                assertThrows(IllegalArgumentException.class, () -> BuildSelection.parse("1-999999999"));
        assertThat(tooMany.getMessage(), containsString("At most"));
        // Overlapping ranges count each time, so the work done is bounded by the input
        assertThrows(
                IllegalArgumentException.class,
                () -> BuildSelection.parse("1-" + BuildSelection.MAX_BUILDS + ",1-" + BuildSelection.MAX_BUILDS));
        assertThrows(
                IllegalArgumentException.class,
                () -> BuildSelection.parse("1,".repeat(BuildSelection.MAX_LENGTH / 2 + 1)));
    }
}

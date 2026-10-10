package org.jenkinsci.plugins.testresultsanalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a list of build numbers and ranges such as {@code 12, 36, 40-53}, for comparing chosen builds
 * rather than the latest ones.
 */
final class BuildSelection {

    /** Longest specification accepted, in characters. */
    static final int MAX_LENGTH = 1000;

    /** Most build numbers a specification may cover, counting every number in each range. */
    static final int MAX_BUILDS = 10_000;

    /** A build number or range; only ASCII digits, spaces and tabs, matching the analyzer page's check. */
    private static final Pattern TOKEN = Pattern.compile("[ \\t]*(\\d{1,9})(?:[ \\t]*-[ \\t]*(\\d{1,9}))?[ \\t]*");

    private static final Pattern BLANK = Pattern.compile("[ \\t]*");

    private BuildSelection() {}

    /**
     * @param spec comma separated build numbers and inclusive ranges, in any order; blank entries are ignored
     * @return the distinct build numbers covered, newest (highest) first
     * @throws IllegalArgumentException with a message for the user when the specification is not valid
     */
    static List<Integer> parse(String spec) {
        return parse(spec, MAX_BUILDS);
    }

    /**
     * The most builds that may be chosen, given the administrator's limit on the runs to fetch.
     *
     * @param noOfRunsToFetch the global limit, or a non-positive number for none
     */
    static int maxBuilds(int noOfRunsToFetch) {
        return noOfRunsToFetch > 0 ? Math.min(noOfRunsToFetch, MAX_BUILDS) : MAX_BUILDS;
    }

    /**
     * @param spec comma separated build numbers and inclusive ranges, in any order; blank entries are ignored
     * @param maxBuilds most build numbers the specification may cover, see {@link #maxBuilds(int)}
     * @return the distinct build numbers covered, newest (highest) first
     * @throws IllegalArgumentException with a message for the user when the specification is not valid
     */
    static List<Integer> parse(String spec, int maxBuilds) {
        if (spec == null) {
            throw new IllegalArgumentException("No build numbers were given");
        }
        if (spec.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("The build numbers must be at most " + MAX_LENGTH + " characters");
        }
        TreeSet<Integer> numbers = new TreeSet<>(Collections.reverseOrder());
        long covered = 0;
        for (String token : spec.split(",", -1)) {
            if (BLANK.matcher(token).matches()) {
                continue;
            }
            Matcher matcher = TOKEN.matcher(token);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(
                        "'" + token.strip() + "' is not a build number or a range of build numbers such as 40-53");
            }
            int from = Integer.parseInt(matcher.group(1));
            int to = matcher.group(2) == null ? from : Integer.parseInt(matcher.group(2));
            if (from > to) {
                int swap = from;
                from = to;
                to = swap;
            }
            if (from < 1) {
                throw new IllegalArgumentException("Build numbers start at 1");
            }
            covered += (long) to - from + 1;
            if (covered > maxBuilds) {
                throw new IllegalArgumentException("At most " + maxBuilds + " builds can be chosen");
            }
            for (int number = from; number <= to; number++) {
                numbers.add(number);
            }
        }
        if (numbers.isEmpty()) {
            throw new IllegalArgumentException("No build numbers were given");
        }
        return new ArrayList<>(numbers);
    }
}

package org.jenkinsci.plugins.testresultsanalyzer.result.info;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * What the analyzer page shows about a build besides its number, so builds can be labelled by name or date.
 *
 * @param number the build number
 * @param displayName the display name of the build, {@code #number} unless it was changed
 * @param timestamp when the build was scheduled, in milliseconds since the epoch
 * @param url the absolute URL of the build
 */
public record BuildInfo(
        int number,
        @CheckForNull String displayName,
        long timestamp,
        @CheckForNull String url) {}

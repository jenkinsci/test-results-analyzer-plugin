export default function (buildResults) {
    var buildsPassed = 0;
    var buildsFailed = 0;
    var testsPassed = 0;
    var testsFailed = 0;

    var buildResultsLength = buildResults.length;
    for (var i = 0; i < buildResultsLength; ++i) {
        if (buildResults[i].status == "N/A") {
            continue;
        }

        if (buildResults[i].totalFailed > 0) {
            ++buildsFailed;
        } else if (buildResults[i].totalPassed > 0) {
            ++buildsPassed;
        }

        testsPassed += buildResults[i].totalPassed;
        testsFailed += buildResults[i].totalFailed;
    }

    var totalBuilds = buildsPassed + buildsFailed;
    var totalTests = testsPassed + testsFailed;
    if (totalBuilds == 0 || totalTests == 0) {
        return "N/A";
    }

    return Math.round(100.0 * buildsPassed / totalBuilds).toString() +
    "% (" + Math.round(100.0 * testsPassed / totalTests) + "%)";
}

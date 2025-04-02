export default function (buildResults) {
    var hasPrevious = false;
    var peviousPassed = false;

    var result = 0;
    var it = buildResults.length;
    while (--it >= 0) {
        var build = buildResults[it];
        if (build.status == "N/A") {
            continue;
        }

        if (hasPrevious) {
            if (build.totalFailed > 0) {
                if (peviousPassed) {
                    ++result;
                    peviousPassed = false;
                }
            } else if (build.totalPassed > 0) {
                if (!peviousPassed) {
                    ++result;
                    peviousPassed = true;
                }
            }
        } else {
            if (build.totalFailed > 0) {
                hasPrevious = true;
                peviousPassed = false;
            } else if (build.totalPassed > 0) {
                hasPrevious = true;
                peviousPassed = true;
            }
        }
    }

    return result;
}

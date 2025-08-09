export default function (buildResults) {
    if (buildResults.length < 2) {
        return '';
    }

    if (buildResults[0].status == "FAILED" &&
    buildResults[1].status == "PASSED") {
        return 'icon icon-exclamation-sign';
    } else {
        return '';
    }
}

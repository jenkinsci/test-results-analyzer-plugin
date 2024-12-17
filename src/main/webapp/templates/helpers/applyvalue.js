export default function (status, totalTimeTaken) {
    if (displayValues == true){
        return isNaN(totalTimeTaken) ? 'N/A' : totalTimeTaken.toFixed(3) ;
    }else{
        var cs = "";
        switch (status) {
            case "FAILED":
                cs = customStatuses['FAILED'];
                break;
            case "PASSED":
                cs = customStatuses['PASSED'];
                break;
            case "SKIPPED":
                cs = customStatuses['SKIPPED'];
                break;
            case "N/A":
                cs = customStatuses['N/A'];
                break;
        }
        return cs;
    }
}

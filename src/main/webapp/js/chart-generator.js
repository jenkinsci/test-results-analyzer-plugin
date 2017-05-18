

var chartResult;

var statusColors = {
    "passed" :"#92D050",
    "failed" :"#F37A7A",
    "skipped" :"#FDED72",
    "total" :"#67A4F5",
    "runtime": "#FDED72",
    "na" :""
};
function generateChart(chartType) {
    var finalResult = {};

    if($j("#tree input[type='checkbox']").size() == 0) {
        $j("#linechart").html("No build data retrieved.  You may need to select a Module.");
        return;
    }
    if(reevaluateChartData){
        chartResult = getChartData(getSelectedRows(), chartType.type);
        reevaluateChartData = false;
    }
    resetCharts();

    if(chartType.type === "runtime") {
        console.log("showing runtimes");
    
        if (chartType.line) {
            generateRuntimeLineChart();
        }
        if (chartType.pie) {
            generateRuntimePieChart();
        }
    } else {
        if (chartType.line) {
            generateLineChart();
        }
    
        if (chartType.bar) {
            generateBarChart();
        }
    
        if (chartType.pie) {
            generatePieChart();
        }
    }
}

function resetCharts(){
    $j("#linechart").html("");
    $j("#barchart").html("");
    $j("#piechart").html("");
}

function generateLineChart(){
    var chartCategories = [];
    var chartData ={
        "Failed" : [],
        "Passed" : [],
        "Skipped" : [],
        "Total" : []
    };

    for(var key in chartResult) {
        if(chartResult.hasOwnProperty(key)){
            var buildResult = chartResult[key];
            chartCategories.push(key);
            chartData["Failed"].push(buildResult["Failed"]);
            chartData["Passed"].push(buildResult["Passed"]);
            chartData["Skipped"].push(buildResult["Skipped"]);
            chartData["Total"].push(buildResult["Total"]);
        }
    }
    $j(function () {$j("#linechart").highcharts(getLineChartConfig(chartCategories, chartData))});

}

function generateRuntimeLineChart() {
    var chartCategories = [];
    var chartData = {
        "Runtime": []
    };

    for (var key in chartResult) {
        if (chartResult.hasOwnProperty(key)) {
            var buildResult = chartResult[key];
            chartCategories.push(key);
            chartData["Runtime"].push(Math.round(eval(buildResult["Runtime"]) * 1000) / 1000);
        }
    }
    $j(function () {
        $j("#linechart").highcharts(getRuntimeLineChartConfig(chartCategories, chartData))
    });
}

function generateRuntimePieChart(inputData) {
    var pieChartResult = getChartData(getTestRows(), "runtime");
    var buildNumber = inputData == undefined ? Object.keys(pieChartResult).pop() : inputData;
    var resultTitle = "Tests runtime details for " + buildNumber;

    var slow = 0;
    var medi = 0;
    var fast = 0;

    var runtimeArray = pieChartResult[buildNumber]["RuntimeArray"];
    var lowThreshold = parseFloat(runtimeLowThreshold);
    var highThreshold = parseFloat(runtimeHighThreshold);

    runtimeArray.each(function (time) {
        if (time < lowThreshold) {
            fast++;
        } else if (time >= highThreshold) {
            slow++;
        } else {
            medi++;
        }
    });

    inputData = [
        ['fast', fast],
        ['slow', slow],
        ['medium', medi]
    ];
    $j("#piechart").highcharts(getRuntimePieChartConfig(inputData, resultTitle))
}

function getTestRows() {
    var $testRows = $j($j("#tree .table-row").filter(function (index, elem) {
        return !($j($j(elem).children().get(2)).children().length > 0);
    }));
    var isSomethingChecked = $j("#tree").find(":checked").length > 0;
    if (isSomethingChecked) {
        $testRows = $testRows.filter(function (index, elem) {
            return $j(elem).find(":checked").length > 0;
        });
    }
    return $testRows;
}

function getRuntimeLineChartConfig(chartCategories, chartData) {
    var seriesVar = [
        {
            name: 'Runtime',
            data: chartData["Runtime"]
        }
    ];
    var colorsVar = [statusColors["runtime"]];
    var clickFunc = {
        click: function (e) {
            generateRuntimePieChart(this.category);
        }
    };
    var titleVar = {
        text: 'Build Runtimes',
        x: -20
    };
    var yAxisVar = {
        title: {
            text: 'Runtime'
        },
        plotLines: [
            {
                value: 0,
                width: 1,
                color: '#808080'
            }
        ],
        floor: 0
    };
    var linechart = {
        title: titleVar,
        xAxis: {
            title: {
                text: 'Build number'
            },
            categories: chartCategories,
            allowDecimals: false
        },
        yAxis: yAxisVar,
        credits: {
            enabled: false
        },
        tooltip: {
            headerFormat: '<b>Build no: {point.x}</b><br>',
            valueSuffix: ' sec',
            shared: true,
            crosshairs: true
        },
        legend: {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'middle',
            borderWidth: 0
        },
        colors: colorsVar,
        plotOptions: {
            series: {
                cursor: 'pointer',
                point: {
                    events: clickFunc
                }
            }
        },
        series: seriesVar
    };
    return linechart;
}

function getRuntimePieChartConfig(inputData, resultTitle) {
    var pieChart = {
        chart: {
            plotBackgroundColor: null,
            plotBorderWidth: 1,
            plotShadow: false
        },
        credits: {
            enabled: false
        },
        title: {
            text: resultTitle ? resultTitle : 'Build details for all'
        },
        tooltip: {
            pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: true,
                    format: '<b>{point.name}</b>: {point.percentage:.1f} %',
                    style: {
                        color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                    }
                }
            }
        },
        colors: [
            statusColors["passed"],
            statusColors["failed"],
            statusColors["skipped"]
        ],
        series: [
            {
                type: 'pie',
                name: 'Build Detail',
                data: inputData
            }
        ]
    };
    return pieChart;
}

function generateBarChart(){
    var chartCategories = [];
    var chartData ={
        "Failed" : [],
        "Passed" : [],
        "Skipped" : []
    };

    for(var key in chartResult) {
        if(chartResult.hasOwnProperty(key)){
            var buildResult = chartResult[key];
            chartCategories.push(key);
            chartData["Failed"].push(buildResult["Failed"]);
            chartData["Passed"].push(buildResult["Passed"]);
            chartData["Skipped"].push(buildResult["Skipped"]);
        }
    }

    var barChartData = [
        {
            name: "Passed",
            data: chartData["Passed"]
        },
        {
            name: "Failed",
            data: chartData["Failed"]
        },
        {
            name: "Skipped",
            data: chartData["Skipped"]
        },
    ];
    $j(function () {$j("#barchart").highcharts(getBarChartConfig(chartCategories, barChartData))});
}

function generatePieChart(inputData, resultTitle){
    if(inputData == undefined){
        var total = 0;
        var passed = 0;
        var failed = 0;
        var skipped = 0;
        for(var key in chartResult) {
            if(chartResult.hasOwnProperty(key)){
                total++;
                var buildResult = chartResult[key];
                if(buildResult["Failed"]>0){
                    failed ++;
                } else if(buildResult["Passed"]>0) {
                    passed++;
                } else {
                    skipped ++;
                }
                inputData = calculatePercentage(passed,failed,skipped,total);
            }
        }
    }
    $j(function () {$j("#piechart").highcharts(getPieChartConfig(inputData,resultTitle))})
}

function getChartData(selectedRows, type) {
    var chartResult = {};
    var baseRows;

    if(selectedRows.size() > 0) {
        baseRows = selectedRows;
    } else {
        baseRows = $j('[hierarchyLevel="0"]');
    }

    $j.each(baseRows, function(index, baseRow) {
        var buildResults = $j(baseRow).find(".build-result.table-cell");
        $j.each(buildResults, function(index, buildResult){
            var jsonResult = $j.parseJSON($j(buildResult).attr("data-result"));
            var buildNumber = jsonResult["buildNumber"];

            if (type == "runtime") {
                var tempBuildResult = {
                    "Runtime" : jsonResult["totalTimeTaken"] ? jsonResult["totalTimeTaken"] : 0,
                    "RuntimeArray": jsonResult["totalTimeTaken"] !== undefined ? [jsonResult["totalTimeTaken"]] : []
                }
                if (chartResult[buildNumber]) {
                    var tempChartBuildResult = chartResult[buildNumber];
                    var result = {
                        "Runtime": tempChartBuildResult["Runtime"] + tempBuildResult["Runtime"],
                        "RuntimeArray": tempChartBuildResult["RuntimeArray"].concat(tempBuildResult["RuntimeArray"])
                    }
                    chartResult[buildNumber] = result;
                } else {
                    chartResult[buildNumber] = tempBuildResult;
                }
            } else {
                var tempBuildResult = {
                    "Failed" :   jsonResult["totalFailed"] ? jsonResult["totalFailed"] : 0,
                    "Skipped" :   jsonResult["totalSkipped"] ? jsonResult["totalSkipped"] : 0,
                    "Passed" :   jsonResult["totalPassed"] ? jsonResult["totalPassed"] : 0,
                    "Total" :   jsonResult["totalTests"] ? jsonResult["totalTests"] : 0
                };
                if(chartResult[buildNumber]) {
                    var tempChartBuildResult = chartResult[buildNumber];
                    var result = {
                        "Failed": tempChartBuildResult["Failed"] + tempBuildResult["Failed"],
                        "Skipped": tempChartBuildResult["Skipped"] + tempBuildResult["Skipped"],
                        "Passed": tempChartBuildResult["Passed"] + tempBuildResult["Passed"],
                        "Total": tempChartBuildResult["Total"] + tempBuildResult["Total"]
                    }
                    chartResult[buildNumber] = result;
                } else {
                    chartResult[buildNumber] = tempBuildResult;
                }
            }
        });
    });
    return chartResult;
}

function getSelectedRows(){
    var lastCheckedLevel = 2147483647; // large number, to avoid additional checks
    return $j(".table .table-row").filter(function (index, element) {
        var elementLevel = parseInt($j(element).attr("hierarchyLevel"));

        if ($j(element).find("input:checked").length == 0) {
            if (elementLevel >= lastCheckedLevel) {
                // reset level to ensure that a selection in a different branch has no effect on this branch
                lastCheckedLevel = 2147483647;
            }
            return false;
        }

        if (elementLevel <= lastCheckedLevel) {
            lastCheckedLevel = elementLevel;
            return true;
        } else {
            // Do not add element when an ancestor was already added.
            // This avoids accounting for tests multiple times.
            return false;
        }
    });
}

function getLineChartConfig(chartCategories, chartData){
    var linechart = {
        title: {
            text: 'Build Status',
            x: -20 //center
        },
        xAxis: {
            title: {
                text: 'Build number'
            },
            categories: chartCategories,
            allowDecimals: false
        },
        yAxis: {
            title: {
                text: 'No of tests'
            },
            plotLines: [{
                value: 0,
                width: 1,
                color: '#808080'
            }],
            allowDecimals: false,
            floor: 0
        },
        credits: {
            enabled: false
        },
        tooltip: {
            headerFormat: '<b>Build no: {point.x}</b><br>',
            shared: true,
            crosshairs: true
        },
        legend: {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'middle',
            borderWidth: 0
        },
        colors : [statusColors["passed"], statusColors["failed"], statusColors["skipped"],statusColors["total"]]
        ,
        plotOptions: {
            series: {
                cursor: 'pointer',
                point: {
                    events: {
                        click: function (e) {
                            var x1 = this.x;
                            var category = this.category;
                            var passed = this.series.chart.series[0].data[x1].y;
                            var failed = this.series.chart.series[1].data[x1].y;
                            var skipped = this.series.chart.series[2].data[x1].y;
                            var total = this.series.chart.series[3].data[x1].y;
                            var resultTitle = 'Build details for build: '+category;
                            generatePieChart(calculatePercentage(passed,failed,skipped,total),resultTitle);

                        }
                    }
                }
            }
        },
        series: [{
            name: 'Passed',
            data: chartData["Passed"]
            //color: '#24A516'
        }, {
            name: 'Failed',
            data: chartData["Failed"]
            //color: '#FD0505'
        }, {
            name: 'Skipped',
            data: chartData["Skipped"]
            //color: '#AEAEAE'
        }, {
            name: 'Total',
            data:  chartData["Total"]
        }]
    }

    return linechart;
}

function getBarChartConfig(chartCategories, chartData){
    var barchart = {
        chart: {
            type: 'bar'
        },
        title: {
            text: 'Build Status',
            x: -20 //center
        },
        xAxis: {
            title: {
                text: 'Build number'
            },
            categories: chartCategories,
            allowDecimals: false
        },
        yAxis: {
            title: {
                text: 'No of tests'
            },
            plotLines: [{
                value: 0,
                width: 1,
                color: '#808080'
            }],
            allowDecimals: false
        },
        colors : [statusColors["passed"], statusColors["failed"], statusColors["skipped"]],
        credits: {
            enabled: false
        },
        tooltip: {
            headerFormat: '<b>Build no: {point.x}</b><br>',
            shared: true,
            crosshairs: true
        },
        legend: {
            reversed: true
        },
        plotOptions: {
            series: {
                stacking: 'normal'
            }
        },
        series: chartData
    }

    return barchart;
}

function calculatePercentage(passed, failed, skipped, total){
    var failedPercentage = (failed * 100)/total;
    var skippedPercentage = (skipped * 100)/total;
    var passedPercentage = (passed * 100)/total;

    return [['Passed',   passedPercentage],
        ['Failed',       failedPercentage],
        ['Skipped',   skippedPercentage]];
}

function getPieChartConfig(inputData, resultTitle){
    var pieChart = {
        chart: {
            plotBackgroundColor: null,
            plotBorderWidth: 1,//null,
            plotShadow: false
        },
        credits: {
            enabled: false
        },
        title: {
            text: resultTitle ? resultTitle : 'Build details for all'
        },
        tooltip: {
            pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: true,
                    format: '<b>{point.name}</b>: {point.percentage:.1f} %',
                    style: {
                        color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                    }
                }
            }
        },
        colors : [statusColors["passed"], statusColors["failed"], statusColors["skipped"],statusColors["total"]],
        series: [{
            type: 'pie',
            name: 'Build Detail',
            data: inputData
        }]
    }
    return pieChart;
}
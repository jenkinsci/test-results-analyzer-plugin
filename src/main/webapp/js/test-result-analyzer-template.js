var tableContent = '<div class="table-row" name = "{{addName text}}" ' +
                         '{{#if hierarchyLevel}}' +
                            'hierarchyLevel="{{hierarchyLevel}}" style="display:none"' +
                         '{{else}}' +
                            'hierarchyLevel="0"' +
                         '{{/if}}' +
                   '>' +
    '\n' + '         ' +
    '\n' + '         ' +
    '\n' + '         <div class="table-cell"><input type="checkbox"/></div> ' +
    ' <div class="name row-heading table-cell" ' +
        '{{#if hierarchyLevel}}' +
            'style="padding-left:{{addspaces hierarchyLevel}}em;"' +
        '{{/if}}' +
    '>' +
        '{{#if children}}' +
            '<span class="icon icon-plus-sign" title="Show Children"></span> ' +
        '{{/if}}' +
        '<span class="{{failureIconWhenNecessary buildResults}}" title="New Failure" ></span>' +
        '&nbsp;{{text}}</span>' +
    '</div>' +
    '\n' + '<div class="table-cell" title="Builds (Tests)">{{percentPassed buildResults}}</div> ' +
    '\n' + '<div class="table-cell" title="Number of transitions from passed to failed and failed to passed.">{{numberTransitions buildResults}}</div> ' +
    '{{#each this.buildResults}}' +
    '\n' + '         <div class="table-cell build-result {{applystatus status}}" data-result=\'{{JSON2string this}}\' ' +
                          'title="Build {{buildNumber}}"><a href="{{url}}">{{applyvalue status totalTimeTaken}}</a></div>' +
    '{{/each}}' +
    '\n' + '</div>' +
    '{{#each children}}\n' +
    '\n' + '{{addHierarchy this ../hierarchyLevel}}' +
    '\n' + '{{> tableBodyTemplate this}}' +
    '{{/each}}';

var worstTestsTableContent = '<div class="worst-tests-table-row">' +
    '\n' + '         <div class="table-cell row-heading">{{k}}</div>' +
    '\n' + '         <div class="table-cell build-result">{{v.length}}</div>' +
    '\n' + '         <div class="table-cell build-result">{{{buildLinks v}}}</div>' +
    '</div>'

var tableBody = '<div class="heading">' +
    '\n' + '        <div class="table-cell">Chart</div> ' +
    '<div class="table-cell">Package/Class/Testmethod</div>' +
    ' <div class="table-cell">Passed</div> ' +
    ' <div class="table-cell" title="Number of transitions from passed to failed and failed to passed.">Transitions</div> ' +
    '{{#each builds}}' +
    '\n' + '         <div class="table-cell" title="Build {{this}}">{{this}}</div>' +
    '{{/each}}' +
    '\n' + '      </div>' +
    '{{#each results}}' +
    '{{> tableBodyTemplate}}' +
    '\n' + '{{/each}}';

var worstTestsTableBody = '<h2 align="center">Top 10 Most Broken Tests</h2>' +
    '\n' + '{{#if this.length}}' +
    '<div class=table>' +
    '\n' + '<div class="heading">' +
    '\n' + '        <div class="table-cell">Test Name</div>' +
    '\n' + '        <div class="table-cell">Times Failed</div>' +
    '\n' + '        <div class="table-cell">Recent Failed Builds</div>' +
    '\n' + '      </div>' +
    '{{#each this}}' +
    '{{> worstTestsTableBodyTemplate}}' +
    '\n' + '{{/each}}' +
    '</div>' +
    '\n' + '{{else}}' +
    '\n' + '<p>There are no failing tests</p>' +
    '\n' + '{{/if}}';

function removeSpecialChars(name){
    var modName = "";
    //modName = name.split('.').join('_');
    modName = name.replace(/[^a-z\d/-]+/gi, "_");
    return modName;
}

Handlebars.registerPartial("tableBodyTemplate", tableContent);
Handlebars.registerPartial("worstTestsTableBodyTemplate", worstTestsTableContent);

Handlebars.registerHelper('JSON2string', function (object) {
    return JSON.stringify(object);
});

Handlebars.registerHelper('buildLinks', function (object) {
    return new Handlebars.SafeString($j.map(object.slice(0, 10), function( value, index ) {
       var url = Handlebars.escapeExpression(value.buildUrl),
           text = Handlebars.escapeExpression(value.buildNumber);
      return "<a href=" + url + ">" + text + "</a>";
    }).join(', '));
});


Handlebars.registerHelper('addName', function (name) {
    return removeSpecialChars(name);
});

Handlebars.registerHelper('applyvalue', function (status, totalTimeTaken) {
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
});


Handlebars.registerHelper('applystatus', function (status) {
    var statusClass = "no_status";
    switch (status) {
        case "FAILED":
            statusClass = "failed";
            break;
        case "PASSED":
            statusClass = "passed";
            break;
        case "SKIPPED":
            statusClass = "skipped";
            break;
    }
    return statusClass;
});

Handlebars.registerHelper('addspaces', function (hierarchyLevel) {
    var spaces = 1.5;

    spaces = spaces * hierarchyLevel;
    return new Handlebars.SafeString(spaces);
});

Handlebars.registerHelper('addIndent', function (hierarchyLevel) {
    var parent = "|"
    var ident = "-";
    for(var i =0;i<hierarchyLevel;i++){
        ident = ident + ident;
    }

    return new Handlebars.SafeString(parent+ident);
});

Handlebars.registerHelper('addHierarchy', function (context, parentHierarchy, options) {
    if (parentHierarchy == undefined)
        parentHierarchy = 0;
    context["hierarchyLevel"] = parentHierarchy + 1;
});

Handlebars.registerHelper('failureIconWhenNecessary', function (buildResults) {
    if (buildResults.length < 2) {
        return '';
    }

    if (buildResults[0].status == "FAILED" &&
        buildResults[1].status == "PASSED") {
        return 'icon icon-exclamation-sign';
    } else {
        return '';
    }
});

Handlebars.registerHelper('percentPassed', function (buildResults) {
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
});

Handlebars.registerHelper('numberTransitions', function (buildResults) {
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
});

var analyzerTemplate = Handlebars.compile(tableBody),
    analyzerWorstTestsTemplate = Handlebars.compile(worstTestsTableBody);

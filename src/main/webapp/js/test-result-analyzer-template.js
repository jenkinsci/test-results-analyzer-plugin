var tableContent = '<div class="table-row {{parentclass}}-{{addName text}}" parentclass= "{{parentclass}}" parentname="{{parentname}}" name = "{{addName text}}" {{#if isChild}} style="display:none"{{/if}}>' +
    '\n' + '         ' +
    '\n' + '         ' +
    '\n' + '         <div class="table-cell"><div class="icon icon-exclamation-sign" style="display:none" ></div></div>' +
    '\n' + '         <div class="table-cell"><input type="checkbox" parentclass= "{{parentclass}}" parentname="{{parentname}}" name = "checkbox-{{addName text}}" result-name = "{{addName text}}"/></div> ' +
    '<div class="children  table-cell" >  ' +
    '{{#if children}}' +
        '<div class="icon icon-plus-sign" ></div> ' +
    '{{/if}}</div>' +
    ' <div class="name row-heading table-cell" ' +
        '{{#if hierarchyLevel}}' +
            'style="padding-left:{{addspaces hierarchyLevel}}em;"' +
        '{{/if}}' +
        '>&nbsp;{{text}}</div>' +
    '' +
    '{{#each this.buildResults}}' +
    '\n' + '         <div class="table-cell build-result {{applystatus status}}" is-config="{{isConfig}}" data-result=\'{{JSON2string this}}\'><a href="{{url}}">{{applyvalue status totalTimeTaken}}</a></div>' +
    '{{/each}}' +
    '\n' + '</div>' +
    '{{#each children}}\n' +
    '\n' + '{{storeParent this "parentclass" ../parentclass ../text}}' +
    '\n' + '{{store this "parentname" ../text}}' +
    '\n' + '{{addHierarchy this ../hierarchyLevel}}' +
    '\n' + '{{store this "isChild" true}}' +
    '\n' + '{{> tableBodyTemplate this}}' +
    '{{/each}}';

var worstTestsTableContent = '<div class="worst-tests-table-row">' +
    '\n' + '         <div class="table-cell row-heading">{{k}}</div>' +
    '\n' + '         <div class="table-cell build-result">{{v.length}}</div>' +
    '\n' + '         <div class="table-cell build-result">{{{buildLinks v}}}</div>' +
    '</div>'

var tableBody = '<div class="heading">' +
    '\n' + '        <div class="table-cell" >New Failures</div>' +
    '\n' + '        <div class="table-cell">Chart</div><div class="table-cell">See children</div> <div class="table-cell">Build Number &rArr;<br>Package-Class-Testmethod names &dArr;</div>' +
    '{{#each builds}}' +
    '\n' + '         <div class="table-cell">{{this}}</div>' +
    '{{/each}}' +
    '\n' + '      </div>' +
    '{{#each results}}' +
    '{{store this "parentname" "base"}}' +
    '{{store this "parentclass" "base"}}' +
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
Handlebars.registerHelper('store', function (context, key, value, options) {
    if (key !== undefined && value != undefined) {
        context[key] = value;
    }
    return "";
});

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

Handlebars.registerHelper('storeParent', function (context, key, value1, value2, options) {
    if (value1 == undefined) {
        value1 = "";
    }
    if (value2 == undefined) {
        value2 = "";
    }

    if (key !== undefined) {
        context[key] = removeSpecialChars(value1) + "-" + removeSpecialChars(value2);
    }
    return "";
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

var analyzerTemplate = Handlebars.compile(tableBody);
var analyzerWorstTestsTemplate = Handlebars.compile(worstTestsTableBody);

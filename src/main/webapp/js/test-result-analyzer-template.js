var tableContent = '<div class="table-row {{parentclass}}-{{addName text}}" parentclass= "{{parentclass}}" parentname="{{parentname}}" name = "{{addName text}}" {{#if isChild}} style="display:none"{{/if}}>' +
    '\n' + '         ' +
    '\n' + '         ' +
    '\n' + '         <div class="table-cell"><input type="checkbox" parentclass= "{{parentclass}}" parentname="{{parentname}}" name = "checkbox-{{addName text}}" result-name = "{{addName text}}"/></div> ' +
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
    '' +
    '{{#each this.buildResults}}' +
    '\n' + '         <div class="table-cell build-result {{applystatus status}}" data-result=\'{{JSON2string this}}\'><a href="{{url}}">{{applyvalue status totalTimeTaken}}</a></div>' +
    '{{/each}}' +
    '\n' + '</div>' +
    '{{#each children}}\n' +
    '\n' + '{{storeParent this "parentclass" ../parentclass ../text}}' +
    '\n' + '{{store this "parentname" ../text}}' +
    '\n' + '{{addHierarchy this ../hierarchyLevel}}' +
    '\n' + '{{store this "isChild" true}}' +
    '\n' + '{{> tableBodyTemplate this}}' +
    '{{/each}}';

var tableBody = '<div class="heading">' +
    '\n' + '        <div class="table-cell">Chart</div> ' +
    '<div class="table-cell">Build Number &rArr;<br>Package-Class-Testmethod names &dArr;</div>' +
    '{{#each builds}}' +
    '\n' + '         <div class="table-cell">{{this}}</div>' +
    '{{/each}}' +
    '\n' + '      </div>' +
    '{{#each results}}' +
    '{{store this "parentname" "base"}}' +
    '{{store this "parentclass" "base"}}' +
    '{{> tableBodyTemplate}}' +
    '\n' + '{{/each}}';

function removeSpecialChars(name){
    var modName = "";
    //modName = name.split('.').join('_');
    modName = name.replace(/[^a-z\d/-]+/gi, "_");
    return modName;
}

Handlebars.registerPartial("tableBodyTemplate", tableContent);
Handlebars.registerHelper('store', function (context, key, value, options) {
    if (key !== undefined && value != undefined) {
        context[key] = value;
    }
    return "";
});

Handlebars.registerHelper('JSON2string', function (object) {
    return JSON.stringify(object);
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
var analyzerTemplate = Handlebars.compile(tableBody);
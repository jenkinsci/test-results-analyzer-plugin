var tableContent = '<div class="table-row {{parentclass}}-{{addName text}}" parentclass= "{{parentclass}}" parentname="{{parentname}}" name = "{{addName text}}" {{#if isChild}} style="display:none"{{/if}}>' +
    '\n' + '         ' +
    '\n' + '         ' +
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
    '\n' + '         <div class="table-cell build-result {{applystatus status}}" data-result=\'{{JSON2string this}}\'>{{status}}</div>' +
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
    '\n' + '        <div class="table-cell">Graph report selection</div><div class="table-cell">See children</div> <div class="table-cell">Build Number &rArr;<br>Package-Class-Testmethod names &dArr;</div>' +
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

Handlebars.registerHelper('applystatus', function (status) {
    var statusClass = "";
    switch (status) {
        case "FAILED":
            statusClass = "failed";
            break;
        case "PASSED":
            statusClass = "passed";
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
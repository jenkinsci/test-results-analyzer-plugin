var tableContent = '<div class="table-row {{parentname}}" parentname="{{parentname}}" name = "{{addName text}}" {{#if isChild}} style="display:none"{{/if}}>' +
    '\n' + '         ' +
    '\n' + '         ' +
    '\n' + '         <div class="row-heading table-cell">' +
    '{{#if children}}' +
        '<div class="icon icon-plus-sign" ' +
            '{{#if hierarchyLevel}}' +
                'style="margin-left:{{addspaces hierarchyLevel}}em;>"' +
            '{{/if}}>' +
        '</div><div class="name">&nbsp;{{text}}</div>' +
    '{{else}} <div class="name" ' +
        '{{#if hierarchyLevel}}' +
            'style="margin-left:{{addspaces hierarchyLevel}}em;">&nbsp;{{text}}</div>' +
        '{{/if}}' +
    '{{/if}}</div>' +
    '{{#each this.buildResults}}' +
    '\n' + '         <div class="table-cell {{applystatus status}}" data-result=\'{{JSON2string this}}\'>{{status}}</div>' +
    '{{/each}}' +
    '\n' + '</div>' +
    '{{#each children}}\n' +
    '\n' + '{{storeParent this "parentname" ../parentname ../text}}' +
    '\n' + '{{addHierarchy this ../hierarchyLevel}}' +
    '\n' + '{{store this "isChild" true}}' +
    '\n' + '{{> tableBodyTemplate this}}' +
    '{{/each}}';

var tableBody = '<div class="heading">' +
    '\n' + '         <div class="table-cell">Build Number &rArr;<br>Package-Class-Testmethod names &dArr;</div>' +
    '{{#each builds}}' +
    '\n' + '         <div class="table-cell">{{this}}</div>' +
    '{{/each}}' +
    '\n' + '      </div>' +
    '{{#each results}}' +
    '{{store this "parentname" "base"}}' +
    '{{> tableBodyTemplate}}' +
    '\n' + '{{/each}}';

function removeSpecialChars(name){
    var modName = "";
    modName = name.split('.').join('_');
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

Handlebars.registerHelper('addHierarchy', function (context, parentHierarchy, options) {
    if (parentHierarchy == undefined)
        parentHierarchy = 0;
    context["hierarchyLevel"] = parentHierarchy + 1;
});

var analyzerTemplate = Handlebars.compile(tableBody);

function addEvents() {

    var toggleHandler = function (node) {
        var parent = $j(node).parent().parent(".table-row").attr("parentname");
        var nodeName = $j(node).parent().parent(".table-row").attr("name");
        var childNodeClass = (parent + "." + nodeName).replace(/\./g, "-").replace(/\s/g, "-");
        if ($j(node).hasClass('icon-plus-sign')) {
            $j(node).removeClass('icon-plus-sign');
            $j(node).addClass('icon-minus-sign');
            $j("." + childNodeClass).show();
        } else {
            $j(node).removeClass('icon-minus-sign');
            $j(node).addClass('icon-plus-sign');
            $j("." + childNodeClass).hide();
            hideChilds($j("." + childNodeClass));
        }
    };

    var hideChilds = function (childs) {
        childs.each(function () {
            var parent = $j(this).attr("parentname");
            var nodeName = $j(this).attr("name");
            var childNodeClass = (parent + "." + nodeName).replace(/\./g, "-").replace(/\s/g, "-");
            if ($j(this).find('.icon').hasClass('icon-minus-sign')) {
                $j(this).find('.icon').removeClass('icon-minus-sign');
                $j(this).find('.icon').addClass('icon-plus-sign');
            }
            var childElements = $j("." + childNodeClass);
            childElements.hide();
            hideChilds(childElements);
        });

    };

    $j(".table .table-row .icon").click(function () {
        toggleHandler(this);
    });
}
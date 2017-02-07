var colTemplate = "{'cellClass':'col1','value':'build20','header':'20','title':'20'}";
var treeMarkup = "";
var worstTestsMarkup = "";
var reevaluateChartData = true;
var displayValues = false;

function searchTests(){
    var table = $j(".test-history-table")[0];
    var rows = $j(table).find(".table-row");
    $j.each(rows, function(index, row){
        var searchStr = $j("#filter").val().toLowerCase();

        // If user has removed the filter text, revert to showing the most-high level packages
        if (searchStr == ""){
            if ($j(row).attr("parentname") == "base"){
                $j(row).css("display", "table-row");
            }
            else {
                $j(row).css("display", "none");
            }
        }
        // Filter tests by searchStr
        else {  

            var testCell = $j(row).find(".row-heading")[0];
            var rowText = $j(testCell).text().toLowerCase();
            if (rowText.indexOf(searchStr) == -1 ){
                $j(row).css("display", "none");
            }
            else {
                $j(row).css("display", "table-row");
            }
        }   
    });

    // If user has removed the filter text, 
    // but expanded some options during/before the search, 
    //they should remain expanded after the search

    var rows_to_expand = $j(table).find(".icon-minus-sign").parent().parent();  // expanded rows
    var searchStr = $j("#filter").val().toLowerCase();
    if (searchStr == "") {
        $j.each($j(rows_to_expand), function(index,row){
            // for every expanded test, all ancestor packages need to be expanded as well
            var parentclass = $j(row).attr("parentclass");
            var parent = $j("." + parentclass);
            var ancestor_arr = [];
            while (parentclass!="base" && $j(parent).find(".icon-plus-sign").length>0){
                ancestor_arr.push(parent);
                var parentclass = $j(parent).attr("parentclass");
                parent = $j("." + parentclass);
            }
            // Expand ancestors in top-down manner
            $j.each($j(ancestor_arr).get().reverse(),function(index, row){
                $j(row).find(".icon-plus-sign")[0].click();
            });

            // Make sure that it has been clicked
            if ($j(row).find(".icon-minus-sign").length!=0){
                $j(row).find(".icon-minus-sign")[0].click();
            }

            $j(row).find(".icon-plus-sign")[0].click();
            
            // Make sure that it has been clicked
            if ($j(row).find(".icon-plus-sign")[0]){
                $j(row).find(".icon-plus-sign")[0].click();
            }
            
        });
    }
}

function reset(){
    reevaluateChartData = true;
    $j(".test-history-table").html("")
    $j(".worst-tests-table").html("")
	treeMarkup = "";
	worstTestsMarkup = "";
    resetCharts();
}

function populateTemplate(){
    reset();
    displayValues  = $j("#show-build-durations").is(":checked");
    $j("#table-loading").show();
    remoteAction.getTreeResult(getUserConfig(),$j.proxy(function(t) {
        var itemsResponse = t.responseObject();
        treeMarkup = analyzerTemplate(itemsResponse);
        $j(".test-history-table").html(treeMarkup);
        var worstTests = getWorstTests(itemsResponse);
        worstTestsMarkup = analyzerWorstTestsTemplate(worstTests);
        $j(".worst-tests-table").html(worstTestsMarkup);
        addEvents();
        generateCharts();
        $j("#table-loading").hide();

        // Can this ever be defined?
        if (typeof hideConfigMethods !== "undefined") {
          hideConfigMethods();
        }
    },this));
}

function getUserConfig(){
    var userConfig = {};

    var noOfBuilds = "-1";

    if (!$j("#allnoofbuilds").is(":checked")) {
        noOfBuilds = $j("#noofbuilds").val();
    }
    userConfig["noOfBuildsNeeded"] = noOfBuilds;

    var hideConfig = $j("#hide-config-methods").is(":checked");
    userConfig["hideConfigMethods"] = hideConfig;
    return userConfig;
}

function collapseAll(){
    reevaluateChartData = true;
    $j(".test-history-table").html("")
    $j(".test-history-table").html(treeMarkup);
    $j(".worst-tests-table").html("");
    $j(".worst-tests-table").html(worstTestsMarkup);
    addEvents();
}

function expandAll(){
    reevaluateChartData = true;
	collapseAll();
	$j(".test-history-table .table-row .icon").each(function(){
		$j(this).click();
	});
	
}

function addEvents() {

    var toggleHandler = function (node) {
        var parent = $j(node).parent().parent(".table-row").attr("parentclass");
        var nodeName = $j(node).parent().parent(".table-row").attr("name");
        var childLocator = "[parentclass='" + parent + "-" + nodeName + "']";
        var childNodeClass = (parent + "." + nodeName).replace(/\./g, "-").replace(/\s/g, "-");
        if ($j(node).hasClass('icon-plus-sign')) {
            $j(node).removeClass('icon-plus-sign');
            $j(node).addClass('icon-minus-sign');
            $j(node).attr('title', 'Hide Children');
            $j(childLocator).show();
        } else if ($j(node).hasClass('icon-minus-sign')) {
            $j(node).removeClass('icon-minus-sign');
            $j(node).addClass('icon-plus-sign');
            $j(node).attr('title', 'Show Children');
            $j(childLocator).hide();
            hideChilds($j(childLocator));
        }
    };

    var hideChilds = function (childs) {
        childs.each(function () {
            var parent = $j(this).parent().parent(".table-row").attr("parentclass");
            var nodeName = $j(this).parent().parent(".table-row").attr("name");
            var childLocator = "[parentclass='" + parent + "-" + nodeName + "']";

            var icon = $j(this).find('.icon');
            if (icon.hasClass('icon-minus-sign')) {
                icon.removeClass('icon-minus-sign');
                icon.addClass('icon-plus-sign');
                icon.attr('title', 'Show Children');
            }
            var childElements = $j(childLocator);
            childElements.hide();
            hideChilds(childElements);
        });

    };

    $j(".test-history-table .table-row .icon").click(function () {
        toggleHandler(this);
    });
    checkBoxEvents();
}

function checkBoxEvents() {
    var table = $j(".test-history-table")[0];
    $j(table).find("input[type='checkbox']").change(function () {
        reevaluateChartData = true;
        if (this.checked) {
            checkChildren(this, true);
            checkParent(this);
        } else {
            checkChildren(this, false);
            checkParent(this);
        }
        generateCharts();
    });
}

function checkChildren(node, checked) {
    var parent = $j(node).attr("parentclass");
    var nodeName = $j(node).attr("result-name");
    var childLocator = "[parentclass='" + parent + "-" + nodeName + "']";
    var childElements = $j(childLocator);
    childElements.find("input[type='checkbox']").prop("checked", checked);
    childElements.each(function () {
        checkChildren(this, checked)
    });
}

function checkParent(node) {
    var parent = $j(node).attr("parentclass");
    var childLocator = "[parentclass='" + parent + "']";
    var childElements = $j(childLocator);
    var childCheckBoxes = childElements.find("input[type='checkbox']");
    var selectParent = true;
    childCheckBoxes.each(function () {
        if ($j(this).prop("checked") != true) {
            selectParent = false;
        }
    });
    var parentCheckBox = $j("." + parent).find("input[type='checkbox']");
    parentCheckBox.prop("checked", selectParent);
    if ((parentCheckBox.size() > 0) && ($j(parentCheckBox).attr("parentclass") != 'base')) {
        checkParent(parentCheckBox);
    }
}

function resetAdvancedOptions(){
    $j("#show-build-durations").prop('checked', false);
}

function getWorstTests(itemsResponse, range = 10) {
  worstTests = {}
  findChildren(itemsResponse);
  worstTests = $j.map(worstTests, function(v,k) { return {k, v}});
  worstTests.sort(function (a,b) { return compareInteger(b.v.length, a.v.length)});
  return worstTests.slice(0, range);
}

function compareInteger(integer1, integer2) {
  if (parseInt(integer1) > parseInt(integer2)) return 1;
  else if (parseInt(integer1) < parseInt(integer2)) return -1;
  else return 0;
}

function findChildren(hash, path = '') {
  if ( hash.text != undefined ) { path += (path == '') ? hash.text : '.' + hash.text }
  $j.each(hash, function( index, value ) {
    if ( index == 'children' && value.length > 0 ) {
      findChildren(value, path);
    } else if ( index == 'buildResults' ) {
      $j.each(value, function(index1, buildResult) {
        // if totalTests is equal to 1 then it should be at the lowest level of the itemsResponse hash
        if ((buildResult.status == 'FAILED') && (buildResult.totalTests == '1')) {
          if ( worstTests[path] === undefined ) { worstTests[path] = [] }
          worstTests[path].push({buildNumber: buildResult.buildNumber, buildUrl: buildResult.url});
        }
      });
    } else if ( $j.type(value) == 'object' ) {
      findChildren(value, path);
    } else if ( $j.isArray(value) ) {
      findChildren(value, path);
    }
  });
}

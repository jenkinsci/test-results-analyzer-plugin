var colTemplate = "{'cellClass':'col1','value':'build20','header':'20','title':'20'}";
var treeMarkup = "";
var reevaluateChartData = true;
var displayValues = false;

function newFailingTests(){
    var table_rows = $j(".table-row");
    var i;
    for (i=0;i<table_rows.length;i++){
        row = table_rows[i];
            row_cells = $j(row).find(".build-result");
            last_test = row_cells[0];
            if (!JSON.parse($j(last_test).attr("data-result"))["isPassed"] && row_cells.length>1){
                second_to_last = row_cells[1];
                if (JSON.parse($j(second_to_last).attr("data-result"))["isPassed"]){
                    var cell = $j(row).find(".icon-exclamation-sign")[0];
                    $j(cell).css("display","inline-block");
                }   
            }
    }
}

function searchTests(){
    var table = $j(".table")[0];
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
    $j(".table").html("")
	treeMarkup = "";
    resetCharts();
}

function populateTemplate(){
    reset();
    var noOfBuilds = "-1";

    if (!$j("#allnoofbuilds").is(":checked")) {
        noOfBuilds = $j("#noofbuilds").val();
    }
    displayValues  = $j("#show-build-durations").is(":checked");
    $j("#table-loading").show();
    remoteAction.getTreeResult(noOfBuilds,$j.proxy(function(t) {
        var itemsResponse = t.responseObject();
        treeMarkup = analyzerTemplate(itemsResponse);
        $j(".table").html(treeMarkup);
        addEvents();
        generateCharts();
        $j("#table-loading").hide();
    },this));
}

function collapseAll(){
    reevaluateChartData = true;
    $j(".table").html("")
    $j(".table").html(treeMarkup);
    addEvents();
}

function expandAll(){
    reevaluateChartData = true;
	collapseAll();
	$j(".table .table-row .icon").each(function(){
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
            $j(childLocator).show();
        } else {
            $j(node).removeClass('icon-minus-sign');
            $j(node).addClass('icon-plus-sign');
            $j(childLocator).hide();
            hideChilds($j(childLocator));
        }
    };

    var hideChilds = function (childs) {
        childs.each(function () {
            var parent = $j(this).parent().parent(".table-row").attr("parentclass");
            var nodeName = $j(this).parent().parent(".table-row").attr("name");
            var childLocator = "[parentclass='" + parent + "-" + nodeName + "']";

            if ($j(this).find('.icon').hasClass('icon-minus-sign')) {
                $j(this).find('.icon').removeClass('icon-minus-sign');
                $j(this).find('.icon').addClass('icon-plus-sign');
            }
            var childElements = $j(childLocator);
            childElements.hide();
            hideChilds(childElements);
        });

    };

    $j(".table .table-row .icon").click(function () {
        toggleHandler(this);
    });
    checkBoxEvents();
    newFailingTests();
}

function checkBoxEvents() {
    var table = $j(".table")[0];
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

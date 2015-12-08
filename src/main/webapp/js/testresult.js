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

function reset(){
    reevaluateChartData = true;
    $j(".table").html("")
	treeMarkup = "";
    resetCharts();
}

function populateTemplate(){
    reset();
    var noOfBuilds = $j('#noofbuilds').val();
    displayValues  = $j("#show-build-durations").is(":checked");

    remoteAction.getTreeResult(noOfBuilds,$j.proxy(function(t) {
        var itemsResponse = t.responseObject();
        treeMarkup = analyzerTemplate(itemsResponse);
        $j(".table").html(treeMarkup);
        addEvents();
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
    $j("input[type='checkbox']").change(function () {
            reevaluateChartData = true;
            if (this.checked) {
                checkChildren(this, true);
                checkParent(this);
                console.log("checked");
            } else {
                checkChildren(this, false);
                checkParent(this);
                console.log("unchecked");
            }
        }
    );
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

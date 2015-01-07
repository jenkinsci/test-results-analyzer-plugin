var colTemplate = "{'cellClass':'col1','value':'build20','header':'20','title':'20'}";
var treeMarkup = "";

function reset(){
    $j(".table").html("")
	treeMarkup = "";
}

function populateTemplate(){
    reset();
    var noOfBuilds = $j('#noofbuilds').val();
    remoteAction.getTreeResult(noOfBuilds,$j.proxy(function(t) {
        var itemsResponse = t.responseObject();
        treeMarkup = analyzerTemplate(itemsResponse);
        $j(".table").html(treeMarkup);
        addEvents();
    },this));
}

function collapseAll(){
    $j(".table").html("")
    $j(".table").html(treeMarkup);
    addEvents();
}

function expandAll(){
	collapseAll();
	$j(".table .table-row .icon").each(function(){
		$j(this).click();
	});
	
}



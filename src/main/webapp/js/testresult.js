var colTemplate = "{'cellClass':'col1','value':'build20','header':'20','title':'20'}";
var template = '{}';
var treeMarkup = "";

var treeColumns = [];
var even = true;

function getColumnDef(){
	var colDef = {};
	colDef.cellClass = "column";
	colDef.value = "";
	colDef.header = "";
	colDef.title = "";
	return colDef;	
}

function showdata(data) {
    console.log(data);
}

function constructNodes(node,parentName, level) {
	var testStatus = $('#teststatus').val();
    var self = this;
    var datacol = "";
	var tabSpace ="";
	var rowClass = parentName.replace(/\./g,"-");
	for(var i=0; i< (8*level); i++){
		tabSpace+="&nbsp;";
	}
	if(testStatus != "all") {
		if(node.buildStatuses.indexOf(testStatus) < 0){
			return;
		}
	}
    $.each(treeColumns, function (index, value) {
		var buildDetails;
		var status = "";
		var statusClass = "";
		var buildResult = "";
		if(node.data.hasOwnProperty(value.value)){
			buildDetails = node.data[value.value];
			status = buildDetails.status;
			if(status == "FAILED") {
				statusClass = "failed";
			} else if (status == "PASSED") {
				statusClass = "passed";
			} 
			//Build results is generated but not used. 
			// TODO: Implement a pop-up to show the buildresults
			buildResult = JSON.stringify(buildDetails);			
		}
        datacol += "<div class='cell builddetail "+value.cls+" "+ statusClass +"' >"+ status + "</div>";
    })
	treeMarkup +="<div class = 'group "+rowClass+"'";
    if(level>0){
		treeMarkup +=" style='display:none;'"
	}
	var evenOddClass = 'odd';
	if(this.even){
		evenOddClass = 'even';
		this.even = false;
	} else {
		this.even = true;
	}
	treeMarkup += "><div class='row "+evenOddClass+"'><div class='cell'>" + tabSpace;
	if (node.children && node.children.length > 0){
		treeMarkup += "<a href='#' parentname = '"+parentName+"' name='"+node.text+"'><div class='icon icon-plus-sign' /></a>"
	}
	treeMarkup +=" <div class='data'>"+ node.text + "</div></div>" + datacol + "  </div>\n";
    if (node.children && node.children.length > 0) {
        level++;
        $.each(node.children, function (index, value) {
            constructNodes(value,parentName+"."+node.text, level);
        })
    }
	treeMarkup +="</div>";
}
function addEvents(){

	$(".table .row .cell > a").click(function () {
		var image = $(this).find('.icon');
		if(image.hasClass('icon-minus-sign')){
			image.removeClass('icon-minus-sign');
		} else {
			image.addClass('icon-minus-sign');
		}
		var parent = $(this).attr("parentname");
		var nodeName = $(this).attr("name");
		var childNodeClass = (parent+"."+nodeName).replace(/\./g,"-");
		$("."+childNodeClass).toggle();
	});	
	
	/*$(".cell.builddetail").mouseover(function(){
		var buildDetails = $(this).find(".popup.builddetail");
		$(buildDetails).show();
	});
	$(".cell.builddetail").mouseout(function(){
		$('#builddetail').html("");
	});*/
}

function createTree(source){	
	treeMarkup = treeMarkup + "<div class='table'>\n<div class='heading'><div class='cell'>Build Number &rArr;<br/><br/>Package-Class-Testmethod names &dArr;</div>";
	$.each(source.columns, function(index, value){
		var column = {};
		column.header = value.header;
		column.cls = value.cellClass;
		column.value = value.value;
		treeColumns.push(column);
		treeMarkup = treeMarkup + "<div class='cell'>" + value.header + "</div>";
	});
		treeMarkup = treeMarkup + "</div>\n";
	$.each(source.children, function (index, value) {		
	    constructNodes(value,"base", 0)
	})
	treeMarkup = treeMarkup + "</div>";
	
	$('#tree').html(treeMarkup);
	this.addEvents();
}

function reset(){
	treeMarkup = "";
	treeColumns.clear();
}

function populate(){
	reset();
	var noOfBuilds = $('#noofbuilds').val();
	remoteAction.getNoOfBuilds(noOfBuilds,$.proxy(function(t) {
		var self= this;
	      var builds = t.responseObject();
	      var source = JSON.parse(template);
	      var cols = [];
			for(var i=0; i < builds.length; i++){
				var columnDefJson = getColumnDef();
				columnDefJson.value = "build"+builds[i].toString();
				columnDefJson.header = builds[i].toString();
				columnDefJson.title = builds[i].toString();
				cols.push(columnDefJson);			
			}
			source.columns = cols;
			
			remoteAction.getTreeResult(noOfBuilds,$.proxy(function(t) {
				var itemsResponse = t.responseObject();
				source.children = itemsResponse;
				self.createTree(source);
		    },self)
		    );
			
  },this));
	
	
}

function collapseAll(){
	$('#tree').html(treeMarkup);
	this.addEvents();	
}

function expandAll(){
	$('#tree').html(treeMarkup);
	this.addEvents();
	$(".table .row .cell > a").each(function(){
		$(this).click();
	});
	
}



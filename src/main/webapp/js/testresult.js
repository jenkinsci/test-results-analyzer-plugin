var colTemplate = "{'cellClass':'col1','value':'build20','header':'20','title':'20'}";
var reevaluateChartData = true;
var displayValues = false;

function clearedFilter(rows) {
    var levelsToShow = [0]; // stack to keep track of hierarchy

    // Ensure that every node is expanded, that was expanded before or by the user when filtering.
    $j(rows).each(function(index, row) {
        var rowLevel = parseInt($j(row).attr("hierarchyLevel"));

        // Remove all generations that happen after the current one, since these are not relevant anymore,
        // when we are at a (great*)uncle.
        while (levelsToShow[levelsToShow.length - 1] > rowLevel) {
            levelsToShow.pop();
        }

        if ($j(row).find(".icon-minus-sign").length > 0) {
            // also show children of this node
            levelsToShow.push(rowLevel + 1);
            $j(row).show();
        } else if (levelsToShow[levelsToShow.length - 1] == rowLevel) {
            $j(row).show();
        } else {
            $j(row).hide();
        }
    });
}

function applyFilter(rows, filter) {
    $j(rows).each(function(index, row) {
        var testCell = $j(row).find(".row-heading")[0];
        var rowText = $j(testCell).text().toLowerCase();
        if (rowText.indexOf(filter) == -1) {
            $j(row).hide();
        }
        else {
            $j(row).show();
        }
    });
}

function searchTests(){
    var rows = $j(".test-history-table .table-row");
    var filter = $j("#filter").val().toLowerCase();
    if (filter == "") {
        clearedFilter(rows);
    } else {
        applyFilter(rows, filter);
    }
}

function reset(){
    reevaluateChartData = true;
    $j(".test-history-table").html("");
    $j(".worst-tests-table").html("");
    resetCharts();
}

function populateTemplate(){
    reset();
    displayValues  = $j("#show-build-durations").is(":checked");
    $j("#table-loading").show();
    remoteAction.getTreeResult(getUserConfig(),$j.proxy(function(t) {
        var itemsResponse = t.responseObject();
        $j(".test-history-table").html(
            analyzerTemplate(itemsResponse)
        );
        var worstTests = getWorstTests(itemsResponse);
        $j(".worst-tests-table").html(
            analyzerWorstTestsTemplate(worstTests)
        );
        addEvents();
        generateCharts();
        $j("#table-loading").hide();
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

function changeToExpandedState(node) {
    if ($j(node).hasClass('icon-plus-sign')) {
        $j(node).removeClass('icon-plus-sign');
        $j(node).addClass('icon-minus-sign');
        $j(node).attr('title', 'Hide Children');
        return true;
    } else {
        return false;
    }
}

function changeToCollapsedState(node) {
    if ($j(node).hasClass('icon-minus-sign')) {
        $j(node).removeClass('icon-minus-sign');
        $j(node).addClass('icon-plus-sign');
        $j(node).attr('title', 'Show Children');
        return true;
    } else {
        return false;
    }
}

function collapseAll(){
    reevaluateChartData = true;
    $j(".test-history-table .icon-minus-sign").each(function() {
        changeToCollapsedState($j(this));
    });
    $j(".test-history-table .table-row").each(function (index, element) {
        var elementLevel = parseInt($j(element).attr("hierarchyLevel"));
        if (elementLevel == 0) {
            $j(element).show();
        } else {
            $j(element).hide();
        }
    });
}

function expandAll() {
    reevaluateChartData = true;
    $j(".test-history-table .icon-plus-sign").each(function() {
        changeToExpandedState($j(this));
    });
    $j(".test-history-table .table-row").show();
}

function getDescendants(parentRow, level) {
    var parentLevel = parseInt($(parentRow).attr("hierarchyLevel"));
    var descendantLevel = parentLevel + level;
    var done = false;

    return $j(parentRow).nextAll().filter(function(index, element) {
        if (done) {
          return false;
        }

        var elementLevel = parseInt($j(element).attr("hierarchyLevel"));
        if (parentLevel >= elementLevel) {
          // not a descendant, done
          done = true;
          return false;
        } else if (level == -1 || elementLevel <= descendantLevel) {
          return true;
        } else {
          // a descendant, but not in the scope of level
          return false;
        }
    });
}

function getAllDescendants(parentRow) {
    return getDescendants(parentRow, -1);
}

function getAllAncestors(parentRow) {
    var result = [];

    var parentLevel = parseInt($(parentRow).attr("hierarchyLevel"));
    var nextAncestorLevel = parentLevel - 1;

    var done = parentLevel < 0; // might not have any ancestors
    return $j(parentRow).prevAll().filter(function (index, element) {
        if (done) {
            return false;
        }

        var elementLevel = parseInt($j(element).attr("hierarchyLevel"));
        if (elementLevel === nextAncestorLevel) {
            nextAncestorLevel -= 1;
            if (nextAncestorLevel < 0) {
                // last ancestors found
                done = true;
            }

            return true;
        } else {
            return false;
        }
    });
}

function getSiblings(row) {
    var level = parseInt($j(row).attr("hierarchyLevel"));

    var done = false;
    var isSibling = function(index, element) {
        if (done) {
            return false;
        }

        var elementLevel = parseInt($j(element).attr("hierarchyLevel"));
        if (elementLevel == level) {
            return true;
        } else if (elementLevel < level) {
            // at a parent or uncle, so cannot be a sibling
            done = true;
            return false;
        } else {
            // another descendant that is not a sibling
            return false;
        }
    };

    var previousSiblings = $j(row).prevAll().filter(isSibling);

    done = false;
    var followingSiblings = $j(row).nextAll().filter(isSibling);

    return $(previousSiblings).add(followingSiblings);
}

function addEvents() {

    var toggleHandler = function (node) {
        var row = $j(node).parent().parent(".table-row");
        if (changeToExpandedState(node)) {
            $j(getDescendants(row, 1)).show();

            // When a filter is applied you can look at nodes currently not visible in the hierarchy,
            // thus you have to ensure that the ancestors' state is updated correctly, so that all display
            // the minus sign.
            // This keeps the invariant that to expand a child its parent has to be expanded too.
            var filter = $j("#filter").val();
            if (filter != "") {
                $j(getAllAncestors(row)).each(function (index, element) {
                    changeToExpandedState($j(element).find('.icon-plus-sign'));
                });
            }
        } else if (changeToCollapsedState(node)) {
            $j(getAllDescendants(row)).hide().each(function (index, element) {
                changeToCollapsedState($j(element).find('.icon-minus-sign'));
            });
        }
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

        var row = $j(this).parent().parent(".table-row");
        var check = this.checked;
        checkChildren(row, check);
        checkParent(row, check);

        generateCharts();
    });
}

function checkChildren(row, checked) {
    $j(getAllDescendants(row)).find("input[type='checkbox']").prop("checked", checked);
}

function areAllSiblingsChecked(row) {
    var siblings = getSiblings(row);
    return $(siblings).find("input:checked").length == siblings.length;
}

function checkParent(row, checked) {
    var ancestors = getAllAncestors(row);
    var ancestorsLength = ancestors.length;

    if (checked) {
        var child = row;
        $j(ancestors).each(function (index, element) {
            if (areAllSiblingsChecked(child)) {
                $j(element).find("input[type='checkbox']").prop("checked", checked);
                child = element;
            } else {
                return false;
            }
        });
    } else {
        $j(ancestors).find("input[type='checkbox']").prop("checked", checked);
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

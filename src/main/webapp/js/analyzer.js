/* global echarts, remoteAction */
/*
 * Test Results Analyzer page.
 *
 * Renders the test history table, the "most broken tests" table and the charts from the
 * tree returned by TestResultsAnalyzerAction#getTreeResult. No third-party libraries apart
 * from ECharts (provided by the echarts-api plugin).
 */
(function () {
  "use strict";

  var STATUS_CLASS = {
    PASSED: "passed",
    FAILED: "failed",
    SKIPPED: "skipped",
  };

  var state = {
    root: null,
    labels: {},
    thresholds: { low: 0, high: 0 },
    displayValues: false,
    data: null,
    rows: [],
    charts: {},
    pieOverride: null,
  };

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  function byId(id) {
    return document.getElementById(id);
  }

  function el(tag, className, text) {
    var node = document.createElement(tag);
    if (className) {
      node.className = className;
    }
    if (text !== undefined && text !== null) {
      node.textContent = text;
    }
    return node;
  }

  function cloneIcon(templateId) {
    var template = byId(templateId);
    return template ? template.content.cloneNode(true) : document.createDocumentFragment();
  }

  function statusLabel(status) {
    switch (status) {
      case "PASSED":
        return state.labels.passed;
      case "FAILED":
        return state.labels.failed;
      case "SKIPPED":
        return state.labels.skipped;
      default:
        return state.labels.na;
    }
  }

  function isChecked(id) {
    var input = byId(id);
    return input !== null && input.checked;
  }

  function noOfBuildsNeeded() {
    return isChecked("tra-all-builds") ? "-1" : byId("tra-builds").value;
  }

  // ---------------------------------------------------------------------------
  // Per-row statistics
  // ---------------------------------------------------------------------------

  function isNewFailure(buildResults) {
    return buildResults.length >= 2 && buildResults[0].status === "FAILED" && buildResults[1].status === "PASSED";
  }

  function percentPassed(buildResults) {
    var buildsPassed = 0;
    var buildsFailed = 0;
    var testsPassed = 0;
    var testsFailed = 0;

    buildResults.forEach(function (result) {
      if (result.status === "N/A") {
        return;
      }
      if (result.totalFailed > 0) {
        buildsFailed++;
      } else if (result.totalPassed > 0) {
        buildsPassed++;
      }
      testsPassed += result.totalPassed;
      testsFailed += result.totalFailed;
    });

    var totalBuilds = buildsPassed + buildsFailed;
    var totalTests = testsPassed + testsFailed;
    if (totalBuilds === 0 || totalTests === 0) {
      return { text: state.labels.na, title: "" };
    }
    var buildPercent = Math.round((100 * buildsPassed) / totalBuilds);
    var testPercent = Math.round((100 * testsPassed) / totalTests);
    return {
      text: buildPercent + "% (" + testPercent + "%)",
      title: buildPercent + "% of builds passed, " + testPercent + "% of test runs passed",
    };
  }

  function numberOfTransitions(buildResults) {
    var hasPrevious = false;
    var previousPassed = false;
    var result = 0;

    // Oldest build last, so walk backwards to go forward in time.
    for (var i = buildResults.length - 1; i >= 0; i--) {
      var build = buildResults[i];
      if (build.status === "N/A") {
        continue;
      }
      var failed = build.totalFailed > 0;
      var passed = !failed && build.totalPassed > 0;
      if (!failed && !passed) {
        continue;
      }
      if (hasPrevious && previousPassed !== passed) {
        result++;
      }
      hasPrevious = true;
      previousPassed = passed;
    }
    return result;
  }

  // ---------------------------------------------------------------------------
  // Test history: one card per package, a row per class / test with a build strip
  // ---------------------------------------------------------------------------

  function didNotRun(node) {
    return node.buildResults.every(function (result) {
      return result.status === "N/A";
    });
  }

  function flatten(nodes, level, out) {
    nodes.forEach(function (node) {
      // Tests that were renamed or removed have no results in the builds shown
      if (!isChecked("tra-show-not-run") && didNotRun(node)) {
        return;
      }
      out.push({ node: node, level: level });
      if (node.children && node.children.length > 0) {
        flatten(node.children, level + 1, out);
      }
    });
    return out;
  }

  function formatDuration(result) {
    return typeof result.totalTimeTaken === "number" ? result.totalTimeTaken.toFixed(3) + "s" : state.labels.na;
  }

  function renderBuildResult(result) {
    var cell = el(result.url ? "a" : "span", "tra-build");
    var statusClass = STATUS_CLASS[result.status];
    if (statusClass) {
      cell.classList.add("tra-build--" + statusClass);
    }
    if (result.url) {
      cell.href = result.url;
    }
    var label = statusLabel(result.status);
    var title = "Build #" + result.buildNumber + ": " + label;
    if (typeof result.totalTimeTaken === "number") {
      title += " in " + formatDuration(result);
    }
    cell.title = title;
    cell.setAttribute("aria-label", title);
    if (state.displayValues) {
      cell.classList.add("tra-build--value");
      cell.textContent = result.status === "N/A" ? label : formatDuration(result);
    }
    return cell;
  }

  function renderStrip(buildResults) {
    var strip = el("div", "tra-strip");
    if (state.displayValues) {
      strip.classList.add("tra-strip--values");
    }
    buildResults.forEach(function (result) {
      strip.appendChild(renderBuildResult(result));
    });
    return strip;
  }

  function renderRow(entry, index) {
    var node = entry.node;
    var hasChildren = node.children && node.children.length > 0;
    var row = el("div", "tra-row");
    row.setAttribute("role", "treeitem");
    row.setAttribute("aria-level", String(entry.level + 1));
    row.dataset.level = String(entry.level);
    row.dataset.name = node.text;
    row.trNode = node;
    row.style.setProperty("--tra-level", String(entry.level));
    if (entry.level === 0) {
      row.classList.add("tra-row--package");
    } else {
      row.hidden = true;
    }
    if (!hasChildren) {
      row.classList.add("tra-row--leaf");
    }

    // Selection
    var checkbox = el("span", "jenkins-checkbox tra-row__select");
    var input = el("input");
    input.type = "checkbox";
    input.id = "tra-row-" + index;
    input.className = "tra-row-select";
    input.setAttribute("aria-label", "Include " + node.text + " in charts");
    var label = el("label");
    label.htmlFor = input.id;
    checkbox.appendChild(input);
    checkbox.appendChild(label);
    row.appendChild(checkbox);

    // Name
    var name = el("div", "tra-row__name");
    if (hasChildren) {
      var toggle = el("button", "tra-toggle");
      toggle.type = "button";
      toggle.setAttribute("aria-expanded", "false");
      toggle.setAttribute("aria-label", "Show children of " + node.text);
      toggle.appendChild(cloneIcon("tra-icon-toggle"));
      name.appendChild(toggle);
    } else {
      name.appendChild(el("span", "tra-toggle-spacer"));
    }
    name.appendChild(el("span", "tra-row__text", node.text));
    if (isNewFailure(node.buildResults)) {
      var marker = el("span", "tra-new-failure");
      marker.setAttribute("tooltip", state.root.dataset.newFailureLabel);
      marker.setAttribute("aria-label", state.root.dataset.newFailureLabel);
      marker.appendChild(cloneIcon("tra-icon-new-failure"));
      name.appendChild(marker);
    }
    row.appendChild(name);

    // Statistics
    var stats = el("div", "tra-row__stats");
    var passed = percentPassed(node.buildResults);
    var passedStat = el("span", "tra-stat tra-stat--passed", passed.text);
    passedStat.title = passed.title;
    stats.appendChild(passedStat);
    var transitionCount = numberOfTransitions(node.buildResults);
    var transitions = el("span", "tra-stat tra-stat--transitions", String(transitionCount));
    transitions.title = transitionCount + " transitions between passing and failing";
    if (transitionCount > 0) {
      transitions.classList.add("tra-stat--flaky");
    }
    stats.appendChild(transitions);
    row.appendChild(stats);

    row.appendChild(renderStrip(node.buildResults));
    return row;
  }

  function renderHeader(builds) {
    var header = el("div", "tra-history__header");
    header.setAttribute("aria-hidden", "true");
    header.appendChild(el("span", "tra-row__select"));
    header.appendChild(el("span", "tra-row__name", "Package / Class / Test"));
    var stats = el("div", "tra-row__stats");
    var passed = el("span", "tra-stat", "Passed");
    passed.title = "Builds passed (test runs passed)";
    stats.appendChild(passed);
    var transitions = el("span", "tra-stat tra-stat--transitions", "Flips");
    transitions.title = "Transitions from passed to failed and failed to passed";
    stats.appendChild(transitions);
    header.appendChild(stats);
    var strip = el("div", "tra-strip");
    if (state.displayValues) {
      strip.classList.add("tra-strip--values");
    }
    builds.forEach(function (build) {
      var label = el("span", "tra-build tra-build--header", "#" + build);
      if (state.displayValues) {
        label.classList.add("tra-build--value");
      }
      strip.appendChild(label);
    });
    header.appendChild(strip);
    return header;
  }

  function renderHistory(data) {
    var history = el("div", "tra-history");
    history.setAttribute("role", "tree");
    history.setAttribute("aria-label", "Test history");
    history.appendChild(renderHeader(data.builds));

    var card = null;
    flatten(data.results, 0, []).forEach(function (entry, index) {
      if (entry.level === 0) {
        card = el("div", "jenkins-card tra-package");
        history.appendChild(card);
      }
      card.appendChild(renderRow(entry, index));
    });
    return history;
  }

  /** Hides package cards whose rows are all hidden (e.g. by the filter). */
  function syncCards() {
    var cards = byId("tra-history").querySelectorAll(".tra-package");
    Array.prototype.forEach.call(cards, function (card) {
      card.hidden = card.querySelector(".tra-row:not([hidden])") === null;
    });
  }

  // ---------------------------------------------------------------------------
  // Tree behaviour (expand / collapse / filter / selection)
  // ---------------------------------------------------------------------------

  function levelOf(row) {
    return parseInt(row.dataset.level, 10);
  }

  function indexOf(row) {
    return state.rows.indexOf(row);
  }

  function toggleOf(row) {
    return row.querySelector(".tra-toggle");
  }

  function checkboxOf(row) {
    return row.querySelector(".tra-row-select");
  }

  function isExpanded(row) {
    var toggle = toggleOf(row);
    return toggle !== null && toggle.getAttribute("aria-expanded") === "true";
  }

  function setExpanded(row, expanded) {
    var toggle = toggleOf(row);
    if (toggle === null) {
      return;
    }
    toggle.setAttribute("aria-expanded", expanded ? "true" : "false");
    toggle.setAttribute("aria-label", (expanded ? "Hide children of " : "Show children of ") + row.dataset.name);
  }

  /** Descendants of the row; maxDepth -1 means all of them. */
  function descendants(row, maxDepth) {
    var level = levelOf(row);
    var result = [];
    for (var i = indexOf(row) + 1; i < state.rows.length; i++) {
      var candidateLevel = levelOf(state.rows[i]);
      if (candidateLevel <= level) {
        break;
      }
      if (maxDepth === -1 || candidateLevel <= level + maxDepth) {
        result.push(state.rows[i]);
      }
    }
    return result;
  }

  /** Ancestors of the row, nearest first. */
  function ancestors(row) {
    var result = [];
    var wanted = levelOf(row) - 1;
    for (var i = indexOf(row) - 1; i >= 0 && wanted >= 0; i--) {
      if (levelOf(state.rows[i]) === wanted) {
        result.push(state.rows[i]);
        wanted--;
      }
    }
    return result;
  }

  function siblings(row) {
    var level = levelOf(row);
    var index = indexOf(row);
    var result = [];
    var i;
    for (i = index - 1; i >= 0 && levelOf(state.rows[i]) >= level; i--) {
      if (levelOf(state.rows[i]) === level) {
        result.push(state.rows[i]);
      }
    }
    for (i = index + 1; i < state.rows.length && levelOf(state.rows[i]) >= level; i++) {
      if (levelOf(state.rows[i]) === level) {
        result.push(state.rows[i]);
      }
    }
    return result;
  }

  function toggleRow(row) {
    if (!isExpanded(row)) {
      setExpanded(row, true);
      descendants(row, 1).forEach(function (child) {
        child.hidden = false;
      });
      // While filtering, a row can be expanded although its ancestors are collapsed;
      // keep the invariant that an expanded row has expanded ancestors.
      if (byId("tra-filter").value !== "") {
        ancestors(row).forEach(function (ancestor) {
          setExpanded(ancestor, true);
        });
      }
    } else {
      setExpanded(row, false);
      descendants(row, -1).forEach(function (child) {
        child.hidden = true;
        setExpanded(child, false);
      });
    }
    syncCards();
  }

  function expandAll() {
    state.rows.forEach(function (row) {
      setExpanded(row, true);
      row.hidden = false;
    });
    syncCards();
  }

  function collapseAll() {
    state.rows.forEach(function (row) {
      setExpanded(row, false);
      row.hidden = levelOf(row) !== 0;
    });
    syncCards();
  }

  /** Restore visibility from the expanded state after the filter has been cleared. */
  function restoreTreeVisibility() {
    var visibleLevels = [0];
    state.rows.forEach(function (row) {
      var level = levelOf(row);
      while (visibleLevels[visibleLevels.length - 1] > level) {
        visibleLevels.pop();
      }
      var visible = visibleLevels[visibleLevels.length - 1] === level;
      row.hidden = !visible;
      if (visible && isExpanded(row)) {
        visibleLevels.push(level + 1);
      }
    });
  }

  function applyFilter() {
    var filter = byId("tra-filter").value.trim().toLowerCase();
    if (filter === "") {
      restoreTreeVisibility();
      syncCards();
      return;
    }
    state.rows.forEach(function (row) {
      row.hidden = row.dataset.name.toLowerCase().indexOf(filter) === -1;
    });
    syncCards();
  }

  function onRowSelectionChange(row, checked) {
    descendants(row, -1).forEach(function (child) {
      checkboxOf(child).checked = checked;
    });

    var parents = ancestors(row);
    if (checked) {
      var child = row;
      for (var i = 0; i < parents.length; i++) {
        var allSiblingsChecked = siblings(child).every(function (sibling) {
          return checkboxOf(sibling).checked;
        });
        if (!allSiblingsChecked) {
          break;
        }
        checkboxOf(parents[i]).checked = true;
        child = parents[i];
      }
    } else {
      parents.forEach(function (parent) {
        checkboxOf(parent).checked = false;
      });
    }
    state.pieOverride = null;
    renderCharts();
  }

  function bindHistoryEvents(history) {
    history.addEventListener("click", function (event) {
      var toggle = event.target.closest(".tra-toggle");
      if (toggle) {
        toggleRow(toggle.closest(".tra-row"));
      }
    });
    history.addEventListener("change", function (event) {
      if (event.target.classList.contains("tra-row-select")) {
        onRowSelectionChange(event.target.closest(".tra-row"), event.target.checked);
      }
    });
  }

  // ---------------------------------------------------------------------------
  // Most broken tests
  // ---------------------------------------------------------------------------

  function worstTests(data, limit) {
    var failures = {};

    function visit(node, path) {
      var name = path === "" ? node.text : path + "." + node.text;
      if (node.children && node.children.length > 0) {
        node.children.forEach(function (child) {
          visit(child, name);
        });
        return;
      }
      node.buildResults.forEach(function (result) {
        if (result.status === "FAILED") {
          if (!failures[name]) {
            failures[name] = [];
          }
          failures[name].push({ buildNumber: result.buildNumber, url: result.url });
        }
      });
    }

    data.results.forEach(function (node) {
      visit(node, "");
    });

    return Object.keys(failures)
      .map(function (name) {
        return { name: name, builds: failures[name] };
      })
      .sort(function (a, b) {
        return b.builds.length - a.builds.length;
      })
      .slice(0, limit);
  }

  function renderWorstTests(data) {
    var container = byId("tra-worst-tests");
    container.textContent = "";
    var tests = worstTests(data, Math.max(1, parseInt(byId("tra-worst-count").value, 10) || 10));
    if (tests.length === 0) {
      container.appendChild(el("p", "jenkins-!-text-color-secondary", "There are no failing tests."));
      return;
    }

    var list = el("ol", "jenkins-card tra-worst");
    tests.forEach(function (test) {
      var item = el("li", "tra-worst__item");
      var heading = el("div", "tra-worst__heading");
      heading.appendChild(el("span", "tra-row__text", test.name));
      heading.appendChild(
        el("span", "tra-stat tra-stat--failed", test.builds.length + (test.builds.length === 1 ? " failure" : " failures")),
      );
      item.appendChild(heading);

      var links = el("div", "tra-worst__builds");
      test.builds.slice(0, 10).forEach(function (build) {
        var link = el(build.url ? "a" : "span", "tra-chip", "#" + build.buildNumber);
        if (build.url) {
          link.href = build.url;
        }
        link.title = "Failed in build #" + build.buildNumber;
        links.appendChild(link);
      });
      item.appendChild(links);
      list.appendChild(item);
    });
    container.appendChild(list);
  }

  // ---------------------------------------------------------------------------
  // Chart data
  // ---------------------------------------------------------------------------

  /** Checked rows, without rows whose ancestor is already included (avoids double counting). */
  function selectedRows() {
    var result = [];
    var lastCheckedLevel = Number.MAX_SAFE_INTEGER;
    state.rows.forEach(function (row) {
      var level = levelOf(row);
      if (!checkboxOf(row).checked) {
        if (level <= lastCheckedLevel) {
          lastCheckedLevel = Number.MAX_SAFE_INTEGER;
        }
        return;
      }
      if (level <= lastCheckedLevel) {
        lastCheckedLevel = level;
        result.push(row);
      }
    });
    return result;
  }

  function baseRows() {
    var selected = selectedRows();
    if (selected.length > 0) {
      return selected;
    }
    return state.rows.filter(function (row) {
      return levelOf(row) === 0;
    });
  }

  /** Leaf (test case) rows, restricted to checked ones when anything is checked. */
  function testRows() {
    var leaves = state.rows.filter(function (row) {
      return row.classList.contains("tra-row--leaf");
    });
    var anythingChecked = state.rows.some(function (row) {
      return checkboxOf(row).checked;
    });
    if (!anythingChecked) {
      return leaves;
    }
    return leaves.filter(function (row) {
      return checkboxOf(row).checked;
    });
  }

  /** Aggregates results per build, ordered oldest build first. */
  function aggregate(rows) {
    var perBuild = {};
    rows.forEach(function (row) {
      row.trNode.buildResults.forEach(function (result) {
        var entry = perBuild[result.buildNumber];
        if (!entry) {
          entry = { build: result.buildNumber, passed: 0, failed: 0, skipped: 0, total: 0, runtime: 0, runtimes: [] };
          perBuild[result.buildNumber] = entry;
        }
        entry.passed += result.totalPassed || 0;
        entry.failed += result.totalFailed || 0;
        entry.skipped += result.totalSkipped || 0;
        entry.total += result.totalTests || 0;
        if (typeof result.totalTimeTaken === "number") {
          entry.runtime += result.totalTimeTaken;
          entry.runtimes.push(result.totalTimeTaken);
        }
      });
    });
    return Object.keys(perBuild)
      .map(function (key) {
        return perBuild[key];
      })
      .sort(function (a, b) {
        return parseInt(a.build, 10) - parseInt(b.build, 10);
      });
  }

  // ---------------------------------------------------------------------------
  // Charts
  // ---------------------------------------------------------------------------

  var colorProbe = null;

  /** Resolves any CSS colour (including oklch / color-mix) into rgba() that ECharts understands. */
  function resolveColor(value, fallback) {
    if (!value) {
      return fallback;
    }
    if (colorProbe === null) {
      var canvas = document.createElement("canvas");
      canvas.width = 1;
      canvas.height = 1;
      colorProbe = canvas.getContext("2d", { willReadFrequently: true });
    }
    if (!colorProbe) {
      return value;
    }
    try {
      colorProbe.clearRect(0, 0, 1, 1);
      colorProbe.fillStyle = fallback;
      colorProbe.fillStyle = value;
      colorProbe.fillRect(0, 0, 1, 1);
      var p = colorProbe.getImageData(0, 0, 1, 1).data;
      return "rgba(" + p[0] + "," + p[1] + "," + p[2] + "," + Math.round((p[3] / 255) * 100) / 100 + ")";
    } catch (e) {
      return value;
    }
  }

  function theme() {
    var style = getComputedStyle(state.root);
    function token(name, fallback) {
      return resolveColor(style.getPropertyValue(name).trim(), fallback);
    }
    return {
      text: token("--text-color", "#333"),
      textSecondary: token("--text-color-secondary", "#666"),
      border: token("--card-border-color", "#ddd"),
      tooltipBackground: token("--card-background", "#fff"),
      passed: token("--tra-passed", "#1ea64b"),
      failed: token("--tra-failed", "#e6001f"),
      skipped: token("--tra-skipped", "#fe820a"),
      total: token("--tra-total", "#0b6aa2"),
      fontFamily: style.fontFamily,
    };
  }

  function baseOptions(colors, title) {
    return {
      backgroundColor: "transparent",
      color: [],
      textStyle: { fontFamily: colors.fontFamily, color: colors.text },
      title: {
        text: title,
        left: "center",
        textStyle: { color: colors.text, fontSize: 15, fontWeight: 600 },
      },
      tooltip: {
        backgroundColor: colors.tooltipBackground,
        borderColor: colors.border,
        textStyle: { color: colors.text },
      },
      legend: {
        bottom: 0,
        icon: "circle",
        itemWidth: 10,
        itemHeight: 10,
        textStyle: { color: colors.textSecondary },
      },
      toolbox: {
        right: 0,
        iconStyle: { borderColor: colors.textSecondary },
        feature: { saveAsImage: { title: "Save as image", backgroundColor: colors.tooltipBackground } },
      },
    };
  }

  function axes(colors, categories, yName) {
    var axisLine = { lineStyle: { color: colors.border } };
    return {
      grid: { left: 8, right: 16, top: 48, bottom: 48, containLabel: true },
      xAxis: {
        type: "category",
        name: "Build",
        nameLocation: "middle",
        nameGap: 28,
        nameTextStyle: { color: colors.textSecondary },
        data: categories.map(function (build) {
          return "#" + build;
        }),
        axisLine: axisLine,
        axisTick: { show: false },
        axisLabel: { color: colors.textSecondary },
      },
      yAxis: {
        type: "value",
        name: yName,
        nameTextStyle: { color: colors.textSecondary, align: "left" },
        minInterval: 1,
        min: 0,
        axisLabel: { color: colors.textSecondary },
        splitLine: { lineStyle: { color: colors.border, type: "dashed" } },
      },
    };
  }

  function lineSeries(name, data, color) {
    return {
      name: name,
      type: "line",
      data: data,
      smooth: false,
      symbol: "circle",
      showSymbol: true,
      showAllSymbol: true,
      symbolSize: 7,
      lineStyle: { width: 2.5, color: color },
      itemStyle: { color: color },
      emphasis: { focus: "series" },
    };
  }

  function chartFor(key, containerId, elementId, visible) {
    var container = byId(containerId);
    container.hidden = !visible;
    if (!visible) {
      if (state.charts[key]) {
        state.charts[key].dispose();
        delete state.charts[key];
      }
      return null;
    }
    if (!state.charts[key]) {
      state.charts[key] = echarts.init(byId(elementId), null, { renderer: "svg" });
    }
    state.charts[key].clear();
    return state.charts[key];
  }

  function passFailPieData(builds) {
    var passed = 0;
    var failed = 0;
    var skipped = 0;
    builds.forEach(function (build) {
      if (build.failed > 0) {
        failed++;
      } else if (build.passed > 0) {
        passed++;
      } else {
        skipped++;
      }
    });
    return { passed: passed, failed: failed, skipped: skipped };
  }

  function pieOptions(colors, title, slices, seriesName) {
    var options = baseOptions(colors, title);
    options.tooltip.trigger = "item";
    options.tooltip.formatter = function (params) {
      var hint = params.data.hint ? " (" + params.data.hint + ")" : "";
      return params.name + hint + ": " + params.value + " (" + params.percent + "%)";
    };
    options.series = [
      {
        name: seriesName,
        type: "pie",
        radius: ["45%", "70%"],
        center: ["50%", "52%"],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: colors.tooltipBackground, borderWidth: 2, borderRadius: 4 },
        label: { color: colors.text, formatter: "{b}\n{d}%" },
        labelLine: { lineStyle: { color: colors.border } },
        data: slices.filter(function (slice) {
          return slice.value > 0;
        }),
      },
    ];
    return options;
  }

  function renderPassFailPie(chart, colors, builds) {
    var title;
    var counts;
    if (state.pieOverride) {
      var build = state.pieOverride;
      title = "Build #" + build.build;
      counts = { passed: build.passed, failed: build.failed, skipped: build.skipped };
    } else {
      title = builds.length === 1 ? "Last build by result" : "Last " + builds.length + " builds by result";
      counts = passFailPieData(builds);
    }
    chart.setOption(
      pieOptions(
        colors,
        title,
        [
          { name: "Passed", value: counts.passed, itemStyle: { color: colors.passed } },
          { name: "Failed", value: counts.failed, itemStyle: { color: colors.failed } },
          { name: "Skipped", value: counts.skipped, itemStyle: { color: colors.skipped } },
        ],
        title,
      ),
    );
  }

  function renderRuntimePie(chart, colors) {
    var builds = aggregate(testRows());
    if (builds.length === 0) {
      return;
    }
    var build = state.pieOverride || builds[builds.length - 1];
    var match = builds.filter(function (candidate) {
      return candidate.build === build.build;
    })[0];
    var runtimes = match ? match.runtimes : [];

    var fast = 0;
    var medium = 0;
    var slow = 0;
    runtimes.forEach(function (time) {
      if (time < state.thresholds.low) {
        fast++;
      } else if (time >= state.thresholds.high) {
        slow++;
      } else {
        medium++;
      }
    });
    var title = "Test run times, build #" + build.build;
    chart.setOption(
      pieOptions(
        colors,
        title,
        [
          { name: "Fast", hint: "under " + state.thresholds.low + "s", value: fast, itemStyle: { color: colors.passed } },
          { name: "Medium", hint: state.thresholds.low + "s to " + state.thresholds.high + "s", value: medium, itemStyle: { color: colors.skipped } },
          { name: "Slow", hint: state.thresholds.high + "s or more", value: slow, itemStyle: { color: colors.failed } },
        ],
        title,
      ),
    );
  }

  function renderCharts() {
    if (!state.data || typeof echarts === "undefined") {
      return;
    }
    var mode = byId("tra-chart-data").value;
    var runtime = mode === "runtime";
    var passRate = mode === "passrate";
    var showLine = isChecked("tra-chart-line");
    var showBar = isChecked("tra-chart-bar") && mode === "passfail";
    var showPie = isChecked("tra-chart-pie");
    byId("tra-charts-section").hidden = state.rows.length === 0 || !(showLine || showBar || showPie);

    var colors = theme();
    var builds = aggregate(baseRows());
    var categories = builds.map(function (build) {
      return build.build;
    });

    var line = chartFor("line", "tra-chart-line-container", "tra-line-chart", showLine && builds.length > 0);
    if (line) {
      var lineTitle = runtime ? "Build run time" : passRate ? "Pass rate" : "Build status";
      var yName = runtime ? "Seconds" : passRate ? "%" : "Tests";
      var lineOptions = baseOptions(colors, lineTitle);
      Object.assign(lineOptions, axes(colors, categories, yName));
      lineOptions.tooltip.trigger = "axis";
      if (passRate) {
        lineOptions.yAxis.max = 100;
        lineOptions.yAxis.minInterval = null;
        lineOptions.tooltip.valueFormatter = function (value) {
          return value === null ? "–" : value + "%";
        };
        lineOptions.series = [
          lineSeries(
            "Pass rate",
            builds.map(function (build) {
              // Skipped tests neither pass nor fail, so they are left out of the rate
              var ran = build.passed + build.failed;
              return ran === 0 ? null : Math.round((1000 * build.passed) / ran) / 10;
            }),
            colors.passed,
          ),
        ];
      } else if (runtime) {
        lineOptions.yAxis.minInterval = null;
        lineOptions.tooltip.valueFormatter = function (value) {
          return value + " s";
        };
        lineOptions.series = [
          lineSeries(
            "Run time",
            builds.map(function (build) {
              return Math.round(build.runtime * 1000) / 1000;
            }),
            colors.total,
          ),
        ];
      } else {
        lineOptions.series = [
          lineSeries("Passed", builds.map(function (b) { return b.passed; }), colors.passed),
          lineSeries("Failed", builds.map(function (b) { return b.failed; }), colors.failed),
          lineSeries("Skipped", builds.map(function (b) { return b.skipped; }), colors.skipped),
          lineSeries("Total", builds.map(function (b) { return b.total; }), colors.total),
        ];
      }
      // Clicking anywhere in a build's column shows that build in the pie chart.
      lineOptions.tooltip.axisPointer = { type: "shadow" };
      var zr = line.getZr();
      zr.off("click");
      zr.on("click", function (event) {
        var point = [event.offsetX, event.offsetY];
        if (!line.containPixel("grid", point)) {
          return;
        }
        var index = line.convertFromPixel({ seriesIndex: 0 }, point)[0];
        if (builds[index] === undefined) {
          return;
        }
        state.pieOverride = builds[index];
        if (isChecked("tra-chart-pie")) {
          renderPie(colors, builds, runtime);
        }
      });
      line.setOption(lineOptions);
    }

    byId("tra-chart-pie-container").classList.toggle("tra-chart-card--wide", !showBar && showPie);

    var bar = chartFor("bar", "tra-chart-bar-container", "tra-bar-chart", showBar && builds.length > 0);
    if (bar) {
      var barOptions = baseOptions(colors, "Results per build");
      Object.assign(barOptions, axes(colors, categories, "Tests"));
      barOptions.tooltip.trigger = "axis";
      barOptions.tooltip.axisPointer = { type: "shadow" };
      var barSeries = function (name, key, color) {
        return {
          name: name,
          type: "bar",
          stack: "results",
          barMaxWidth: 32,
          itemStyle: { color: color },
          emphasis: { focus: "series" },
          data: builds.map(function (b) {
            return b[key];
          }),
        };
      };
      barOptions.series = [
        barSeries("Passed", "passed", colors.passed),
        barSeries("Failed", "failed", colors.failed),
        barSeries("Skipped", "skipped", colors.skipped),
      ];
      bar.setOption(barOptions);
    }

    if (showPie && builds.length > 0) {
      renderPie(colors, builds, runtime);
    } else {
      chartFor("pie", "tra-chart-pie-container", "tra-pie-chart", false);
    }
  }

  function renderPie(colors, builds, runtime) {
    var pie = chartFor("pie", "tra-chart-pie-container", "tra-pie-chart", true);
    if (runtime) {
      renderRuntimePie(pie, colors);
    } else {
      renderPassFailPie(pie, colors, builds);
    }
    // Slice labels do not fit on small screens; the legend and tooltip carry the same information.
    pie.setOption({ series: [{ label: { show: pie.getWidth() >= 420 }, labelLine: { show: pie.getWidth() >= 420 } }] });
  }

  function resizeCharts() {
    Object.keys(state.charts).forEach(function (key) {
      state.charts[key].resize();
    });
  }

  function watchTheme() {
    var scheduled = false;
    function rerender() {
      if (scheduled) {
        return;
      }
      scheduled = true;
      // Wait a frame so the new theme's stylesheet has been applied before reading tokens.
      window.requestAnimationFrame(function () {
        scheduled = false;
        renderCharts();
      });
    }
    if (window.MutationObserver) {
      var observer = new MutationObserver(rerender);
      observer.observe(document.documentElement, { attributes: true, attributeFilter: ["data-theme", "class", "style"] });
      observer.observe(document.body, { attributes: true, attributeFilter: ["data-theme", "class"] });
    }
    if (window.matchMedia) {
      var query = window.matchMedia("(prefers-color-scheme: dark)");
      if (query.addEventListener) {
        query.addEventListener("change", rerender);
      }
    }
    var rerenderAfterResize = debounce(renderCharts, 200);
    window.addEventListener("resize", function () {
      resizeCharts();
      rerenderAfterResize();
    });
  }

  // ---------------------------------------------------------------------------
  // Loading
  // ---------------------------------------------------------------------------

  function load() {
    state.displayValues = isChecked("tra-show-durations");
    state.pieOverride = null;
    state.data = null;
    state.rows = [];
    byId("tra-history").textContent = "";
    byId("tra-worst-tests").textContent = "";
    byId("tra-empty").hidden = true;
    byId("tra-loading").hidden = false;

    var userConfig = {
      noOfBuildsNeeded: noOfBuildsNeeded(),
      hideConfigMethods: isChecked("tra-hide-config"),
    };
    remoteAction.getTreeResult(userConfig, function (response) {
      var data = response.responseObject() || {};
      data.builds = data.builds || [];
      data.results = data.results || [];
      state.data = data;
      byId("tra-loading").hidden = true;
      render();
    });
  }

  /** Renders the loaded data; also used when an option that needs no new data changes. */
  function render() {
    var data = state.data;
    var container = byId("tra-history");
    container.textContent = "";
    state.rows = [];
    state.pieOverride = null;

    var history = renderHistory(data);
    var rows = history.querySelectorAll(".tra-row");
    byId("tra-empty").hidden = rows.length > 0;
    if (rows.length > 0) {
      bindHistoryEvents(history);
      container.appendChild(history);
      // Tooltips are attached by a behaviour rule, which only runs on what is in the page
      if (window.Behaviour) {
        history.querySelectorAll("[tooltip]").forEach(function (marker) {
          Behaviour.applySubtree(marker, true);
        });
      }
      state.rows = Array.prototype.slice.call(rows);
      applyFilter();
    }
    renderWorstTests(data);
    renderCharts();
  }

  function downloadCsv() {
    remoteAction.getExportCSV(String(isChecked("tra-show-durations")), noOfBuildsNeeded(), function (response) {
      var blob = new Blob([response.responseObject()], { type: "text/csv;charset=utf-8" });
      var url = URL.createObjectURL(blob);
      var link = el("a");
      link.href = url;
      link.download = "Test Results.csv";
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.setTimeout(function () {
        URL.revokeObjectURL(url);
      }, 0);
    });
  }

  // ---------------------------------------------------------------------------
  // Bootstrap
  // ---------------------------------------------------------------------------

  function applyCustomColors(dataset) {
    if (dataset.useCustomColors !== "true") {
      return;
    }
    var mapping = {
      "--tra-passed": dataset.passedColor,
      "--tra-failed": dataset.failedColor,
      "--tra-skipped": dataset.skippedColor,
      "--tra-na": dataset.naColor,
    };
    Object.keys(mapping).forEach(function (property) {
      if (mapping[property]) {
        state.root.style.setProperty(property, mapping[property]);
      }
    });
  }

  /** Shows or hides the row checkboxes; leaving selection mode clears the selection. */
  function setSelecting(selecting) {
    var toggle = byId("tra-select-toggle");
    toggle.setAttribute("aria-pressed", selecting ? "true" : "false");
    toggle.textContent = selecting ? toggle.dataset.labelDone : toggle.dataset.labelSelect;
    byId("tra-history").classList.toggle("tra-history-container--selecting", selecting);
    byId("tra-legend-default").hidden = selecting;
    byId("tra-legend-selecting").hidden = !selecting;
    if (!selecting) {
      var cleared = false;
      state.rows.forEach(function (row) {
        if (checkboxOf(row).checked) {
          checkboxOf(row).checked = false;
          cleared = true;
        }
      });
      if (cleared) {
        state.pieOverride = null;
        renderCharts();
      }
    }
  }

  function debounce(fn, wait) {
    var timer = null;
    return function () {
      window.clearTimeout(timer);
      timer = window.setTimeout(fn, wait);
    };
  }

  function init() {
    state.root = byId("tra-root");
    if (!state.root) {
      return;
    }
    var dataset = state.root.dataset;
    state.labels = {
      passed: dataset.passedLabel || "PASSED",
      failed: dataset.failedLabel || "FAILED",
      skipped: dataset.skippedLabel || "SKIPPED",
      na: dataset.naLabel || "N/A",
    };
    state.thresholds = {
      low: parseFloat(dataset.runtimeLowThreshold) || 0,
      high: parseFloat(dataset.runtimeHighThreshold) || 0,
    };
    applyCustomColors(dataset);

    var optionsToggle = byId("tra-options-toggle");
    optionsToggle.addEventListener("click", function () {
      var options = byId("tra-options");
      options.hidden = !options.hidden;
      optionsToggle.setAttribute("aria-expanded", options.hidden ? "false" : "true");
    });
    byId("tra-all-builds").addEventListener("change", function (event) {
      byId("tra-builds").disabled = event.target.checked;
    });
    byId("tra-chart-data").addEventListener("change", function (event) {
      byId("tra-chart-bar").disabled = event.target.value !== "passfail";
      state.pieOverride = null;
      renderCharts();
    });
    ["tra-chart-line", "tra-chart-bar", "tra-chart-pie"].forEach(function (id) {
      byId(id).addEventListener("change", renderCharts);
    });
    byId("tra-apply").addEventListener("click", load);
    byId("tra-show-not-run").addEventListener("change", function () {
      if (state.data) {
        render();
      }
    });
    byId("tra-worst-count").addEventListener("change", function () {
      if (state.data) {
        renderWorstTests(state.data);
      }
    });
    byId("tra-download-csv").addEventListener("click", downloadCsv);
    byId("tra-select-toggle").addEventListener("click", function (event) {
      setSelecting(event.currentTarget.getAttribute("aria-pressed") !== "true");
    });
    byId("tra-expand-all").addEventListener("click", expandAll);
    byId("tra-collapse-all").addEventListener("click", function () {
      byId("tra-filter").value = "";
      collapseAll();
    });
    byId("tra-filter").addEventListener("input", debounce(applyFilter, 150));

    watchTheme();
    load();
  }

  // Exposed for tests.
  window.testResultsAnalyzer = {
    percentPassed: percentPassed,
    numberOfTransitions: numberOfTransitions,
    isNewFailure: isNewFailure,
    worstTests: worstTests,
    applyFilter: applyFilter,
    expandAll: expandAll,
    collapseAll: collapseAll,
    reload: load,
  };

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();

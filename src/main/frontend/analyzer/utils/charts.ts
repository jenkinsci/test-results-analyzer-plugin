import type { EChartsOption, LineSeriesOption, PieSeriesOption } from "echarts";

import { type BuildLabeler, numberLabels, truncate } from "./buildLabels.ts";
import type { BuildTotals } from "./stats.ts";

export interface ChartTheme {
  text: string;
  textSecondary: string;
  border: string;
  background: string;
  passed: string;
  failed: string;
  skipped: string;
  total: string;
  fontFamily: string;
}

let colorProbe: CanvasRenderingContext2D | null | undefined;

/** Resolves any CSS colour (including oklch and color-mix) into rgba() that ECharts understands. */
function resolveColor(value: string, fallback: string): string {
  if (!value) {
    return fallback;
  }
  if (colorProbe === undefined) {
    const canvas = document.createElement("canvas");
    canvas.width = 1;
    canvas.height = 1;
    try {
      colorProbe = canvas.getContext("2d", { willReadFrequently: true });
    } catch {
      colorProbe = null;
    }
  }
  if (!colorProbe) {
    return value;
  }
  try {
    colorProbe.clearRect(0, 0, 1, 1);
    colorProbe.fillStyle = fallback;
    colorProbe.fillStyle = value;
    colorProbe.fillRect(0, 0, 1, 1);
    const [r, g, b, a] = colorProbe.getImageData(0, 0, 1, 1).data;
    return `rgba(${r},${g},${b},${Math.round((a / 255) * 100) / 100})`;
  } catch {
    return value;
  }
}

/** Reads the colours of the active Jenkins theme from the CSS variables on the element. */
export function readTheme(element: Element): ChartTheme {
  const style = getComputedStyle(element);
  const token = (name: string, fallback: string) =>
    resolveColor(style.getPropertyValue(name).trim(), fallback);
  return {
    text: token("--text-color", "#333"),
    textSecondary: token("--text-color-secondary", "#666"),
    border: token("--card-border-color", "#ddd"),
    background: token("--card-background", "#fff"),
    passed: token("--tra-passed", "#1ea64b"),
    failed: token("--tra-failed", "#e6001f"),
    skipped: token("--tra-skipped", "#fe820a"),
    total: token("--tra-total", "#0b6aa2"),
    fontFamily: style.fontFamily,
  };
}

function baseOptions(theme: ChartTheme, title: string): EChartsOption {
  return {
    backgroundColor: "transparent",
    textStyle: { fontFamily: theme.fontFamily, color: theme.text },
    title: {
      text: title,
      left: "center",
      textStyle: { color: theme.text, fontSize: 15, fontWeight: 600 },
    },
    tooltip: {
      backgroundColor: theme.background,
      borderColor: theme.border,
      textStyle: { color: theme.text },
    },
    legend: {
      bottom: 0,
      icon: "circle",
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { color: theme.textSecondary },
    },
    toolbox: {
      right: 0,
      iconStyle: { borderColor: theme.textSecondary },
      feature: {
        saveAsImage: {
          title: "Save as image",
          backgroundColor: theme.background,
        },
      },
    },
  };
}

function axes(
  theme: ChartTheme,
  builds: BuildTotals[],
  yName: string,
  label: BuildLabeler,
): EChartsOption {
  return {
    grid: { left: 8, right: 16, top: 48, bottom: 48, containLabel: true },
    xAxis: {
      type: "category",
      name: "Build",
      nameLocation: "middle",
      nameGap: 28,
      nameTextStyle: { color: theme.textSecondary },
      // The whole label shows in the tooltip, a shortened one on the axis
      data: builds.map((build) => label(build.build).text),
      axisLine: { lineStyle: { color: theme.border } },
      axisTick: { show: false },
      axisLabel: {
        color: theme.textSecondary,
        formatter: (value: string) => truncate(value),
      },
    },
    yAxis: {
      type: "value",
      name: yName,
      nameTextStyle: { color: theme.textSecondary, align: "left" },
      minInterval: 1,
      min: 0,
      axisLabel: { color: theme.textSecondary },
      splitLine: { lineStyle: { color: theme.border, type: "dashed" } },
    },
  };
}

function lineSeries(
  name: string,
  data: (number | null)[],
  color: string,
): LineSeriesOption {
  return {
    name,
    type: "line",
    data,
    smooth: false,
    symbol: "circle",
    showSymbol: true,
    showAllSymbol: true,
    symbolSize: 7,
    lineStyle: { width: 2.5, color },
    itemStyle: { color },
    emphasis: { focus: "series" },
  };
}

export function lineChartOptions(
  theme: ChartTheme,
  builds: BuildTotals[],
  mode: "passfail" | "passrate" | "runtime",
  label: BuildLabeler = numberLabels,
): EChartsOption {
  const title =
    mode === "runtime"
      ? "Build run time"
      : mode === "passrate"
        ? "Pass rate"
        : "Build status";
  const yName =
    mode === "runtime" ? "Seconds" : mode === "passrate" ? "%" : "Tests";
  const axisOptions = axes(theme, builds, yName, label);
  const yAxis = axisOptions.yAxis as Record<string, unknown>;
  // Clicking anywhere in a build's column shows that build in the pie chart, so shade the column
  const tooltip = {
    ...(baseOptions(theme, title).tooltip as object),
    trigger: "axis" as const,
    axisPointer: { type: "shadow" as const },
  } as Record<string, unknown>;

  let series: LineSeriesOption[];
  if (mode === "passrate") {
    yAxis.max = 100;
    yAxis.minInterval = undefined;
    tooltip.valueFormatter = (value: unknown) =>
      value === null ? "–" : `${value}%`;
    series = [
      lineSeries(
        "Pass rate",
        builds.map((build) => {
          // Skipped tests neither pass nor fail, so they are left out of the rate
          const ran = build.passed + build.failed;
          return ran === 0
            ? null
            : Math.round((1000 * build.passed) / ran) / 10;
        }),
        theme.passed,
      ),
    ];
  } else if (mode === "runtime") {
    yAxis.minInterval = undefined;
    tooltip.valueFormatter = (value: unknown) => `${value} s`;
    series = [
      lineSeries(
        "Run time",
        builds.map((build) => Math.round(build.runtime * 1000) / 1000),
        theme.total,
      ),
    ];
  } else {
    series = [
      lineSeries(
        "Passed",
        builds.map((b) => b.passed),
        theme.passed,
      ),
      lineSeries(
        "Failed",
        builds.map((b) => b.failed),
        theme.failed,
      ),
      lineSeries(
        "Skipped",
        builds.map((b) => b.skipped),
        theme.skipped,
      ),
      lineSeries(
        "Total",
        builds.map((b) => b.total),
        theme.total,
      ),
    ];
  }
  return {
    ...baseOptions(theme, title),
    ...axisOptions,
    tooltip: tooltip as EChartsOption["tooltip"],
    series,
  };
}

export function barChartOptions(
  theme: ChartTheme,
  builds: BuildTotals[],
  label: BuildLabeler = numberLabels,
): EChartsOption {
  const bar = (
    name: string,
    key: "passed" | "failed" | "skipped",
    color: string,
  ) => ({
    name,
    type: "bar" as const,
    stack: "results",
    barMaxWidth: 32,
    itemStyle: { color },
    emphasis: { focus: "series" as const },
    data: builds.map((b) => b[key]),
  });
  const base = baseOptions(theme, "Results per build");
  return {
    ...base,
    ...axes(theme, builds, "Tests", label),
    tooltip: {
      ...(base.tooltip as object),
      trigger: "axis",
      axisPointer: { type: "shadow" },
    },
    series: [
      bar("Passed", "passed", theme.passed),
      bar("Failed", "failed", theme.failed),
      bar("Skipped", "skipped", theme.skipped),
    ],
  };
}

interface Slice {
  name: string;
  value: number;
  color: string;
  hint?: string;
}

function pieOptions(
  theme: ChartTheme,
  title: string,
  slices: Slice[],
  showLabels: boolean,
): EChartsOption {
  const base = baseOptions(theme, title);
  const series: PieSeriesOption = {
    name: title,
    type: "pie",
    radius: ["45%", "70%"],
    center: ["50%", "52%"],
    avoidLabelOverlap: true,
    itemStyle: {
      borderColor: theme.background,
      borderWidth: 2,
      borderRadius: 4,
    },
    // Slice labels do not fit on small screens; the legend and tooltip carry the same information
    label: { show: showLabels, color: theme.text, formatter: "{b}\n{d}%" },
    labelLine: { show: showLabels, lineStyle: { color: theme.border } },
    data: slices
      .filter((slice) => slice.value > 0)
      .map((slice) => ({
        name: slice.name,
        value: slice.value,
        hint: slice.hint,
        itemStyle: { color: slice.color },
      })),
  };
  return {
    ...base,
    tooltip: {
      ...(base.tooltip as object),
      trigger: "item",
      formatter: (params: unknown) => {
        const { name, value, percent, data } = params as {
          name: string;
          value: number;
          percent: number;
          data: { hint?: string };
        };
        const hint = data.hint ? ` (${data.hint})` : "";
        return `${name}${hint}: ${value} (${percent}%)`;
      },
    },
    series: [series],
  };
}

/** Builds by result: a build with any failure counts as failed. */
export function passFailPieOptions(
  theme: ChartTheme,
  builds: BuildTotals[],
  focus: BuildTotals | null,
  showLabels: boolean,
  label: BuildLabeler = numberLabels,
): EChartsOption {
  let title: string;
  let counts: { passed: number; failed: number; skipped: number };
  if (focus) {
    title = `Build ${label(focus.build).short}`;
    counts = focus;
  } else {
    title =
      builds.length === 1
        ? "Last build by result"
        : `Last ${builds.length} builds by result`;
    counts = { passed: 0, failed: 0, skipped: 0 };
    for (const build of builds) {
      if (build.failed > 0) {
        counts.failed++;
      } else if (build.passed > 0) {
        counts.passed++;
      } else {
        counts.skipped++;
      }
    }
  }
  return pieOptions(
    theme,
    title,
    [
      { name: "Passed", value: counts.passed, color: theme.passed },
      { name: "Failed", value: counts.failed, color: theme.failed },
      { name: "Skipped", value: counts.skipped, color: theme.skipped },
    ],
    showLabels,
  );
}

/** Passed and failed test runs; skipped tests are left out, as in the pass rate line chart. */
export function passRatePieOptions(
  theme: ChartTheme,
  builds: BuildTotals[],
  focus: BuildTotals | null,
  showLabels: boolean,
  label: BuildLabeler = numberLabels,
): EChartsOption {
  const covered = focus ? [focus] : builds;
  const title = focus
    ? `Pass rate, build ${label(focus.build).short}`
    : builds.length === 1
      ? "Pass rate, last build"
      : `Pass rate, last ${builds.length} builds`;
  const passed = covered.reduce((sum, build) => sum + build.passed, 0);
  const failed = covered.reduce((sum, build) => sum + build.failed, 0);
  return pieOptions(
    theme,
    title,
    [
      { name: "Passed", value: passed, color: theme.passed },
      { name: "Failed", value: failed, color: theme.failed },
    ],
    showLabels,
  );
}

/** Test cases of one build by run time, using the configured thresholds. */
export function runtimePieOptions(
  theme: ChartTheme,
  build: BuildTotals,
  thresholds: { low: number; high: number },
  showLabels: boolean,
  label: BuildLabeler = numberLabels,
): EChartsOption {
  let fast = 0;
  let medium = 0;
  let slow = 0;
  for (const time of build.runtimes) {
    if (time < thresholds.low) {
      fast++;
    } else if (time >= thresholds.high) {
      slow++;
    } else {
      medium++;
    }
  }
  return pieOptions(
    theme,
    `Test run times, build ${label(build.build).short}`,
    [
      {
        name: "Fast",
        hint: `under ${thresholds.low}s`,
        value: fast,
        color: theme.passed,
      },
      {
        name: "Medium",
        hint: `${thresholds.low}s to ${thresholds.high}s`,
        value: medium,
        color: theme.skipped,
      },
      {
        name: "Slow",
        hint: `${thresholds.high}s or more`,
        value: slow,
        color: theme.failed,
      },
    ],
    showLabels,
  );
}

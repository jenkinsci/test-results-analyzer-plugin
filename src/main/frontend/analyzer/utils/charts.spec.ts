import type { EChartsOption } from "echarts";
import { describe, expect, it } from "vitest";
import { createBuildLabeler } from "./buildLabels.ts";
import {
  barChartOptions,
  type ChartTheme,
  lineChartOptions,
  passFailPieOptions,
  passRatePieOptions,
  runtimePieOptions,
} from "./charts.ts";
import type { BuildTotals } from "./stats.ts";

const theme: ChartTheme = {
  text: "text",
  textSecondary: "secondary",
  border: "border",
  background: "background",
  passed: "green",
  failed: "red",
  skipped: "orange",
  total: "blue",
  fontFamily: "sans-serif",
};

function build(
  number: string,
  passed: number,
  failed: number,
  skipped = 0,
  runtimes: number[] = [],
): BuildTotals {
  return {
    build: number,
    passed,
    failed,
    skipped,
    total: passed + failed + skipped,
    runtime: runtimes.reduce((sum, time) => sum + time, 0),
    runtimes,
  };
}

// Oldest build first, as aggregate() returns them
const builds = [build("1", 4, 0, 1), build("2", 3, 1), build("3", 0, 0, 2)];

type Series = { name: string; data: unknown[]; itemStyle?: { color?: string } };

function series(options: EChartsOption): Series[] {
  return options.series as Series[];
}

function title(options: EChartsOption): string {
  return (options.title as { text: string }).text;
}

/** Pie slices as [name, value, colour]. */
function slices(options: EChartsOption) {
  const data = series(options)[0].data as {
    name: string;
    value: number;
    itemStyle: { color: string };
  }[];
  return data.map((slice) => [slice.name, slice.value, slice.itemStyle.color]);
}

function labels(options: EChartsOption) {
  return (series(options)[0] as unknown as { label: { show: boolean } }).label
    .show;
}

describe("lineChartOptions", () => {
  it("plots passed, failed, skipped and total tests per build", () => {
    const options = lineChartOptions(theme, builds, "passfail");
    expect(title(options)).toBe("Build status");
    expect((options.xAxis as { data: string[] }).data).toEqual([
      "#1",
      "#2",
      "#3",
    ]);
    expect(series(options).map((s) => [s.name, s.data])).toEqual([
      ["Passed", [4, 3, 0]],
      ["Failed", [0, 1, 0]],
      ["Skipped", [1, 0, 2]],
      ["Total", [5, 4, 2]],
    ]);
    expect(series(options)[1].itemStyle?.color).toBe("red");
  });

  it("plots the pass rate without skipped tests, leaving gaps for builds where nothing ran", () => {
    const options = lineChartOptions(theme, builds, "passrate");
    expect(title(options)).toBe("Pass rate");
    expect(series(options)[0].data).toEqual([100, 75, null]);
    expect((options.yAxis as { max: number }).max).toBe(100);
  });

  it("plots the run time per build", () => {
    const options = lineChartOptions(
      theme,
      [build("1", 1, 0, 0, [0.1234, 0.2])],
      "runtime",
    );
    expect(title(options)).toBe("Build run time");
    expect(series(options)[0].data).toEqual([0.323]);
  });
});

describe("barChartOptions", () => {
  it("stacks passed, failed and skipped tests per build", () => {
    const options = barChartOptions(theme, builds);
    expect(series(options).map((s) => [s.name, s.data])).toEqual([
      ["Passed", [4, 3, 0]],
      ["Failed", [0, 1, 0]],
      ["Skipped", [1, 0, 2]],
    ]);
    expect(
      new Set(
        series(options).map((s) => (s as unknown as { stack: string }).stack),
      ),
    ).toEqual(new Set(["results"]));
  });
});

describe("passFailPieOptions", () => {
  it("counts builds by result, a build with any failure counting as failed", () => {
    const options = passFailPieOptions(theme, builds, null, true);
    expect(title(options)).toBe("Last 3 builds by result");
    expect(slices(options)).toEqual([
      ["Passed", 1, "green"],
      ["Failed", 1, "red"],
      ["Skipped", 1, "orange"],
    ]);
  });

  it("shows the tests of a focused build, leaving out empty slices", () => {
    const options = passFailPieOptions(theme, builds, builds[1], true);
    expect(title(options)).toBe("Build #2");
    expect(slices(options)).toEqual([
      ["Passed", 3, "green"],
      ["Failed", 1, "red"],
    ]);
  });

  it("names a single build", () => {
    expect(title(passFailPieOptions(theme, [builds[0]], null, true))).toBe(
      "Last build by result",
    );
  });

  it("hides slice labels when there is no room", () => {
    expect(labels(passFailPieOptions(theme, builds, null, true))).toBe(true);
    expect(labels(passFailPieOptions(theme, builds, null, false))).toBe(false);
  });
});

describe("passRatePieOptions", () => {
  it("adds up passed and failed tests over the builds, without skipped tests", () => {
    const options = passRatePieOptions(theme, builds, null, true);
    expect(title(options)).toBe("Pass rate, last 3 builds");
    expect(slices(options)).toEqual([
      ["Passed", 7, "green"],
      ["Failed", 1, "red"],
    ]);
  });

  it("covers only a focused build", () => {
    const options = passRatePieOptions(theme, builds, builds[0], true);
    expect(title(options)).toBe("Pass rate, build #1");
    expect(slices(options)).toEqual([["Passed", 4, "green"]]);
  });
});

describe("runtimePieOptions", () => {
  it("sorts tests into fast, medium and slow by the thresholds", () => {
    const options = runtimePieOptions(
      theme,
      build("7", 6, 0, 0, [0.1, 0.5, 1, 1.49, 1.5, 3]),
      { low: 0.5, high: 1.5 },
      true,
    );
    expect(title(options)).toBe("Test run times, build #7");
    // The low threshold is medium; the high threshold is slow
    expect(slices(options)).toEqual([
      ["Fast", 1, "green"],
      ["Medium", 3, "orange"],
      ["Slow", 2, "red"],
    ]);
  });

  it("describes the thresholds in the tooltip", () => {
    const options = runtimePieOptions(
      theme,
      build("1", 1, 0, 0, [0.1]),
      { low: 1, high: 2 },
      true,
    );
    const formatter = (
      options.tooltip as { formatter: (params: unknown) => string }
    ).formatter;
    expect(
      formatter({
        name: "Fast",
        value: 1,
        percent: 100,
        data: { hint: "under 1s" },
      }),
    ).toBe("Fast (under 1s): 1 (100%)");
  });
});

describe("build labels", () => {
  const label = createBuildLabeler(
    [
      { number: 1, displayName: "nightly-2026-10-08" },
      { number: 2, displayName: "a display name that is far too long" },
      { number: 3, displayName: "#3" },
    ],
    "name",
  );

  it("labels the build axis, shortening long labels only on the axis", () => {
    for (const options of [
      lineChartOptions(theme, builds, "passfail", label),
      barChartOptions(theme, builds, label),
    ]) {
      const xAxis = options.xAxis as {
        data: string[];
        axisLabel: { formatter: (value: string) => string };
      };
      expect(xAxis.data).toEqual([
        "nightly-2026-10-08",
        "a display name that is far too long",
        "#3",
      ]);
      expect(xAxis.axisLabel.formatter(xAxis.data[1])).toBe(
        "a display name that…",
      );
    }
  });

  it("heads axis tooltips with the whole title of the hovered build", () => {
    const named = createBuildLabeler(
      [
        { number: 1, displayName: "nightly-2026-10-08" },
        { number: 2, displayName: "<b>a & b</b>" },
      ],
      "name",
    );
    const params = (dataIndex: number, values: unknown[]) =>
      values.map((value, index) => ({
        dataIndex,
        marker: `<span class="marker${index}"></span>`,
        seriesName: `Series <${index}>`,
        value,
      }));
    const format = (options: EChartsOption, param: unknown) =>
      (options.tooltip as { formatter: (params: unknown) => string }).formatter(
        param,
      );

    const bar = format(
      barChartOptions(theme, builds, named),
      params(1, [3, 1]),
    );
    expect(bar).toContain("#2 &lt;b&gt;a &amp; b&lt;/b&gt;");
    expect(bar).not.toContain("<b>");
    expect(bar).toContain('<span class="marker0"></span>Series &lt;0&gt;');
    expect(bar).toContain(">3</strong>");
    expect(bar).toContain(">1</strong>");

    const line = format(
      lineChartOptions(theme, builds, "passfail", named),
      params(0, [4]),
    );
    expect(line).toContain("#1 nightly-2026-10-08");

    expect(
      format(
        lineChartOptions(theme, builds, "passrate", named),
        params(2, [null]),
      ),
    ).toContain(">–</strong>");
    expect(
      format(
        lineChartOptions(theme, builds, "passrate", named),
        params(0, [80]),
      ),
    ).toContain(">80%</strong>");
    expect(
      format(
        lineChartOptions(theme, builds, "runtime", named),
        params(0, [1.5]),
      ),
    ).toContain(">1.5 s</strong>");
  });

  it("titles the pie of a clicked build with its label", () => {
    expect(
      title(passFailPieOptions(theme, builds, builds[0], false, label)),
    ).toBe("Build nightly-2026-10-08");
    expect(
      title(passRatePieOptions(theme, builds, builds[0], false, label)),
    ).toBe("Pass rate, build nightly-2026-10-08");
    expect(
      title(
        runtimePieOptions(theme, builds[2], { low: 1, high: 2 }, false, label),
      ),
    ).toBe("Test run times, build #3");
  });
});

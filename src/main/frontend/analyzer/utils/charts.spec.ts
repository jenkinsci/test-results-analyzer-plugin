import type { EChartsOption } from "echarts";
import { describe, expect, it } from "vitest";

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

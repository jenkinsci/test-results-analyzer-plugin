import { useCallback, useMemo, useState } from "react";

import type { ChartData, TreeNode } from "../model.ts";
import type { BuildLabeler } from "../utils/buildLabels.ts";
import {
  barChartOptions,
  type ChartTheme,
  lineChartOptions,
  passFailPieOptions,
  passRatePieOptions,
  runtimePieOptions,
} from "../utils/charts.ts";
import { aggregate, type BuildTotals } from "../utils/stats.ts";
import { EChart } from "./EChart.tsx";

interface ChartsProps {
  /** The rows the line, bar and pass/fail charts cover. */
  nodes: TreeNode[];
  /** The test cases the run time pie covers. */
  tests: TreeNode[];
  mode: ChartData;
  line: boolean;
  bar: boolean;
  pie: boolean;
  thresholds: { low: number; high: number };
  buildLabel?: BuildLabeler;
}

interface Focus {
  build: string;
  /** The chart data the build was clicked in; the focus is dropped when that changes. */
  builds: BuildTotals[];
  mode: ChartData;
}

export function Charts({
  nodes,
  tests,
  mode,
  line,
  bar,
  pie,
  thresholds,
  buildLabel,
}: ChartsProps) {
  const builds = useMemo(() => aggregate(nodes), [nodes]);
  const testBuilds = useMemo(() => aggregate(tests), [tests]);
  // The build clicked on the line chart is shown in the pie chart
  const [focus, setFocus] = useState<Focus | null>(null);
  const focused =
    focus && focus.builds === builds && focus.mode === mode
      ? (builds.find((build) => build.build === focus.build) ?? null)
      : null;

  const showBar = bar && mode === "passfail";
  const hasData = builds.length > 0;

  const lineOption = useCallback(
    (theme: ChartTheme) => lineChartOptions(theme, builds, mode, buildLabel),
    [builds, mode, buildLabel],
  );

  const barOption = useCallback(
    (theme: ChartTheme) => barChartOptions(theme, builds, buildLabel),
    [builds, buildLabel],
  );

  const pieOption = useCallback(
    (theme: ChartTheme, wide: boolean) => {
      if (mode === "runtime") {
        const build = focused ?? builds[builds.length - 1];
        const match = testBuilds.find(
          (candidate) => candidate.build === build?.build,
        );
        return match
          ? runtimePieOptions(theme, match, thresholds, wide, buildLabel)
          : {};
      }
      if (mode === "passrate") {
        return passRatePieOptions(theme, builds, focused, wide, buildLabel);
      }
      return passFailPieOptions(theme, builds, focused, wide, buildLabel);
    },
    [mode, builds, testBuilds, focused, thresholds, buildLabel],
  );

  const onColumnClick = useCallback(
    (index: number) => {
      if (builds[index]) {
        setFocus({ build: builds[index].build, builds, mode });
      }
    },
    [builds, mode],
  );

  if (!hasData || !(line || showBar || pie)) {
    return null;
  }
  return (
    <section id="tra-charts-section" className="tra-section">
      <h2 className="jenkins-section__title">Charts</h2>
      <div className="tra-charts">
        {line && (
          <div
            id="tra-chart-line-container"
            className="jenkins-card tra-chart-card tra-chart-card--wide"
          >
            <EChart buildOption={lineOption} onColumnClick={onColumnClick} />
          </div>
        )}
        {showBar && (
          <div
            id="tra-chart-bar-container"
            className="jenkins-card tra-chart-card"
          >
            <EChart buildOption={barOption} />
          </div>
        )}
        {pie && (
          <div
            id="tra-chart-pie-container"
            className={`jenkins-card tra-chart-card${showBar ? "" : " tra-chart-card--wide"}`}
          >
            <EChart buildOption={pieOption} />
          </div>
        )}
      </div>
    </section>
  );
}

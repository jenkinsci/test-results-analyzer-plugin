import { useCallback, useMemo, useState } from "react";

import type { ChartData, CountBy, TreeNode } from "../model.ts";
import type { BuildLabeler } from "../utils/buildLabels.ts";
import {
  barChartOptions,
  type ChartTheme,
  lineChartOptions,
  passFailPieOptions,
  passRatePieOptions,
  runtimePieOptions,
} from "../utils/charts.ts";
import {
  aggregate,
  aggregateGroups,
  type BuildTotals,
} from "../utils/stats.ts";
import { EChart } from "./EChart.tsx";

interface ChartsProps {
  /** The rows the line, bar and pass/fail charts cover. */
  nodes: TreeNode[];
  /** The test cases the run time pie covers. */
  tests: TreeNode[];
  /** The test cases grouped by class or package, when counting those rather than tests. */
  groups: TreeNode[][] | null;
  countBy: CountBy;
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
  groups,
  countBy,
  mode,
  line,
  bar,
  pie,
  thresholds,
  buildLabel,
}: ChartsProps) {
  // Run times add up the same way whatever is counted, so only passes and failures are grouped
  const unit = mode === "runtime" || !groups ? "tests" : countBy;
  const builds = useMemo(
    () =>
      unit === "tests" || !groups ? aggregate(nodes) : aggregateGroups(groups),
    [unit, nodes, groups],
  );
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
    (theme: ChartTheme) =>
      lineChartOptions(theme, builds, mode, buildLabel, unit),
    [builds, mode, buildLabel, unit],
  );

  const barOption = useCallback(
    (theme: ChartTheme) => barChartOptions(theme, builds, buildLabel, unit),
    [builds, buildLabel, unit],
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
        return passRatePieOptions(
          theme,
          builds,
          focused,
          wide,
          buildLabel,
          unit,
        );
      }
      return passFailPieOptions(theme, builds, focused, wide, buildLabel, unit);
    },
    [mode, unit, builds, testBuilds, focused, thresholds, buildLabel],
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

export type Status = "PASSED" | "FAILED" | "SKIPPED" | "N/A";

/** The result of a package, class or test in one build, as served by JsTreeUtil. */
export interface BuildResult {
  buildNumber: string;
  status: Status;
  totalTests?: number;
  totalFailed?: number;
  totalPassed?: number;
  totalSkipped?: number;
  totalTimeTaken?: number;
  url?: string;
}

export interface TreeNode {
  text: string;
  buildResults: BuildResult[];
  children?: TreeNode[];
}

/** Details of a build, as served by JsTreeUtil alongside the build numbers. */
export interface BuildInfo {
  number: number;
  displayName?: string;
  /** When the build was scheduled, in milliseconds since the epoch. */
  timestamp?: number;
  url?: string;
}

export interface AnalyzerData {
  /** Build numbers, newest first. */
  builds: string[];
  /** Details of the builds, in the same order; missing when served by an older version. */
  buildInfo?: BuildInfo[];
  results: TreeNode[];
}

/** What builds are labelled by. */
export type BuildLabelMode = "number" | "name" | "date";

export type ChartData = "passfail" | "passrate" | "runtime";

export interface StatusLabels {
  passed: string;
  failed: string;
  skipped: string;
  na: string;
}

/** Page configuration rendered by the Jelly view into the data-bootstrap attribute. */
export interface Bootstrap {
  labels: StatusLabels;
  runTimeLowThreshold: number;
  runTimeHighThreshold: number;
  /** Colours set by the administrator, or null to use the theme's status colours. */
  customColors: StatusLabels | null;
  /** The most builds that may be chosen at once, capped by the administrator's limit on the runs to fetch. */
  maxChosenBuilds: number;
  defaults: {
    noOfBuilds: string;
    showAllBuilds: boolean;
    showBuildTime: boolean;
    hideConfigurationMethods: boolean;
    showLineGraph: boolean;
    showBarGraph: boolean;
    showPieGraph: boolean;
    chartDataType: string;
    /** Missing when the page is served by an older version. */
    buildLabel?: string;
  };
}

export interface Options {
  /** Whether to show the latest builds, or the builds listed in buildNumbers. */
  buildMode: "latest" | "specific";
  builds: string;
  allBuilds: boolean;
  /** Build numbers and ranges as typed, such as "12, 36, 40-53". */
  buildNumbers: string;
  /** Show only the tests whose status differs between the builds shown. */
  onlyDiffering: boolean;
  showDurations: boolean;
  showNotRun: boolean;
  hideConfig: boolean;
  line: boolean;
  bar: boolean;
  pie: boolean;
  /** As typed; see worstLimit. */
  worstCount: string;
  chartData: ChartData;
  buildLabel: BuildLabelMode;
}

/** A package, class or test in the flattened history, in display order. */
export interface Row {
  id: number;
  node: TreeNode;
  level: number;
  /** Id of the parent row, or -1 for a package. */
  parent: number;
  children: number[];
}

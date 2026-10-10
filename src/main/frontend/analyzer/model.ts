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

export interface AnalyzerData {
  /** Build numbers, newest first. */
  builds: string[];
  results: TreeNode[];
}

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
  defaults: {
    noOfBuilds: string;
    showAllBuilds: boolean;
    showBuildTime: boolean;
    hideConfigurationMethods: boolean;
    showLineGraph: boolean;
    showBarGraph: boolean;
    showPieGraph: boolean;
    chartDataType: string;
  };
}

export interface Options {
  builds: string;
  allBuilds: boolean;
  showDurations: boolean;
  showNotRun: boolean;
  hideConfig: boolean;
  line: boolean;
  bar: boolean;
  pie: boolean;
  /** As typed; see worstLimit. */
  worstCount: string;
  chartData: ChartData;
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

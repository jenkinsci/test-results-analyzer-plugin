import type {
  AnalyzerData,
  BuildResult,
  Bootstrap,
  Status,
  TreeNode,
} from "./model.ts";

/** A result for one build; counts are derived from the status of a single test. */
export function result(
  buildNumber: string,
  status: Status,
  time = 0.1,
): BuildResult {
  if (status === "N/A") {
    return { buildNumber, status };
  }
  return {
    buildNumber,
    status,
    totalTests: 1,
    totalPassed: status === "PASSED" ? 1 : 0,
    totalFailed: status === "FAILED" ? 1 : 0,
    totalSkipped: status === "SKIPPED" ? 1 : 0,
    totalTimeTaken: time,
    url: `http://jenkins/job/demo/${buildNumber}/`,
  };
}

export function test(text: string, statuses: Status[]): TreeNode {
  return {
    text,
    buildResults: statuses.map((status, index) =>
      result(String(statuses.length - index), status),
    ),
    children: [],
  };
}

/** A class or package whose results add up those of its children. */
export function group(text: string, children: TreeNode[]): TreeNode {
  const buildResults = children[0].buildResults.map((first, index) => {
    const results = children.map((child) => child.buildResults[index]);
    const ran = results.filter((r) => r.status !== "N/A");
    if (ran.length === 0) {
      return { buildNumber: first.buildNumber, status: "N/A" as const };
    }
    const sum = (
      key: "totalTests" | "totalPassed" | "totalFailed" | "totalSkipped",
    ) => ran.reduce((total, r) => total + (r[key] ?? 0), 0);
    const failed = sum("totalFailed");
    return {
      buildNumber: first.buildNumber,
      status: (failed > 0
        ? "FAILED"
        : sum("totalPassed") > 0
          ? "PASSED"
          : "SKIPPED") as Status,
      totalTests: sum("totalTests"),
      totalPassed: sum("totalPassed"),
      totalFailed: failed,
      totalSkipped: sum("totalSkipped"),
      totalTimeTaken: ran.reduce(
        (total, r) => total + (r.totalTimeTaken ?? 0),
        0,
      ),
      url: first.url,
    };
  });
  return { text, buildResults, children };
}

/** Builds 3, 2 and 1: testA always passes, testB fails in the latest build only. */
export function calculatorData(): AnalyzerData {
  return {
    builds: ["3", "2", "1"],
    results: [
      group("com.example", [
        group("CalculatorTest", [
          test("testA", ["PASSED", "PASSED", "PASSED"]),
          test("testB", ["FAILED", "PASSED", "PASSED"]),
        ]),
      ]),
    ],
  };
}

export function bootstrap(overrides: Partial<Bootstrap> = {}): Bootstrap {
  return {
    labels: {
      passed: "PASSED",
      failed: "FAILED",
      skipped: "SKIPPED",
      na: "N/A",
    },
    runTimeLowThreshold: 0.5,
    runTimeHighThreshold: 1.5,
    customColors: null,
    maxChosenBuilds: 10_000,
    defaults: {
      noOfBuilds: "10",
      showAllBuilds: false,
      showBuildTime: false,
      hideConfigurationMethods: false,
      showLineGraph: true,
      showBarGraph: true,
      showPieGraph: true,
      chartDataType: "passfail",
    },
    ...overrides,
  };
}

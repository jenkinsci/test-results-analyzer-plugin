import type { BuildResult, Row, Status, TreeNode } from "../model.ts";

export function isNewFailure(buildResults: BuildResult[]): boolean {
  return (
    buildResults.length >= 2 &&
    buildResults[0].status === "FAILED" &&
    buildResults[1].status === "PASSED"
  );
}

export interface PassRate {
  /** Percentage of builds in which nothing failed. */
  builds: number;
  /** Percentage of test runs that passed. */
  tests: number;
}

/** Pass rates over the builds that ran, or null when nothing passed or failed. */
export function percentPassed(buildResults: BuildResult[]): PassRate | null {
  let buildsPassed = 0;
  let buildsFailed = 0;
  let testsPassed = 0;
  let testsFailed = 0;

  for (const result of buildResults) {
    if (result.status === "N/A") {
      continue;
    }
    const failed = result.totalFailed ?? 0;
    const passed = result.totalPassed ?? 0;
    if (failed > 0) {
      buildsFailed++;
    } else if (passed > 0) {
      buildsPassed++;
    }
    testsPassed += passed;
    testsFailed += failed;
  }

  const totalBuilds = buildsPassed + buildsFailed;
  const totalTests = testsPassed + testsFailed;
  if (totalBuilds === 0 || totalTests === 0) {
    return null;
  }
  return {
    builds: Math.round((100 * buildsPassed) / totalBuilds),
    tests: Math.round((100 * testsPassed) / totalTests),
  };
}

/** How often the result flipped between passing and failing, oldest build first. */
export function numberOfTransitions(buildResults: BuildResult[]): number {
  let previousPassed: boolean | null = null;
  let result = 0;

  // Newest build first, so walk backwards to go forward in time.
  for (let i = buildResults.length - 1; i >= 0; i--) {
    const build = buildResults[i];
    if (build.status === "N/A") {
      continue;
    }
    const failed = (build.totalFailed ?? 0) > 0;
    const passed = !failed && (build.totalPassed ?? 0) > 0;
    if (!failed && !passed) {
      continue;
    }
    if (previousPassed !== null && previousPassed !== passed) {
      result++;
    }
    previousPassed = passed;
  }
  return result;
}

function didNotRun(node: TreeNode): boolean {
  return node.buildResults.every((result) => result.status === "N/A");
}

/**
 * Flattens the tree into display order. Tests that were renamed or removed have no results in the
 * builds shown, so they are left out unless showNotRun is set.
 */
export function flattenTree(results: TreeNode[], showNotRun: boolean): Row[] {
  const rows: Row[] = [];
  const visit = (node: TreeNode, level: number, parent: number) => {
    if (!showNotRun && didNotRun(node)) {
      return;
    }
    const row: Row = { id: rows.length, node, level, parent, children: [] };
    rows.push(row);
    if (parent >= 0) {
      rows[parent].children.push(row.id);
    }
    for (const child of node.children ?? []) {
      visit(child, level + 1, row.id);
    }
  };
  for (const node of results) {
    visit(node, 0, -1);
  }
  return rows;
}

export interface BrokenTest {
  name: string;
  builds: { buildNumber: string; url?: string }[];
}

/** The test cases that failed in the most builds; packages and classes are not counted. */
export function worstTests(results: TreeNode[], limit: number): BrokenTest[] {
  const failures = new Map<string, BrokenTest>();

  const visit = (node: TreeNode, path: string) => {
    const name = path === "" ? node.text : `${path}.${node.text}`;
    if (node.children && node.children.length > 0) {
      for (const child of node.children) {
        visit(child, name);
      }
      return;
    }
    for (const result of node.buildResults) {
      if (result.status !== "FAILED") {
        continue;
      }
      let test = failures.get(name);
      if (!test) {
        test = { name, builds: [] };
        failures.set(name, test);
      }
      test.builds.push({ buildNumber: result.buildNumber, url: result.url });
    }
  };
  for (const node of results) {
    visit(node, "");
  }

  return [...failures.values()]
    .sort((a, b) => b.builds.length - a.builds.length)
    .slice(0, limit);
}

export interface BuildTotals {
  build: string;
  passed: number;
  failed: number;
  skipped: number;
  total: number;
  /** Sum of the run times, in seconds. */
  runtime: number;
  /** Run time of each node that has one, in seconds. */
  runtimes: number[];
}

/** Adds up the results of the nodes per build, oldest build first. */
export function aggregate(nodes: TreeNode[]): BuildTotals[] {
  const perBuild = new Map<string, BuildTotals>();
  for (const node of nodes) {
    for (const result of node.buildResults) {
      let entry = perBuild.get(result.buildNumber);
      if (!entry) {
        entry = {
          build: result.buildNumber,
          passed: 0,
          failed: 0,
          skipped: 0,
          total: 0,
          runtime: 0,
          runtimes: [],
        };
        perBuild.set(result.buildNumber, entry);
      }
      entry.passed += result.totalPassed ?? 0;
      entry.failed += result.totalFailed ?? 0;
      entry.skipped += result.totalSkipped ?? 0;
      entry.total += result.totalTests ?? 0;
      if (typeof result.totalTimeTaken === "number") {
        entry.runtime += result.totalTimeTaken;
        entry.runtimes.push(result.totalTimeTaken);
      }
    }
  }
  return [...perBuild.values()].sort(
    (a, b) => Number.parseInt(a.build, 10) - Number.parseInt(b.build, 10),
  );
}

/**
 * The status of a class or package made up of the given results, worked out as Jenkins does: skipped
 * when every test that ran was skipped, failed when any failed, otherwise passed. A class or package
 * without any tests, such as one whose only tests are hidden configuration methods, did not run.
 */
export function combinedStatus(results: BuildResult[]): Status {
  const ran = results.filter(
    (result) => result.status !== "N/A" && result.totalTests !== 0,
  );
  if (ran.length === 0) {
    return "N/A";
  }
  if (ran.some((result) => result.status === "FAILED")) {
    return "FAILED";
  }
  return ran.every((result) => result.status === "SKIPPED")
    ? "SKIPPED"
    : "PASSED";
}

/**
 * Counts each group of tests, such as the tests of a class, once per build by their combined
 * status, oldest build first. A group none of whose tests ran in a build is not counted in it.
 */
export function aggregateGroups(groups: TreeNode[][]): BuildTotals[] {
  const perBuild = new Map<string, BuildTotals>();
  for (const group of groups) {
    const resultsPerBuild = new Map<string, BuildResult[]>();
    for (const node of group) {
      for (const result of node.buildResults) {
        let results = resultsPerBuild.get(result.buildNumber);
        if (!results) {
          results = [];
          resultsPerBuild.set(result.buildNumber, results);
        }
        results.push(result);
      }
    }
    for (const [build, results] of resultsPerBuild) {
      let entry = perBuild.get(build);
      if (!entry) {
        entry = {
          build,
          passed: 0,
          failed: 0,
          skipped: 0,
          total: 0,
          runtime: 0,
          runtimes: [],
        };
        perBuild.set(build, entry);
      }
      const status = combinedStatus(results);
      if (status === "N/A") {
        continue;
      }
      entry.total++;
      if (status === "FAILED") {
        entry.failed++;
      } else if (status === "SKIPPED") {
        entry.skipped++;
      } else {
        entry.passed++;
      }
      const times = results
        .map((result) => result.totalTimeTaken)
        .filter((time) => typeof time === "number");
      if (times.length > 0) {
        const time = times.reduce((sum, value) => sum + value, 0);
        entry.runtime += time;
        entry.runtimes.push(time);
      }
    }
  }
  return [...perBuild.values()].sort(
    (a, b) => Number.parseInt(a.build, 10) - Number.parseInt(b.build, 10),
  );
}

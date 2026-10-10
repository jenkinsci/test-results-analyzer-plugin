import { describe, expect, it } from "vitest";

import { calculatorData, group, result, test } from "../fixtures.ts";
import type { Status } from "../model.ts";
import {
  aggregate,
  aggregateGroups,
  combinedStatus,
  flattenTree,
  isNewFailure,
  numberOfTransitions,
  percentPassed,
  worstTests,
} from "./stats.ts";

describe("isNewFailure", () => {
  it("is true when the latest build failed after the previous one passed", () => {
    expect(isNewFailure(test("t", ["FAILED", "PASSED"]).buildResults)).toBe(
      true,
    );
  });

  it("is false for repeated failures and single builds", () => {
    expect(isNewFailure(test("t", ["FAILED", "FAILED"]).buildResults)).toBe(
      false,
    );
    expect(isNewFailure(test("t", ["FAILED"]).buildResults)).toBe(false);
  });
});

describe("percentPassed", () => {
  it("returns build and test pass rates, ignoring builds the test did not run in", () => {
    expect(
      percentPassed(
        test("t", ["FAILED", "PASSED", "PASSED", "N/A"]).buildResults,
      ),
    ).toEqual({
      builds: 67,
      tests: 67,
    });
  });

  it("separates builds that passed from test runs that passed", () => {
    const pkg = group("p", [
      test("a", ["FAILED", "PASSED"]),
      test("b", ["PASSED", "PASSED"]),
    ]);
    expect(percentPassed(pkg.buildResults)).toEqual({ builds: 50, tests: 75 });
  });

  it("returns null when nothing ran", () => {
    expect(
      percentPassed(test("t", ["N/A", "SKIPPED"]).buildResults),
    ).toBeNull();
  });
});

describe("numberOfTransitions", () => {
  it("counts flips between passing and failing, skipping builds without a result", () => {
    expect(
      numberOfTransitions(
        test("t", ["PASSED", "N/A", "FAILED", "SKIPPED", "PASSED", "PASSED"])
          .buildResults,
      ),
    ).toBe(2);
  });
});

describe("flattenTree", () => {
  const data = {
    results: [
      group("p", [
        group("T", [
          test("kept", ["PASSED", "PASSED"]),
          test("removed", ["N/A", "N/A"]),
        ]),
      ]),
    ],
  };

  it("lists rows in display order with their parents and children", () => {
    const rows = flattenTree(calculatorData().results, false);
    expect(rows.map((row) => [row.node.text, row.level, row.parent])).toEqual([
      ["com.example", 0, -1],
      ["CalculatorTest", 1, 0],
      ["testA", 2, 1],
      ["testB", 2, 1],
    ]);
    expect(rows[1].children).toEqual([2, 3]);
  });

  it("leaves out tests that did not run unless asked for", () => {
    expect(
      flattenTree(data.results, false).map((row) => row.node.text),
    ).toEqual(["p", "T", "kept"]);
    expect(flattenTree(data.results, true).map((row) => row.node.text)).toEqual(
      ["p", "T", "kept", "removed"],
    );
  });
});

describe("worstTests", () => {
  it("counts only test cases, most failures first, up to the limit", () => {
    const results = [
      group("p", [
        group("T", [
          test("once", ["FAILED", "PASSED"]),
          test("twice", ["FAILED", "FAILED"]),
          test("never", ["PASSED", "PASSED"]),
        ]),
      ]),
    ];
    expect(
      worstTests(results, 10).map((t) => [t.name, t.builds.length]),
    ).toEqual([
      ["p.T.twice", 2],
      ["p.T.once", 1],
    ]);
    expect(worstTests(results, 1)).toHaveLength(1);
  });

  it("links each failure to its build", () => {
    const [worst] = worstTests(calculatorData().results, 10);
    expect(worst.builds).toEqual([
      { buildNumber: "3", url: "http://jenkins/job/demo/3/" },
    ]);
  });
});

describe("aggregate", () => {
  it("adds up results per build, oldest build first", () => {
    const totals = aggregate([
      test("a", ["FAILED", "PASSED"]),
      {
        text: "b",
        buildResults: [result("2", "PASSED", 2), result("1", "SKIPPED", 1)],
      },
    ]);
    expect(
      totals.map((t) => [t.build, t.passed, t.failed, t.skipped, t.total]),
    ).toEqual([
      ["1", 1, 0, 1, 2],
      ["2", 1, 1, 0, 2],
    ]);
    expect(totals[1].runtime).toBeCloseTo(2.1);
    expect(totals[1].runtimes).toEqual([0.1, 2]);
  });
});

describe("combinedStatus", () => {
  const results = (...statuses: Status[]) =>
    statuses.map((status) => result("1", status));

  it("fails when any test failed", () => {
    expect(combinedStatus(results("PASSED", "FAILED", "SKIPPED"))).toBe(
      "FAILED",
    );
  });

  it("passes when nothing failed and something passed", () => {
    expect(combinedStatus(results("PASSED", "SKIPPED", "N/A"))).toBe("PASSED");
  });

  it("is skipped when every test that ran was skipped", () => {
    expect(combinedStatus(results("SKIPPED", "N/A"))).toBe("SKIPPED");
  });

  it("has no result when nothing ran", () => {
    expect(combinedStatus(results("N/A", "N/A"))).toBe("N/A");
    expect(combinedStatus([])).toBe("N/A");
  });
});

describe("aggregateGroups", () => {
  // Builds 3, 2 and 1, newest first
  const groups = [
    // A class with a failure in build 3 only
    [
      test("a", ["FAILED", "PASSED", "PASSED"]),
      test("b", ["PASSED", "PASSED", "PASSED"]),
    ],
    // A class whose tests were all skipped in build 2 and did not exist in build 1
    [test("c", ["PASSED", "SKIPPED", "N/A"])],
    // A class that only ran in build 1
    [test("d", ["N/A", "N/A", "FAILED"])],
  ];

  it("counts each group once per build by its combined status, oldest build first", () => {
    expect(
      aggregateGroups(groups).map((t) => [
        t.build,
        t.passed,
        t.failed,
        t.skipped,
        t.total,
      ]),
    ).toEqual([
      ["1", 1, 1, 0, 2],
      ["2", 1, 0, 1, 2],
      ["3", 1, 1, 0, 2],
    ]);
  });

  it("keeps builds in which no group ran", () => {
    expect(
      aggregateGroups([[test("a", ["PASSED", "N/A"])]]).map((t) => [
        t.build,
        t.total,
      ]),
    ).toEqual([
      ["1", 0],
      ["2", 1],
    ]);
  });

  it("adds up the run time of each group", () => {
    const [, , latest] = aggregateGroups(groups);
    expect(latest.runtimes.map((time) => Math.round(time * 10) / 10)).toEqual([
      0.2, 0.1,
    ]);
    expect(latest.runtime).toBeCloseTo(0.3);
  });

  it("matches counting tests when every group has one test", () => {
    const tests = [
      test("a", ["FAILED", "PASSED"]),
      test("b", ["SKIPPED", "N/A"]),
    ];
    const byTest = aggregate(tests).map((t) => [t.passed, t.failed, t.skipped]);
    expect(
      aggregateGroups(tests.map((node) => [node])).map((t) => [
        t.passed,
        t.failed,
        t.skipped,
      ]),
    ).toEqual(byTest);
  });
});

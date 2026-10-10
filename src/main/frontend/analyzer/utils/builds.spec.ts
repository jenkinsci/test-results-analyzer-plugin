import { describe, expect, it } from "vitest";

import { group, test } from "../fixtures.ts";
import {
  buildNumbersError,
  buildRequest,
  differingTests,
  MAX_BUILDS,
  MAX_LENGTH,
} from "./builds.ts";

describe("buildNumbersError", () => {
  it("accepts build numbers and ranges as the server does", () => {
    for (const spec of [
      "12",
      "12,36,40-53",
      " 53 , 42,36 ,12 ",
      "5-3, 4, ,3,",
      "7 - 7",
      `1-${MAX_BUILDS}`,
    ]) {
      expect(buildNumbersError(spec), spec).toBeNull();
    }
  });

  it("explains what is wrong", () => {
    expect(buildNumbersError("")).toMatch(/Enter build numbers/);
    expect(buildNumbersError(" , ,")).toMatch(/Enter build numbers/);
    expect(buildNumbersError("1, abc")).toBe(
      '"abc" is not a build number or a range such as 40-53.',
    );
    expect(buildNumbersError("0-3")).toBe("Build numbers start at 1.");
    expect(buildNumbersError("1-999999999")).toMatch(/At most/);
    expect(buildNumbersError(`1-${MAX_BUILDS},1`)).toMatch(/At most/);
    expect(buildNumbersError("1, 500, 1000", 3)).toBeNull();
    expect(buildNumbersError("1-4", 3)).toBe("At most 3 builds can be chosen.");
    expect(buildNumbersError("1,".repeat(MAX_LENGTH))).toMatch(/at most/);
  });

  it("refuses anything that is not a plain build number or range", () => {
    for (const spec of [
      "-5",
      "5-",
      "1-2-3",
      "1;2",
      "1.5",
      "+3",
      "1e3",
      "9999999999",
      "１２",
      "1 -2",
      "lastBuild",
    ]) {
      expect(buildNumbersError(spec), spec).not.toBeNull();
    }
  });
});

describe("buildRequest", () => {
  const options = {
    buildMode: "latest" as const,
    builds: "10",
    allBuilds: false,
    buildNumbers: " 12, 36 ",
  };

  it("asks for the latest builds", () => {
    expect(buildRequest(options)).toEqual({ builds: "10" });
    expect(buildRequest({ ...options, allBuilds: true })).toEqual({
      builds: "-1",
    });
  });

  it("asks for the chosen builds", () => {
    expect(buildRequest({ ...options, buildMode: "specific" })).toEqual({
      builds: "10",
      buildNumbers: "12, 36",
    });
  });
});

describe("differingTests", () => {
  it("keeps only the tests whose status changed, with their classes and packages", () => {
    const results = [
      group("p", [
        group("A", [
          test("same", ["PASSED", "PASSED"]),
          test("broke", ["FAILED", "PASSED"]),
          test("added", ["PASSED", "N/A"]),
        ]),
        group("B", [test("stable", ["FAILED", "FAILED"])]),
      ]),
      group("q", [group("C", [test("skipped", ["SKIPPED", "SKIPPED"])])]),
    ];

    const kept = differingTests(results);
    expect(kept.map((node) => node.text)).toEqual(["p"]);
    const classes = kept[0].children ?? [];
    expect(classes.map((node) => node.text)).toEqual(["A"]);
    expect(classes[0].children?.map((node) => node.text)).toEqual([
      "broke",
      "added",
    ]);
    // The results of the package still cover all its tests
    expect(kept[0].buildResults).toBe(results[0].buildResults);
  });

  it("keeps nothing when only one build is shown", () => {
    expect(differingTests([group("p", [test("t", ["FAILED"])])])).toEqual([]);
  });
});

import { describe, expect, it } from "vitest";

import { calculatorData, group, test } from "../fixtures.ts";
import { flattenTree } from "./stats.ts";
import {
  chartNodes,
  expandAll,
  testGroups,
  testNodes,
  toggleChecked,
  toggleExpanded,
  visibleRows,
} from "./tree.ts";

// 0 com.example, 1 CalculatorTest, 2 testA, 3 testB
const rows = flattenTree(calculatorData().results, false);

const names = (visible: boolean[]) =>
  rows.filter((row) => visible[row.id]).map((row) => row.node.text);

describe("visibleRows", () => {
  it("shows packages and the children of expanded rows", () => {
    expect(names(visibleRows(rows, new Set(), ""))).toEqual(["com.example"]);
    expect(names(visibleRows(rows, new Set([0]), ""))).toEqual([
      "com.example",
      "CalculatorTest",
    ]);
    expect(names(visibleRows(rows, new Set([1]), ""))).toEqual(["com.example"]);
  });

  it("shows matching rows while filtering, ignoring case", () => {
    expect(names(visibleRows(rows, new Set(), "TESTB"))).toEqual(["testB"]);
    expect(names(visibleRows(rows, new Set([1]), "calc"))).toEqual([
      "CalculatorTest",
      "testA",
      "testB",
    ]);
  });
});

describe("toggleExpanded", () => {
  it("collapses the descendants of a collapsed row", () => {
    const all = expandAll(rows);
    expect([...all].sort()).toEqual([0, 1]);
    expect([...toggleExpanded(rows, all, 0, false)]).toEqual([]);
  });

  it("expands the ancestors of a row expanded while filtering", () => {
    expect([...toggleExpanded(rows, new Set(), 1, true)].sort()).toEqual([
      0, 1,
    ]);
    expect([...toggleExpanded(rows, new Set(), 1, false)]).toEqual([1]);
  });
});

describe("toggleChecked", () => {
  it("ticks descendants, and the parent once all its children are ticked", () => {
    expect([...toggleChecked(rows, new Set(), 1, true)].sort()).toEqual([
      0, 1, 2, 3,
    ]);
    const one = toggleChecked(rows, new Set(), 2, true);
    expect([...one]).toEqual([2]);
    expect([...toggleChecked(rows, one, 3, true)].sort()).toEqual([0, 1, 2, 3]);
  });

  it("unticks ancestors and descendants", () => {
    const all = new Set([0, 1, 2, 3]);
    expect([...toggleChecked(rows, all, 2, false)]).toEqual([3]);
    expect([...toggleChecked(rows, all, 1, false)]).toEqual([]);
  });
});

describe("chart nodes", () => {
  it("covers every package when nothing is ticked", () => {
    expect(chartNodes(rows, new Set()).map((node) => node.text)).toEqual([
      "com.example",
    ]);
    expect(testNodes(rows, new Set()).map((node) => node.text)).toEqual([
      "testA",
      "testB",
    ]);
  });

  it("covers ticked rows without counting a row and its parent twice", () => {
    expect(
      chartNodes(rows, new Set([1, 2, 3])).map((node) => node.text),
    ).toEqual(["CalculatorTest"]);
    expect(chartNodes(rows, new Set([3])).map((node) => node.text)).toEqual([
      "testB",
    ]);
    expect(testNodes(rows, new Set([3])).map((node) => node.text)).toEqual([
      "testB",
    ]);
  });

  it("does not treat a class whose tests are hidden as a test", () => {
    const hidden = flattenTree(
      [
        group("p", [
          group("T", [test("gone", ["N/A"])]),
          group("U", [test("u", ["PASSED"])]),
        ]),
      ],
      false,
    );
    expect(testNodes(hidden, new Set()).map((node) => node.text)).toEqual([
      "u",
    ]);
  });
});

describe("testGroups", () => {
  // 0 p, 1 T, 2 a, 3 b, 4 U, 5 c, 6 q, 7 V, 8 d, 9 Empty
  const tree = flattenTree(
    [
      group("p", [
        group("T", [test("a", ["PASSED"]), test("b", ["FAILED"])]),
        group("U", [test("c", ["PASSED"])]),
      ]),
      group("q", [
        group("V", [test("d", ["SKIPPED"])]),
        {
          text: "Empty",
          buildResults: [{ buildNumber: "1", status: "N/A" }],
          children: [],
        },
      ]),
    ],
    true,
  );
  const texts = (groups: { text: string }[][]) =>
    groups.map((nodes) => nodes.map((node) => node.text));

  it("groups the test cases by class", () => {
    expect(texts(testGroups(tree, new Set(), "classes"))).toEqual([
      ["a", "b"],
      ["c"],
      ["d"],
      ["Empty"],
    ]);
  });

  it("groups the test cases by package", () => {
    expect(texts(testGroups(tree, new Set(), "packages"))).toEqual([
      ["a", "b", "c"],
      ["d", "Empty"],
    ]);
  });

  it("only covers ticked tests, leaving out classes without any", () => {
    expect(texts(testGroups(tree, new Set([3, 5]), "classes"))).toEqual([
      ["b"],
      ["c"],
    ]);
    expect(texts(testGroups(tree, new Set([3, 5]), "packages"))).toEqual([
      ["b", "c"],
    ]);
  });
});

import type { CountBy, Row, TreeNode } from "../model.ts";

/**
 * Which rows are shown. Without a filter a row is shown when all its ancestors are expanded.
 * With a filter, rows whose name matches are shown, plus the children of shown, expanded rows.
 */
export function visibleRows(
  rows: Row[],
  expanded: ReadonlySet<number>,
  filter: string,
): boolean[] {
  const query = filter.trim().toLowerCase();
  const visible: boolean[] = [];
  for (const row of rows) {
    // Parents come before their children, so their visibility is already known
    const shownByParent =
      row.parent < 0
        ? query === ""
        : visible[row.parent] && expanded.has(row.parent);
    const matches = query !== "" && row.node.text.toLowerCase().includes(query);
    visible.push(shownByParent || matches);
  }
  return visible;
}

export function descendants(rows: Row[], id: number): number[] {
  const result: number[] = [];
  const stack = [...rows[id].children];
  while (stack.length > 0) {
    const child = stack.pop() as number;
    result.push(child);
    stack.push(...rows[child].children);
  }
  return result;
}

/** Ancestors of the row, nearest first. */
export function ancestors(rows: Row[], id: number): number[] {
  const result: number[] = [];
  for (
    let parent = rows[id].parent;
    parent >= 0;
    parent = rows[parent].parent
  ) {
    result.push(parent);
  }
  return result;
}

/**
 * Expands or collapses a row. Collapsing also collapses everything below it. Expanding while
 * filtering expands the ancestors too, so the row stays visible once the filter is cleared.
 */
export function toggleExpanded(
  rows: Row[],
  expanded: ReadonlySet<number>,
  id: number,
  filtering: boolean,
): Set<number> {
  const next = new Set(expanded);
  if (next.has(id)) {
    next.delete(id);
    for (const child of descendants(rows, id)) {
      next.delete(child);
    }
  } else {
    next.add(id);
    if (filtering) {
      for (const ancestor of ancestors(rows, id)) {
        next.add(ancestor);
      }
    }
  }
  return next;
}

export function expandAll(rows: Row[]): Set<number> {
  return new Set(
    rows.filter((row) => row.children.length > 0).map((row) => row.id),
  );
}

/**
 * Ticks or unticks a row and everything below it. Ticking the last unticked child ticks the
 * parent; unticking a row unticks its ancestors.
 */
export function toggleChecked(
  rows: Row[],
  checked: ReadonlySet<number>,
  id: number,
  value: boolean,
): Set<number> {
  const next = new Set(checked);
  for (const row of [id, ...descendants(rows, id)]) {
    if (value) {
      next.add(row);
    } else {
      next.delete(row);
    }
  }
  if (value) {
    for (const parent of ancestors(rows, id)) {
      if (!rows[parent].children.every((sibling) => next.has(sibling))) {
        break;
      }
      next.add(parent);
    }
  } else {
    for (const ancestor of ancestors(rows, id)) {
      next.delete(ancestor);
    }
  }
  return next;
}

/** The nodes the charts cover: ticked rows without ticked ancestors, otherwise every package. */
export function chartNodes(
  rows: Row[],
  checked: ReadonlySet<number>,
): TreeNode[] {
  const selected = rows.filter(
    (row) =>
      checked.has(row.id) && (row.parent < 0 || !checked.has(row.parent)),
  );
  return (
    selected.length > 0 ? selected : rows.filter((row) => row.level === 0)
  ).map((row) => row.node);
}

/** Whether the row is a test case, as opposed to a package or class. */
export function isLeaf(row: Row): boolean {
  return (row.node.children ?? []).length === 0;
}

/** Test case nodes, restricted to the ticked ones when anything is ticked. */
export function testNodes(
  rows: Row[],
  checked: ReadonlySet<number>,
): TreeNode[] {
  return rows
    .filter((row) => isLeaf(row) && (checked.size === 0 || checked.has(row.id)))
    .map((row) => row.node);
}

/**
 * The test cases the charts cover, as with testNodes, grouped by their class or package. A class or
 * package without test cases of its own forms a group by itself.
 */
export function testGroups(
  rows: Row[],
  checked: ReadonlySet<number>,
  countBy: Exclude<CountBy, "tests">,
): TreeNode[][] {
  const level = countBy === "packages" ? 0 : 1;
  const groups = new Map<number, TreeNode[]>();
  for (const row of rows) {
    if (!isLeaf(row) || (checked.size > 0 && !checked.has(row.id))) {
      continue;
    }
    let owner = row;
    while (owner.level > level) {
      owner = rows[owner.parent];
    }
    let group = groups.get(owner.id);
    if (!group) {
      group = [];
      groups.set(owner.id, group);
    }
    group.push(row.node);
  }
  return [...groups.values()];
}

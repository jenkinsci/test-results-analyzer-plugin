import type { Options, TreeNode } from "../model.ts";

/** Limits matching BuildSelection on the server. */
export const MAX_LENGTH = 1000;
export const MAX_BUILDS = 10_000;

const BLANK = /^[ \t]*$/;
const TOKEN = /^[ \t]*(\d{1,9})(?:[ \t]*-[ \t]*(\d{1,9}))?[ \t]*$/;

/**
 * Why a list of build numbers and ranges such as "12, 36, 40-53" would be refused by the server,
 * or null when it is valid.
 */
export function buildNumbersError(spec: string): string | null {
  if (spec.length > MAX_LENGTH) {
    return `Use at most ${MAX_LENGTH} characters.`;
  }
  let covered = 0;
  for (const token of spec.split(",")) {
    if (BLANK.test(token)) {
      continue;
    }
    const match = TOKEN.exec(token);
    if (!match) {
      return `"${token.trim()}" is not a build number or a range such as 40-53.`;
    }
    const first = Number(match[1]);
    const second = match[2] === undefined ? first : Number(match[2]);
    const from = Math.min(first, second);
    const to = Math.max(first, second);
    if (from < 1) {
      return "Build numbers start at 1.";
    }
    covered += to - from + 1;
    if (covered > MAX_BUILDS) {
      return `At most ${MAX_BUILDS} builds can be chosen.`;
    }
  }
  return covered === 0 ? "Enter build numbers, such as 12, 36, 40-53." : null;
}

export interface BuildRequest {
  /** The number of latest builds, or "-1" for all of them. */
  builds: string;
  /** Build numbers and ranges to show instead of the latest builds. */
  buildNumbers?: string;
}

/** The builds to ask the server for. */
export function buildRequest(
  options: Pick<Options, "builds" | "allBuilds" | "buildMode" | "buildNumbers">,
): BuildRequest {
  const builds = options.allBuilds ? "-1" : options.builds;
  return options.buildMode === "specific"
    ? { builds, buildNumbers: options.buildNumbers.trim() }
    : { builds };
}

function statusDiffers(node: TreeNode): boolean {
  return new Set(node.buildResults.map((result) => result.status)).size > 1;
}

/**
 * The tree with only the tests whose status is not the same in every build, and the classes and
 * packages that contain them. A test that ran in some of the builds but not others counts as
 * differing. Packages and classes keep their results across all their tests.
 */
export function differingTests(results: TreeNode[]): TreeNode[] {
  const prune = (node: TreeNode): TreeNode | null => {
    const children = node.children ?? [];
    if (children.length === 0) {
      return statusDiffers(node) ? node : null;
    }
    const kept = children
      .map(prune)
      .filter((child): child is TreeNode => child !== null);
    return kept.length > 0 ? { ...node, children: kept } : null;
  };
  return results.map(prune).filter((node): node is TreeNode => node !== null);
}

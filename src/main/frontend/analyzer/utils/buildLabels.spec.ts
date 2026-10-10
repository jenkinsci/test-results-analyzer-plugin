import { describe, expect, it } from "vitest";

import type { BuildInfo } from "../model.ts";
import {
  buildLabelMode,
  createBuildLabeler,
  dateFormats,
  truncate,
} from "./buildLabels.ts";

// 10 October 2026, 14:32 UTC
const TIMESTAMP = Date.UTC(2026, 9, 10, 14, 32);
const formats = dateFormats("en-GB", "UTC");

const info: BuildInfo[] = [
  { number: 3, displayName: "release-1.3", timestamp: TIMESTAMP },
  { number: 2, displayName: "#2", timestamp: TIMESTAMP - 86_400_000 },
  {
    number: 1,
    displayName: "a display name that is far too long for a header",
  },
];

describe("buildLabelMode", () => {
  it("defaults to the display name", () => {
    expect(buildLabelMode("number")).toBe("number");
    expect(buildLabelMode("date")).toBe("date");
    expect(buildLabelMode("name")).toBe("name");
    expect(buildLabelMode(undefined)).toBe("name");
    expect(buildLabelMode("bogus")).toBe("name");
  });
});

describe("truncate", () => {
  it("keeps short text and ends long text in an ellipsis", () => {
    expect(truncate("#12")).toBe("#12");
    expect(truncate("abcdefghij", 5)).toBe("abcd…");
    expect(truncate("abcde", 5)).toBe("abcde");
  });

  it("does not split characters outside the basic plane", () => {
    expect(truncate("🚀🚀🚀🚀", 3)).toBe("🚀🚀…");
  });
});

describe("createBuildLabeler", () => {
  it("labels builds by display name when it was changed", () => {
    const label = createBuildLabeler(info, "name", formats);
    expect(label("3")).toEqual({
      text: "release-1.3",
      short: "release-1.3",
      title: "#3 release-1.3 (10 Oct 2026, 14:32)",
    });
    expect(label("2")).toEqual({
      text: "#2",
      short: "#2",
      title: "#2 (9 Oct 2026, 14:32)",
    });
  });

  it("shortens long display names, keeping the whole name in the title", () => {
    const label = createBuildLabeler(info, "name", formats)("1");
    expect(label.text).toBe("a display name that is far too long for a header");
    expect(label.short).toBe("a display name that…");
    expect(label.short).toHaveLength(20);
    expect(label.title).toBe(
      "#1 a display name that is far too long for a header",
    );
  });

  it("labels builds by number", () => {
    const label = createBuildLabeler(info, "number", formats);
    expect(label("3").short).toBe("#3");
    expect(label("3").title).toBe("#3 release-1.3 (10 Oct 2026, 14:32)");
  });

  it("labels builds by date, falling back to the number without one", () => {
    const label = createBuildLabeler(info, "date", formats);
    expect(label("3").short).toBe("10 Oct, 14:32");
    expect(label("2").short).toBe("9 Oct, 14:32");
    expect(label("1").short).toBe("#1");
  });

  it("labels builds by number when the server sends no details", () => {
    for (const mode of ["name", "number", "date"] as const) {
      const label = createBuildLabeler(undefined, mode, formats)("7");
      expect(label).toEqual({ text: "#7", short: "#7", title: "#7" });
    }
  });
});

import type { BuildInfo, BuildLabelMode } from "../model.ts";

/** How a build is shown; the build number stays the key everywhere else. */
export interface BuildLabel {
  /** The whole label, e.g. a long display name. */
  text: string;
  /** The label shortened to fit table headers, chips and chart axes. */
  short: string;
  /** Number, display name and date, for tooltips. */
  title: string;
}

export type BuildLabeler = (build: string) => BuildLabel;

export const MAX_LABEL_LENGTH = 20;

export function buildLabelMode(value: string | undefined): BuildLabelMode {
  return value === "number" || value === "date" ? value : "name";
}

/** Shortens text to at most max characters, ending in an ellipsis when cut. */
export function truncate(text: string, max = MAX_LABEL_LENGTH): string {
  const characters = Array.from(text);
  return characters.length <= max
    ? text
    : `${characters.slice(0, max - 1).join("")}…`;
}

export interface DateFormats {
  /** For labels: short enough for a table header. */
  short: (date: Date) => string;
  /** For tooltips. */
  long: (date: Date) => string;
}

/**
 * A date formatter, created when first used. Falls back to the browser's default format where the
 * options are not supported, so a date can never stop the page from rendering.
 */
function formatter(
  options: Intl.DateTimeFormatOptions,
  locale?: string,
  timeZone?: string,
): (date: Date) => string {
  let format: ((date: Date) => string) | undefined;
  return (date) => {
    if (!format) {
      try {
        const intl = new Intl.DateTimeFormat(
          locale,
          timeZone ? { ...options, timeZone } : options,
        );
        format = (value) => intl.format(value);
      } catch {
        format = (value) => value.toLocaleString();
      }
    }
    return format(date);
  };
}

/** Dates in the browser's language and time zone, unless given others. */
export function dateFormats(locale?: string, timeZone?: string): DateFormats {
  return {
    short: formatter(
      { month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" },
      locale,
      timeZone,
    ),
    long: formatter(
      { dateStyle: "medium", timeStyle: "short" },
      locale,
      timeZone,
    ),
  };
}

/**
 * Labels builds by number, display name or date. Builds without details (from an older server, or
 * missing a date) fall back to their number.
 */
export function createBuildLabeler(
  buildInfo: BuildInfo[] | undefined,
  mode: BuildLabelMode,
  formats: DateFormats = dateFormats(),
): BuildLabeler {
  const byNumber = new Map<string, BuildInfo>();
  for (const info of buildInfo ?? []) {
    byNumber.set(String(info.number), info);
  }
  const cache = new Map<string, BuildLabel>();
  return (build) => {
    let label = cache.get(build);
    if (!label) {
      label = labelFor(build, byNumber.get(build), mode, formats);
      cache.set(build, label);
    }
    return label;
  };
}

function labelFor(
  build: string,
  info: BuildInfo | undefined,
  mode: BuildLabelMode,
  formats: DateFormats,
): BuildLabel {
  const number = `#${build}`;
  const name =
    info?.displayName && info.displayName !== number
      ? info.displayName
      : undefined;
  const date =
    typeof info?.timestamp === "number" && info.timestamp > 0
      ? new Date(info.timestamp)
      : undefined;

  let text = number;
  if (mode === "name" && name) {
    text = name;
  } else if (mode === "date" && date) {
    text = formats.short(date);
  }

  let title = name ? `${number} ${name}` : number;
  if (date) {
    title += ` (${formats.long(date)})`;
  }
  return { text, short: truncate(text), title };
}

/** Labels builds by number, for when no labeler is given. */
export const numberLabels: BuildLabeler = (build) => ({
  text: `#${build}`,
  short: `#${build}`,
  title: `#${build}`,
});

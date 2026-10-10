import { type CSSProperties, memo } from "react";

import type { BuildResult, Row, StatusLabels } from "../model.ts";
import { type BuildLabeler, numberLabels } from "../utils/buildLabels.ts";
import {
  isNewFailure,
  numberOfTransitions,
  percentPassed,
} from "../utils/stats.ts";
import { AlertCircleIcon, ChevronForwardIcon } from "./icons.tsx";
import { Tooltip } from "./Tooltip.tsx";

const NEW_FAILURE =
  "New failure: failed in the latest build after passing in the build before";

const STATUS_CLASS: Record<string, string> = {
  PASSED: "tra-build--passed",
  FAILED: "tra-build--failed",
  SKIPPED: "tra-build--skipped",
};

interface HistoryViewProps {
  builds: string[];
  rows: Row[];
  visible: boolean[];
  expanded: ReadonlySet<number>;
  checked: ReadonlySet<number>;
  selecting: boolean;
  showDurations: boolean;
  labels: StatusLabels;
  buildLabel?: BuildLabeler;
  onToggle: (id: number) => void;
  onCheck: (id: number, checked: boolean) => void;
}

function statusLabel(labels: StatusLabels, status: string): string {
  switch (status) {
    case "PASSED":
      return labels.passed;
    case "FAILED":
      return labels.failed;
    case "SKIPPED":
      return labels.skipped;
    default:
      return labels.na;
  }
}

function formatDuration(result: BuildResult, labels: StatusLabels): string {
  return typeof result.totalTimeTaken === "number"
    ? `${result.totalTimeTaken.toFixed(3)}s`
    : labels.na;
}

function BuildSquare({
  result,
  labels,
  showDurations,
  buildLabel,
}: {
  result: BuildResult;
  labels: StatusLabels;
  showDurations: boolean;
  buildLabel: BuildLabeler;
}) {
  const label = statusLabel(labels, result.status);
  let title = `Build ${buildLabel(result.buildNumber).title}: ${label}`;
  if (typeof result.totalTimeTaken === "number") {
    title += ` in ${formatDuration(result, labels)}`;
  }
  const className = [
    "tra-build",
    STATUS_CLASS[result.status],
    showDurations ? "tra-build--value" : undefined,
  ]
    .filter(Boolean)
    .join(" ");
  const content = showDurations
    ? result.status === "N/A"
      ? label
      : formatDuration(result, labels)
    : null;
  return result.url ? (
    <a className={className} href={result.url} title={title} aria-label={title}>
      {content}
    </a>
  ) : (
    <span className={className} title={title} aria-label={title}>
      {content}
    </span>
  );
}

interface HistoryRowProps {
  row: Row;
  expanded: boolean;
  checked: boolean;
  showDurations: boolean;
  labels: StatusLabels;
  buildLabel: BuildLabeler;
  onToggle: (id: number) => void;
  onCheck: (id: number, checked: boolean) => void;
}

const HistoryRow = memo(function HistoryRow({
  row,
  expanded,
  checked,
  showDurations,
  labels,
  buildLabel,
  onToggle,
  onCheck,
}: HistoryRowProps) {
  const { node } = row;
  const hasChildren = row.children.length > 0;
  const passed = percentPassed(node.buildResults);
  const transitions = numberOfTransitions(node.buildResults);
  const className = [
    "tra-row",
    row.level === 0 ? "tra-row--package" : undefined,
    (node.children ?? []).length === 0 ? "tra-row--leaf" : undefined,
  ]
    .filter(Boolean)
    .join(" ");
  const checkboxId = `tra-row-${row.id}`;

  return (
    <div
      className={className}
      data-level={row.level}
      data-name={node.text}
      style={{ "--tra-level": row.level } as CSSProperties}
    >
      <span className="jenkins-checkbox tra-row__select">
        <input
          type="checkbox"
          id={checkboxId}
          className="tra-row-select"
          aria-label={`Include ${node.text} in charts`}
          checked={checked}
          onChange={(event) => onCheck(row.id, event.target.checked)}
        />
        <label htmlFor={checkboxId} />
      </span>

      <div className="tra-row__name">
        {hasChildren ? (
          <button
            type="button"
            className="tra-toggle"
            aria-expanded={expanded}
            aria-label={`${expanded ? "Hide" : "Show"} children of ${node.text}`}
            onClick={() => onToggle(row.id)}
          >
            <ChevronForwardIcon />
          </button>
        ) : (
          <span className="tra-toggle-spacer" />
        )}
        <span className="tra-row__text">{node.text}</span>
        {isNewFailure(node.buildResults) && (
          <Tooltip content={NEW_FAILURE}>
            <span
              className="tra-new-failure"
              role="img"
              aria-label={NEW_FAILURE}
            >
              <AlertCircleIcon />
            </span>
          </Tooltip>
        )}
      </div>

      <div className="tra-row__stats">
        <span
          className="tra-stat tra-stat--passed"
          title={
            passed
              ? `${passed.builds}% of builds passed, ${passed.tests}% of test runs passed`
              : undefined
          }
        >
          {passed ? `${passed.builds}% (${passed.tests}%)` : labels.na}
        </span>
        <span
          className={`tra-stat tra-stat--transitions${transitions > 0 ? " tra-stat--flaky" : ""}`}
          title={`${transitions} transitions between passing and failing`}
        >
          {transitions}
        </span>
      </div>

      <div className={`tra-strip${showDurations ? " tra-strip--values" : ""}`}>
        {node.buildResults.map((result) => (
          <BuildSquare
            key={result.buildNumber}
            result={result}
            labels={labels}
            showDurations={showDurations}
            buildLabel={buildLabel}
          />
        ))}
      </div>
    </div>
  );
});

function HistoryHeader({
  builds,
  showDurations,
  buildLabel,
}: {
  builds: string[];
  showDurations: boolean;
  buildLabel: BuildLabeler;
}) {
  return (
    <div className="tra-history__header" aria-hidden="true">
      <span className="tra-row__select" />
      <span className="tra-row__name">Package / Class / Test</span>
      <div className="tra-row__stats">
        <span className="tra-stat" title="Builds passed (test runs passed)">
          Passed
        </span>
        <span
          className="tra-stat tra-stat--transitions"
          title="Transitions from passed to failed and failed to passed"
        >
          Flips
        </span>
      </div>
      <div className={`tra-strip${showDurations ? " tra-strip--values" : ""}`}>
        {builds.map((build) => {
          const label = buildLabel(build);
          return (
            <span
              key={build}
              className={`tra-build tra-build--header${showDurations ? " tra-build--value" : ""}`}
              title={label.title}
            >
              {label.short}
            </span>
          );
        })}
      </div>
    </div>
  );
}

/** One card per package, with a row per class and test and a strip of build results. */
export function HistoryView({
  builds,
  rows,
  visible,
  expanded,
  checked,
  selecting,
  showDurations,
  labels,
  buildLabel = numberLabels,
  onToggle,
  onCheck,
}: HistoryViewProps) {
  const packages: Row[][] = [];
  for (const row of rows) {
    if (row.level === 0) {
      packages.push([]);
    }
    if (visible[row.id]) {
      packages[packages.length - 1].push(row);
    }
  }

  return (
    <div
      id="tra-history"
      className={`tra-history-container${selecting ? " tra-history-container--selecting" : ""}`}
    >
      <div className="tra-history" role="group" aria-label="Test history">
        <HistoryHeader
          builds={builds}
          showDurations={showDurations}
          buildLabel={buildLabel}
        />
        {packages
          .filter((packageRows) => packageRows.length > 0)
          .map((packageRows) => (
            <div key={packageRows[0].id} className="jenkins-card tra-package">
              {packageRows.map((row) => (
                <HistoryRow
                  key={row.id}
                  row={row}
                  expanded={expanded.has(row.id)}
                  checked={checked.has(row.id)}
                  showDurations={showDurations}
                  labels={labels}
                  buildLabel={buildLabel}
                  onToggle={onToggle}
                  onCheck={onCheck}
                />
              ))}
            </div>
          ))}
      </div>
    </div>
  );
}

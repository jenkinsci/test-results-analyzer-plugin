import {
  useCallback,
  useDeferredValue,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";

import type { AnalyzerClient } from "./api.ts";
import { Charts } from "./components/Charts.tsx";
import { HistoryView } from "./components/HistoryView.tsx";
import { SearchIcon } from "./components/icons.tsx";
import { OptionsCard } from "./components/OptionsCard.tsx";
import { WorstTests } from "./components/WorstTests.tsx";
import type {
  AnalyzerData,
  Bootstrap,
  ChartData,
  Options,
  Row,
} from "./model.ts";
import { flattenTree, worstTests } from "./utils/stats.ts";
import {
  chartNodes,
  expandAll,
  testNodes,
  toggleChecked,
  toggleExpanded,
  visibleRows,
} from "./utils/tree.ts";

interface AnalyzerPageProps {
  bootstrap: Bootstrap;
  client: AnalyzerClient;
  /** The app bar button that shows and hides the options. */
  optionsButton?: HTMLElement | null;
  /** The app bar button that downloads the results as CSV. */
  csvButton?: HTMLElement | null;
  /** Opens a download URL; the response is an attachment, so the browser stays on this page. */
  download?: (url: string) => void;
}

const openUrl = (url: string) => window.location.assign(url);

function chartData(value: string): ChartData {
  return value === "runtime" || value === "passrate" ? value : "passfail";
}

function initialOptions({ defaults }: Bootstrap): Options {
  return {
    builds: defaults.noOfBuilds,
    allBuilds: defaults.showAllBuilds,
    showDurations: defaults.showBuildTime,
    showNotRun: false,
    hideConfig: defaults.hideConfigurationMethods,
    line: defaults.showLineGraph,
    bar: defaults.showBarGraph,
    pie: defaults.showPieGraph,
    worstCount: "10",
    chartData: chartData(defaults.chartDataType),
  };
}

const NO_ROWS: Row[] = [];

export function AnalyzerPage({
  bootstrap,
  client,
  optionsButton,
  csvButton,
  download = openUrl,
}: AnalyzerPageProps) {
  const [options, setOptions] = useState(() => initialOptions(bootstrap));
  const [showOptions, setShowOptions] = useState(false);
  const [data, setData] = useState<AnalyzerData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [filter, setFilter] = useState("");
  const deferredFilter = useDeferredValue(filter);
  const [selecting, setSelecting] = useState(false);
  const [expanded, setExpanded] = useState<ReadonlySet<number>>(new Set());
  const [checked, setChecked] = useState<ReadonlySet<number>>(new Set());

  const rows = useMemo(
    () => (data ? flattenTree(data.results, options.showNotRun) : NO_ROWS),
    [data, options.showNotRun],
  );
  // Row ids change with the rows, so start again with everything collapsed and unticked
  const [rowsShown, setRowsShown] = useState(rows);
  if (rowsShown !== rows) {
    setRowsShown(rows);
    setExpanded(new Set());
    setChecked(new Set());
  }

  const request = useRef(0);
  const optionsRef = useRef(options);
  optionsRef.current = options;
  const builds = options.allBuilds ? "-1" : options.builds;

  const load = useCallback(() => {
    const { allBuilds, builds, hideConfig } = optionsRef.current;
    const id = ++request.current;
    setLoading(true);
    setError(false);
    setData(null);
    client
      .load(allBuilds ? "-1" : builds, hideConfig)
      .then((loaded) => {
        if (id === request.current) {
          setData(loaded);
        }
      })
      .catch(() => {
        if (id === request.current) {
          setError(true);
        }
      })
      .finally(() => {
        if (id === request.current) {
          setLoading(false);
        }
      });
  }, [client]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    if (!optionsButton) {
      return;
    }
    const onClick = () => setShowOptions((shown) => !shown);
    optionsButton.addEventListener("click", onClick);
    return () => optionsButton.removeEventListener("click", onClick);
  }, [optionsButton]);

  useEffect(() => {
    optionsButton?.setAttribute("aria-expanded", String(showOptions));
  }, [optionsButton, showOptions]);

  useEffect(() => {
    if (!csvButton) {
      return;
    }
    const onClick = () =>
      download(client.csvUrl(builds, options.showDurations));
    csvButton.addEventListener("click", onClick);
    return () => csvButton.removeEventListener("click", onClick);
  }, [csvButton, client, download, builds, options.showDurations]);

  const updateOptions = useCallback((changes: Partial<Options>) => {
    setOptions((current) => ({ ...current, ...changes }));
  }, []);

  const onToggle = useCallback(
    (id: number) =>
      setExpanded((current) =>
        toggleExpanded(rows, current, id, filter.trim() !== ""),
      ),
    [rows, filter],
  );

  const onCheck = useCallback(
    (id: number, value: boolean) =>
      setChecked((current) => toggleChecked(rows, current, id, value)),
    [rows],
  );

  const toggleSelecting = () => {
    if (selecting) {
      setChecked(new Set());
    }
    setSelecting(!selecting);
  };

  const visible = useMemo(
    () => visibleRows(rows, expanded, deferredFilter),
    [rows, expanded, deferredFilter],
  );
  const worstLimit = Math.max(1, Number.parseInt(options.worstCount, 10) || 10);
  const worst = useMemo(
    () => (data ? worstTests(data.results, worstLimit) : []),
    [data, worstLimit],
  );
  const nodes = useMemo(() => chartNodes(rows, checked), [rows, checked]);
  const tests = useMemo(() => testNodes(rows, checked), [rows, checked]);
  const thresholds = useMemo(
    () => ({
      low: bootstrap.runTimeLowThreshold,
      high: bootstrap.runTimeHighThreshold,
    }),
    [bootstrap],
  );

  return (
    <>
      {showOptions && (
        <OptionsCard
          options={options}
          onChange={updateOptions}
          onApply={load}
        />
      )}

      <section className="tra-section">
        <div className="tra-toolbar">
          <div className="jenkins-search tra-toolbar__search">
            <div className="jenkins-search__icon">
              <SearchIcon />
            </div>
            <input
              id="tra-filter"
              className="jenkins-input jenkins-search__input"
              type="search"
              placeholder="Filter by package, class or test"
              autoComplete="off"
              spellCheck={false}
              value={filter}
              onChange={(event) => setFilter(event.target.value)}
            />
          </div>
          <div className="tra-toolbar__actions">
            <button
              type="button"
              id="tra-select-toggle"
              className="jenkins-button jenkins-button--tertiary"
              aria-pressed={selecting}
              title="Choose packages, classes or tests to chart"
              onClick={toggleSelecting}
            >
              {selecting ? "Done" : "Select"}
            </button>
            <button
              type="button"
              id="tra-expand-all"
              className="jenkins-button jenkins-button--tertiary"
              onClick={() => setExpanded(expandAll(rows))}
            >
              Expand all
            </button>
            <button
              type="button"
              id="tra-collapse-all"
              className="jenkins-button jenkins-button--tertiary"
              onClick={() => {
                setFilter("");
                setExpanded(new Set());
              }}
            >
              Collapse all
            </button>
          </div>
        </div>

        {loading && (
          <div className="tra-loading">
            <p className="jenkins-spinner">Loading test results</p>
          </div>
        )}
        {error && (
          <p className="tra-empty jenkins-!-error-color">
            The test results could not be loaded. Reload the page to try again.
          </p>
        )}
        {data && rows.length === 0 && (
          <p className="jenkins-!-text-color-secondary tra-empty">
            No test results were found for the selected builds.
          </p>
        )}
        {data && rows.length > 0 && (
          <>
            <HistoryView
              builds={data.builds}
              rows={rows}
              visible={visible}
              expanded={expanded}
              checked={checked}
              selecting={selecting}
              showDurations={options.showDurations}
              labels={bootstrap.labels}
              onToggle={onToggle}
              onCheck={onCheck}
            />
            <p className="tra-legend jenkins-!-text-color-secondary">
              {selecting
                ? "Tick packages, classes or tests to chart only those."
                : "Newest build first."}
            </p>
          </>
        )}
      </section>

      <Charts
        nodes={nodes}
        tests={tests}
        mode={options.chartData}
        line={options.line}
        bar={options.bar}
        pie={options.pie}
        thresholds={thresholds}
      />

      {data && (
        <section className="tra-section">
          <h2 className="jenkins-section__title">Most broken tests</h2>
          <div id="tra-worst-tests">
            <WorstTests tests={worst} />
          </div>
        </section>
      )}
    </>
  );
}

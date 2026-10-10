import type { BuildLabelMode, ChartData, Options } from "../model.ts";
import { buildNumbersError } from "../utils/builds.ts";

interface OptionsCardProps {
  options: Options;
  onChange: (changes: Partial<Options>) => void;
  /** Reloads the results for the number of builds and the configuration method setting. */
  onApply: () => void;
  /** The most builds that may be chosen at once. */
  maxBuilds: number;
}

interface CheckboxProps {
  id: string;
  label: string;
  checked: boolean;
  disabled?: boolean;
  onChange: (checked: boolean) => void;
}

function Checkbox({ id, label, checked, disabled, onChange }: CheckboxProps) {
  return (
    <span className="jenkins-checkbox">
      <input
        type="checkbox"
        id={id}
        checked={checked}
        disabled={disabled}
        onChange={(event) => onChange(event.target.checked)}
      />
      <label htmlFor={id}>{label}</label>
    </span>
  );
}

interface RadioProps {
  id: string;
  name: string;
  label: string;
  checked: boolean;
  onChange: () => void;
}

function Radio({ id, name, label, checked, onChange }: RadioProps) {
  return (
    <span className="jenkins-radio">
      <input
        type="radio"
        className="jenkins-radio__input"
        id={id}
        name={name}
        checked={checked}
        onChange={onChange}
      />
      <label className="jenkins-radio__label" htmlFor={id}>
        {label}
      </label>
    </span>
  );
}

export function OptionsCard({
  options,
  onChange,
  onApply,
  maxBuilds,
}: OptionsCardProps) {
  const specific = options.buildMode === "specific";
  const error = specific
    ? buildNumbersError(options.buildNumbers, maxBuilds)
    : null;
  // Nothing typed yet is not worth an error, though there is nothing to update with either
  const shownError = options.buildNumbers.trim() === "" ? null : error;
  return (
    <div id="tra-options" className="jenkins-card tra-options">
      <div className="jenkins-card__title">Options</div>
      <div className="jenkins-card__content">
        <div className="tra-options__grid">
          <div className="tra-options__group">
            <span id="tra-build-mode-label" className="jenkins-form-label">
              Builds
            </span>
            <div
              className="tra-options__inline"
              role="radiogroup"
              aria-labelledby="tra-build-mode-label"
            >
              <Radio
                id="tra-build-mode-latest"
                name="tra-build-mode"
                label="Latest"
                checked={!specific}
                onChange={() => onChange({ buildMode: "latest" })}
              />
              <Radio
                id="tra-build-mode-specific"
                name="tra-build-mode"
                label="Specific builds"
                checked={specific}
                onChange={() => onChange({ buildMode: "specific" })}
              />
            </div>
            {specific ? (
              <>
                <label
                  className="jenkins-form-label"
                  htmlFor="tra-build-numbers"
                >
                  Build numbers
                </label>
                <input
                  id="tra-build-numbers"
                  className="jenkins-input"
                  type="text"
                  placeholder="12, 36, 40-53"
                  autoComplete="off"
                  spellCheck={false}
                  aria-invalid={shownError !== null}
                  aria-describedby="tra-build-numbers-help"
                  value={options.buildNumbers}
                  onChange={(event) =>
                    onChange({ buildNumbers: event.target.value })
                  }
                  onKeyDown={(event) => {
                    if (event.key === "Enter" && error === null) {
                      onApply();
                    }
                  }}
                />
                <span
                  id="tra-build-numbers-help"
                  className={`tra-options__help ${
                    shownError === null
                      ? "jenkins-!-text-color-secondary"
                      : "jenkins-!-error-color"
                  }`}
                >
                  {shownError ??
                    "Build numbers and ranges, separated by commas."}
                </span>
              </>
            ) : (
              <>
                <label className="jenkins-form-label" htmlFor="tra-builds">
                  Number of builds
                </label>
                <div className="tra-options__inline">
                  <input
                    id="tra-builds"
                    className="jenkins-input tra-options__number"
                    type="number"
                    min="1"
                    step="1"
                    value={options.builds}
                    disabled={options.allBuilds}
                    onChange={(event) =>
                      onChange({ builds: event.target.value })
                    }
                  />
                  <Checkbox
                    id="tra-all-builds"
                    label="All builds"
                    checked={options.allBuilds}
                    onChange={(allBuilds) => onChange({ allBuilds })}
                  />
                </div>
              </>
            )}
          </div>

          <div className="tra-options__group">
            <span className="jenkins-form-label">Table</span>
            <Checkbox
              id="tra-show-durations"
              label="Show run time for each test"
              checked={options.showDurations}
              onChange={(showDurations) => onChange({ showDurations })}
            />
            <Checkbox
              id="tra-show-not-run"
              label="Show tests that did not run in these builds"
              checked={options.showNotRun}
              onChange={(showNotRun) => onChange({ showNotRun })}
            />
            <Checkbox
              id="tra-hide-config"
              label="Hide TestNG configuration methods"
              checked={options.hideConfig}
              onChange={(hideConfig) => onChange({ hideConfig })}
            />
            <Checkbox
              id="tra-only-differing"
              label="Only show tests whose status differs between the builds"
              checked={options.onlyDiffering}
              onChange={(onlyDiffering) => onChange({ onlyDiffering })}
            />
          </div>

          <div className="tra-options__group">
            <span className="jenkins-form-label">Charts</span>
            <Checkbox
              id="tra-chart-line"
              label="Line"
              checked={options.line}
              onChange={(line) => onChange({ line })}
            />
            <Checkbox
              id="tra-chart-bar"
              label="Bar"
              checked={options.bar}
              disabled={options.chartData !== "passfail"}
              onChange={(bar) => onChange({ bar })}
            />
            <Checkbox
              id="tra-chart-pie"
              label="Pie"
              checked={options.pie}
              onChange={(pie) => onChange({ pie })}
            />
          </div>

          <div className="tra-options__group">
            <label className="jenkins-form-label" htmlFor="tra-worst-count">
              Most broken tests to show
            </label>
            <input
              id="tra-worst-count"
              className="jenkins-input tra-options__number"
              type="number"
              min="1"
              step="1"
              value={options.worstCount}
              onChange={(event) => onChange({ worstCount: event.target.value })}
            />
          </div>

          <div className="tra-options__group">
            <label className="jenkins-form-label" htmlFor="tra-chart-data">
              Chart data
            </label>
            <div className="jenkins-select">
              <select
                id="tra-chart-data"
                className="jenkins-select__input"
                value={options.chartData}
                onChange={(event) =>
                  onChange({ chartData: event.target.value as ChartData })
                }
              >
                <option value="passfail">Passes and failures</option>
                <option value="passrate">Pass rate</option>
                <option value="runtime">Test run times</option>
              </select>
            </div>
          </div>

          <div className="tra-options__group">
            <label className="jenkins-form-label" htmlFor="tra-build-label">
              Label builds by
            </label>
            <div className="jenkins-select">
              <select
                id="tra-build-label"
                className="jenkins-select__input"
                value={options.buildLabel}
                onChange={(event) =>
                  onChange({
                    buildLabel: event.target.value as BuildLabelMode,
                  })
                }
              >
                <option value="name">Display name</option>
                <option value="number">Number</option>
                <option value="date">Date</option>
              </select>
            </div>
          </div>
        </div>
        <div className="tra-options__actions">
          <button
            type="button"
            id="tra-apply"
            className="jenkins-button jenkins-button--primary"
            disabled={error !== null}
            onClick={onApply}
          >
            Update
          </button>
        </div>
      </div>
    </div>
  );
}

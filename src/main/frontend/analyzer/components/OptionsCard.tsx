import type { ChartData, Options } from "../model.ts";

interface OptionsCardProps {
  options: Options;
  onChange: (changes: Partial<Options>) => void;
  /** Reloads the results for the number of builds and the configuration method setting. */
  onApply: () => void;
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

export function OptionsCard({ options, onChange, onApply }: OptionsCardProps) {
  return (
    <div id="tra-options" className="jenkins-card tra-options">
      <div className="jenkins-card__title">Options</div>
      <div className="jenkins-card__content">
        <div className="tra-options__grid">
          <div className="tra-options__group">
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
                onChange={(event) => onChange({ builds: event.target.value })}
              />
              <Checkbox
                id="tra-all-builds"
                label="All builds"
                checked={options.allBuilds}
                onChange={(allBuilds) => onChange({ allBuilds })}
              />
            </div>
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
        </div>
        <div className="tra-options__actions">
          <button
            type="button"
            id="tra-apply"
            className="jenkins-button jenkins-button--primary"
            onClick={onApply}
          >
            Update
          </button>
        </div>
      </div>
    </div>
  );
}

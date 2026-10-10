import { type BuildLabeler, numberLabels } from "../utils/buildLabels.ts";
import type { BrokenTest } from "../utils/stats.ts";

export function WorstTests({
  tests,
  buildLabel = numberLabels,
}: {
  tests: BrokenTest[];
  buildLabel?: BuildLabeler;
}) {
  if (tests.length === 0) {
    return (
      <p className="jenkins-!-text-color-secondary">
        There are no failing tests.
      </p>
    );
  }
  return (
    <ol className="jenkins-card tra-worst">
      {tests.map((test) => (
        <li key={test.name} className="tra-worst__item">
          <div className="tra-worst__heading">
            <span className="tra-row__text">{test.name}</span>
            <span className="tra-stat tra-stat--failed">
              {test.builds.length}{" "}
              {test.builds.length === 1 ? "failure" : "failures"}
            </span>
          </div>
          <div className="tra-worst__builds">
            {test.builds.slice(0, 10).map((build) => {
              const label = buildLabel(build.buildNumber);
              const title = `Failed in build ${label.title}`;
              return build.url ? (
                <a
                  key={build.buildNumber}
                  className="tra-chip"
                  href={build.url}
                  title={title}
                >
                  {label.short}
                </a>
              ) : (
                <span
                  key={build.buildNumber}
                  className="tra-chip"
                  title={title}
                >
                  {label.short}
                </span>
              );
            })}
          </div>
        </li>
      ))}
    </ol>
  );
}

import type { BrokenTest } from "../utils/stats.ts";

export function WorstTests({ tests }: { tests: BrokenTest[] }) {
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
            {test.builds.slice(0, 10).map((build) =>
              build.url ? (
                <a
                  key={build.buildNumber}
                  className="tra-chip"
                  href={build.url}
                  title={`Failed in build #${build.buildNumber}`}
                >
                  #{build.buildNumber}
                </a>
              ) : (
                <span
                  key={build.buildNumber}
                  className="tra-chip"
                  title={`Failed in build #${build.buildNumber}`}
                >
                  #{build.buildNumber}
                </span>
              ),
            )}
          </div>
        </li>
      ))}
    </ol>
  );
}

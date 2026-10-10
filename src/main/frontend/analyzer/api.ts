import type { AnalyzerData } from "./model.ts";

export interface AnalyzerClient {
  /**
   * Loads the results of the latest builds; builds is a number, or -1 for all builds.
   * When buildNumbers is given, such as "12,36,40-53", those builds are loaded instead.
   */
  load(
    builds: string,
    hideConfigMethods: boolean,
    buildNumbers?: string,
  ): Promise<AnalyzerData>;
  /** The URL that downloads the results as CSV. */
  csvUrl(builds: string, durations: boolean, buildNumbers?: string): string;
}

/** A client for the endpoints of TestResultsAnalyzerAction at the given URL, ending in a slash. */
export function createClient(actionUrl: string): AnalyzerClient {
  return {
    async load(builds, hideConfigMethods, buildNumbers) {
      const query = new URLSearchParams({
        builds,
        hideConfigMethods: String(hideConfigMethods),
      });
      if (buildNumbers !== undefined) {
        query.set("buildNumbers", buildNumbers);
      }
      const response = await fetch(`${actionUrl}data?${query}`, {
        headers: { Accept: "application/json" },
      });
      if (!response.ok) {
        throw new Error(`${response.status} ${response.statusText}`);
      }
      const data = (await response.json()) as Partial<AnalyzerData>;
      return { builds: data.builds ?? [], results: data.results ?? [] };
    },
    csvUrl(builds, durations, buildNumbers) {
      const query = new URLSearchParams({
        builds,
        durations: String(durations),
      });
      if (buildNumbers !== undefined) {
        query.set("buildNumbers", buildNumbers);
      }
      return `${actionUrl}csv?${query}`;
    },
  };
}

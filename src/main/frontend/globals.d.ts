// Globals provided by Jenkins core and the echarts-api plugin on the analyzer page.
interface Window {
  /** Loaded by the io.jenkins.plugins.echarts adjunct; absent in unit tests. */
  echarts?: typeof import("echarts");
}

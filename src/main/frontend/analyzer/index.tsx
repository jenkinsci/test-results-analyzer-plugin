import "./analyzer.css";

import { StrictMode } from "react";
import { createRoot } from "react-dom/client";

import { AnalyzerPage } from "./AnalyzerPage.tsx";
import { createClient } from "./api.ts";
import type { Bootstrap } from "./model.ts";

const mountNode = document.getElementById("tra-root");
if (mountNode) {
  const bootstrap = JSON.parse(
    mountNode.dataset.bootstrap ?? "{}",
  ) as Bootstrap;
  const colors = bootstrap.customColors;
  if (colors) {
    // Set through the CSSOM, which the Content Security Policy allows, unlike inline styles
    mountNode.style.setProperty("--tra-passed", colors.passed);
    mountNode.style.setProperty("--tra-failed", colors.failed);
    mountNode.style.setProperty("--tra-skipped", colors.skipped);
    mountNode.style.setProperty("--tra-na", colors.na);
  }
  createRoot(mountNode).render(
    <StrictMode>
      <AnalyzerPage
        bootstrap={bootstrap}
        client={createClient(mountNode.dataset.actionUrl ?? "")}
        optionsButton={document.getElementById("tra-options-toggle")}
        csvButton={document.getElementById("tra-download-csv")}
      />
    </StrictMode>,
  );
}

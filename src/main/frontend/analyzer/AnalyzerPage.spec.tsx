import { act, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import { AnalyzerPage } from "./AnalyzerPage.tsx";
import type { AnalyzerClient } from "./api.ts";
import { bootstrap, calculatorData, group, test } from "./fixtures.ts";
import type { AnalyzerData } from "./model.ts";

function client(data: AnalyzerData = calculatorData()): AnalyzerClient {
  return {
    load: vi.fn().mockResolvedValue(data),
    csvUrl: (builds, durations) =>
      `csv?builds=${builds}&durations=${durations}`,
  };
}

async function renderPage(
  data?: AnalyzerData,
  props: Partial<Parameters<typeof AnalyzerPage>[0]> = {},
) {
  const api = props.client ?? client(data);
  const view = render(
    <AnalyzerPage bootstrap={bootstrap()} client={api} {...props} />,
  );
  await screen.findByRole("group", { name: "Test history" });
  return { ...view, api, user: userEvent.setup() };
}

function rowNames(container: HTMLElement): string[] {
  return [...container.querySelectorAll<HTMLElement>(".tra-row")].map(
    (row) => row.dataset.name ?? "",
  );
}

function row(container: HTMLElement, name: string): HTMLElement {
  const element = container.querySelector<HTMLElement>(
    `.tra-row[data-name="${name}"]`,
  );
  if (!element) {
    throw new Error(`No row ${name}`);
  }
  return element;
}

describe("AnalyzerPage", () => {
  it("loads the configured number of builds and shows the packages collapsed", async () => {
    const { container, api } = await renderPage();
    expect(api.load).toHaveBeenCalledWith("10", false);
    expect(rowNames(container)).toEqual(["com.example"]);
    expect(screen.getByText("Newest build first.")).toBeInTheDocument();
  });

  it("expands one level at a time, and all at once", async () => {
    const { container, user } = await renderPage();

    await user.click(
      screen.getByRole("button", { name: "Show children of com.example" }),
    );
    expect(rowNames(container)).toEqual(["com.example", "CalculatorTest"]);
    expect(
      screen.getByRole("button", { name: "Hide children of com.example" }),
    ).toHaveAttribute("aria-expanded", "true");

    await user.click(screen.getByRole("button", { name: "Expand all" }));
    expect(rowNames(container)).toEqual([
      "com.example",
      "CalculatorTest",
      "testA",
      "testB",
    ]);

    await user.click(screen.getByRole("button", { name: "Collapse all" }));
    expect(rowNames(container)).toEqual(["com.example"]);
  });

  it("marks new failures and shows a strip of build results", async () => {
    const { container, user } = await renderPage();
    await user.click(screen.getByRole("button", { name: "Expand all" }));

    const testB = row(container, "testB");
    expect(
      within(testB).getByRole("img", { name: /^New failure/ }),
    ).toBeInTheDocument();
    expect(
      within(row(container, "testA")).queryByRole("img", {
        name: /^New failure/,
      }),
    ).toBeNull();
    expect(testB.querySelectorAll(".tra-build--failed")).toHaveLength(1);
    expect(testB.querySelectorAll(".tra-build--passed")).toHaveLength(2);
    expect(testB.querySelector("a.tra-build")).toHaveAttribute(
      "href",
      "http://jenkins/job/demo/3/",
    );

    const [passed, flips] = testB.querySelectorAll(".tra-stat");
    expect(passed).toHaveTextContent("67% (67%)");
    expect(flips).toHaveTextContent("1");
  });

  it("filters rows by name and restores the tree when the filter is cleared", async () => {
    const { container, user } = await renderPage();
    const filter = screen.getByPlaceholderText(
      "Filter by package, class or test",
    );

    await user.type(filter, "testb");
    await waitFor(() => expect(rowNames(container)).toEqual(["testB"]));

    await user.clear(filter);
    await waitFor(() => expect(rowNames(container)).toEqual(["com.example"]));
  });

  it("lists the most broken tests, as many as asked for", async () => {
    const data = {
      builds: ["1"],
      results: [
        group("p", [
          group("T", [test("one", ["FAILED"]), test("two", ["FAILED"])]),
        ]),
      ],
    };
    const optionsButton = document.createElement("button");
    const { container, user } = await renderPage(data, { optionsButton });
    const worst = () => container.querySelectorAll(".tra-worst__item");
    expect(worst()).toHaveLength(2);
    expect(worst()[0]).toHaveTextContent("p.T.one");

    await act(async () => optionsButton.click());
    const count = await screen.findByLabelText("Most broken tests to show");
    await user.clear(count);
    await user.type(count, "1");
    expect(worst()).toHaveLength(1);
  });

  it("says when nothing failed", async () => {
    await renderPage({
      builds: ["1"],
      results: [group("p", [group("T", [test("ok", ["PASSED"])])])],
    });
    expect(screen.getByText("There are no failing tests.")).toBeInTheDocument();
  });

  it("shows checkboxes only while selecting, and clears the selection when done", async () => {
    const { container, user } = await renderPage();
    const history = container.querySelector("#tra-history") as HTMLElement;
    expect(history).not.toHaveClass("tra-history-container--selecting");

    await user.click(screen.getByRole("button", { name: "Select" }));
    expect(history).toHaveClass("tra-history-container--selecting");
    expect(
      screen.getByText("Tick packages, classes or tests to chart only those."),
    ).toBeVisible();

    await user.click(
      screen.getByRole("checkbox", { name: "Include com.example in charts" }),
    );
    await user.click(screen.getByRole("button", { name: "Expand all" }));
    expect(container.querySelectorAll(".tra-row-select:checked")).toHaveLength(
      4,
    );

    await user.click(screen.getByRole("button", { name: "Done" }));
    expect(history).not.toHaveClass("tra-history-container--selecting");
    expect(container.querySelectorAll(".tra-row-select:checked")).toHaveLength(
      0,
    );
  });

  it("hides tests that did not run unless asked to show them", async () => {
    const data = {
      builds: ["2", "1"],
      results: [
        group("p", [
          group("T", [
            test("kept", ["PASSED", "PASSED"]),
            test("gone", ["N/A", "N/A"]),
          ]),
        ]),
      ],
    };
    const optionsButton = document.createElement("button");
    const { container, user } = await renderPage(data, { optionsButton });
    await user.click(screen.getByRole("button", { name: "Expand all" }));
    expect(rowNames(container)).toEqual(["p", "T", "kept"]);

    await act(async () => optionsButton.click());
    await user.click(
      await screen.findByLabelText(
        "Show tests that did not run in these builds",
      ),
    );
    await user.click(screen.getByRole("button", { name: "Expand all" }));
    expect(rowNames(container)).toEqual(["p", "T", "kept", "gone"]);
  });

  it("toggles the options from the app bar and reloads with the chosen builds", async () => {
    const optionsButton = document.createElement("button");
    const { api, user } = await renderPage(undefined, { optionsButton });
    expect(screen.queryByText("Options")).toBeNull();

    await act(async () => optionsButton.click());
    expect(await screen.findByText("Options")).toBeInTheDocument();
    expect(optionsButton).toHaveAttribute("aria-expanded", "true");

    const builds = screen.getByLabelText("Number of builds");
    await user.clear(builds);
    await user.type(builds, "2");
    await user.click(
      screen.getByLabelText("Hide TestNG configuration methods"),
    );
    await user.click(screen.getByRole("button", { name: "Update" }));
    expect(api.load).toHaveBeenLastCalledWith("2", true);

    await user.click(screen.getByLabelText("All builds"));
    expect(builds).toBeDisabled();
    await user.click(screen.getByRole("button", { name: "Update" }));
    expect(api.load).toHaveBeenLastCalledWith("-1", true);

    await act(async () => optionsButton.click());
    await waitFor(() => expect(screen.queryByText("Options")).toBeNull());
  });

  it("only offers the bar chart for passes and failures", async () => {
    const optionsButton = document.createElement("button");
    const { user } = await renderPage(undefined, { optionsButton });
    await act(async () => optionsButton.click());
    const bar = await screen.findByLabelText("Bar");
    expect(bar).toBeEnabled();
    await user.selectOptions(screen.getByLabelText("Chart data"), "passrate");
    expect(bar).toBeDisabled();
  });

  it("shows run times when asked", async () => {
    const optionsButton = document.createElement("button");
    const { container, user } = await renderPage(undefined, { optionsButton });
    await act(async () => optionsButton.click());
    await user.click(
      await screen.findByLabelText("Show run time for each test"),
    );
    expect(
      row(container, "com.example").querySelector(".tra-build"),
    ).toHaveTextContent("0.200s");
  });

  it("downloads the CSV for the chosen builds from the app bar", async () => {
    const csvButton = document.createElement("button");
    const download = vi.fn();
    await renderPage(undefined, { csvButton, download });
    await act(async () => csvButton.click());
    expect(download).toHaveBeenCalledWith("csv?builds=10&durations=false");
  });

  it("says when the builds have no test results", async () => {
    render(
      <AnalyzerPage
        bootstrap={bootstrap()}
        client={client({ builds: [], results: [] })}
      />,
    );
    expect(
      await screen.findByText(
        "No test results were found for the selected builds.",
      ),
    ).toBeInTheDocument();
  });

  it("says when the results cannot be loaded", async () => {
    const failing: AnalyzerClient = {
      load: vi.fn().mockRejectedValue(new Error("500")),
      csvUrl: () => "",
    };
    render(<AnalyzerPage bootstrap={bootstrap()} client={failing} />);
    expect(await screen.findByText(/could not be loaded/)).toBeInTheDocument();
  });

  it("uses the custom status names", async () => {
    const { container } = await renderPage(
      { builds: ["1"], results: [group("p", [test("t", ["PASSED"])])] },
      {
        bootstrap: bootstrap({
          labels: { passed: "OK", failed: "KO", skipped: "-", na: "none" },
        }),
      },
    );
    expect(row(container, "p").querySelector(".tra-build")).toHaveAttribute(
      "title",
      "Build #1: OK in 0.100s",
    );
  });
});

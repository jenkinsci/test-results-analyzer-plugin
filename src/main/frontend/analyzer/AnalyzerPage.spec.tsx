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
    csvUrl: (builds, durations, buildNumbers) =>
      `csv?builds=${builds}&durations=${durations}${
        buildNumbers === undefined ? "" : `&buildNumbers=${buildNumbers}`
      }`,
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

  it("loads and downloads the chosen builds", async () => {
    const optionsButton = document.createElement("button");
    const csvButton = document.createElement("button");
    const download = vi.fn();
    const { api, user } = await renderPage(undefined, {
      optionsButton,
      csvButton,
      download,
    });
    await act(async () => optionsButton.click());
    expect(
      await screen.findByRole("radiogroup", { name: "Builds" }),
    ).toBeInTheDocument();
    await user.click(await screen.findByLabelText("Specific builds"));
    expect(screen.queryByLabelText("Number of builds")).toBeNull();
    const update = screen.getByRole("button", { name: "Update" });
    expect(update).toBeDisabled();

    const numbers = screen.getByLabelText("Build numbers");
    await user.type(numbers, "3, x");
    expect(numbers).toHaveAttribute("aria-invalid", "true");
    expect(
      screen.getByText('"x" is not a build number or a range such as 40-53.'),
    ).toBeInTheDocument();
    expect(update).toBeDisabled();

    // Not applied yet, so the CSV is still of the builds shown
    await act(async () => csvButton.click());
    expect(download).toHaveBeenLastCalledWith("csv?builds=10&durations=false");

    await user.clear(numbers);
    await user.type(numbers, " 1, 3 ");
    expect(numbers).toHaveAttribute("aria-invalid", "false");
    await user.click(update);
    expect(api.load).toHaveBeenLastCalledWith("10", false, "1, 3");

    await act(async () => csvButton.click());
    expect(download).toHaveBeenLastCalledWith(
      "csv?builds=10&durations=false&buildNumbers=1, 3",
    );

    await user.click(screen.getByLabelText("Latest"));
    await user.click(screen.getByRole("button", { name: "Update" }));
    expect(api.load).toHaveBeenLastCalledWith("10", false);
  });

  it("shows only the tests whose status differs when asked", async () => {
    const data = {
      builds: ["2", "1"],
      results: [
        group("p", [
          group("T", [
            test("same", ["PASSED", "PASSED"]),
            test("broke", ["FAILED", "PASSED"]),
          ]),
        ]),
      ],
    };
    const optionsButton = document.createElement("button");
    const { container, user } = await renderPage(data, { optionsButton });
    await act(async () => optionsButton.click());
    await user.click(
      await screen.findByLabelText(
        "Only show tests whose status differs between the builds",
      ),
    );
    await user.click(screen.getByRole("button", { name: "Expand all" }));
    expect(rowNames(container)).toEqual(["p", "T", "broke"]);
  });

  it("says when no test differs between the builds", async () => {
    const optionsButton = document.createElement("button");
    const { user } = await renderPage(
      {
        builds: ["2", "1"],
        results: [group("p", [test("same", ["PASSED", "PASSED"])])],
      },
      { optionsButton },
    );
    await act(async () => optionsButton.click());
    await user.click(
      await screen.findByLabelText(
        "Only show tests whose status differs between the builds",
      ),
    );
    expect(
      screen.getByText("No test results differ between the selected builds."),
    ).toBeInTheDocument();
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

  it("labels builds by display name, number or date", async () => {
    const data: AnalyzerData = {
      ...calculatorData(),
      buildInfo: [
        {
          number: 3,
          displayName: "release-1.3",
          timestamp: Date.UTC(2026, 9, 10, 14, 32),
        },
        { number: 2, displayName: "#2", timestamp: Date.UTC(2026, 9, 9, 9, 5) },
        { number: 1, displayName: "#1" },
      ],
    };
    const optionsButton = document.createElement("button");
    const { container, user } = await renderPage(data, { optionsButton });
    const headers = () =>
      [...container.querySelectorAll(".tra-build--header")].map(
        (header) => header.textContent,
      );
    expect(headers()).toEqual(["release-1.3", "#2", "#1"]);
    expect(container.querySelector(".tra-build--header")).toHaveAttribute(
      "title",
      expect.stringMatching(/^#3 release-1\.3 \(.*2026.*\)$/),
    );
    expect(
      row(container, "com.example").querySelector(".tra-build"),
    ).toHaveAttribute(
      "title",
      expect.stringMatching(/^Build #3 release-1\.3 \(.*\): FAILED/),
    );
    expect(container.querySelector(".tra-chip")).toHaveTextContent(
      "release-1.3",
    );

    await act(async () => optionsButton.click());
    const labelBy = await screen.findByLabelText("Label builds by");
    expect(labelBy).toHaveValue("name");
    await user.selectOptions(labelBy, "number");
    expect(headers()).toEqual(["#3", "#2", "#1"]);
    expect(container.querySelector(".tra-chip")).toHaveTextContent("#3");

    await user.selectOptions(labelBy, "date");
    const [newest, previous, oldest] = headers();
    expect(newest).toMatch(/Oct/);
    expect(previous).toMatch(/Oct/);
    expect(newest).not.toBe(previous);
    expect(oldest).toBe("#1");
  });

  it("labels builds as configured by the administrator", async () => {
    const defaults = bootstrap().defaults;
    const { container } = await renderPage(
      {
        ...calculatorData(),
        buildInfo: [{ number: 3, displayName: "release-1.3" }],
      },
      {
        bootstrap: bootstrap({
          defaults: { ...defaults, buildLabel: "number" },
        }),
      },
    );
    expect(container.querySelector(".tra-build--header")).toHaveTextContent(
      "#3",
    );
  });
});

# [Test Results Analyzer](https://plugins.jenkins.io/test-results-analyzer/)

Shows the history of your test results across builds, so you can see when a package, class or
test started failing, how often it flips between passing and failing, and which tests break most.

## About

When a test fails it is usually not enough to know that it failed in the latest build: you want to
know whether it is a new failure, whether it is flaky, and how long it has been like that.
Finding that out by opening every build's test report is slow.

The analyzer collects the results published by "Publish JUnit test result report" (or the TestNG
plugin) and shows them in one place. Open it from the **Test Results Analyzer** link in a job's
side panel.

![Test Results Analyzer](docs/images/analyzer-light.png)

- **History**: one card per package. Expand a package to see its classes and tests. Each row has a
  strip of squares, one per build with the newest first, coloured by result. Click a square to open
  that build. Rows also show how often the item passed and how many times it switched between
  passing and failing. A red marker flags a test that passed in the previous build and fails now.
- **Filter**: type part of a package, class or test name to narrow the list.
- **Charts**: line, stacked bar and pie charts of passes and failures per build, or of test run
  times. Select rows to chart only those items. Click a build on the line chart to show it in the
  pie chart. Each chart can be saved as an image.
- **Most broken tests**: the tests that failed most often, with links to the builds they failed in.
- **Download CSV**: exports the visible history.

Use **Options** to change the number of builds, show run times instead of results, hide TestNG
configuration methods, or pick which charts to draw. Defaults, run-time thresholds, custom status
names and custom status colours are set under *Manage Jenkins › System › Test Results Analyzer*.

The page follows the Jenkins theme, including dark mode, and adapts to small screens.

![Dark theme](docs/images/analyzer-dark.png)

## Change Log
**[Changelog](./CHANGELOG.md)**


## License
[**Apache-2.0 license**](https://www.apache.org/licenses/LICENSE-2.0)


## Report an Issue
Please report issues and enhancements through the
[Jenkins issue tracker](https://www.jenkins.io/participate/report-issue/redirect/#19327).
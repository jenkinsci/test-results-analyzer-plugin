<!-- Comment:
A great PR typically begins with the line below.
-->

<!-- in case you work on a Jira issue, replace XXXXX with the numeric part of the issue ID you created in Jira -->
See [JENKINS-XXXXX](https://issues.jenkins.io/browse/JENKINS-XXXXX).
<!-- in case you work on github issue -->
See #XXXXX
<!-- in case this PR solves Github issue use close #### or closes, closed, fix, fixes, fixed, resolve, resolves, resolved -->

<!-- Comment:
If the issue is not fully described in Jira / Github, add more information here (justification, pull request links, etc.).

 * We do not require Jira / Github issues for minor improvements.
 * Bug fixes should have a Jira / Github issue to facilitate the backporting process.
 * Major new features should have a Jira / Github issue.
-->

### Testing done

<!-- Comment:
Provide a clear description of how this change was tested.
At minimum this should include proof that a computer has executed the changed lines.
Ideally this should include an automated test or an explanation as to why this change has no tests.
Note that automated test coverage is less than complete, so a successful PR build does not necessarily imply that a computer has executed the changed lines.
If automated test coverage does not exist for the lines you are changing, **you must describe** the scenario(s) in which you manually tested the change.
For frontend changes, include screenshots of the relevant page(s) before and after the change.
For refactoring and code cleanup changes, exercise the code before and after the change and verify the behavior remains the same.
-->

### Proposed upgrade guidelines

N/A

### Submitter checklist

- [ ] The Jira / Github issue, if it exists, is well-described.
- [ ] The changelog entries and upgrade guidelines are appropriate for the audience affected by the change (users or developers, depending on the change) and are in the [imperative mood](https://en.wikipedia.org/wiki/Imperative_mood).
  - The changelog generator for plugins uses the **pull request title as the changelog entry**.
  - Fill in the **Proposed upgrade guidelines** section only if there are breaking changes or changes that may require extra steps from users during the upgrade.
- [ ] There is automated testing or an explanation that explains why this change has no tests.
- [ ] New public functions for internal use only are annotated with `@NoExternalUse`. In case it is used by non java code the `Used by {@code <panel>.jelly}` Javadocs are annotated.
- [ ] New or substantially changed JavaScript is not defined inline and does not call `eval` to ease the future introduction of Content Security Policy (CSP) directives (see [documentation](https://www.jenkins.io/doc/developer/security/csp/)).
- [ ] For dependency updates, there are links to external changelogs and, if possible, full differentials.
- [ ] For new APIs and extension points, there is a link to at least one consumer.
- [ ] Changes in the interface are documented.

### Maintainer checklist

Before the changes are marked as `ready-for-merge`:

- [ ] Conversations in the pull request are over, or it is explicit that a reviewer is not blocking the change.
- [ ] Changelog entries in the **pull request title** and/or **Proposed changelog entries** are accurate, human-readable, and in the imperative mood.
- [ ] Proper changelog labels are set so that the changelog can be generated automatically. See also [release-drafter-labels](https://github.com/jenkinsci/.github/blob/ce466227c534c42820a597cb8e9cac2f2334920a/.github/release-drafter.yml#L9-L50).

# Contributing Guidelines

There are two main ways to contribute to the project &mdash; submitting issues and submitting
fixes/changes/improvements via pull requests.

## Submitting issues

Both bug reports and feature requests are welcome.
Submit issues [here](https://github.com/neurallayer/roboquant/issues).

* Search for existing issues to avoid reporting duplicates.
* When submitting a bug report:
  * Test it against the most recently released version. It might have been already fixed.
  * Include the code that reproduces the problem or attach the link to the repository with the project which fully reproduces the problem.
  * However, don’t put off reporting any unique or rarely appearing issues just because you cannot consistently reproduce them.
  * If the bug is in behavior, then explain what behavior you’ve expected and what you’ve got.
* When submitting a feature request:
  * Explain why you need the feature &mdash; what’s your use-case, what’s your domain.
  * Explaining the problem you face is more important than suggesting a solution. Report your problem even if you don’t have any proposed solution.
  * If there is an alternative way to do what you need, then show the code of the alternative.

## Submitting PRs

We love PRs. Submit PRs [here](https://github.com/neurallayer/roboquant/pulls).
However, please keep in mind that maintainers will have to support the resulting code of the project,
so do familiarize yourself with the following guidelines.

* All development (both new features and bug fixes) is performed in the `master` branch.
  * Base PRs against the `master` branch.
  * PR should be linked with the issue, excluding minor documentation changes, the addition of unit tests, and fixing typos.
* If you make any code changes:
  * Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html).
  * Build the project to make sure it all works and passes the tests.
* If you fix a bug:
  * Write a test that reproduces the bug.
  * Fixes without tests are accepted only in exceptional circumstances if it can be shown that writing the corresponding test is too hard or otherwise impractical.
  * Follow the style of writing tests that is used in this project: name test classes as `xxxTest`. Don’t use backticks or underscores in test names.
* If you introduce any new public APIs:
  * All new APIs must come with documentation and tests.
  * If you plan API additions, then please start by submitting an issue with the proposed API design to gather community feedback.
  * Contact the maintainers to coordinate any big piece of work in advance via submitting an issue.
* If you fix documentation:
  * If you plan extensive rewrites/additions to the docs, then please contact the maintainers to coordinate the work in advance.

## PR workflow

1. Contributor builds roboquant locally and runs all unit tests via maven `mvn clean test`
 and integration tests (if it’s possible on contributor machine) via `mvn clean integration`.
2. Contributor submits the PR if the local build is successful and tests are green.
3. Reviewer marks the PR with the "Review" label at the start of the review process.
4. Reviewer leaves the comments or marks the PR with the label "LGTM."
5. Contributor answers the comments or fixes the proposed PR.
6. Reviewer marks the PR with the label "LGTM."
7. Maintainer could suggest merging the main branch to the PR branch a few times due to changes in the `main` branch.
8. Maintainer runs TC builds (unit tests and examples as integration tests).
9. The TC writes the result (passed or not passed) to the PR checks at the bottom of the proposed PR.
10. If it is possible, maintainers share the details of the failed build with the contributor.
11. Maintainer merges the PR if all checks are successful and there is no conflict with the main branch.

## How to fix an existing issue

* If you are going to work on the existing issue:
  * Comment on the existing issue if you want to work on it.
  * Wait till it is assigned to you by a maintainer.
  * Ensure that the issue not only describes a problem, but also describes a solution that has received positive feedback. Propose a solution if there isn’t any.
* If you are going to submit your first PR in roboquant project:
  * Find tickets with the label ["good first issue"](https://github.com/neurallayer/roboquant/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22+no%3Aassignee)
  which are not assigned to somebody.
* If you are an experienced developer with good knowledge of Kotlin, roboquant and trading, find tickets with the label
["good second issue"](https://github.com/neurallayer/roboquant/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+second+issue%22+no%3Aassignee), which are not assigned to somebody.
* If you are ready to participate in library design and add new functionality, find tickets with the label ["research"](https://github.com/neurallayer/roboquant/issues?q=is%3Aissue+is%3Aopen+label%3Aresearch), or join our [discussions](https://github.com/neurallayer/roboquant/discussions).

## Building

Roboquant is built with Maven.

* Run `mvn clean test` to build. It also runs all the tests.

You can import this project into _IntelliJ IDEA_ and use the Maven plugin to build and test the project.

## Contacting maintainers

* If something cannot be done, not convenient, or does not work - submit an issue.

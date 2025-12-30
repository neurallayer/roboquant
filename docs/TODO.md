# Release planning

## Introduction

There are still many features planned for _roboquant_. For sure, the lists on this page are also not complete and new features will pop up over time. Prioritization might shift based also based on community input and contribution.

But at least it gives insight into the current planning when to add certain features. And PRs are very welcome and might very well expedite certain features.

See also the [contributing](CONTRIBUTING.md) page on how to submit a PR.

## Version 0.8.x (end of 2021)

Version 0.8 is all about making sure that (back-)testing works, and the most common use-cases are covered. Much of the foundation for algo trading is in place with this release:

* [x] Improve the documentation in source code
* [x] Improve unit test coverage
* [x] Add visualizations for the Jupyter notebooks
* [x] Improve CSV parsing with extra configurable options
* [x] Improve error messages and warnings to be more helpful
* [x] Add documentation on how to install and get started
* [x] Support for advanced order types
* [x] Add info on key design concepts behind the software, so it becomes easier to contribute
* [x] Bring back Interactive Brokers integration
* [x] See how to best fit crypto trading with current account structure

## Version 0.9.x (2022)

Ensure the API is stable and start including AI and Machine Learning support

* [X] Improve documentation, notebooks, and examples
* [X] Include demo feeds for quick experimentation
* [X] Use better approach for generating charts

## Version 1.0 (end of 2022)

This version is all about adding stabilizing concepts and APIs. Although there is already integration available in earlier versions, that is just to validate the architectural concepts and design choices:

* [X] Improve the test coverage
* [X] Stabilize the exposed APIs
* [X] Optimize performance and avoid regressions
* [X] Refactor the order execution simulation
* [X] Improve the running of roboquant notebooks on public infra

## Version 2 (Q3 2023)

The topics mentioned here are some of the ideas for the future releases:

* [X] Add common back test approaches
* [X] Improve performance for specific use cases
* [X] Improve the default FlexPolicy
* [X] More flexible charting
* [X] Support for more feed formats (QuestDB)
* [X] Easier running of code on remote machines (web server, docker)
* [X] Separate charts functionality from Jupyter notebooks
* [X] Improve the Broker API
* [X] Smaller core module
* [X] More consistent naming of API’s
* [X] Support more CSV file formats out of the box
* [X] Improved usability of TaLib with auto sizing
* [X] Sunset no longer suported 3rd party data providers
* [X] Optimize memory usage for large back tests
* [X] Improve overall test coverage
* [X] Hardening code base

## Version 3.0 and later (Q4 2025)

The topics mentioned here are some of the ideas for the future releases:

* [x] Move to Kotlin 2.x
* [x] Harmonize API with the Python version
* [x] Simplify ways to extend the various components
* [x] Remove unused integrations


## Version 3.x and later (2026 and beyond)

* [ ] Move to Maven 4 or Amper once stable
* [ ] Improve ML integration with popular ML libraries
* [ ] Re-assess which 3rd party data providers to support
* [ ] Improve live trading capabilities (especially around visualizations and monitoring)
* [ ] Improve code documentation and examples

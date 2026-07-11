![roboquant Logo](/docs/roboquant_header.png)

![Kotlin](https://img.shields.io/badge/kotlin-2.4-blue.svg?logo=kotlin)
![Automated Build](https://github.com/neurallayer/roboquant/actions/workflows/verify.yml/badge.svg)
![GitHub code size](https://img.shields.io/github/languages/code-size/neurallayer/roboquant)
![APL v2](https://img.shields.io/badge/license-Apache%202-blue.svg)
![Bugs](https://img.shields.io/github/issues/neurallayer/roboquant/bug?color=red&label=bugs)
![Enhancements](https://img.shields.io/github/issues/neurallayer/roboquant/enhancement?color=yellow&label=enhancements)
![GitHub last commit](https://img.shields.io/github/last-commit/neurallayer/roboquant)
![GitHub commit activity](https://img.shields.io/github/commit-activity/m/neurallayer/roboquant)
![Maven Central](https://img.shields.io/maven-central/v/org.roboquant/roboquant?color=blue&)
![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=neurallayer_roboquant&metric=alert_status)
![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=neurallayer_roboquant&metric=security_rating)
![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=neurallayer_roboquant&metric=sqale_rating)

_Roboquant_ is an algorithmic trading platform that is fast and flexible while at the same time strives to be developer friendly. It is fully open source, and can be used in Jupyter Notebooks as well as standalone applications.

It is designed to be used by anyone serious about algo trading, from beginning retail traders to established trading firms. You can find out more at **[roboquant.org](https://roboquant.org)**.

Please consider giving this repository a star ⭐ if you like the project.

## Code sample
To demonstrate how easy it is to get started, the following code snippet shows all the ingredients required to back-test a trading strategy:

```kotlin
val strategy = EMACrossover() // ①
val feed = CSVFeed("data/US") // ②
val account = run(feed, strategy) // ③
println(account) // ④
```
1. Create the strategy that you want to validate
2. What data should be used, in this case, a directory with CSV files
3. Run the back test
4. Print the account state at the end of the run

You can find out more at the online [tutorial](https://roboquant.org/tutorial/index.html).

## Installation

### Library
You can develop our own trading application in your favourite IDE using _roboquant_ as a library.

![IntelliJ IDEA](/docs/idea_screenshot.png)

Just add roboquant and optional any of the additional modules as a dependency to your build tool, like Maven or Gradle or Kotlin Toolchain.

#### Maven
```xml
<dependency>
    <groupId>org.roboquant</groupId>
    <artifactId>roboquant</artifactId>
    <version>VERSION</version>
</dependency>
```

#### Gradle
```groovy
implementation group: 'org.roboquant', name: 'roboquant', version: 'VERSION'
```

#### Kotlin Toolchain
```yaml
dependencies:
    - org.roboquant:roboquant:VERSION
```

The latest available version is ![Maven Central](https://img.shields.io/maven-central/v/org.roboquant/roboquant?color=blue&)

## Features
Some key features of _roboquant_ are:

* [x] Blazingly fast back-testing, even on large volumes of historical data
* [x] Easy to develop your own strategies and integrate with third party brokers and data providers
* [x] Trade in multiple asset classes at the same time
* [x] Run anything from a technical indicator to complex machine-learning based strategies
* [x] Transition from back-testing to live trading with minimal changes
* [x] Trade on multi-markets with multi-currencies
* [x] Developed under open source with a permissive Apache license
* [x] Batteries included, for example, 150+ technical indicators and ready to use datasets
* [x] Out of the box integration with Alpaca, Interactive Brokers and the ECB, and many CSV data providers.

See also [Features](https://roboquant.org/background/features.html) for a more extensive feature list and how roboquant compares to some other platforms.

## License
Roboquant is distributed under the [Apache 2.0 License](/LICENSE). The Apache 2.0 license is a permissive license, meaning there are few restrictions on the use of the code.

## Thanks
Thanks to all the [great opensource software](docs/THIRDPARTY.md) that is powering _roboquant_.

## Disclaimer
_Roboquant_ also comes with live trading capabilities. Using this is at your own risk, and there are **NO GUARANTEES** about the correct functioning of the software.

PR are more than welcome, see also the [Contribution Guide](/docs/CONTRIBUTING.md) document.

If you’re missing some features, you can also open an issue on GitHub. See also the [todo documentation](/docs/TODO.md) for already identified backlog items if you look for something to work on.

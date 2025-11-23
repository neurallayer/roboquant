![roboquant Logo](/docs/roboquant_header.png)

![Kotlin](https://img.shields.io/badge/kotlin-2.2-blue.svg?logo=kotlin)
![Automated Build](https://github.com/neurallayer/roboquant/actions/workflows/maven.yml/badge.svg)
![GitHub code size](https://img.shields.io/github/languages/code-size/neurallayer/roboquant)
![APL v2](https://img.shields.io/badge/license-Apache%202-blue.svg)
![Bugs](https://img.shields.io/github/issues/neurallayer/roboquant/bug?color=red&label=bugs)
![Enhancements](https://img.shields.io/github/issues/neurallayer/roboquant/enhancement?color=yellow&label=enhancements)
![GitHub last commit](https://img.shields.io/github/last-commit/neurallayer/roboquant)
![GitHub commit activity](https://img.shields.io/github/commit-activity/m/neurallayer/roboquant)
![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/neurallayer/roboquant)
![Maven Central](https://img.shields.io/maven-central/v/org.roboquant/roboquant?color=blue&)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/org.roboquant/roboquant?server=https%3A%2F%2Fs01.oss.sonatype.org)
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

Just add roboquant and optional any of the additional modules as a dependency to your build tool, like Maven or Gradle.

#### Maven
Add the following dependency to your `pom.xml` file:
```xml
<dependency>
    <groupId>org.roboquant</groupId>
    <artifactId>roboquant</artifactId>
    <version>VERSION</version>
</dependency>
```

Or if you want to create your own new algo-trading project from scratch, you can run the Maven Archetype:

```shell
mvn archetype:generate                          \
-DarchetypeGroupId=org.roboquant                \
-DarchetypeArtifactId=roboquant-quickstart      \
-DarchetypeVersion=2.0.0                        \
-DgroupId=org.mydomain                          \
-DartifactId=myapp                              \
-Dversion=1.0-SNAPSHOT
```

#### Gradle
Add the following line to your gradle script:
```groovy
implementation group: 'org.roboquant', name: 'roboquant', version: 'VERSION'
```

See also [installation guide](/docs/INSTALL.md) for more ways to install and use _roboquant_.

Latest available versions:

* Regular version: ![Maven Central](https://img.shields.io/maven-central/v/org.roboquant/roboquant?color=blue&)
* Snapshot version: ![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/org.roboquant/roboquant?server=https%3A%2F%2Fs01.oss.sonatype.org)

### Jupyter Notebooks
You can interactively develop your own strategies using a Jupyter Notebook.

![Jupyter Lab](/docs/jupyter_screenshot.png)

If you just want to try it out without any installation, go to ![Binder](https://mybinder.org/badge_logo.svg)

If you already have Docker installed, all it takes is a single command to have a fully functional Jupyter Notebook environment available on your local machine. This image also comes with several example notebooks included.

```shell
docker run -p 8888:8888 roboquant/jupyter
```

## Features
Some key features of _roboquant_ are:

* [x] Blazingly fast back-testing, even on large volumes of historical data
* [x] Easy to develop your own strategies and integrate with third party brokers and data providers
* [x] Trade in multiple asset classes at the same time
* [x] Run anything from a technical indicator to complex machine-learning based strategies
* [x] Transition from back-testing to live trading with minimal changes
* [x] Trade on multi-markets with multi-currencies
* [x] Developed under open source with a permissive Apache license
* [x] Use Jupyter Notebooks with insightful charts if you prefer interactive development
* [x] Batteries included, for example, 150+ technical indicators and ready to use datasets
* [x] Out of the box integration with Alpaca, Interactive Brokers and the ECB, and many CSV data providers.

See also [Features](https://roboquant.org/background/features.html) for a more extensive feature list and how roboquant compares to some other platforms.

## License
Roboquant is distributed under the [Apache 2.0 License](/LICENSE). The Apache 2.0 license is a permissive license, meaning there are few restrictions on the use of the code.

## Related projects
The following list includes projects that extend the behavior of roboquant in some ways:

* Support for trading using the Bybit broker and feeds: https://github.com/alleyway/roboquant-bybit
* Support for a Maven archetype: https://github.com/neurallayer/roboquant-quickstart
* Several sample notebooks and a docker container: https://github.com/neurallayer/roboquant-notebook

## Thanks
Besides all the [great opensource software](docs/THIRDPARTY.md) that is powering _roboquant_, also special thanks to JetBrains for making a [license](https://www.jetbrains.com/community/opensource/) available for **IntelliJ IDEA**.

![JetBrains](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.png)

## Disclaimer
_Roboquant_ also comes with live trading capabilities. Using this is at your own risk, and there are **NO GUARANTEES** about the correct functioning of the software.

PR are more than welcome, see also the [Contribution Guide](/docs/CONTRIBUTING.md) document.

If you’re missing some features, you can also open an issue on GitHub. See also the [todo documentation](/docs/TODO.md) for already identified backlog items if you look for something to work on.

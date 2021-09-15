# Roboquant

[![Automated Build](https://github.com/neurallayer/roboquant/actions/workflows/maven.yml/badge.svg)](https://github.com/neurallayer/roboquant/actions/workflows/maven.yml)
[![APL v2](https://img.shields.io/badge/license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/neurallayer/roboquant/main?filepath=notebooks)
[![Git](https://img.shields.io/github/release/neurallayer/roboquant.svg)](https://github.com/neurallayer/roboquant/releases)

Roboquant is an algorithmic trading platform that is fast and flexible while at the same time strives to be easy to use. 
It is fully open source and written in Kotlin. It can be used in Jupyter notebooks as well as standalone applications.
It is meant to be used by anyone serious about algo trading, from retail traders to proprietary trading firms. 

You can find out more at **[roboquant.org](https://roboquant.org)**
![roboquant Logo](/docs/roboquant_logo.png)


## Code sample
To demonstrate how easy it is to get started, the following code snippet shows all the key 
ingredients required to back-test a trading strategy:

```kotlin
val strategy = EMACrossover()
val metric = AccountSummary()
val roboquant =  Roboquant(strategy, metric)

val feed = CSVFeed("data/US")
roboquant.run(feed)
```


## Features
Some key features of roboquant are:

* [x]  Fast back-tests, even on large volumes of data.
* [x]  Easy to develop your own strategies and integrate with brokers and data feeds
* [x]  Trade in multiple asset classes at the same time
* [x]  Run anything from a technical indicator to complex deep learning based strategies
* [x]  Easy to transition from back-testing to live trading
* [x]  Trade on multi-markets with multi-currencies
* [x]  Developed under open source with a permissive Apache license
* [x]  Use Jupyter notebooks with insightful charts if you prefer interactive development
* [x]  Batteries included: over 200 indicators


## Installation
If you have already Docker installed, all it takes is a single command to have a fully functional notebook environment available:

```shell
docker run --rm -p 8888:8888 roboquant/jupyter 
```

This also comes with several tutorials. See the [installation guide](/INSTALL.md) for more ways to install and use roboquant, for example if you want to use it in a standalone application.

## License
Roboquant is distributed under the [Apache 2.0 License](/LICENSE).  

## Disclaimer
Roboquant also comes with live trading and paper trading capabilities. Using this is at your own risk and there are NO guarantees about the correct functioning of the software. 

## Beta version
Roboquant is in its pre-1.0 release and bugs are fore sure still present. Also expect API's to change without notice.  PR are more than welcome, see also the [Contribution Guide](/CONTRIBUTING.md) document. If you're missing some features, just open an issue on GitHub. 

See also the [todo documentation](/TODO.md) for already identified backlog items if you look
for something to work on.




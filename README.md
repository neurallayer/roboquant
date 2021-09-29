# Roboquant

![roboquant](https://img.shields.io/badge/roboquant-0.8-blue.svg)
[![Kotlin](https://img.shields.io/badge/kotlin-1.5-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Automated Build](https://github.com/neurallayer/roboquant/actions/workflows/maven.yml/badge.svg)](https://github.com/neurallayer/roboquant/actions/workflows/maven.yml)
[![APL v2](https://img.shields.io/badge/license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/neurallayer/roboquant-notebook/main?filepath=tutorials)
![GitHub issues](https://img.shields.io/github/issues/neurallayer/roboquant)
![GitHub last commit](https://img.shields.io/github/last-commit/neurallayer/roboquant)
![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/neurallayer/roboquant)


Roboquant is an algorithmic trading platform that is fast and flexible while at the same time strives to be easy to use. It is fully open source and written in Kotlin. It can be used in Jupyter Notebooks as well as standalone applications. It is meant to be used by anyone serious about algo trading, from retail traders to proprietary trading firms.

You can find out more at **[roboquant.org](https://roboquant.org)**
![roboquant Logo](/docs/roboquant_logo.png)


## Code sample
To demonstrate how easy it is to get started, the following code snippet shows all the key ingredients required to back-test a trading strategy:

```kotlin
val strategy = EMACrossover()
val metric = AccountSummary()
val roboquant =  Roboquant(strategy, metric)

val feed = CSVFeed("data/US")
roboquant.run(feed)
```


## Features
Some key features of roboquant are:

* [x]  Very fast back-testing, even on large volumes of data.
* [x]  Easy to develop your own strategies and integrate with brokers and data feeds
* [x]  Trade in multiple asset classes at the same time
* [x]  Run anything from a technical indicator to complex deep learning based strategies
* [x]  Easy to transition from back-testing to live trading
* [x]  Trade on multi-markets with multi-currencies
* [x]  Developed under open source with a permissive Apache license
* [x]  Use Jupyter Notebooks with insightful charts if you prefer interactive development
* [x]  Batteries included: over 200 technical indicators
* [x]  Out of the box integration with  Alpaca, Interactive Brokers, IEX Cloud, Yahoo Finance, Alpha Vantage, Binance and most other crypto exchanges 

## Installation
If you have already Docker installed, all it takes is a single command to have a fully functional Jupyter Lab environment available:

```shell
docker run --rm -p 8888:8888 roboquant/jupyter 
```

This also comes with several tutorials. And if you just want to try it without any installation, go to [![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/neurallayer/roboquant-notebook/main?filepath=tutorials), although it might be a bit slow to start the environment. 

![Jupyter Lab](/docs/jupyter-lab.png)

See the [installation guide](/docs/INSTALL.md) for more ways to install and use roboquant, for example if you want to use it in a standalone application and want to include the libraries.

## License
Roboquant is distributed under the [Apache 2.0 License](/LICENSE).  

## Disclaimer
Roboquant also comes with live trading and paper trading capabilities. Using this is at your own risk and there are NO guarantees about the correct functioning of the software. 

## Beta version
Roboquant is in its pre-1.0 release and bugs are fore sure still present. Also expect API's to change without notice.  PR are more than welcome, see also the [Contribution Guide](/docs/CONTRIBUTING.md) document. If you're missing some features, just open an issue on GitHub. 

See also the [todo documentation](/docs/TODO.md) for already identified backlog items if you look for something to work on.




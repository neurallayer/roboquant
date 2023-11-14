# Module roboquant

The roboquant module contains the core functionality required to create trading strategies and run them.  

This module doesn't contain the integration with third party data providers and brokers. 
For this, see the additional `roboquant-...` modules like `roboquant-alpaca`.

# Package org.roboquant.feeds

A feed represents a source of information that can be used during back-testing and live trading. The core package
comes with several feeds out of the box:

- csv: for reading historic prices in CSV files
- random walk: for simulating random walks
- test: for testing against a predefined set of prices

If you want to use other brokers and feeds, have a look at the other `roboquant-...` modules.

# Package org.roboquant.strategies

Contains the `Strategy` interface and several inplementations


# Package org.roboquant.policies

Policies receive zero or more signals and based on these signals create orders. 

# Package org.roboquant.brokers

This package has the `Broker` interface and contains a single implementation, the SimBroker to simulate a broker during back tests.

If you want to use live brokers and paper trading, have a look at one of the other modules.

# Package org.roboquant.metrics

This package has various metrics that help to monitor and understand how a strategy is performing.

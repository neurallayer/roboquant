# Module roboquant

The roboquant-core module contains the functionality required to create trading strategies and back-test them.

The flow of data between the components is as follows:

    Feed --|event|--> Strategy --|signal|--> Policy --|order|--> Broker --|account|--> Metric

It is the Roboquant that orchestrates and coordinates this flow of data. 

The core module doesn't contain the integration with 3rd party data providers and brokers. For this see the 
roboquant-extra and roboquant-crypto modules.

# Package org.roboquant.feeds

A feed represents a source of information that can be used during back-testing and live trading. The core package
comes with several feeds out of the box:

- csv: for reading historic prices in CSV files
- random walk: for simulating random walks
- test: for testing against a predefined set of prices
- avro: for creating and reading historic prices in AVRO files

If you want to use other feeds, have a look at the roboquant-extra and roboquant-crypto modules.

# Package org.roboquant.strategies

Strategies contain the logic that interpreted the events in a feed and generates zero or more Signal. So a strategy
doesn't directly generate an Order (that is left to a Policy).


# Package org.roboquant.policy

Policies receive zero or more signals and based on these signals create orders. 

# Package org.roboquant.brokers

This package has the Broker interface and contains a single implementation, the SimBroker to simulate a broker
during back tests.

If you want to use live brokers and paper trading, have a look at the roboquant-extra and roboquant-crypto modules.

# Package org.roboquant.metrics

This package has various metrics that help to monitor and understand how a strategy is performing.
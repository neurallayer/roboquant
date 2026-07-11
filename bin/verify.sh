#!/bin/bash

# Check all modules except ibkr since that requires the ibkr jar file to be installed locally
./kotlin check -m 'roboquant' -m 'roboquant-avro' -m 'roboquant-charts' -m 'roboquant-questdb' -m 'roboquant-alpaca'
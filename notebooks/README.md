# Tutorials
This directory contains a number of Jupyter notebook tutorials that demonstrate some key capabilities of roboquant.

## Run

You can run the notebooks without installing anything on your local machine by just clicking the following link [![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/neurallayer/roboquant/main?filepath=notebooks)

It might take some time before the MyBinder environment is setup and you can run the notebooks. This is mainly due to the limited resources that these free environments have available and the fact that Java/Kotlin isn't included in the default environment and needs to be installed first.   

Alternatively you can run a docker container on your local machine that has all the notebooks included and will automatically start a jupyter-lab environment:

```shell
docker run --rm -p 8888:8888 roboquant/jupyter 
```

## Content

The following content is included and if you click on the notebook name, the notebook will be automatically opened on MyBinder.org:

- [visualization.ipynb](https://mybinder.org/v2/gh/neurallayer/roboquant/main?filepath=notebooks/visualization.ipynb) - Shows the charting capabilities of roboquant
- [alpaca.ipynb](https://mybinder.org/v2/gh/neurallayer/roboquant/main?filepath=notebooks/alpaca.ipynb) - Shows the integration with Alpaca broker
- [binance.ipynb](https://mybinder.org/v2/gh/neurallayer/roboquant/main?filepath=notebooks/binance.ipynb) - Shows the integration with Binance crypto exchange
- [crypto.ipynb](https://mybinder.org/v2/gh/neurallayer/roboquant/main?filepath=notebooks/crypto.ipynb) - Shows the integration with many crypto exchange using the XChange library 
- [custom_strategies.ipynb](https://mybinder.org/v2/gh/neurallayer/roboquant/main?filepath=notebooks/custom_strategies.ipynb) - How to develop a custom strategy
- dotenv - Example environment file that can hold keys and secrets that are often required to connect to third party data providers and brokers
- [iex_cloud.ipynb](https://mybinder.org/v2/gh/neurallayer/roboquant/main?filepath=notebooks/iex_cloud.ipynb) - Shows the integration with IEX Cloud data provider
- [introduction.ipynb](https://mybinder.org/v2/gh/neurallayer/roboquant/main?filepath=notebooks/introduction.ipynb) - High level introduction
- README.md - This README file
- [technical_analysis.ipynb](https://mybinder.org/v2/gh/neurallayer/roboquant/main?filepath=notebooks/technical_analysis.ipynb) - How to develop technical analysis strategies quickly 



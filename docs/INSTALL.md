# Installation

There are two ways to use roboquant:

1. Interactively in a Jupyter notebook. If you want to get up and running quickly, and want to experiment with many strategies, this is the best approach. Additionally, you get many charts out-of-the-box.
2. As a library in your own Kotlin or Java application. If you plan to develop large and complex trading strategies, this is the good approach since you have the full power of an IDE like IntelliJ IDEA at your disposal.

## Jupyter Notebook
If you have already Docker installed, it only takes a single command to have a fully functional Jupyter Lab environment available:

```shell
docker run -p 8888:8888 roboquant/jupyter
```

This will pull the latest image from DockerHub and run it on your local machine. The image comes with several notebooks included that demonstrate how to develop and run your own strategies.

If you don’t have Docker yet installed on your computer, check out [Docker get started](https://www.docker.com/get-started) and download `Docker Desktop` from there. If you are running Linux, then your distribution likely already has Docker included.
If you don’t have Docker yet installed on your computer, check out [Docker get started](https://www.docker.com/get-started) and download `Docker Desktop` from there. If you are running Linux, then your distribution likely already has Docker included.

If you don’t want to install anything locally, you can:

1. Try some same notebooks right now in your browser by clicking: ![Binder](https://mybinder.org/badge_logo.svg)
2. Go to [JetBrains Datalore](https://datalore.jetbrains.com/) and create an account there. It supports Kotlin Notebooks and has a free tier available if you just want to try it out.

## Standalone Application
Just add `roboquant` as a dependency to your build tool, like Maven or Gradle.

**Maven**

```xml
<dependency>
    <groupId>org.roboquant</groupId>
    <artifactId>roboquant</artifactId>
    <version>VERSION</version>
</dependency>
```

**Gradle**
```groovy
implementation group: 'org.roboquant', name: 'roboquant', version: 'VERSION'
```

Next to `roboquant`, the following additional artefacts are available for inclusion in your application:

* **roboquant-jupyter** Adds support for running roboquant inside Jupyter Notebooks
* **roboquant-ibkr** Adds support for Interactive Brokers
* **roboquant-alpaca** Adds support for Alpaca broker
* **roboquant-questdb** Adds support for storing prices and metrics in QuestDB
* **roboquant-avro** Adds support for storing prices and metrics in Avro files
* **roboquant-charts** Adds chart support using the ECharts library
* **roboquant-ssr** Adds support for serverside rendering of charts running the JavaScript on GraalVM

## Building from source
First start with cloning the roboquant GitHub repository to your local disk. The quickest way to be up and running is then to install IntelliJ IDEA (either the free community edition or the paid Ultimate version) and open the directory you just cloned. IntelliJ IDEA will recognize it as a Kotlin/Maven project, and you can build it and run test directly from the IDE.

_Roboquant_ uses a directory setup that is similar to most other Kotlin projects:

```
root
    submodule1
        src/main/kotlin
        src/test/kotlin
    submodule2
        src/main/kotlin
        src/test/kotlin
```

All source code is written in Kotlin, so there are no Java or other language source files.

Roboquant uses Maven wrapper for the build process, so building and installing the roboquant libraries locally, is as easy as:

```shell
./mvnw clean install
```

The build and install is tested using JDK 17 runtime.

**💡 TIP**\
If you plan to make many changes and updates to the source code, checkout the [Maven Daemon](https://github.com/apache/maven-mvnd) project that provides faster builds. It is an almost 100% drop-in replacement of the regular maven and is much faster.

If you want to deploy a regular release or snapshot, use the `-P release` option. This will include the required plugins and also generate source- and documentation-jar files. Additionally, it will also build and deploy the `roboquant-ibkr` module, so you’ll need the locally installed IBKR Java client library.

```shell
./mvnw clean deploy -P release
```

Of course, this requires the having the right credentials for deploying to the Maven Central repo. Also note that `autoReleaseAfterClose` of the `nexus-staging-maven-plugin` is set to false, meaning that if the deployment was sucessfull, you still need to (manually) release the software from staging to production.

## Interactive Brokers
If you don’t require integration with Interactive Brokers for your trading, you can skip this step.

Unfortunately, it is not allowed to redistribute the Interactive Brokers Java client. So you’ll have to download the TwsApi.jar file yourself. You can download the stable version `10.19` from here: https://interactivebrokers.github.io and within the downloaded archive file you’ll find the required `TwsApi.jar`.

Then install the jar file in the local Maven repository on your machine using the following command:

```shell
mvn install:install-file -Dfile=TwsApi.jar -DgroupId=com.interactivebrokers -DartifactId=tws-api -Dversion=10.19 -Dpackaging=jar
```

After this step, you can compile and install the modules including the `roboquant-ibkr` module

```shell
./mvnw install -P ibkr
```

**⚠️ WARNING**\
If the artefact cannot be found in your local Maven repository during a build, the ibkr profile with the module `roboquant-ibkr` will be skipped.

## Documentation
There is a special `doc` profile to generate documentation and see if there is something missing. You can run the following command to find missing documentation:

```shell
./mvnw dokka:dokka -P doc | grep WARNING
```

Please note the `release` profile has its own dokka task to generate a javadoc jar file.

## Performance Test
To see the built-in performance tests, run `roboquant/test/org/roboquant/samples/Performance.kt`:

The output should look something like this:
```
              _______
            | $   $ |             roboquant
            |   o   |             version: 3.0.0
            |_[___]_|             build: 2025-09-19T16:58:14Z
        ___ ___|_|___ ___         os: Mac OS X 26.0
       ()___)       ()___)        home: /Users/peter/.roboquant
      /  / |         | \  \       jvm: OpenJDK 64-Bit Server VM 17.0.16
     (___) |_________| (___)      kotlin: 2.2.0
      | |   __/___\__   | |       memory: 4096MB
      /_\  |_________|  /_\       cpu cores: 8
     // \\  |||   |||  // \\
     \\ //  |||   |||  \\ //
           ()__) ()__)
           ///     \\\
        __///_     _\\\__
       |______|   |______|

 CANDLES ASSETS EVENTS RUNS    FEED    FULL SEQUENTIAL PARALLEL CANDLES/S
 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     1M      10   1000  100    10ms    18ms     141ms      53ms       18M
     5M      50   1000  100     2ms     5ms     139ms      20ms      250M
    10M      50   2000  100     5ms     9ms     265ms      41ms      243M
    50M     100   5000  100     9ms    47ms    1073ms     130ms      384M
   100M     200   5000  100    16ms    98ms    1811ms     315ms      317M
   500M     500  10000  100    63ms   485ms   12031ms    2155ms      232M
  1000M     500  20000  100   115ms   979ms   23543ms    3974ms      251M

```

**📌 NOTE**\
The main purpose is to test the performance and stability of the back-test engine itself, not any specific feed, strategy or metric. So the overhead of those components is kept to a minimum, while still running full back tests.

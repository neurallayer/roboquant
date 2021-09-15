
# Installation
There are two ways to use roboquant:

* Interactively in a Jupyter notebook. If you want to get up and running quickly, and want to experiment with many strategies, this is the best approach. Additionally, you get many charts out-of-the-box.

* As a library in your own Kotlin or Java application. If you plan to develop large and complex trading strategies,
  this is the good approach since you have the full power of an IDE like IntelliJ IDEA at your disposal.


### Jupyter Notebook
If you have already Docker installed, all it takes is a single command to have a fully functional notebook environment
available:

```shell
docker run --rm -p 8888:8888 roboquant/jupyter 
```

The installation comes with several tutorial-notebooks that demonstrate how to develop and run your own strategies. If
you don't have Docker yet installed on your computer, check out [Docker get started](https://www.docker.com/get-started)
and download Docker for Desktop from there. If you are running Linux, then your distribution likely already has Docker
or Podman included.

If you don't want to install anything locally, you can use [JetBrains Datalore](https://datalore.jetbrains.com/).
It supports Kotlin notebooks and has a free tier available if you just want to try it out.

Or try some same notebooks right now [![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/neurallayer/roboquant/main?filepath=notebooks)

### Standalone application
Just add *roboquant-core* as a dependency to your build tool, like Maven or Gradle.

**Maven**
```xml
<dependency>
    <groupId>org.roboquant</groupId>
    <artifactId>roboquant-core</artifactId>
    <version>the version</version>
</dependency>
```

**Gradle**
```shell
implementation group: 'org.roboquant', name: 'roboquant-core', version: 'the version'    
```

Next to roboquant-core, the following additional modules are available for inclusion in your application:

* **roboquant-crypto** Adds support for many of today's most popular crypto exchanges
* **roboquant-extra** Adds out-of-the-box integrations with 3rd party brokers and data providers.

## Build from source
First start with cloning the roboquant GitHub repository to your local disk. The quickest way to be up and running is then to install IntelliJ IDEA (either the free community edition or the paid Ultimate version) and open the directory you just cloned. IntelliJ IDEA will recognize it as Kotlin/Maven project, and you can build it and run test directly from the IDE.

You can also use command line tools. Roboquant uses a setup and directory structure that removes much of the ceremony often found in many Java/Kotlin projects. So no need to go 10 directories deep to locate the source file you were looking for.

Roboquant uses Maven for the build process and assumes you already have this installed. Building, testing and installing the libraries locally, is as easy as:

```shell
mvn install
```

You can also build the Docker images from source. This script has to be run from the project root directory:

```shell
./docker/dockerbuild.sh
```

The build script has been tested and used with [Podman](https://podman.io/), but should also work with Docker. 

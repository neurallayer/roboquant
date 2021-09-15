# Roboquant Docker image

The roboquant docker image includes the following software:
- Jupyter notebook image (`jupyter/base-notebook`) as the base image
- Kotlin kernel for Jupyter
- OpenJDK 11 runtime
- Roboquant tutorials and test data

# Usage

You can start using Jupyter-Lab straight out-of-the-box by running the following command:

```bash
docker run --rm -p 8888:8888 roboquant/jupyter
```

or if you like the classic Jupyter-Notebook environment better:

```bash
docker run --rm -p 8888:8888 -e JUPYTER_ENABLE_LAB="" roboquant/jupyter
```

Then open the url that appears in your terminal in your browser to try it out. The tutorials' directory contains several 
notebooks that provide a nice introduction into roboquant.

# Build it

You can also build and run the Docker images from the source. The build script has to be run from the project root
directory:

```shell
./docker/dockerbuild.sh
```

Right now only bash shell scripts are provided to build and start notebooks. So if you're on Windows you either have to 
use WSL or run the commands in the shell script from the command line manually.

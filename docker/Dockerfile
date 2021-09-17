FROM jupyter/base-notebook:notebook-6.4.3

USER root

# Install the Java JRE runtime.
RUN apt-get update && apt-get install -y openjdk-11-jre

# The remaining we run under the standard user
USER 1000

# Install the Kotlin kernel in the Jupyter environment
RUN conda install -y -c jetbrains kotlin-jupyter-kernel
# RUN conda install -y openjdk
# RUN conda install -y -c jetbrains-dev kotlin-jupyter-kernel

# Make additional Kotlin kernels available with different memory configurations
RUN python -m kotlin_kernel add-kernel --name "SMALL 1GB" --jvm-arg=-Xmx1G
RUN python -m kotlin_kernel add-kernel --name "MEDIUM 4GB" --jvm-arg=-Xmx4G
RUN python -m kotlin_kernel add-kernel --name "LARGE 8GB" --jvm-arg=-Xmx8G
RUN python -m kotlin_kernel add-kernel --name "XLARGE 16GB" --jvm-arg=-Xmx16G

# Install the json files for the %use roboquant line magic to work
COPY --chown=1000:100 docker/assets/roboquant.json  /opt/conda/lib/python3.9/site-packages/run_kotlin_kernel/libraries/

# Copy the tutorial notebooks and sample data to the home directory
RUN mkdir -p /home/jovyan/tutorials
COPY --chown=1000:100 notebooks/*.ipynb /home/jovyan/tutorials/
COPY --chown=1000:100 data /home/jovyan/tutorials/data

# Better width for jupyter notebooks with a custom css
RUN mkdir -p /home/jovyan/.jupyter/custom
COPY --chown=1000:100  docker/assets/custom.css /home/jovyan/.jupyter/custom/

# Override a default settings of the base image, we switch to Jupyter Lab as the default
ENV JUPYTER_ENABLE_LAB=yes


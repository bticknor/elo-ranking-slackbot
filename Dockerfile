FROM ubuntu

MAINTAINER benjamin ticknor <ticknorbenjamin@gmail.com>

# DEPENDENCIES
# ================================

# update package index and installed packages
RUN apt-get update
RUN apt-get upgrade -y

# gnupg
RUN apt-get install -y gnupg && \
    apt-get install -y gnupg2 && \
    apt-get install -y gnupg1

# sbt 
RUN echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
RUN apt-get update
RUN apt-get install -y sbt

# git
RUN apt-get install -y git

# redis
RUN apt-get install -y redis

# java
RUN apt-get install -y default-jdk

# ================================

# copy the source code into the Docker image
RUN mkdir /randy
COPY / /randy/

# build the "fat jar" file via the sbt assembly command
RUN cd randy && sbt assembly 

# run bot script
CMD /randy/run_bot.sh


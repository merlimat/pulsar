<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->

# Apache Pulsar Docker Images

The Apache Pulsar community produces 2 docker images with each official release.

* `apachepulsar/pulsar` - contains the necessary components for a working Pulsar cluster
* `apachepulsar/pulsar-functions-python` - extends the `apachepulsar/pulsar` image by adding the Python
   dependencies required to run Pulsar Functions with Python runtime. 

Since the 2.10.0 release, these docker images run as an unnamed, non-root user that is also part of the root group, by
default. This was done to increase container security. The user is part of the root group to ensure that the container
image can easily run on OpenShift and to ensure that the Pulsar process can write to configuration files.

## Development

You can build and test these docker images on your own machine by running the `./build.sh` script in this directory.
Note that you first must build the project in order to have the right dependencies in your local environment.

## Building Derivative Custom Images

You can easily build an image with a curated list of connectors or offloaders based on the official Apache Pulsar
images. You can use the following sample docker image as a guide:

```Dockerfile
ARG VERSION

FROM apachepulsar/pulsar:${VERSION}

# Add the cassandra connector
RUN mkdir -p connectors && \
    cd connectors && \
    wget https://downloads.apache.org/pulsar/pulsar-${VERSION}/connectors/pulsar-io-cassandra-${VERSION}.nar 
```

Assuming that you have the above `Dockerfile` in your local directory and are running docker on your local host, you can
run the following command to build a custom image with the cassandra connector.

```shell
docker build --build-arg VERSION=2.9.1 -t pulsar-custom:2.9.1 .
```

## Troubleshooting non-root containers

Troubleshooting is harder because the docker image runs as a non-root user. For example, a non-root user won't be able
to download arbitrary utilities. There are several ways to troubleshoot.

One option is to build a custom docker image that includes your preferred debugging tools. Here is an example of adding
some tools to an existing docker image.

```Dockerfile
FROM apachepulsar/pulsar:2.10.0

# Switch to root user to download tools
USER 0

# Install your preferred utilities
RUN apt-get update \
     && apt-get install -y vim net-tools unzip \
     && apt-get clean \
     && rm -rf /var/lib/apt/lists/*
     
# Assuming you still want to run as a non root user by default
USER 10000
```

The remaining debug options depend on your environment. For example, if you have access to the host running your
container, you might be able to use the `docker exec` command to shell into the container. By using the `--user`
argument, you can run as the root user.

If you're running your container on kubernetes, you can override the container's default user by setting the pod's
`securityContext`.

Bitnami provides a helpful guide here: https://engineering.bitnami.com/articles/running-non-root-containers-on-openshift.html.
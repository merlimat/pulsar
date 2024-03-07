#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

ARG PULSAR_CLIENT_PYTHON_VERSION
ARG PULSAR_IMAGE

## Create stage to build the Python dependencies
## Since it needs to have GCC available, we're doing it in a different layer
FROM alpine:3.19 AS python-deps

RUN apk add --no-cache \
       bash \
       python3-dev \
       g++ \
       musl-dev \
       libffi-dev \
       py3-pip \
       py3-grpcio \
       py3-yaml

RUN pip3 install --break-system-packages \
        kazoo

ARG PULSAR_CLIENT_PYTHON_VERSION
RUN pip3 install --break-system-packages \
    pulsar-client[all]==${PULSAR_CLIENT_PYTHON_VERSION}

# Main image stage
ARG PULSAR_IMAGE
FROM $PULSAR_IMAGE

# Switch to root user to install PIP package
USER root
RUN apk add --no-cache py3-pip
USER 10000

# Copy Python depedencies from the other stage
COPY --from=python-deps /usr/lib/python3.11/site-packages /usr/lib/python3.11/site-packages

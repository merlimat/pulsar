#!/usr/bin/env bash

set -x -e

ARTIFACTS=(
    buildtools
    .
    pulsar-client
    pulsar-client-shaded
    pulsar-client-api
    pulsar-common
    pulsar-client-admin
    pulsar-client-admin-shaded
    pulsar-functions
    pulsar-functions/runtime
    pulsar-functions/instance
    pulsar-functions/proto
    pulsar-functions/secrets
    pulsar-functions/api-java
    pulsar-functions/utils
    pulsar-broker-common
    pulsar-io
    pulsar-io/core
    pulsar-io/common
    pulsar-zookeeper-utils
)

for DIR in ${ARTIFACTS[@]}; do
	echo "Processing module :" ${DIR}
    mvn --settings ./settings.xml -B deploy -DskipTests -DaltDeploymentRepository=releaseRepository::default::http://repo.splunk.com/artifactory/maven-splunk-local -pl ${DIR}
done

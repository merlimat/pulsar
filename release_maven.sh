#!/usr/bin/env bash

set -x -e

mvn --settings ./settings.xml -B deploy -DskipTests -DaltDeploymentRepository=releaseRepository::default::http://repo.splunk.com/artifactory/maven-splunk-local
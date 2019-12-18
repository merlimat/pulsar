#!/usr/bin/env bash

set -x -e

mvn -B -ntp --settings ./settings.xml -B deploy -DskipTests -DaltDeploymentRepository=releaseRepository::default::http://repo.splunk.com/artifactory/maven-splunk-local
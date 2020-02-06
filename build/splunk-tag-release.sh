#!/usr/bin/env bash

set -o pipefail
set -o errexit

if [ -z "$CI_JOB_ID" ]; then
    echo "CI_JOB_ID environment variable must be set"
    exit 1
fi

if [ -z "$SSH_PRIVATE_KEY" ]; then
    echo "SSH_PRIVATE_KEY environment variables must all be set";
    exit 2
fi

if [ ! -d pulsar-broker ]; then
    echo "Script must be run from top-level of repo"
    exit 3
fi

CUR_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
NEW_VERSION=$(echo $CUR_VERSION | sed 's/\([0-9]*\.[0-9]\.[0-9]\).*/\1.SPLK.'$CI_JOB_ID'/')
echo "Updating poms to version ${NEW_VERSION}"

mvn -f buildtools/pom.xml versions:set -DnewVersion=$NEW_VERSION
mvn -f pulsar-sql/presto-distribution/pom.xml versions:set -DnewVersion=$NEW_VERSION
mvn versions:set -DnewVersion=$NEW_VERSION

TAG=release-$NEW_VERSION

git commit -am "Committing version $TAG"
git tag $TAG -m "Tag for $TAG"

build/go-go tag


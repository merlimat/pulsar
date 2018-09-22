#!/bin/bash
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

set -e

MAVEN_OPTS=" -B -Dstyle.color=always"
MVN="mvn $MAVEN_OPTS"

if [ "X$TEST_SUITE" = "Xjava" ]; then
    echo "Running Java build"
    $MVN license:check install

    echo "Checking license on distribution files"
    src/check-binary-license ./distribution/server/target/apache-pulsar-*-bin.tar.gz

elif [ "X$TEST_SUITE" = "Xintegration" ]; then
    tests/scripts/pre-integ-tests.sh

    echo "Building docker images"
    $MVN install -Pdocker -DskipTests

    echo "Running integration tests"
    $MVN test -DintegrationTests

    tests/scripts/post-integ-tests.sh

elif [ "X$TEST_SUITE" = "Xcpp" ]; then
    echo "Running C++ / Python build"

    $MVN install -DskipTests

    export CMAKE_ARGS="-DCMAKE_BUILD_TYPE=Debug -DBUILD_DYNAMIC_LIB=OFF"
    pulsar-client-cpp/docker-build.sh
    pulsar-client-cpp/docker-tests.sh

else
    echo "Invalid test suite '$1'"
    exit 1
fi

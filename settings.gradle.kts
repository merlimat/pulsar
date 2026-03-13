/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

rootProject.name = "pulsar"

// Tier 0 — no internal dependencies
include("buildtools")
include("bouncy-castle:bc")
include("bouncy-castle:bcfips")
include("pulsar-config-validation")
include("structured-event-log")
include("pulsar-client-api")

// Tier 1
include("pulsar-client-admin-api")
include("testmocks")

// Tier 2
include("pulsar-common")

// Tier 3
include("pulsar-cli-utils")
include("pulsar-client")
include("pulsar-metadata")
include("pulsar-opentelemetry")
include("pulsar-client-messagecrypto-bc")

// Tier 4
include("pulsar-client-admin")
include("managed-ledger")
include("pulsar-broker-common")

// Tier 5 — functions (Maven uses short directory names)
include("pulsar-functions:pulsar-functions-proto")
project(":pulsar-functions:pulsar-functions-proto").projectDir = file("pulsar-functions/proto")

include("pulsar-functions:pulsar-functions-api")
project(":pulsar-functions:pulsar-functions-api").projectDir = file("pulsar-functions/api-java")

include("pulsar-functions:pulsar-functions-utils")
project(":pulsar-functions:pulsar-functions-utils").projectDir = file("pulsar-functions/utils")

include("pulsar-functions:pulsar-functions-instance")
project(":pulsar-functions:pulsar-functions-instance").projectDir = file("pulsar-functions/instance")

include("pulsar-functions:pulsar-functions-secrets")
project(":pulsar-functions:pulsar-functions-secrets").projectDir = file("pulsar-functions/secrets")

include("pulsar-functions:pulsar-functions-runtime")
project(":pulsar-functions:pulsar-functions-runtime").projectDir = file("pulsar-functions/runtime")

include("pulsar-functions:pulsar-functions-worker")
project(":pulsar-functions:pulsar-functions-worker").projectDir = file("pulsar-functions/worker")

include("pulsar-functions:pulsar-functions-local-runner")
project(":pulsar-functions:pulsar-functions-local-runner").projectDir = file("pulsar-functions/localrun")

include("pulsar-functions:pulsar-functions-api-examples")
project(":pulsar-functions:pulsar-functions-api-examples").projectDir = file("pulsar-functions/java-examples")

// Tier 5 — transaction (Maven uses short directory names)
include("pulsar-transaction:pulsar-transaction-common")
project(":pulsar-transaction:pulsar-transaction-common").projectDir = file("pulsar-transaction/common")

include("pulsar-transaction:pulsar-transaction-coordinator")
project(":pulsar-transaction:pulsar-transaction-coordinator").projectDir = file("pulsar-transaction/coordinator")

// Tier 5 — IO
include("pulsar-io:pulsar-io-core")
project(":pulsar-io:pulsar-io-core").projectDir = file("pulsar-io/core")

include("pulsar-io:pulsar-io-common")
project(":pulsar-io:pulsar-io-common").projectDir = file("pulsar-io/common")

include("pulsar-io:pulsar-io-batch-discovery-triggerers")
project(":pulsar-io:pulsar-io-batch-discovery-triggerers").projectDir = file("pulsar-io/batch-discovery-triggerers")

// Tier 6
include("pulsar-docs-tools")

include("pulsar-package-management:pulsar-package-core")
project(":pulsar-package-management:pulsar-package-core").projectDir = file("pulsar-package-management/core")

include("pulsar-package-management:pulsar-package-filesystem-storage")
project(":pulsar-package-management:pulsar-package-filesystem-storage").projectDir = file("pulsar-package-management/filesystem-storage")

include("pulsar-websocket")
include("pulsar-broker")

// Tier 7
include("pulsar-proxy")
include("pulsar-testclient")
include("pulsar-client-tools-api")
include("pulsar-client-tools")
include("pulsar-client-tools-test")
include("pulsar-broker-auth-oidc")
include("pulsar-broker-auth-sasl")
include("pulsar-client-auth-sasl")

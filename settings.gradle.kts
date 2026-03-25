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
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        maven {
            url = uri("https://packages.confluent.io/maven/")
            content {
                includeGroupByRegex("io\\.confluent(\\..*)?")
            }
        }
    }
}

rootProject.name = "pulsar"

// ──────────────────────────────────────────────────────────────────────────────
// Core modules (equivalent to Maven -Pcore-modules)
// Build only these with: ./gradlew assemble -PcoreModules
// ──────────────────────────────────────────────────────────────────────────────

// Enforced platform for dependency version management (Maven dependencyManagement equivalent)
include("pulsar-dependencies")
// BOM for external consumers to align Pulsar module versions
include("pulsar-bom")

// Tier 0 — no internal dependencies
include("buildtools")
// Maven artifactId is "bouncy-castle-bc" (directory is "bouncy-castle/bc")
include("bouncy-castle:bouncy-castle-bc")
project(":bouncy-castle:bouncy-castle-bc").projectDir = file("bouncy-castle/bc")
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
// Maven artifactId is "pulsar-client-original" (directory is "pulsar-client")
include("pulsar-client-original")
project(":pulsar-client-original").projectDir = file("pulsar-client")
include("pulsar-metadata")
include("pulsar-opentelemetry")
include("pulsar-client-messagecrypto-bc")

// Tier 4
// Maven artifactId is "pulsar-client-admin-original" (directory is "pulsar-client-admin")
include("pulsar-client-admin-original")
project(":pulsar-client-admin-original").projectDir = file("pulsar-client-admin")
include("managed-ledger")
include("pulsar-broker-common")

// Tier 5 — functions core (Maven core-modules profile)
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

// Maven artifactId is "pulsar-functions-local-runner-original" (directory is "pulsar-functions/localrun")
include("pulsar-functions:pulsar-functions-local-runner-original")
project(":pulsar-functions:pulsar-functions-local-runner-original").projectDir = file("pulsar-functions/localrun")

include("pulsar-functions:pulsar-functions-api-examples")
project(":pulsar-functions:pulsar-functions-api-examples").projectDir = file("pulsar-functions/java-examples")

include("pulsar-functions:pulsar-functions-api-examples-builtin")
project(":pulsar-functions:pulsar-functions-api-examples-builtin").projectDir = file("pulsar-functions/java-examples-builtin")

include("pulsar-functions:pulsar-functions-runtime-all")
project(":pulsar-functions:pulsar-functions-runtime-all").projectDir = file("pulsar-functions/runtime-all")

// Tier 5 — transaction
include("pulsar-transaction:pulsar-transaction-common")
project(":pulsar-transaction:pulsar-transaction-common").projectDir = file("pulsar-transaction/common")

include("pulsar-transaction:pulsar-transaction-coordinator")
project(":pulsar-transaction:pulsar-transaction-coordinator").projectDir = file("pulsar-transaction/coordinator")

// Tier 5 — IO core modules (Maven core-modules profile)
include("pulsar-io:pulsar-io-core")
project(":pulsar-io:pulsar-io-core").projectDir = file("pulsar-io/core")

include("pulsar-io:pulsar-io-common")
project(":pulsar-io:pulsar-io-common").projectDir = file("pulsar-io/common")

include("pulsar-io:pulsar-io-batch-discovery-triggerers")
project(":pulsar-io:pulsar-io-batch-discovery-triggerers").projectDir = file("pulsar-io/batch-discovery-triggerers")

include("pulsar-io:pulsar-io-batch-data-generator")
project(":pulsar-io:pulsar-io-batch-data-generator").projectDir = file("pulsar-io/batch-data-generator")

include("pulsar-io:pulsar-io-cassandra")
project(":pulsar-io:pulsar-io-cassandra").projectDir = file("pulsar-io/cassandra")

include("pulsar-io:pulsar-io-data-generator")
project(":pulsar-io:pulsar-io-data-generator").projectDir = file("pulsar-io/data-generator")

include("pulsar-io:pulsar-io-netty")
project(":pulsar-io:pulsar-io-netty").projectDir = file("pulsar-io/netty")

// Tier 6
include("pulsar-docs-tools")

include("pulsar-package-management:pulsar-package-core")
project(":pulsar-package-management:pulsar-package-core").projectDir = file("pulsar-package-management/core")

include("pulsar-package-management:pulsar-package-filesystem-storage")
project(":pulsar-package-management:pulsar-package-filesystem-storage").projectDir = file("pulsar-package-management/filesystem-storage")

include("pulsar-websocket")
include("pulsar-broker")

include("pulsar-package-management:pulsar-package-bookkeeper-storage")
project(":pulsar-package-management:pulsar-package-bookkeeper-storage").projectDir = file("pulsar-package-management/bookkeeper-storage")

// Tier 6.5 — jetty upgrade modules
include("jetty-upgrade:pulsar-bookkeeper-prometheus-metrics-provider")
project(":jetty-upgrade:pulsar-bookkeeper-prometheus-metrics-provider").projectDir = file("jetty-upgrade/bookkeeper-prometheus-metrics-provider")
include("jetty-upgrade:pulsar-zookeeper-prometheus-metrics")
project(":jetty-upgrade:pulsar-zookeeper-prometheus-metrics").projectDir = file("jetty-upgrade/zookeeper-prometheus-metrics")
include("jetty-upgrade:zookeeper-with-patched-admin")
project(":jetty-upgrade:zookeeper-with-patched-admin").projectDir = file("jetty-upgrade/zookeeper-with-patched-admin")

// Tier 6.5 — bouncy castle test
include("bouncy-castle:bcfips-include-test")

// Tier 7
include("pulsar-proxy")
include("pulsar-testclient")
include("pulsar-client-tools-api")
include("pulsar-client-tools")
include("pulsar-client-tools-test")
include("pulsar-client-tools-customcommand-example")
include("pulsar-broker-auth-oidc")
include("pulsar-broker-auth-sasl")
include("pulsar-client-auth-sasl")

// Tier 9 — shaded utility modules (in core-modules)
include("pulsar-client-dependencies-minimized")

// Tier 10 — shaded client modules (in core-modules)
include("pulsar-client-shaded")
include("pulsar-client-all")
include("pulsar-client-admin-shaded")

// Tier 11 — distribution (server is in core-modules)
include("distribution:pulsar-server-distribution")
project(":distribution:pulsar-server-distribution").projectDir = file("distribution/server")

// ──────────────────────────────────────────────────────────────────────────────
// Extra modules (excluded when building with -PcoreModules)
// ──────────────────────────────────────────────────────────────────────────────

if (!settings.extra.has("coreModules")) {
    // Functions — localrun-shaded (not in Maven core-modules)
    include("pulsar-functions:pulsar-functions-local-runner-shaded")
    project(":pulsar-functions:pulsar-functions-local-runner-shaded").projectDir = file("pulsar-functions/localrun-shaded")

    // IO connectors (not in Maven core-modules)
    include("pulsar-io:pulsar-io-aws")
    project(":pulsar-io:pulsar-io-aws").projectDir = file("pulsar-io/aws")
    include("pulsar-io:pulsar-io-http")
    project(":pulsar-io:pulsar-io-http").projectDir = file("pulsar-io/http")
    include("pulsar-io:pulsar-io-file")
    project(":pulsar-io:pulsar-io-file").projectDir = file("pulsar-io/file")
    include("pulsar-io:pulsar-io-aerospike")
    project(":pulsar-io:pulsar-io-aerospike").projectDir = file("pulsar-io/aerospike")
    include("pulsar-io:pulsar-io-alluxio")
    project(":pulsar-io:pulsar-io-alluxio").projectDir = file("pulsar-io/alluxio")
    include("pulsar-io:pulsar-io-azure-data-explorer")
    project(":pulsar-io:pulsar-io-azure-data-explorer").projectDir = file("pulsar-io/azure-data-explorer")
    include("pulsar-io:pulsar-io-canal")
    project(":pulsar-io:pulsar-io-canal").projectDir = file("pulsar-io/canal")
    include("pulsar-io:pulsar-io-elastic-search")
    project(":pulsar-io:pulsar-io-elastic-search").projectDir = file("pulsar-io/elastic-search")
    include("pulsar-io:pulsar-io-kafka")
    project(":pulsar-io:pulsar-io-kafka").projectDir = file("pulsar-io/kafka")
    include("pulsar-io:pulsar-io-kafka-connect-adaptor")
    project(":pulsar-io:pulsar-io-kafka-connect-adaptor").projectDir = file("pulsar-io/kafka-connect-adaptor")
    include("pulsar-io:pulsar-io-kafka-connect-adaptor-nar")
    project(":pulsar-io:pulsar-io-kafka-connect-adaptor-nar").projectDir = file("pulsar-io/kafka-connect-adaptor-nar")
    include("pulsar-io:pulsar-io-dynamodb")
    project(":pulsar-io:pulsar-io-dynamodb").projectDir = file("pulsar-io/dynamodb")
    include("pulsar-io:pulsar-io-hbase")
    project(":pulsar-io:pulsar-io-hbase").projectDir = file("pulsar-io/hbase")
    include("pulsar-io:pulsar-io-hdfs3")
    project(":pulsar-io:pulsar-io-hdfs3").projectDir = file("pulsar-io/hdfs3")
    include("pulsar-io:pulsar-io-influxdb")
    project(":pulsar-io:pulsar-io-influxdb").projectDir = file("pulsar-io/influxdb")
    include("pulsar-io:pulsar-io-mongo")
    project(":pulsar-io:pulsar-io-mongo").projectDir = file("pulsar-io/mongo")
    include("pulsar-io:pulsar-io-nsq")
    project(":pulsar-io:pulsar-io-nsq").projectDir = file("pulsar-io/nsq")
    include("pulsar-io:pulsar-io-rabbitmq")
    project(":pulsar-io:pulsar-io-rabbitmq").projectDir = file("pulsar-io/rabbitmq")
    include("pulsar-io:pulsar-io-redis")
    project(":pulsar-io:pulsar-io-redis").projectDir = file("pulsar-io/redis")
    include("pulsar-io:pulsar-io-solr")
    project(":pulsar-io:pulsar-io-solr").projectDir = file("pulsar-io/solr")
    include("pulsar-io:pulsar-io-kinesis-kpl-shaded")
    project(":pulsar-io:pulsar-io-kinesis-kpl-shaded").projectDir = file("pulsar-io/kinesis-kpl-shaded")
    include("pulsar-io:pulsar-io-kinesis")
    project(":pulsar-io:pulsar-io-kinesis").projectDir = file("pulsar-io/kinesis")

    // IO JDBC
    include("pulsar-io:pulsar-io-jdbc")
    project(":pulsar-io:pulsar-io-jdbc").projectDir = file("pulsar-io/jdbc")
    // Use qualified names for JDBC sub-modules to avoid name clashes with debezium sub-modules
    // (both have "core" and "postgres" children).
    include("pulsar-io:pulsar-io-jdbc:pulsar-io-jdbc-core")
    project(":pulsar-io:pulsar-io-jdbc:pulsar-io-jdbc-core").projectDir = file("pulsar-io/jdbc/core")
    include("pulsar-io:pulsar-io-jdbc:pulsar-io-jdbc-clickhouse")
    project(":pulsar-io:pulsar-io-jdbc:pulsar-io-jdbc-clickhouse").projectDir = file("pulsar-io/jdbc/clickhouse")
    include("pulsar-io:pulsar-io-jdbc:pulsar-io-jdbc-mariadb")
    project(":pulsar-io:pulsar-io-jdbc:pulsar-io-jdbc-mariadb").projectDir = file("pulsar-io/jdbc/mariadb")
    include("pulsar-io:pulsar-io-jdbc:pulsar-io-jdbc-openmldb")
    project(":pulsar-io:pulsar-io-jdbc:pulsar-io-jdbc-openmldb").projectDir = file("pulsar-io/jdbc/openmldb")
    include("pulsar-io:pulsar-io-jdbc:pulsar-io-jdbc-postgres")
    project(":pulsar-io:pulsar-io-jdbc:pulsar-io-jdbc-postgres").projectDir = file("pulsar-io/jdbc/postgres")
    include("pulsar-io:pulsar-io-jdbc:pulsar-io-jdbc-sqlite")
    project(":pulsar-io:pulsar-io-jdbc:pulsar-io-jdbc-sqlite").projectDir = file("pulsar-io/jdbc/sqlite")

    // IO Debezium
    include("pulsar-io:pulsar-io-debezium")
    project(":pulsar-io:pulsar-io-debezium").projectDir = file("pulsar-io/debezium")
    // Use qualified names for debezium sub-modules to avoid name clashes with JDBC sub-modules
    include("pulsar-io:pulsar-io-debezium:pulsar-io-debezium-core")
    project(":pulsar-io:pulsar-io-debezium:pulsar-io-debezium-core").projectDir = file("pulsar-io/debezium/core")
    include("pulsar-io:pulsar-io-debezium:pulsar-io-debezium-mongodb")
    project(":pulsar-io:pulsar-io-debezium:pulsar-io-debezium-mongodb").projectDir = file("pulsar-io/debezium/mongodb")
    include("pulsar-io:pulsar-io-debezium:pulsar-io-debezium-mssql")
    project(":pulsar-io:pulsar-io-debezium:pulsar-io-debezium-mssql").projectDir = file("pulsar-io/debezium/mssql")
    include("pulsar-io:pulsar-io-debezium:pulsar-io-debezium-mysql")
    project(":pulsar-io:pulsar-io-debezium:pulsar-io-debezium-mysql").projectDir = file("pulsar-io/debezium/mysql")
    include("pulsar-io:pulsar-io-debezium:pulsar-io-debezium-oracle")
    project(":pulsar-io:pulsar-io-debezium:pulsar-io-debezium-oracle").projectDir = file("pulsar-io/debezium/oracle")
    include("pulsar-io:pulsar-io-debezium:pulsar-io-debezium-postgres")
    project(":pulsar-io:pulsar-io-debezium:pulsar-io-debezium-postgres").projectDir = file("pulsar-io/debezium/postgres")

    // IO docs (depends on all IO connectors)
    include("pulsar-io:pulsar-io-docs")
    project(":pulsar-io:pulsar-io-docs").projectDir = file("pulsar-io/docs")

    // Tiered storage
    include("jclouds-shaded")
    include("tiered-storage:tiered-storage-jcloud")
    project(":tiered-storage:tiered-storage-jcloud").projectDir = file("tiered-storage/jcloud")
    include("tiered-storage:tiered-storage-file-system")
    project(":tiered-storage:tiered-storage-file-system").projectDir = file("tiered-storage/file-system")

    // Athenz auth
    include("pulsar-broker-auth-athenz")
    include("pulsar-client-auth-athenz")

    // Distribution — extra (shell, IO, offloaders)
    include("distribution:pulsar-shell-distribution")
    project(":distribution:pulsar-shell-distribution").projectDir = file("distribution/shell")
    include("distribution:pulsar-io-distribution")
    project(":distribution:pulsar-io-distribution").projectDir = file("distribution/io")
    include("distribution:pulsar-offloader-distribution")
    project(":distribution:pulsar-offloader-distribution").projectDir = file("distribution/offloaders")

    // Misc
    include("microbench")
}

// ──────────────────────────────────────────────────────────────────────────────
// Docker modules (enabled with -Pdocker)
// ──────────────────────────────────────────────────────────────────────────────

// Also auto-enable when running docker-related tasks (e.g., ./gradlew docker)
val dockerRequested = settings.extra.has("docker") ||
    gradle.startParameter.taskNames.any { it.contains("docker", ignoreCase = true) }
if (dockerRequested) {
    include("docker:pulsar-docker-image")
    project(":docker:pulsar-docker-image").projectDir = file("docker/pulsar")
    include("docker:pulsar-all-docker-image")
    project(":docker:pulsar-all-docker-image").projectDir = file("docker/pulsar-all")

    // Test Docker images
    include("tests:java-test-functions")
    project(":tests:java-test-functions").projectDir = file("tests/docker-images/java-test-functions")
    include("tests:java-test-plugins")
    project(":tests:java-test-plugins").projectDir = file("tests/docker-images/java-test-plugins")
    include("tests:java-test-image")
    project(":tests:java-test-image").projectDir = file("tests/docker-images/java-test-image")
    include("tests:latest-version-image")
    project(":tests:latest-version-image").projectDir = file("tests/docker-images/latest-version-image")
}

// ──────────────────────────────────────────────────────────────────────────────
// Integration test modules (enabled with -PintegrationTests)
// ──────────────────────────────────────────────────────────────────────────────

val integrationTestsRequested = settings.extra.has("integrationTests") ||
    gradle.startParameter.taskNames.any { it.contains("integrationTest", ignoreCase = false) }
if (integrationTestsRequested) {
    include("tests:integration")
    project(":tests:integration").projectDir = file("tests/integration")
}

// ──────────────────────────────────────────────────────────────────────────────
// Shade test modules (only included when their tasks are invoked)
// ──────────────────────────────────────────────────────────────────────────────

val shadeTestsRequested = gradle.startParameter.taskNames.any { it.contains("shade-test") }
if (shadeTestsRequested) {
    include("tests:pulsar-client-shade-test")
    project(":tests:pulsar-client-shade-test").projectDir = file("tests/pulsar-client-shade-test")
    include("tests:pulsar-client-admin-shade-test")
    project(":tests:pulsar-client-admin-shade-test").projectDir = file("tests/pulsar-client-admin-shade-test")
    include("tests:pulsar-client-all-shade-test")
    project(":tests:pulsar-client-all-shade-test").projectDir = file("tests/pulsar-client-all-shade-test")
}

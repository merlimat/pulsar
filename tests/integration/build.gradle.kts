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

dependencies {
    testImplementation(libs.gson)
    testImplementation(project(":pulsar-functions:pulsar-functions-api-examples"))
    testImplementation(project(":pulsar-broker"))
    testImplementation(project(":pulsar-broker-common"))
    testImplementation(project(path = ":pulsar-broker-common", configuration = "testJar"))
    testImplementation(project(":pulsar-common"))
    testImplementation(project(":pulsar-client-original"))
    testImplementation(project(":pulsar-client-admin-original"))
    testImplementation(project(":pulsar-proxy"))
    testImplementation(project(":managed-ledger"))
    testImplementation(project(":buildtools"))
    testImplementation(project(":testmocks"))
    testImplementation(project(":pulsar-functions:pulsar-functions-worker"))
    testImplementation(project(":pulsar-functions:pulsar-functions-instance"))
    testImplementation(project(":pulsar-functions:pulsar-functions-runtime"))
    testImplementation(project(":pulsar-functions:pulsar-functions-secrets"))
    testImplementation(libs.bookkeeper.server)
    testImplementation(libs.ant)
    testImplementation(libs.cassandra.driver) {
        exclude(group = "io.netty", module = "netty-handler")
    }
    testImplementation(libs.testcontainers.kafka)
    testImplementation(project(":pulsar-io:pulsar-io-kafka"))
    testImplementation(libs.failsafe)
    testImplementation(libs.testcontainers.mysql)
    testRuntimeOnly(libs.postgresql.jdbc)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.docker.java.core)
    testImplementation(libs.bcpkix.jdk18on)
    testImplementation(project(":pulsar-io:pulsar-io-jdbc:pulsar-io-jdbc-postgres"))
    testImplementation(libs.jackson.databind)
    testImplementation(libs.jackson.dataformat.yaml)
    testImplementation(libs.opensearch.rest.high.level.client)
    testImplementation(libs.elasticsearch.java)
    testImplementation(libs.testcontainers.elasticsearch)
    testImplementation(libs.rabbitmq.amqp.client)
    testImplementation(libs.joda.time)
    testImplementation(libs.awaitility)
    testImplementation(libs.restassured)
    testImplementation(libs.testcontainers.localstack)
    testImplementation(libs.amazon.kinesis.client.v3)
    testImplementation(libs.aws.java.sdk.core)
    testImplementation(libs.testcontainers.mongodb)
    testImplementation(libs.mongodb.driver.reactivestreams)
    testImplementation(libs.testcontainers.k3s)
    testImplementation(libs.jetty.websocket.jetty.client)
    testImplementation(libs.kubernetes.client.java) {
        exclude(group = "io.prometheus", module = "simpleclient_httpserver")
        exclude(group = "org.bouncycastle")
        exclude(group = "javax.annotation", module = "javax.annotation-api")
    }
    testImplementation(libs.kubernetes.client.java.api.fluent) {
        exclude(group = "io.prometheus", module = "simpleclient_httpserver")
        exclude(group = "org.bouncycastle")
        exclude(group = "javax.annotation", module = "javax.annotation-api")
    }
}

// Copy certificate-authority resources to test output
val copyCertificateAuthority by tasks.registering(Copy::class) {
    from("${rootDir}/tests/certificate-authority")
    into(layout.buildDirectory.dir("resources/test/certificate-authority"))
}

tasks.named("processTestResources") {
    dependsOn(copyCertificateAuthority)
}

// Tests are skipped by default — only run when explicitly invoked via the integration test runner
tasks.test {
    enabled = false
}

// Register a task for each integration test suite
val integrationTestSuiteFile = providers.gradleProperty("integrationTestSuiteFile").getOrElse("pulsar.xml")
val integrationTestGroups = providers.gradleProperty("testGroups").orNull
val integrationTestExcludedGroups = providers.gradleProperty("excludedTestGroups").orNull
val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
val kafkaVersion = catalog.findVersion("kafka-client").get().requiredVersion
val debeziumVersion = catalog.findVersion("debezium").get().requiredVersion

val integrationTest by tasks.registering(Test::class) {
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    useTestNG {
        suites("src/test/resources/${integrationTestSuiteFile}")
        if (!integrationTestGroups.isNullOrEmpty()) {
            includeGroups(integrationTestGroups)
        }
        if (!integrationTestExcludedGroups.isNullOrEmpty()) {
            excludeGroups(integrationTestExcludedGroups)
        }
    }

    systemProperty("currentVersion", project.version.toString())
    systemProperty("maven.buildDirectory", layout.buildDirectory.get().asFile.absolutePath)
    systemProperty("kafka.version", kafkaVersion)
    systemProperty("debezium.version", debeziumVersion)

    jvmArgs(
        "-XX:+ExitOnOutOfMemoryError",
        "-Xmx1G",
        "-XX:MaxDirectMemorySize=1G",
    )

    maxParallelForks = 1
    forkEvery = 0

    testLogging {
        events("passed", "skipped", "failed")
        showExceptions = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

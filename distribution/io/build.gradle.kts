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

// Distribution module — no Java compilation needed
tasks.named("compileJava") { enabled = false }
tasks.named("compileTestJava") { enabled = false }
tasks.named("jar") { enabled = false }

val pulsarVersion = project.version.toString()
val rootDir = rootProject.projectDir

// IO connector project paths
val connectorNarProjects = listOf(
    ":pulsar-io:pulsar-io-cassandra",
    ":pulsar-io:pulsar-io-kafka",
    ":pulsar-io:pulsar-io-http",
    ":pulsar-io:pulsar-io-kinesis",
    ":pulsar-io:pulsar-io-rabbitmq",
    ":pulsar-io:pulsar-io-nsq",
    ":pulsar-io:pulsar-io-jdbc:sqlite",
    ":pulsar-io:pulsar-io-jdbc:mariadb",
    ":pulsar-io:pulsar-io-jdbc:clickhouse",
    ":pulsar-io:pulsar-io-jdbc:postgres",
    ":pulsar-io:pulsar-io-jdbc:openmldb",
    ":pulsar-io:pulsar-io-data-generator",
    ":pulsar-io:pulsar-io-batch-data-generator",
    ":pulsar-io:pulsar-io-aerospike",
    ":pulsar-io:pulsar-io-elastic-search",
    ":pulsar-io:pulsar-io-kafka-connect-adaptor-nar",
    ":pulsar-io:pulsar-io-hbase",
    ":pulsar-io:pulsar-io-hdfs3",
    ":pulsar-io:pulsar-io-file",
    ":pulsar-io:pulsar-io-canal",
    ":pulsar-io:pulsar-io-netty",
    ":pulsar-io:pulsar-io-mongo",
    ":pulsar-io:pulsar-io-debezium:mysql",
    ":pulsar-io:pulsar-io-debezium:postgres",
    ":pulsar-io:pulsar-io-debezium:oracle",
    ":pulsar-io:pulsar-io-debezium:mssql",
    ":pulsar-io:pulsar-io-debezium:mongodb",
    ":pulsar-io:pulsar-io-influxdb",
    ":pulsar-io:pulsar-io-redis",
    ":pulsar-io:pulsar-io-solr",
    ":pulsar-io:pulsar-io-dynamodb",
    ":pulsar-io:pulsar-io-alluxio",
    ":pulsar-io:pulsar-io-azure-data-explorer",
)

val ioDistDir by tasks.registering(Sync::class) {
    destinationDir = layout.buildDirectory.dir("apache-pulsar-io-connectors-${pulsarVersion}-bin").get().asFile

    from(rootDir.resolve("LICENSE")) {
        into(".")
    }
    from("src/assemble/README") {
        into(".")
    }

    // Collect NAR files from each connector project's build/libs directory
    connectorNarProjects.forEach { projectPath ->
        val narDir = project(projectPath).layout.buildDirectory.dir("libs")
        from(narDir) {
            into(".")
            include("*.nar")
        }
        dependsOn("${projectPath}:nar")
    }
}

tasks.named("assemble") {
    dependsOn(ioDistDir)
}

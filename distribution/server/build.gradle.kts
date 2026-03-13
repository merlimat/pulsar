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

// Configuration for collecting runtime dependencies
val distLib by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
    isTransitive = true
    // Exclude test-scope and provided-scope dependencies
    exclude(group = "org.projectlombok", module = "lombok")
}

dependencies {
    distLib(project(":pulsar-broker"))
    distLib(project(":pulsar-metadata"))
    distLib(project(":jetcd-core-shaded"))
    distLib(project(":pulsar-docs-tools"))
    distLib(project(":pulsar-proxy"))
    distLib(project(":pulsar-broker-auth-oidc"))
    distLib(project(":pulsar-broker-auth-sasl"))
    distLib(project(":pulsar-client-auth-sasl"))
    distLib(project(":jetty-upgrade:pulsar-bookkeeper-prometheus-metrics-provider"))
    distLib(project(":jetty-upgrade:pulsar-zookeeper-prometheus-metrics"))
    distLib(project(":pulsar-package-management:pulsar-package-bookkeeper-storage")) {
        exclude(group = "org.objenesis")
    }
    distLib(project(":pulsar-package-management:pulsar-package-filesystem-storage"))
    distLib(project(":pulsar-client-tools")) {
        exclude(group = "io.grpc", module = "grpc-testing")
    }
    distLib(project(":pulsar-testclient")) {
        exclude(group = "org.apache.zookeeper", module = "zookeeper")
    }
    distLib(project(":pulsar-functions:pulsar-functions-worker")) {
        exclude(group = "io.grpc")
        exclude(group = "org.bouncycastle")
    }
    distLib(project(":pulsar-functions:pulsar-functions-local-runner")) {
        exclude(group = "io.grpc")
    }

    // Logging
    distLib(libs.log4j.api)
    distLib(libs.log4j.core)
    distLib(libs.log4j.web)
    distLib(libs.log4j.layout.template.json)
    distLib(libs.log4j.slf4j2.impl)
    distLib(libs.simpleclient.log4j2)

    // Metrics
    distLib(libs.dropwizardmetrics.core)
    distLib(libs.dropwizardmetrics.graphite) {
        exclude(group = "com.rabbitmq", module = "amqp-client")
    }
    distLib(libs.dropwizardmetrics.jvm)

    // Other
    distLib(libs.jline2)
    distLib(libs.snappy.java)
    distLib(libs.jackson.dataformat.yaml)
    distLib(libs.bcpkix.jdk18on)
    distLib(libs.perfmark.api)
    distLib(libs.grpc.all)

    // BookKeeper HTTP server
    distLib(libs.bookkeeper.http.vertx.server) {
        exclude(group = "io.netty")
    }
    distLib(libs.vertx.core)
    distLib(libs.vertx.web)

    // Bouncy Castle
    distLib(project(":bouncy-castle:bc"))
}

val pulsarVersion = project.version.toString()
val rootDir = rootProject.projectDir

val serverDistTar by tasks.registering(Tar::class) {
    archiveBaseName.set("apache-pulsar")
    archiveVersion.set(pulsarVersion)
    archiveClassifier.set("bin")
    archiveExtension.set("tar.gz")
    compression = Compression.GZIP
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))

    // Use a top-level directory in the tarball
    val baseDir = "apache-pulsar-${pulsarVersion}"

    // README, LICENSE, NOTICE
    from("src/assemble/README.bin.txt") {
        rename("README.bin.txt", "README")
        into(baseDir)
    }
    from("src/assemble/LICENSE.bin.txt") {
        rename("LICENSE.bin.txt", "LICENSE")
        into(baseDir)
    }
    from("src/assemble/NOTICE.bin.txt") {
        rename("NOTICE.bin.txt", "NOTICE")
        into(baseDir)
    }

    // conf/ directory
    from(rootDir.resolve("conf")) {
        into("${baseDir}/conf")
    }

    // bin/ directory with executable permissions
    from(rootDir.resolve("bin")) {
        into("${baseDir}/bin")
        filePermissions { unix("755") }
    }

    // licenses/ directory
    from(file("licenses")) {
        into("${baseDir}/licenses")
    }

    // Runtime dependency JARs into lib/
    // Include groupId in jar names to identify provenance (matches Maven assembly outputFileNameMapping)
    from(distLib) {
        into("${baseDir}/lib")
        // Exclude items that go elsewhere or shouldn't be in lib/
        exclude("**/pulsar-functions-runtime-all-*.jar")
        exclude("**/pulsar-functions-api-examples-*.jar")
        // Exclude annotation libraries
        exclude("**/animal-sniffer-annotations-*.jar")
        exclude("**/annotations-*.jar")
        // Exclude JNA (only needed in pulsar-shell distro)
        exclude("**/jna-*.jar")
        // Exclude original zookeeper (patched version is included)
        exclude("**/zookeeper-${libs.versions.zookeeper.get()}.jar")
    }

    // Build file-name -> groupId-prefixed-name map from resolved artifacts
    val renameMap = distLib.incoming.artifacts.resolvedArtifacts.map { artifacts ->
        artifacts.associate { result ->
            val id = result.id.componentIdentifier
            val file = result.file
            val ext = file.extension
            val newName = when (id) {
                is org.gradle.api.artifacts.component.ModuleComponentIdentifier ->
                    "${id.group}-${id.module}-${id.version}.${ext}"
                is org.gradle.api.artifacts.component.ProjectComponentIdentifier ->
                    "org.apache.pulsar-${file.nameWithoutExtension}.${ext}"
                else -> file.name
            }
            file.name to newName
        }
    }
    // Rename JARs to groupId-artifactId-version.jar format
    eachFile {
        if (path.startsWith("${baseDir}/lib/")) {
            val map = renameMap.get()
            map[name]?.let { name = it }
        }
    }

    // Python instances
    from(rootDir.resolve("pulsar-functions/instance/target/python-instance")) {
        into("${baseDir}/instances/python-instance")
    }

    // Python examples
    from(rootDir.resolve("pulsar-functions/python-examples")) {
        into("${baseDir}/examples/python-examples")
    }

    // Java instance JAR (runtime-all fat jar)
    from(project(":pulsar-functions:pulsar-functions-runtime-all").tasks.named("jar")) {
        into("${baseDir}/instances")
        rename(".*", "java-instance.jar")
    }

    // Java examples JAR
    from(project(":pulsar-functions:pulsar-functions-api-examples").tasks.named("jar")) {
        into("${baseDir}/examples")
        rename(".*", "api-examples.jar")
    }

    // Example config files
    from(rootDir.resolve("pulsar-functions/java-examples/src/main/resources")) {
        into("${baseDir}/examples")
        include("example-function-config.yaml")
        include("example-window-function-config.yaml")
        include("example-stateful-function-config.yaml")
    }

    // Create empty instances/deps directory
    into("${baseDir}/instances/deps") {
        from(files())
    }
}

tasks.named("assemble") {
    dependsOn(serverDistTar)
}

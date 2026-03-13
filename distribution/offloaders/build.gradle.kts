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

val offloaderDistTar by tasks.registering(Tar::class) {
    val baseDir = "apache-pulsar-offloaders-${pulsarVersion}"

    archiveBaseName.set("apache-pulsar-offloaders")
    archiveVersion.set(pulsarVersion)
    archiveClassifier.set("bin")
    archiveExtension.set("tar.gz")
    compression = Compression.GZIP
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))

    from(rootDir.resolve("LICENSE")) {
        into(baseDir)
    }
    from("src/assemble/README") {
        into(baseDir)
    }

    // Tiered storage NAR files
    val jcloudNarFiles = project(":tiered-storage:tiered-storage-jcloud")
        .layout.buildDirectory.dir("libs")
    val fileSystemNarFiles = project(":tiered-storage:tiered-storage-file-system")
        .layout.buildDirectory.dir("libs")
    from(jcloudNarFiles) {
        into("${baseDir}/offloaders")
        include("*.nar")
    }
    from(fileSystemNarFiles) {
        into("${baseDir}/offloaders")
        include("*.nar")
    }
    dependsOn(":tiered-storage:tiered-storage-jcloud:nar")
    dependsOn(":tiered-storage:tiered-storage-file-system:nar")
}

tasks.named("assemble") {
    dependsOn(offloaderDistTar)
}

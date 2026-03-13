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

val distLib by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
    isTransitive = true
    exclude(group = "org.projectlombok", module = "lombok")
}

dependencies {
    distLib(project(":pulsar-client-tools"))
    distLib(libs.log4j.core)
    distLib(libs.log4j.web)
    distLib(libs.log4j.slf4j2.impl)
    distLib(libs.simpleclient.log4j2)
}

val pulsarVersion = project.version.toString()
val rootDir = rootProject.projectDir

fun createShellDist(taskName: String, archiveExt: String): TaskProvider<out AbstractArchiveTask> {
    return if (archiveExt == "tar.gz") {
        tasks.register<Tar>(taskName) {
            archiveBaseName.set("apache-pulsar-shell")
            archiveVersion.set(pulsarVersion)
            archiveClassifier.set("bin")
            archiveExtension.set("tar.gz")
            compression = Compression.GZIP
            destinationDirectory.set(layout.buildDirectory.dir("distributions"))
        }
    } else {
        tasks.register<Zip>(taskName) {
            archiveBaseName.set("apache-pulsar-shell")
            archiveVersion.set(pulsarVersion)
            archiveClassifier.set("bin")
            archiveExtension.set("zip")
            destinationDirectory.set(layout.buildDirectory.dir("distributions"))
        }
    }
}

val baseDir = "apache-pulsar-shell-${pulsarVersion}"

fun AbstractArchiveTask.configureShellDist() {
    from("src/assemble/LICENSE.bin.txt") {
        rename("LICENSE.bin.txt", "LICENSE")
        into(baseDir)
    }
    from("src/assemble/NOTICE.bin.txt") {
        rename("NOTICE.bin.txt", "NOTICE")
        into(baseDir)
    }
    from("src/assemble/README") {
        into(baseDir)
    }
    // Shell scripts
    from(rootDir.resolve("bin/pulsar-admin-common.sh")) {
        into("${baseDir}/bin")
        filePermissions { unix("755") }
    }
    from(rootDir.resolve("bin/pulsar-shell")) {
        into("${baseDir}/bin")
        filePermissions { unix("755") }
    }
    from(rootDir.resolve("bin/pulsar-admin-common.cmd")) {
        into("${baseDir}/bin")
        filePermissions { unix("755") }
    }
    from(rootDir.resolve("bin/pulsar-shell.cmd")) {
        into("${baseDir}/bin")
        filePermissions { unix("755") }
    }
    // Config files
    from(rootDir.resolve("conf/client.conf")) {
        into("${baseDir}/conf")
    }
    from(rootDir.resolve("conf/log4j2.yaml")) {
        into("${baseDir}/conf")
    }
    // Runtime dependency JARs
    from(distLib) {
        into("${baseDir}/lib")
    }
}

val shellDistTar = createShellDist("shellDistTar", "tar.gz")
val shellDistZip = createShellDist("shellDistZip", "zip")

shellDistTar.configure { configureShellDist() }
shellDistZip.configure { configureShellDist() }

tasks.named("assemble") {
    dependsOn(shellDistTar, shellDistZip)
}
